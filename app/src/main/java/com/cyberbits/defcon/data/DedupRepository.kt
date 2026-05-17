package com.cyberbits.defcon.data

import com.cyberbits.defcon.data.dao.DedupRecordDao
import com.cyberbits.defcon.data.entity.DedupRecordEntity

/**
 * Wraps [DedupRecordDao]. Mirrors iOS `DedupStore`; matches docker
 * `RECENT_TITLES_MAX = 300`.
 */
class DedupRepository(private val dao: DedupRecordDao) {

    companion object {
        const val MAX_RECORDS = 300
    }

    suspend fun fingerprints(): Set<String> = dao.allFingerprints().toSet()

    suspend fun recentTitles(limit: Int = MAX_RECORDS): List<String> =
        dao.mostRecent(limit).map { it.title }

    suspend fun record(fingerprint: String, title: String, now: Long = System.currentTimeMillis()) {
        dao.insert(DedupRecordEntity(fingerprint = fingerprint, title = title, addedAt = now))
    }

    suspend fun trim() = dao.trimTo(MAX_RECORDS)
}
