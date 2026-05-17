package com.cyberbits.defcon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.model.FeedSource
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun observeAll(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC LIMIT :limit")
    suspend fun mostRecent(limit: Int): List<ArticleEntity>

    @Query("SELECT guid FROM articles")
    suspend fun allGuids(): List<String>

    @Query("SELECT fingerprint FROM articles")
    suspend fun allFingerprints(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ArticleEntity>): List<Long>

    @Query("UPDATE articles SET isRead = 1, readAt = :now WHERE id = :id")
    suspend fun markRead(id: String, now: Long)

    @Query("UPDATE articles SET isRead = 1, readAt = :now WHERE isRead = 0")
    suspend fun markAllRead(now: Long)

    @Query("DELETE FROM articles WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("SELECT * FROM articles WHERE source = :source ORDER BY publishedAt DESC")
    suspend fun bySource(source: FeedSource): List<ArticleEntity>

    @Query("SELECT COUNT(*) FROM articles WHERE publishedAt >= :sinceMs")
    suspend fun countSince(sinceMs: Long): Int

    @Query("SELECT * FROM articles WHERE publishedAt >= :sinceMs ORDER BY publishedAt DESC")
    suspend fun articlesSince(sinceMs: Long): List<ArticleEntity>

    @Query("SELECT COUNT(*) FROM articles WHERE guid = :guid")
    suspend fun guidCount(guid: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ArticleEntity): Long
}
