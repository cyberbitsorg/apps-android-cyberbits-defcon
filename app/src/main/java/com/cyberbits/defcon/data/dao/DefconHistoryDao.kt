package com.cyberbits.defcon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cyberbits.defcon.data.entity.DefconHistoryEntity

@Dao
interface DefconHistoryDao {
    @Query("SELECT * FROM defcon_history ORDER BY computedAt DESC LIMIT 1")
    suspend fun latest(): DefconHistoryEntity?

    @Query("SELECT * FROM defcon_history ORDER BY computedAt DESC LIMIT 2")
    suspend fun latestTwo(): List<DefconHistoryEntity>

    @Query("SELECT * FROM defcon_history WHERE computedAt >= :sinceMs ORDER BY computedAt ASC")
    suspend fun since(sinceMs: Long): List<DefconHistoryEntity>

    @Insert
    suspend fun insert(entity: DefconHistoryEntity)

    @Query("DELETE FROM defcon_history WHERE computedAt < :beforeMs")
    suspend fun trim(beforeMs: Long)
}
