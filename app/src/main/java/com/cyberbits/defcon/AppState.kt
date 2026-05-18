package com.cyberbits.defcon

import com.cyberbits.defcon.model.DefconStatus
import com.cyberbits.defcon.model.FeedSource
import kotlinx.coroutines.flow.MutableStateFlow

class AppState {
    val defconStatus = MutableStateFlow<DefconStatus?>(null)
    val isRefreshing = MutableStateFlow(false)
    val articleSeverityFilter = MutableStateFlow<Int?>(null)
    val articleSourceFilter = MutableStateFlow<FeedSource?>(null)
}
