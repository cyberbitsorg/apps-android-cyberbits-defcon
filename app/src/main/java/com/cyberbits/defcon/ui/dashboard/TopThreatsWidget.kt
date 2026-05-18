package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.pipeline.Scorer
import com.cyberbits.defcon.ui.common.RelativeTime
import com.cyberbits.defcon.ui.theme.DefconColors

@Composable
fun TopThreatsWidget(articles: List<ArticleEntity>, modifier: Modifier = Modifier) {
    val top = articles.sortedByDescending { it.defconScore }.take(5)
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text("Top threats", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (top.isEmpty()) {
            Text("No articles yet", style = MaterialTheme.typography.bodySmall)
        } else {
            top.forEach { a ->
                ThreatRow(a)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ThreatRow(article: ArticleEntity) {
    val level = Scorer.levelFromScore(article.defconScore)
    Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(DefconColors.colorForLevel(level))
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(article.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(article.source.displayName, style = MaterialTheme.typography.labelSmall)
                Text(RelativeTime.format(article.publishedAt), style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text("${"%.0f".format(article.defconScore)}", style = MaterialTheme.typography.titleMedium)
    }
}
