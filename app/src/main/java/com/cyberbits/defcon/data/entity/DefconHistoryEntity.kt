package com.cyberbits.defcon.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "defcon_history", indices = [Index("computedAt")])
data class DefconHistoryEntity(
    @PrimaryKey val id: String,
    val score: Double,
    val level: Int,
    val computedAt: Long,
    val weightedMax: Double,
    val volumeBonus: Double,
    val triggerRaw: String?,
    val triggerArticleTitle: String?,
    val triggerArticlePublishedAt: Long? = null,
)
