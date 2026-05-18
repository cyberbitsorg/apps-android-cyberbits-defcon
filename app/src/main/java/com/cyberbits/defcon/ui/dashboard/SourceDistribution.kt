package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.ui.common.SectionHeader
import com.cyberbits.defcon.ui.theme.accentColor
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.textSecondary

@Composable
fun SourceDistribution(articles: List<ArticleEntity>, modifier: Modifier = Modifier) {
    val counts = FeedSource.entries
        .map { src -> src to articles.count { it.source == src } }
        .filter { it.second > 0 }
        .sortedBy { it.first.displayName }
    val total = counts.sumOf { it.second }.coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(adaptiveSurface())
            .padding(16.dp),
    ) {
        SectionHeader("Source distribution", tracking = 1f)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Donut
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val stroke = 18.dp.toPx()
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val tl = Offset(stroke / 2f, stroke / 2f)
                    var startAngle = -90f
                    counts.forEach { (src, count) ->
                        val sweep = count.toFloat() / total * 360f
                        drawArc(
                            color = src.accentColor(),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = tl,
                            size = arcSize,
                            style = Stroke(width = stroke),
                        )
                        startAngle += sweep
                    }
                }
            }
            Spacer(Modifier.size(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                counts.forEach { (src, count) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(src.accentColor()),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            "${src.displayName} ($count)",
                            color = textSecondary(),
                            style = TextStyle(fontSize = 10.sp),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
