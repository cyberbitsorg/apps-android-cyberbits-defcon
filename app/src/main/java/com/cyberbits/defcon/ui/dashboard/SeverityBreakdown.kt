package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.pipeline.Scorer
import com.cyberbits.defcon.ui.common.SectionHeader
import com.cyberbits.defcon.ui.theme.DefconColors
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.textSecondary

/** Tappable severity bars — tap filters Articles tab. Mirrors iOS SeverityBreakdown. */
@Composable
fun SeverityBreakdown(
    articles: List<ArticleEntity>,
    activeFilter: Int?,
    onSeverityTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val recent = articles.take(20)
    val counts = IntArray(6)
    for (a in recent) counts[Scorer.levelFromScore(a.defconScore).coerceIn(1, 5)]++
    val maxCount = (counts.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(adaptiveSurface())
            .padding(16.dp),
    ) {
        SectionHeader("Severity breakdown", tracking = 1f)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(96.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (level in 1..5) {
                val count = counts[level]
                val active = activeFilter == level
                val barColor = DefconColors.colorForLevel(level)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSeverityTap(level) },
                ) {
                    Text(
                        "$count",
                        color = textSecondary(),
                        style = TextStyle(fontSize = 10.sp),
                    )
                    Spacer(Modifier.height(4.dp))
                    val heightDp = (56.dp.value * (count.toFloat() / maxCount)).coerceAtLeast(8f).dp
                    val shape = RoundedCornerShape(4.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heightDp)
                            .then(
                                if (level == 1) Modifier.shadow(
                                    elevation = 4.dp,
                                    shape = shape,
                                    ambientColor = androidx.compose.ui.graphics.Color(0xFFDC2626),
                                    spotColor = androidx.compose.ui.graphics.Color(0xFFDC2626),
                                ) else Modifier
                            )
                            .clip(shape)
                            .background(barColor.copy(alpha = if (active) 1f else 0.85f))
                            .border(
                                if (active) 2.dp else 0.dp,
                                barColor,
                                shape,
                            ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "DEF $level",
                        color = textSecondary(),
                        style = TextStyle(fontSize = 10.sp),
                    )
                }
            }
        }
    }
}
