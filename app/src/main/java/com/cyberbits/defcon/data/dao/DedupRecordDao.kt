package com.cyberbits.defcon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberbits.defcon.data.entity.DedupRecordEntity

@Dao
interface DedupRecordDao {
    @Query("SELECT * FROM dedup_records ORDER BY addedAt DESC LIMIT :limit")
    suspend fun mostRecent(limit: Int): List<DedupRecordEntity>

    @Query("SELECT fingerprint FROM dedup_records")
    suspend fun allFingerprints(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<DedupRecordEntity>)

    /** Keep at most `keep` rows — delete the rest (oldest by addedAt). */
    @Query("DELETE FROM dedup_records WHERE fingerprint NOT IN " +
           "(SELECT fingerprint FROM dedup_records ORDER BY addedAt DESC LIMIT :keep)")
    suspend fun trimTo(keep: Int)
}
