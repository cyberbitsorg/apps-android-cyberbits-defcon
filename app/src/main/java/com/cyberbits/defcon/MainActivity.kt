package com.cyberbits.defcon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.cyberbits.defcon.ui.DefconRoot
import com.cyberbits.defcon.work.RefreshScheduler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DefconApp).container
        setContent { DefconRoot(container) }

        lifecycleScope.launch {
            val now = System.currentTimeMillis()
            if (container.settingsRepository.needsRefresh(now)) {
                container.appState.isRefreshing.value = true
                container.appState.defconStatus.value = container.fetchCoordinator.runFetchCycle()
                container.appState.isRefreshing.value = false
            } else {
                container.appState.defconStatus.value = container.fetchCoordinator.tickStickyLevel()
            }
            val interval = container.settingsRepository.current().refreshIntervalMinutes
            RefreshScheduler.schedule(applicationContext, interval)
        }
    }
}
