package com.cyberbits.defcon.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.ui.theme.DefconColors
import com.cyberbits.defcon.ui.theme.accentColor
import com.cyberbits.defcon.ui.theme.adaptiveBorder
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.colorHex
import com.cyberbits.defcon.ui.theme.textSecondary

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
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Chip(
            label = "All Sources",
            isSelected = sourceFilter == null,
            onClick = { onSource(null) },
        )
        Spacer(Modifier.width(8.dp))
        FeedSource.entries.forEach { src ->
            Chip(
                label = src.displayName,
                isSelected = sourceFilter == src,
                accentColor = src.accentColor(),
                onClick = { onSource(if (sourceFilter == src) null else src) },
            )
            Spacer(Modifier.width(8.dp))
        }

        Divider()
        Spacer(Modifier.width(8.dp))

        for (level in 1..5) {
            Chip(
                label = "DEF $level",
                isSelected = severityFilter == level,
                accentColor = DefconColors.colorForLevel(level),
                onClick = { onSeverity(if (severityFilter == level) null else level) },
            )
            Spacer(Modifier.width(8.dp))
        }

        Divider()
        Spacer(Modifier.width(8.dp))

        Chip(
            label = "Unread",
            isSelected = unreadOnly,
            accentColor = colorHex("22c55e"),
            onClick = { onUnreadOnly(!unreadOnly) },
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .clip(CircleShape)
            .background(adaptiveBorder()),
    )
}

@Composable
private fun Chip(
    label: String,
    isSelected: Boolean,
    accentColor: Color = colorHex("dc2626"),
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) accentColor.copy(alpha = 0.15f) else adaptiveSurface())
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor.copy(alpha = 0.5f) else adaptiveBorder(),
                shape = CircleShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            color = if (isSelected) accentColor else textSecondary(),
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
        )
    }
}
