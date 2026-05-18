package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.pipeline.Scorer
import com.cyberbits.defcon.ui.theme.DefconColors

@Composable
fun SeverityBreakdown(
    articles: List<ArticleEntity>,
    onSeverityTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val counts = IntArray(6)
    for (a in articles) {
        val level = Scorer.levelFromScore(a.defconScore).coerceIn(1, 5)
        counts[level]++
    }
    val maxCount = (counts.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text("Severity breakdown", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (level in 1..5) {
                val ratio = counts[level].toFloat() / maxCount
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(40.dp)
                        .clickable { onSeverityTap(level) },
                ) {
                    Text("${counts[level]}", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height((90.dp * ratio).coerceAtLeast(2.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(DefconColors.colorForLevel(level))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("L$level", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
