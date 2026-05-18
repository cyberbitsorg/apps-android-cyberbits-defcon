package com.cyberbits.defcon.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cyberbits.defcon.AppContainer
import com.cyberbits.defcon.ui.articles.ArticlesScreen
import com.cyberbits.defcon.ui.dashboard.DashboardScreen
import com.cyberbits.defcon.ui.settings.SettingsScreen
import com.cyberbits.defcon.ui.theme.DefconTheme

enum class Destination(val label: String) {
    Dashboard("Dashboard"),
    Articles("Articles"),
    Settings("Settings"),
}

@Composable
fun DefconRoot(container: AppContainer) {
    DefconTheme {
        var selected by remember { mutableStateOf(Destination.Dashboard) }
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                Destination.entries.forEach { d ->
                    val icon = when (d) {
                        Destination.Dashboard -> Icons.Filled.Dashboard
                        Destination.Articles -> Icons.Filled.Article
                        Destination.Settings -> Icons.Filled.Settings
                    }
                    item(
                        selected = selected == d,
                        onClick = { selected = d },
                        icon = { Icon(icon, contentDescription = d.label) },
                        label = { Text(d.label) },
                    )
                }
            }
        ) {
            when (selected) {
                Destination.Dashboard -> DashboardScreen(
                    container = container,
                    onSeverityTap = {
                        container.appState.articleSeverityFilter.value = it
                        selected = Destination.Articles
                    },
                )
                Destination.Articles -> ArticlesScreen(container)
                Destination.Settings -> SettingsScreen(container)
            }
        }
    }
}
