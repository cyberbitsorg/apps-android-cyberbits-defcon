package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.pipeline.Vocabulary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendingKeywords(articles: List<ArticleEntity>, modifier: Modifier = Modifier) {
    val counts = HashMap<String, Int>()
    val allKeywords = (Vocabulary.tier1 + Vocabulary.tier2 + Vocabulary.tier3).distinct()
    for (a in articles) {
        val text = "${a.title} ${a.summary}".lowercase()
        for (kw in allKeywords) {
            if (text.contains(kw)) counts[kw] = (counts[kw] ?: 0) + 1
        }
    }
    val top = counts.entries.sortedByDescending { it.value }.take(20)

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text("Trending keywords", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (top.isEmpty()) {
            Text("No data yet", style = MaterialTheme.typography.bodySmall)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                top.forEach { (kw, count) ->
                    val tierColor = when (kw) {
                        in Vocabulary.tier1 -> Color(0xFFDC2626)
                        in Vocabulary.tier2 -> Color(0xFFEAB308)
                        else -> Color(0xFF3B82F6)
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text("$kw ($count)") },
                        colors = AssistChipDefaults.assistChipColors(labelColor = tierColor),
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
