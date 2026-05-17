package com.cyberbits.defcon.data

import com.cyberbits.defcon.data.dao.CveRecordDao
import com.cyberbits.defcon.data.entity.CveRecordEntity

/**
 * Wraps [CveRecordDao]. Mirrors iOS `CVEStore`; matches docker
 * `CVE_TTL = 36 * 3600`.
 */
class CveRepository(private val dao: CveRecordDao) {

    companion object {
        const val TTL_MILLIS: Long = 36L * 3600L * 1000L
    }

    /** CVE IDs still within the 36h window. */
    suspend fun recentCVEs(now: Long = System.currentTimeMillis()): Set<String> =
        dao.cveIdsSince(now - TTL_MILLIS).toSet()

    suspend fun record(cveId: String, now: Long = System.currentTimeMillis()) {
        dao.upsertAll(listOf(CveRecordEntity(cveId = cveId, seenAt = now)))
    }

    suspend fun recordAll(cveIds: Collection<String>, now: Long = System.currentTimeMillis()) {
        if (cveIds.isEmpty()) return
        dao.upsertAll(cveIds.map { CveRecordEntity(cveId = it, seenAt = now) })
    }

    suspend fun trim(now: Long = System.currentTimeMillis()) =
        dao.purgeOlderThan(now - TTL_MILLIS)
}
