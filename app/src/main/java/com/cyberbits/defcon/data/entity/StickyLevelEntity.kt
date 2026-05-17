package com.cyberbits.defcon.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sticky_level")
data class StickyLevelEntity(
    @PrimaryKey val id: Int = 1,
    val floor: Int?,
    val untilAt: Long?,
)
