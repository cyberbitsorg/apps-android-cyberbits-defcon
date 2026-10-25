package com.cyberbits.defcon

import android.content.Context
import com.cyberbits.defcon.data.ArticleRepository
import com.cyberbits.defcon.data.CveRepository
import com.cyberbits.defcon.data.DedupRepository
import com.cyberbits.defcon.data.DefconDatabase
import com.cyberbits.defcon.data.DefconRepository
import com.cyberbits.defcon.data.SettingsRepository
import com.cyberbits.defcon.data.VolumeRepository
import com.cyberbits.defcon.model.DefconStatus
import com.cyberbits.defcon.notification.DefconNotifier
import com.cyberbits.defcon.pipeline.FetchCoordinator

class AppContainer(val applicationContext: Context) {
    val database: DefconDatabase by lazy { DefconDatabase.build(applicationContext) }

    val articleRepository by lazy { ArticleRepository(database.articleDao()) }
    val defconRepository by lazy {
        DefconRepository(database.defconHistoryDao(), database.stickyLevelDao())
    }
    val dedupRepository by lazy { DedupRepository(database.dedupRecordDao()) }
    val volumeRepository by lazy { VolumeRepository(database.volumeRecordDao()) }
    val cveRepository by lazy { CveRepository(database.cveRecordDao()) }
    val settingsRepository by lazy { SettingsRepository(applicationContext) }

    val notifier by lazy { DefconNotifier(applicationContext) }

    val appState = AppState()

    val fetchCoordinator by lazy {
        FetchCoordinator(
            articles = articleRepository,
            defcon = defconRepository,
            dedup = dedupRepository,
            volume = volumeRepository,
            cves = cveRepository,
            settings = settingsRepository,
            notifier = notifier,
        )
    }

    /** Runs a fetch cycle, publishing refresh progress and status to [appState] for the UI. */
    suspend fun fetchPublishingState(): DefconStatus? {
        appState.isRefreshing.value = true
        val status = fetchCoordinator.runFetchCycle()
        appState.defconStatus.value = status
        appState.isRefreshing.value = false
        return status
    }
}
