package com.cyberbits.defcon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cyberbits.defcon.data.entity.VolumeRecordEntity

@Dao
interface VolumeRecordDao {
    @Insert
    suspend fun insert(entity: VolumeRecordEntity)

    @Query("SELECT * FROM volume_records ORDER BY recordedAt DESC LIMIT :limit")
    suspend fun mostRecent(limit: Int): List<VolumeRecordEntity>

    @Query("DELETE FROM volume_records WHERE recordedAt < :beforeMs")
    suspend fun trim(beforeMs: Long)

    @Query("DELETE FROM volume_records WHERE id NOT IN (SELECT id FROM volume_records ORDER BY recordedAt DESC LIMIT :keep)")
    suspend fun trimTo(keep: Int)
}
