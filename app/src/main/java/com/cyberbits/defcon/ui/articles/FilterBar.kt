package com.cyberbits.defcon.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cyberbits.defcon.model.FeedSource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBar(
    sourceFilter: FeedSource?,
    severityFilter: Int?,
    unreadOnly: Boolean,
    onSource: (FeedSource?) -> Unit,
    onSeverity: (Int?) -> Unit,
    onUnreadOnly: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = sourceFilter == null,
                onClick = { onSource(null) },
                label = { Text("All sources") },
            )
            FeedSource.entries.forEach { src ->
                FilterChip(
                    selected = sourceFilter == src,
                    onClick = { onSource(if (sourceFilter == src) null else src) },
                    label = { Text(src.displayName) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
            FilterChip(
                selected = severityFilter == null,
                onClick = { onSeverity(null) },
                label = { Text("All levels") },
            )
            for (lvl in 1..5) {
                FilterChip(
                    selected = severityFilter == lvl,
                    onClick = { onSeverity(if (severityFilter == lvl) null else lvl) },
                    label = { Text("L$lvl") },
                )
            }
            FilterChip(
                selected = unreadOnly,
                onClick = { onUnreadOnly(!unreadOnly) },
                label = { Text("Unread only") },
            )
        }
    }
}
