package com.cyberbits.defcon.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.ui.common.SectionHeader
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.colorHex
import com.cyberbits.defcon.ui.theme.textTertiary

// Same tracked list as iOS TrendingKeywords (keyword, tier weight, color).
private val TRACKED: List<Triple<String, Int, Color>> = listOf(
    Triple("zero-day", 5, colorHex("dc2626")),
    Triple("ransomware", 4, colorHex("dc2626")),
    Triple("exploit", 3, colorHex("ea580c")),
    Triple("apt", 3, colorHex("ea580c")),
    Triple("vulnerability", 2, colorHex("f59e0b")),
    Triple("malware", 2, colorHex("f59e0b")),
    Triple("breach", 2, colorHex("f59e0b")),
    Triple("cve", 1, colorHex("3b82f6")),
    Triple("backdoor", 1, colorHex("3b82f6")),
    Triple("phishing", 1, colorHex("8b5cf6")),
    Triple("ddos", 1, colorHex("8b5cf6")),
    Triple("supply chain", 2, colorHex("ea580c")),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendingKeywords(articles: List<ArticleEntity>, modifier: Modifier = Modifier) {
    val combined = articles.take(50)
        .joinToString(" ") { "${it.title} ${it.summary}".lowercase() }

    val keywords = TRACKED.mapNotNull { (kw, tier, color) ->
        val count = combined.split(kw).size - 1
        if (count > 0) Triple(kw, count * tier, color) else null
    }.sortedByDescending { it.second }.take(12)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(adaptiveSurface())
            .padding(16.dp),
    ) {
        SectionHeader("Trending keywords", tracking = 1f)
        Spacer(Modifier.height(8.dp))
        if (keywords.isEmpty()) {
            Text("No data yet", color = textTertiary(), style = TextStyle(fontSize = 11.sp))
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                keywords.forEach { (kw, count, color) ->
                    val size = (10 + (count / 2).coerceAtMost(6)).sp
                    Text(
                        kw,
                        color = color,
                        style = TextStyle(fontSize = size),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
