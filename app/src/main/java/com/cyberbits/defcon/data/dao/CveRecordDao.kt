package com.cyberbits.defcon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberbits.defcon.data.entity.CveRecordEntity

@Dao
interface CveRecordDao {
    @Query("SELECT cveId FROM cve_records")
    suspend fun allCveIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CveRecordEntity>)

    @Query("DELETE FROM cve_records WHERE seenAt < :beforeMs")
    suspend fun purgeOlderThan(beforeMs: Long)
}
