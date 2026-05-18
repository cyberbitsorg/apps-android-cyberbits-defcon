package com.cyberbits.defcon.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.cyberbits.defcon.data.entity.ArticleEntity

private val CVE_REGEX = Regex("""\bCVE-\d{4}-\d{3,}\b""", RegexOption.IGNORE_CASE)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentCVEs(articles: List<ArticleEntity>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cves = LinkedHashSet<String>()
    for (a in articles) {
        for (m in CVE_REGEX.findAll("${a.title} ${a.summary}")) {
            cves += m.value.uppercase()
        }
    }
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text("Recent CVEs", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (cves.isEmpty()) {
            Text("None in window", style = MaterialTheme.typography.bodySmall)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                cves.take(20).forEach { id ->
                    AssistChip(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://nvd.nist.gov/vuln/detail/$id".toUri())
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        },
                        label = { Text(id) },
                    )
                }
            }
        }
    }
}
