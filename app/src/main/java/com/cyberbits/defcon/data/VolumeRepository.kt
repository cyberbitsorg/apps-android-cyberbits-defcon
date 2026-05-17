package com.cyberbits.defcon.data

import com.cyberbits.defcon.data.dao.VolumeRecordDao
import com.cyberbits.defcon.data.entity.VolumeRecordEntity

/**
 * Rolling per-refresh article-count window. Mirrors iOS `VolumeStore`,
 * which matches docker `cache/volume.py`.
 */
class VolumeRepository(private val dao: VolumeRecordDao) {

    companion object {
        const val WINDOW_SIZE = 168
        const val COLD_START_MIN = 3
        const val POISON_MULTIPLIER: Double = 3.0
    }

    /** Mean of the most recent [WINDOW_SIZE] entries, or null below cold-start. */
    suspend fun baseline(): Double? {
        val recent = dao.mostRecent(WINDOW_SIZE)
        if (recent.size < COLD_START_MIN) return null
        return recent.sumOf { it.count.toDouble() } / recent.size.toDouble()
    }

    /**
     * Push a new count. Skipped if [count] would poison the baseline
     * (>3x current mean, e.g. cold-start dumps).
     */
    suspend fun record(count: Int, now: Long = System.currentTimeMillis()) {
        val base = baseline()
        if (base != null && base > 0.0 && count.toDouble() > POISON_MULTIPLIER * base) {
            return
        }
        dao.insert(VolumeRecordEntity(count = count, recordedAt = now))
    }

    /** Drop everything beyond the most recent [WINDOW_SIZE] entries. */
    suspend fun trim() = dao.trimTo(WINDOW_SIZE)
}
