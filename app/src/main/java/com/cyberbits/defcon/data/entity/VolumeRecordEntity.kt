package com.cyberbits.defcon.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "volume_records", indices = [Index("recordedAt")])
data class VolumeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val count: Int,
    val recordedAt: Long,
)
