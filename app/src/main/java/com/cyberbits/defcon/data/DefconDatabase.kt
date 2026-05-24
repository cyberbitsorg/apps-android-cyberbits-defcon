package com.cyberbits.defcon.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 2,
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
        fun build(context: Context): DefconDatabase =
            Room.databaseBuilder(context, DefconDatabase::class.java, "defcon.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
