package com.cyberbits.defcon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberbits.defcon.data.entity.StickyLevelEntity

@Dao
interface StickyLevelDao {
    @Query("SELECT * FROM sticky_level WHERE id = 1")
    suspend fun read(): StickyLevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StickyLevelEntity)
}
