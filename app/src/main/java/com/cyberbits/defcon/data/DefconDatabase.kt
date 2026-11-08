package com.cyberbits.defcon.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cyberbits.defcon.data.dao.ArticleDao
import com.cyberbits.defcon.data.dao.CveRecordDao
import com.cyberbits.defcon.data.dao.DedupRecordDao
import com.cyberbits.defcon.data.dao.DefconHistoryDao
import com.cyberbits.defcon.data.dao.StickyLevelDao
import com.cyberbits.defcon.data.dao.VolumeRecordDao
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.data.entity.CveRecordEntity
import com.cyberbits.defcon.data.entity.DedupRecordEntity
import com.cyberbits.defcon.data.entity.DefconHistoryEntity
import com.cyberbits.defcon.data.entity.StickyLevelEntity
import com.cyberbits.defcon.data.entity.VolumeRecordEntity

@Database(
    entities = [
        ArticleEntity::class, DefconHistoryEntity::class, StickyLevelEntity::class,
        DedupRecordEntity::class, VolumeRecordEntity::class, CveRecordEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class DefconDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun defconHistoryDao(): DefconHistoryDao
    abstract fun stickyLevelDao(): StickyLevelDao
    abstract fun dedupRecordDao(): DedupRecordDao
    abstract fun volumeRecordDao(): VolumeRecordDao
    abstract fun cveRecordDao(): CveRecordDao

    companion object {
        /** Adds content-based category tags (Tagger). Existing rows stay empty until backfill. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE articles ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
            }
        }

        fun build(context: Context): DefconDatabase =
            Room.databaseBuilder(context, DefconDatabase::class.java, "defcon.db")
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
