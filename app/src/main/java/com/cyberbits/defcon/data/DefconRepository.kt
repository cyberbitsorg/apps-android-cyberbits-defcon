package com.cyberbits.defcon.data

import com.cyberbits.defcon.data.dao.DefconHistoryDao
import com.cyberbits.defcon.data.dao.StickyLevelDao
import com.cyberbits.defcon.data.entity.DefconHistoryEntity
import com.cyberbits.defcon.data.entity.StickyLevelEntity
import com.cyberbits.defcon.model.DefconBreakdown
import com.cyberbits.defcon.model.DefconStatus
import com.cyberbits.defcon.model.Trend

/**
 * Wraps DEFCON-state DAOs (history + sticky floor). Mirrors iOS `DefconStore`.
 */
class DefconRepository(
    private val historyDao: DefconHistoryDao,
    private val stickyDao: StickyLevelDao,
) {

    suspend fun latestHistory(): DefconHistoryEntity? = historyDao.latest()

    suspend fun history(hours: Int = 24, now: Long = System.currentTimeMillis()): List<DefconHistoryEntity> {
        val sinceMs = now - hours.toLong() * 3600L * 1000L
        return historyDao.since(sinceMs)
    }

    suspend fun insertHistory(entity: DefconHistoryEntity) = historyDao.insert(entity)

    /**
     * Resolve the current sticky-adjusted [DefconStatus] from history.
     * Returns `null` if no entries exist. Trend is inverted-scale aware:
     * a LOWER level number is a HIGHER threat, so `latest.level < prev.level`
     * means RISING.
     */
    suspend fun currentStatus(): DefconStatus? {
        val entries = historyDao.latestTwo()
        val latest = entries.firstOrNull() ?: return null
        val previous = entries.getOrNull(1)
        val trend = when {
            previous == null -> Trend.STABLE
            latest.level < previous.level -> Trend.RISING
            latest.level > previous.level -> Trend.FALLING
            else -> Trend.STABLE
        }
        val breakdown = DefconBreakdown(
            weightedMax = latest.weightedMax,
            volumeBonus = latest.volumeBonus,
            triggerLabel = latest.triggerRaw,
            triggerArticleTitle = latest.triggerArticleTitle,
            triggerArticlePublishedAt = latest.triggerArticlePublishedAt,
        )
        return DefconStatus(
            score = latest.score,
            level = latest.level,
            label = DefconStatus.labelFor(latest.level),
            colorHex = DefconStatus.colorHexFor(latest.level),
            breakdown = breakdown,
            trend = trend,
            computedAt = latest.computedAt,
        )
    }

    suspend fun readSticky(): StickyLevelEntity? = stickyDao.read()

    suspend fun writeSticky(floor: Int?, untilAt: Long?) =
        stickyDao.upsert(StickyLevelEntity(id = 1, floor = floor, untilAt = untilAt))
}
