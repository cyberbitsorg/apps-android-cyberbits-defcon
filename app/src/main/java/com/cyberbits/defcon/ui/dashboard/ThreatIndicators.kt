package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberbits.defcon.model.DefconStatus
import com.cyberbits.defcon.model.Trend
import com.cyberbits.defcon.ui.common.SectionHeader
import com.cyberbits.defcon.ui.theme.DefconColors
import com.cyberbits.defcon.ui.theme.adaptiveBorder
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.colorHex
import com.cyberbits.defcon.ui.theme.textSecondary

/** 2x2 stat grid: Threat Level, Score, Trend, Articles. Mirrors iOS ThreatIndicators. */
@Composable
fun ThreatIndicators(
    status: DefconStatus?,
    articleCount: Int,
    modifier: Modifier = Modifier,
) {
    val level = status?.level ?: 5
    val color = DefconColors.colorForLevel(level)
    val (trendText, trendColor) = when (status?.trend) {
        Trend.RISING -> "Rising" to colorHex("ef4444")
        Trend.FALLING -> "Falling" to colorHex("22c55e")
        else -> "Stable" to textSecondary()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        SectionHeader("Threat Indicators", tracking = 1.5f)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Threat Level", "DEFCON $level", color, Modifier.weight(1f))
            StatCard("Score", "${((status?.score) ?: 0.0).toInt()}/100", color, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Trend", trendText, trendColor, Modifier.weight(1f))
            StatCard("Articles", articleCount.toString(), textSecondary(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(adaptiveSurface())
            .border(1.dp, adaptiveBorder().copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(10.dp),
    ) {
        Column {
            Text(
                label.uppercase(),
                color = textSecondary(),
                style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = valueColor,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            )
        }
    }
}
