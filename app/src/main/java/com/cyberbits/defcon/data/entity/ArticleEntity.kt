package com.cyberbits.defcon.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cyberbits.defcon.model.FeedSource

@Entity(
    tableName = "articles",
    indices = [Index(value = ["guid"], unique = true), Index("fingerprint"), Index("publishedAt")],
)
data class ArticleEntity(
    @PrimaryKey val id: String,
    val guid: String,
    val title: String,
    val summary: String,
    val url: String,
    val source: FeedSource,
    val publishedAt: Long,
    val defconScore: Double,
    val categories: List<String>,
    @ColumnInfo(defaultValue = "0") val isRead: Boolean,
    val readAt: Long?,
    val fingerprint: String,
    val triggerRaw: String?,
)
