package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.cyberbits.defcon.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(container: AppContainer, onSeverityTap: (Int) -> Unit) {
    val vm: DashboardViewModel = viewModel(
        factory = remember(container) {
            viewModelFactory { initializer { DashboardViewModel(container) } }
        }
    )
    val status by vm.status.collectAsState()
    val articles by vm.articles.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { vm.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
            item { DefconGauge(status, modifier = Modifier.fillMaxSize()) }
            item { FactorScoresWidget(status) }
            item { HorizontalDivider() }
            item { SeverityBreakdown(articles, onSeverityTap = onSeverityTap) }
            item { HorizontalDivider() }
            item { TopThreatsWidget(articles) }
            item { HorizontalDivider() }
            item { TrendingKeywords(articles) }
            item { HorizontalDivider() }
            item { RecentCVEs(articles) }
            item { HorizontalDivider() }
            item { SourceDistribution(articles) }
            items(emptyList<Unit>()) { }
        }
    }
}
