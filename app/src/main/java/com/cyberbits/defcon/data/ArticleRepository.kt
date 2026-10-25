package com.cyberbits.defcon.data

import com.cyberbits.defcon.data.dao.ArticleDao
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.model.RawArticle
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Wraps [ArticleDao] with the same convenience operations the iOS
 * `ArticleStore` exposes. Trim limits mirror iOS byte-for-byte:
 * `total = 100`, `perSource = 20`.
 */
class ArticleRepository(private val dao: ArticleDao) {

    fun observeAll(): Flow<List<ArticleEntity>> = dao.observeAll()

    suspend fun mostRecent(limit: Int): List<ArticleEntity> = dao.mostRecent(limit)

    suspend fun existingGuids(): Set<String> = dao.allGuids().toSet()

    /**
     * Insert a new article. Returns `true` if inserted, `false` if the guid
     * already exists (mirrors iOS `ArticleStore.upsert`'s early-return).
     * The caller is responsible for computing [fingerprint] (Deduplicator).
     */
    suspend fun upsert(
        raw: RawArticle,
        score: Double,
        triggerRaw: String?,
        fingerprint: String,
    ): Boolean {
        if (dao.guidCount(raw.guid) > 0) return false
        val entity = ArticleEntity(
            id = UUID.randomUUID().toString(),
            guid = raw.guid,
            title = raw.title,
            summary = raw.summary,
            url = raw.url,
            source = raw.source,
            publishedAt = raw.publishedAt,
            defconScore = score,
            categories = raw.categories,
            isRead = false,
            readAt = null,
            fingerprint = fingerprint,
            triggerRaw = triggerRaw,
        )
        val rowId = dao.insert(entity)
        return rowId != -1L
    }

    /**
     * Newest-first two-pass trim:
     * 1. Keep up to [perSource] newest per feed source (fairness, like iOS).
     * 2. If [total] is not yet filled, top up with the next-newest articles
     *    regardless of source. This guarantees the DB tends to [total]
     *    whenever there is enough raw material across sources — the iOS
     *    single-pass version would leave gaps when a feed yields < perSource.
     */
    suspend fun trim(total: Int = 100, perSource: Int = 20) {
        val all = dao.mostRecent(Int.MAX_VALUE)
        if (all.isEmpty()) return
        val kept = HashSet<String>()
        val keptPerSource = mutableMapOf<FeedSource, Int>()

        // Pass 1: per-source cap
        for (a in all) {
            if (kept.size >= total) break
            val per = keptPerSource[a.source] ?: 0
            if (per < perSource) {
                keptPerSource[a.source] = per + 1
                kept += a.id
            }
        }
        // Pass 2: top-up to [total] from remaining articles (newest first)
        if (kept.size < total) {
            for (a in all) {
                if (kept.size >= total) break
                if (a.id !in kept) kept += a.id
            }
        }

        val toDelete = all.filter { it.id !in kept }.map { it.id }
        if (toDelete.isNotEmpty()) dao.deleteByIds(toDelete)
    }

    suspend fun markRead(id: String, now: Long = System.currentTimeMillis()) =
        dao.markRead(id, now)

    suspend fun markAllRead(now: Long = System.currentTimeMillis()) =
        dao.markAllRead(now)

    suspend fun markAllUnread() = dao.markAllUnread()
}
