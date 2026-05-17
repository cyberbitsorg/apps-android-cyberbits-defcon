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

    suspend fun articlesInWindow(hours: Int = 24, now: Long = System.currentTimeMillis()): List<ArticleEntity> {
        val sinceMs = now - hours.toLong() * 3600L * 1000L
        return dao.articlesSince(sinceMs)
    }

    suspend fun existingFingerprints(): Set<String> = dao.allFingerprints().toSet()

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
     * Newest-first sequential filter: keep up to [perSource] per feed source,
     * up to [total] overall. Everything else is deleted. Mirrors the iOS
     * `ArticleStore.trim` algorithm exactly.
     */
    suspend fun trim(total: Int = 100, perSource: Int = 20) {
        val all = dao.mostRecent(Int.MAX_VALUE)
        val keptPerSource = mutableMapOf<FeedSource, Int>()
        var keptTotal = 0
        val toDelete = mutableListOf<String>()
        for (a in all) {
            val per = keptPerSource[a.source] ?: 0
            if (per < perSource && keptTotal < total) {
                keptPerSource[a.source] = per + 1
                keptTotal++
            } else {
                toDelete += a.id
            }
        }
        if (toDelete.isNotEmpty()) dao.deleteByIds(toDelete)
    }

    suspend fun markRead(id: String, now: Long = System.currentTimeMillis()) =
        dao.markRead(id, now)

    suspend fun markAllRead(now: Long = System.currentTimeMillis()) =
        dao.markAllRead(now)

    suspend fun countSince(sinceMs: Long): Int = dao.countSince(sinceMs)
}
