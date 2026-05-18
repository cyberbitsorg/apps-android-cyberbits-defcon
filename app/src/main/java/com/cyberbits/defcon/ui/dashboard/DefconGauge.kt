package com.cyberbits.defcon.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberbits.defcon.model.DefconStatus
import com.cyberbits.defcon.model.Trend
import com.cyberbits.defcon.ui.common.SectionHeader
import com.cyberbits.defcon.ui.theme.DefconColors
import com.cyberbits.defcon.ui.theme.adaptiveBorder
import com.cyberbits.defcon.ui.theme.colorHex
import com.cyberbits.defcon.ui.theme.textSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * 240° speedometer gauge matching iOS DefconGauge.
 * 5 coloured segments (DEFCON 5 → 1 → blue, green, yellow, red, white), needle pointing at score.
 */
@Composable
fun DefconGauge(status: DefconStatus?, modifier: Modifier = Modifier) {
    val level = status?.level ?: 5
    val score = status?.score ?: 0.0
    val color = DefconColors.colorForLevel(level)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader("Cybersecurity DEFCON", tracking = 1.5f, modifier = Modifier.weight(1f))
            TrendBadge(status?.trend, color)
        }
        Spacer(Modifier.height(10.dp))

        GaugeArc(level = level, score = score, modifier = Modifier
            .fillMaxWidth()
            .height(180.dp))

        Spacer(Modifier.height(8.dp))
        DriverRow(status, color)
    }
}

@Composable
private fun TrendBadge(trend: Trend?, color: Color) {
    val (icon, label) = when (trend) {
        Trend.RISING -> Icons.AutoMirrored.Filled.TrendingUp to "RISING"
        Trend.FALLING -> Icons.AutoMirrored.Filled.TrendingDown to "FALLING"
        else -> Icons.AutoMirrored.Filled.ArrowForward to "STABLE"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.height(14.dp))
        Spacer(Modifier.padding(2.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DriverRow(status: DefconStatus?, color: Color) {
    val label: String
    val title: String?
    when {
        status?.breakdown?.triggerLabel != null -> {
            label = status.breakdown.triggerLabel!!
            title = status.breakdown.triggerArticleTitle
        }
        (status?.score ?: 0.0) == 0.0 -> {
            label = "No notable threats"; title = null
        }
        else -> {
            label = "Elevated"; title = "by recent activity"
        }
    }
    Column {
        SectionHeader("Driver", tracking = 1f)
        Spacer(Modifier.height(2.dp))
        Text(
            buildString {
                append(label)
                if (!title.isNullOrEmpty()) append(": $title")
            },
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color),
            maxLines = 2,
        )
    }
}

@Composable
private fun GaugeArc(level: Int, score: Double, modifier: Modifier) {
    val animatedScore by animateFloatAsState(score.toFloat(), tween(500), label = "scoreSweep")

    val startDeg = 150.0
    val sweepDeg = 240.0
    val segColors = listOf(
        colorHex("3b82f6"), // 5
        colorHex("22c55e"), // 4
        colorHex("eab308"), // 3
        colorHex("dc2626"), // 2
        Color.White,        // 1
    )
    val levelColor = DefconColors.colorForLevel(level)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * (90f / 155f)
            val r = (h * 78f / 155f).coerceAtMost(w * 0.42f)
            val stroke = 14.dp.toPx()

            val arcSize = Size(r * 2f, r * 2f)
            val topLeft = Offset(cx - r, cy - r)

            // Background track
            drawArc(
                color = Color.Gray.copy(alpha = 0.25f),
                startAngle = startDeg.toFloat(),
                sweepAngle = sweepDeg.toFloat(),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            )
            // Five coloured segments (48° each)
            for (i in 0..4) {
                drawArc(
                    color = segColors[i].copy(alpha = 0.85f),
                    startAngle = (startDeg + i * 48.0).toFloat(),
                    sweepAngle = 48f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
            }
            // Score-fill overlay
            val needleDeg = startDeg + (animatedScore.coerceIn(0f, 100f) / 100.0) * sweepDeg
            drawArc(
                color = levelColor.copy(alpha = 0.35f),
                startAngle = startDeg.toFloat(),
                sweepAngle = (needleDeg - startDeg).toFloat(),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            )

            // Needle triangle
            val rad = (needleDeg * Math.PI / 180.0).toFloat()
            val length = r * 0.55f
            val width = 4.dp.toPx()
            val tip = Offset(cx + length * cos(rad), cy + length * sin(rad))
            val perp = rad + Math.PI.toFloat() / 2f
            val left = Offset(cx + width * cos(perp), cy + width * sin(perp))
            val right = Offset(cx - width * cos(perp), cy - width * sin(perp))
            val path = Path().apply {
                moveTo(tip.x, tip.y); lineTo(left.x, left.y); lineTo(right.x, right.y); close()
            }
            drawPath(path, Color.White.copy(alpha = 0.9f))

            // Pivot
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 5.dp.toPx(), center = Offset(cx, cy))
            drawCircle(levelColor, radius = 3.dp.toPx(), center = Offset(cx, cy))
        }

        // Center labels (below pivot)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(top = 110.dp),
        ) {
            Text(
                "$level",
                style = TextStyle(
                    fontSize = 30.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, color = levelColor,
                ),
            )
            Text(
                DefconStatus.labelFor(level).uppercase(),
                style = TextStyle(
                    fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp, color = levelColor.copy(alpha = 0.85f),
                ),
            )
            Text(
                "${score.toInt()} / 100",
                style = TextStyle(fontSize = 8.sp, color = textSecondary()),
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}
