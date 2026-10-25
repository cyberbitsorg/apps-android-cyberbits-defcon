package com.cyberbits.defcon.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberbits.defcon.AppContainer
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.pipeline.Scorer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArticlesViewModel(private val container: AppContainer) : ViewModel() {

    val query = MutableStateFlow("")
    val unreadOnly = MutableStateFlow(false)
    val sourceFilter = container.appState.articleSourceFilter
    val severityFilter = container.appState.articleSeverityFilter

    private val baseFlow = combine(
        container.articleRepository.observeAll(),
        query,
        unreadOnly,
        sourceFilter,
        severityFilter,
    ) { all, q, unread, src, sev ->
        all.asSequence()
            .filter { src == null || it.source == src }
            .filter { !unread || !it.isRead }
            .filter { sev == null || Scorer.levelFromScore(it.defconScore) == sev }
            .filter { q.isBlank() || it.title.contains(q, true) || it.summary.contains(q, true) }
            .toList()
    }

    val articles: StateFlow<List<ArticleEntity>> =
        baseFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { query.value = q }
    fun setUnreadOnly(v: Boolean) { unreadOnly.value = v }
    fun setSourceFilter(s: FeedSource?) { sourceFilter.value = s }
    fun setSeverityFilter(i: Int?) { severityFilter.value = i }

    fun markRead(id: String) = viewModelScope.launch {
        container.articleRepository.markRead(id, System.currentTimeMillis())
    }
    fun markAllRead() = viewModelScope.launch {
        container.articleRepository.markAllRead(System.currentTimeMillis())
    }
    fun markAllUnread() = viewModelScope.launch {
        container.articleRepository.markAllUnread()
    }
    fun refresh() = viewModelScope.launch {
        container.fetchPublishingState()
    }
}
