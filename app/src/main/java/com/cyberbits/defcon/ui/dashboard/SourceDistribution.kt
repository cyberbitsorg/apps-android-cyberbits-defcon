package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.ui.theme.DefconColors

@Composable
fun SourceDistribution(articles: List<ArticleEntity>, modifier: Modifier = Modifier) {
    val counts = FeedSource.entries.associateWith { src -> articles.count { it.source == src } }
    val total = counts.values.sum().coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text("By source", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        counts.entries.sortedByDescending { it.value }.forEach { (src, count) ->
            val frac = count.toFloat() / total
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(DefconColors.colorForSource(src))
                )
                Spacer(Modifier.size(8.dp))
                Text(src.displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text("$count", style = MaterialTheme.typography.bodyMedium)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(frac)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DefconColors.colorForSource(src))
            )
        }
    }
}
