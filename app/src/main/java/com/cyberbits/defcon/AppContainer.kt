package com.cyberbits.defcon

import android.content.Context
import com.cyberbits.defcon.data.ArticleRepository
import com.cyberbits.defcon.data.CveRepository
import com.cyberbits.defcon.data.DedupRepository
import com.cyberbits.defcon.data.DefconDatabase
import com.cyberbits.defcon.data.DefconRepository
import com.cyberbits.defcon.data.VolumeRepository

class AppContainer(val applicationContext: Context) {
    val database: DefconDatabase by lazy { DefconDatabase.build(applicationContext) }

    val articleRepository by lazy { ArticleRepository(database.articleDao()) }
    val defconRepository by lazy {
        DefconRepository(database.defconHistoryDao(), database.stickyLevelDao())
    }
    val dedupRepository by lazy { DedupRepository(database.dedupRecordDao()) }
    val volumeRepository by lazy { VolumeRepository(database.volumeRecordDao()) }
    val cveRepository by lazy { CveRepository(database.cveRecordDao()) }
}
