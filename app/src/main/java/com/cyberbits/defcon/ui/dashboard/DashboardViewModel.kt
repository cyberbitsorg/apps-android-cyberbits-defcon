package com.cyberbits.defcon.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberbits.defcon.AppContainer
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.data.entity.DefconHistoryEntity
import com.cyberbits.defcon.model.DefconStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(private val container: AppContainer) : ViewModel() {

    val status: StateFlow<DefconStatus?> = container.appState.defconStatus
    val isRefreshing: StateFlow<Boolean> = container.appState.isRefreshing

    val articles: StateFlow<List<ArticleEntity>> =
        container.articleRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history24h: StateFlow<List<DefconHistoryEntity>> =
        flow { emit(container.defconRepository.history(24)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refresh() = viewModelScope.launch {
        container.fetchPublishingState()
    }
}
