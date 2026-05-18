package com.cyberbits.defcon.ui.articles

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cyberbits.defcon.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(container: AppContainer) {
    val vm: ArticlesViewModel = viewModel(
        factory = remember(container) {
            viewModelFactory { initializer { ArticlesViewModel(container) } }
        }
    )
    val articles by vm.articles.collectAsState()
    val query by vm.query.collectAsState()
    val unreadOnly by vm.unreadOnly.collectAsState()
    val sourceFilter by vm.sourceFilter.collectAsState()
    val severityFilter by vm.severityFilter.collectAsState()
    val isRefreshing by container.appState.isRefreshing.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles (${articles.size})") },
                actions = {
                    IconButton(onClick = { vm.markAllRead() }) {
                        Icon(Icons.Filled.DoneAll, contentDescription = "Mark all read")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { vm.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = vm::setQuery,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp),
                        placeholder = { Text("Search title or summary") },
                        singleLine = true,
                    )
                    FilterBar(
                        sourceFilter = sourceFilter,
                        severityFilter = severityFilter,
                        unreadOnly = unreadOnly,
                        onSource = vm::setSourceFilter,
                        onSeverity = vm::setSeverityFilter,
                        onUnreadOnly = vm::setUnreadOnly,
                    )
                    HorizontalDivider()
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = articles, key = { it.id }) { article ->
                            ArticleRow(
                                article = article,
                                onClick = {
                                    vm.markRead(article.id)
                                    val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
