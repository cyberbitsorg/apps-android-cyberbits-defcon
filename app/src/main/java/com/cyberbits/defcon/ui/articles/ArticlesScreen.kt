package com.cyberbits.defcon.ui.articles

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cyberbits.defcon.AppContainer
import com.cyberbits.defcon.ui.theme.adaptiveBackground
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.textSecondary
import kotlin.math.ceil

private const val PAGE_SIZE = 20

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

    var searchVisible by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }

    // Reset page when filters change
    val filterKey = "${sourceFilter}|${severityFilter}|${unreadOnly}|${query}"
    remember(filterKey) { currentPage = 1; filterKey }

    val totalPages = ceil(articles.size.toDouble() / PAGE_SIZE).toInt().coerceAtLeast(1)
    val pageArticles = articles.drop((currentPage - 1) * PAGE_SIZE).take(PAGE_SIZE)
    val allRead = articles.isNotEmpty() && articles.all { it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles") },
                actions = {
                    IconButton(onClick = { searchVisible = !searchVisible }) {
                        Icon(
                            if (searchVisible) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = "Search",
                        )
                    }
                    TextButton(
                        onClick = { if (allRead) vm.markAllUnread() else vm.markAllRead() },
                        enabled = articles.isNotEmpty(),
                    ) {
                        Text(
                            if (allRead) "Mark all unread" else "Mark all read",
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        )
                    }
                },
            )
        },
        containerColor = adaptiveBackground(),
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(adaptiveBackground()),
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { vm.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (searchVisible) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = vm::setQuery,
                            placeholder = { Text("Search threats…") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                    FilterBar(
                        sourceFilter = sourceFilter,
                        severityFilter = severityFilter,
                        unreadOnly = unreadOnly,
                        onSource = vm::setSourceFilter,
                        onSeverity = vm::setSeverityFilter,
                        onUnreadOnly = vm::setUnreadOnly,
                    )
                    HorizontalDivider()
                    if (pageArticles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No articles match the current filters",
                                color = textSecondary(),
                                style = TextStyle(fontSize = 12.sp),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp, vertical = 8.dp,
                            ),
                        ) {
                            items(items = pageArticles, key = { it.id }) { article ->
                                ArticleRow(
                                    article = article,
                                    onClick = {
                                        vm.markRead(article.id)
                                        val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    },
                                )
                            }
                        }
                        if (articles.size > PAGE_SIZE) {
                            PaginationBar(
                                currentPage = currentPage,
                                totalPages = totalPages,
                                totalCount = articles.size,
                                pageSize = PAGE_SIZE,
                                onPrev = { if (currentPage > 1) currentPage-- },
                                onNext = { if (currentPage < totalPages) currentPage++ },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    totalCount: Int,
    pageSize: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val rangeStart = (currentPage - 1) * pageSize + 1
    val rangeEnd = (currentPage * pageSize).coerceAtMost(totalCount)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(adaptiveSurface())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev, enabled = currentPage > 1) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Page $currentPage of $totalPages",
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            )
            Text(
                "$rangeStart–$rangeEnd of $totalCount",
                color = textSecondary(),
                style = TextStyle(fontSize = 10.sp),
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onNext, enabled = currentPage < totalPages) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}
