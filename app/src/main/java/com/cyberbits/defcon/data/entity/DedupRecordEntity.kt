package com.cyberbits.defcon.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dedup_records",
    indices = [Index(value = ["fingerprint"], unique = true), Index("addedAt")],
)
data class DedupRecordEntity(
    @PrimaryKey val fingerprint: String,
    val title: String,
    val addedAt: Long,
)
