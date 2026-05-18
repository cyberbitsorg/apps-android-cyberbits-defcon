package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cyberbits.defcon.model.DefconStatus

/** 2x2 grid of factor cards (Volume / Weighted Max / Trigger / Score). */
@Composable
fun FactorScoresWidget(status: DefconStatus?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FactorCell("Weighted Max", status?.breakdown?.weightedMax ?: 0.0, 100.0, Modifier.weight(1f))
            FactorCell("Volume Bonus", status?.breakdown?.volumeBonus ?: 0.0, 10.0, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FactorCell("Total Score", status?.score ?: 0.0, 100.0, Modifier.weight(1f))
            TriggerCell(status, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FactorCell(label: String, value: Double, max: Double, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("%.1f".format(value), style = MaterialTheme.typography.titleLarge)
            LinearProgressIndicator(
                progress = { (value / max).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun TriggerCell(status: DefconStatus?, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Trigger", style = MaterialTheme.typography.labelMedium)
            Text(
                status?.breakdown?.triggerLabel ?: "—",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                status?.breakdown?.triggerArticleTitle ?: "",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
            )
        }
    }
}
