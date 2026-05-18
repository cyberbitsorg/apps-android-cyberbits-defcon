package com.cyberbits.defcon.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cyberbits.defcon.DefconApp

class RefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as DefconApp
        return try {
            app.container.fetchCoordinator.runFetchCycle()
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}
