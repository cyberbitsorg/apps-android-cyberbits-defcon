package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cyberbits.defcon.AppContainer
import com.cyberbits.defcon.ui.theme.adaptiveBackground

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
    val severityFilter by container.appState.articleSeverityFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DEFCON Dashboard") },
                actions = {
                    IconButton(onClick = { vm.refresh() }, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = adaptiveBackground(),
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(adaptiveBackground()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DefconGauge(status)
                ThreatIndicators(status, articleCount = articles.size)
                SeverityBreakdown(
                    articles = articles,
                    activeFilter = severityFilter,
                    onSeverityTap = onSeverityTap,
                )
                TopThreatsWidget(articles)
                TrendingKeywords(articles)
                RecentCVEs(articles)
                SourceDistribution(articles)
            }
        }
    }
}

