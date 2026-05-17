package com.cyberbits.defcon.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cve_records",
    indices = [Index(value = ["cveId"], unique = true), Index("seenAt")],
)
data class CveRecordEntity(
    @PrimaryKey val cveId: String,
    val seenAt: Long,
)
