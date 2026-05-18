package com.cyberbits.defcon.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberbits.defcon.AppContainer
import com.cyberbits.defcon.data.SettingsRepository
import com.cyberbits.defcon.work.RefreshScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val settings: StateFlow<SettingsRepository.Settings> =
        container.settingsRepository.settings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsRepository.Settings())

    fun setRefreshIntervalMinutes(context: Context, v: Int) = viewModelScope.launch {
        container.settingsRepository.setRefreshIntervalMinutes(v)
        RefreshScheduler.schedule(context.applicationContext, v)
    }

    fun setDefconNotificationsEnabled(v: Boolean) = viewModelScope.launch {
        container.settingsRepository.setDefconNotificationsEnabled(v)
    }

    fun setNotificationThreshold(v: Int) = viewModelScope.launch {
        container.settingsRepository.setNotificationThreshold(v)
    }

    fun setNewArticleNotificationsEnabled(v: Boolean) = viewModelScope.launch {
        container.settingsRepository.setNewArticleNotificationsEnabled(v)
    }

    fun refreshNow() = viewModelScope.launch {
        container.appState.isRefreshing.value = true
        container.appState.defconStatus.value = container.fetchCoordinator.runFetchCycle()
        container.appState.isRefreshing.value = false
    }
}
