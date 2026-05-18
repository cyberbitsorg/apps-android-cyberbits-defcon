package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.data.ArticleRepository
import com.cyberbits.defcon.data.CveRepository
import com.cyberbits.defcon.data.DedupRepository
import com.cyberbits.defcon.data.DefconRepository
import com.cyberbits.defcon.data.SettingsRepository
import com.cyberbits.defcon.data.VolumeRepository
import com.cyberbits.defcon.data.entity.DefconHistoryEntity
import com.cyberbits.defcon.model.DefconStatus
import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.model.RawArticle
import com.cyberbits.defcon.notification.DefconNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Orchestrates a full fetch → dedup → score → store → sticky-level cycle.
 * Mirrors iOS `FetchCoordinator.runFetchCycle`.
 */
class FetchCoordinator(
    private val articles: ArticleRepository,
    private val defcon: DefconRepository,
    private val dedup: DedupRepository,
    private val volume: VolumeRepository,
    private val cves: CveRepository,
    private val settings: SettingsRepository,
    private val notifier: DefconNotifier,
    private val fetcher: FeedFetcher = FeedFetcher(),
) {
    private val mutex = Mutex()

    suspend fun runFetchCycle(): DefconStatus? = mutex.withLock {
        val now = System.currentTimeMillis()
        val prevSettings = settings.current()
        val prevHistory = defcon.latestHistory()
        val prevLevel = prevHistory?.level ?: 5

        // 1. Load dedup state
        val existingGuids = articles.existingGuids()
        val existingFingerprints = dedup.fingerprints()
        val recentTitles = dedup.recentTitles()
        val recentCves = cves.recentCVEs(now)

        // 2. Fetch concurrently
        val fetchResults = coroutineScope {
            FeedSource.entries.map { src ->
                async(Dispatchers.IO) { fetcher.fetch(src) }
            }.awaitAll()
        }
        val raw: List<RawArticle> = fetchResults.flatMap { res ->
            val body = res.body ?: return@flatMap emptyList()
            when (res.source.format) {
                FeedSource.Format.WORDPRESS_JSON -> runCatching { FeedParser.parseWordPressJson(res.source, body) }.getOrDefault(emptyList())
                else -> runCatching { FeedParser.parseXml(res.source, body) }.getOrDefault(emptyList())
            }
        }

        // 3. Deduplicate. GUID match first, then full Deduplicator pass against an
        //    incrementally-growing corpus so within-batch dupes are caught too.
        val corpusTitles = recentTitles.toMutableList()
        val seenCves = recentCves.toMutableSet()
        val acceptedFps = HashSet<String>()
        val unique = ArrayList<RawArticle>()
        val acceptedCvesPerArticle = ArrayList<Set<String>>()
        for (article in raw) {
            if (article.guid in existingGuids) continue
            val fp = Deduplicator.fingerprint(article.title)
            if (fp in acceptedFps) continue
            if (fp in existingFingerprints) continue
            val survivors = Deduplicator.deduplicate(
                incoming = listOf(article),
                existingFingerprints = emptySet(),
                recentTitles = corpusTitles,
                recentCves = seenCves,
            )
            if (survivors.isEmpty()) continue
            acceptedFps += fp
            corpusTitles += article.title
            val articleCves = Deduplicator.extractCVEs("${article.title} ${article.summary}")
            seenCves += articleCves
            acceptedCvesPerArticle += articleCves
            unique += article
        }
        val newCount = unique.size

        // 4. Score + store
        for ((idx, article) in unique.withIndex()) {
            val s = Scorer.computeArticleScore(article.title, article.summary)
            articles.upsert(
                raw = article,
                score = s.score,
                triggerRaw = s.trigger?.name,
                fingerprint = Deduplicator.fingerprint(article.title),
            )
            dedup.record(Deduplicator.fingerprint(article.title), article.title, now)
            cves.recordAll(acceptedCvesPerArticle[idx], now)
        }

        // 5. Trim
        articles.trim()
        dedup.trim()
        cves.trim(now)
        volume.record(newCount, now)
        volume.trim()

        // 6. Global DEFCON over 20 most recent
        val windowArticles = articles.mostRecent(20)
        val window = windowArticles.map { a ->
            Scorer.WindowArticle(a.title, a.defconScore, null, a.publishedAt)
        }
        val baseline = volume.baseline()
        val global = Scorer.computeGlobalScore(window, newCount, baseline, now)
        val rawLevel = Scorer.levelFromScore(global.score)

        // 7. Sticky level
        val sticky = defcon.readSticky()
        val anchor = evidenceAnchor(sticky?.floor, windowArticles.map { it.publishedAt to it.defconScore })
        val stickyR = StickyLevel.computeDisplayedLevel(
            rawLevel = rawLevel,
            state = StickyLevel.State(sticky?.floor, sticky?.untilAt),
            evidenceAnchor = anchor,
            now = now,
        )
        defcon.writeSticky(stickyR.newFloor, stickyR.newUntilAt)
        val displayedLevel = stickyR.displayedLevel

        defcon.insertHistory(
            DefconHistoryEntity(
                id = UUID.randomUUID().toString(),
                score = global.score,
                level = displayedLevel,
                computedAt = now,
                weightedMax = global.weightedMax,
                volumeBonus = global.volumeBonus,
                triggerRaw = global.trigger?.name,
                triggerArticleTitle = global.triggerArticleTitle,
            )
        )

        // 8. Settings: last refreshed
        settings.setLastRefreshedAt(now)

        // 9. Status
        val status = defcon.currentStatus()

        // 10. Notify (inverted DEFCON: lower level number == higher threat)
        if (status != null && displayedLevel < prevLevel &&
            displayedLevel <= prevSettings.notificationThreshold &&
            prevSettings.defconNotificationsEnabled
        ) {
            notifier.postDefconLevel(displayedLevel, status.label)
        }
        if (newCount > 0 && prevSettings.newArticleNotificationsEnabled) {
            notifier.postNewArticles(newCount)
        }
        status
    }

    /**
     * Re-applies the sticky-level state machine without doing a network fetch.
     * Mirrors iOS `tickStickyLevel` — cheap to call on app open so the level
     * decays in real time even if background refreshes were skipped.
     */
    suspend fun tickStickyLevel(): DefconStatus? = mutex.withLock {
        val now = System.currentTimeMillis()
        val windowArticles = articles.mostRecent(20)
        if (windowArticles.isEmpty()) return@withLock null
        val window = windowArticles.map { a ->
            Scorer.WindowArticle(a.title, a.defconScore, null, a.publishedAt)
        }
        val baseline = volume.baseline()
        val global = Scorer.computeGlobalScore(window, newCount = 0, baseline = baseline, now = now)
        val rawLevel = Scorer.levelFromScore(global.score)
        val sticky = defcon.readSticky()
        val anchor = evidenceAnchor(sticky?.floor, windowArticles.map { it.publishedAt to it.defconScore })
        val r = StickyLevel.computeDisplayedLevel(
            rawLevel = rawLevel,
            state = StickyLevel.State(sticky?.floor, sticky?.untilAt),
            evidenceAnchor = anchor,
            now = now,
        )
        defcon.writeSticky(r.newFloor, r.newUntilAt)
        defcon.insertHistory(
            DefconHistoryEntity(
                id = UUID.randomUUID().toString(),
                score = global.score,
                level = r.displayedLevel,
                computedAt = now,
                weightedMax = global.weightedMax,
                volumeBonus = global.volumeBonus,
                triggerRaw = global.trigger?.name,
                triggerArticleTitle = global.triggerArticleTitle,
            )
        )
        defcon.currentStatus()
    }

    /**
     * Most recent published-at among window articles whose own score maps to
     * `floor` or more severe. Null if no such article exists.
     */
    private fun evidenceAnchor(floor: Int?, pairs: List<Pair<Long, Double>>): Long? {
        if (floor == null) return null
        return pairs
            .filter { (_, score) -> Scorer.levelFromScore(score) <= floor }
            .maxOfOrNull { it.first }
    }
}
