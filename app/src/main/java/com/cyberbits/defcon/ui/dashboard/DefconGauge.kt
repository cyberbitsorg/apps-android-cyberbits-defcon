package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cyberbits.defcon.model.DefconStatus
import com.cyberbits.defcon.ui.theme.DefconColors

/** Circular gauge with foreground arc proportional to the 0–100 DEFCON score. */
@Composable
fun DefconGauge(status: DefconStatus?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        val score = status?.score ?: 0.0
        val level = status?.level ?: 5
        val ringColor = DefconColors.colorForLevel(level)
        val onSurface = MaterialTheme.colorScheme.onSurface

        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 22.dp.toPx()
            val arcSize = Size(size.minDimension - strokeWidth, size.minDimension - strokeWidth)
            val topLeft = Offset(
                (size.width - arcSize.width) / 2f,
                (size.height - arcSize.height) / 2f,
            )
            drawArc(
                color = Color.LightGray.copy(alpha = 0.25f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )
            val sweep = (score / 100.0 * 270.0).toFloat().coerceIn(0f, 270f)
            drawArc(
                color = ringColor,
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )
        }

        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DEFCON $level",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface,
                )
                Text(
                    text = "${"%.0f".format(score)}",
                    style = MaterialTheme.typography.displayLarge,
                    color = onSurface,
                )
                Text(
                    text = status?.label ?: "—",
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurface,
                )
            }
        }
    }
}
