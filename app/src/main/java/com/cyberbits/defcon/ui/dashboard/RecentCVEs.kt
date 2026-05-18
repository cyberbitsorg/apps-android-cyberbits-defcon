package com.cyberbits.defcon.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.ui.common.SectionHeader
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.colorHex
import com.cyberbits.defcon.ui.theme.textTertiary

private val CVE_REGEX = Regex("""CVE-\d{4}-\d{4,}""", RegexOption.IGNORE_CASE)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentCVEs(articles: List<ArticleEntity>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cves = LinkedHashSet<String>()
    for (a in articles.take(50)) {
        for (m in CVE_REGEX.findAll("${a.title} ${a.summary}")) {
            cves += m.value.uppercase()
            if (cves.size >= 10) break
        }
        if (cves.size >= 10) break
    }
    val blue = colorHex("3b82f6")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(adaptiveSurface())
            .padding(16.dp),
    ) {
        SectionHeader("Recent CVEs", tracking = 1f)
        Spacer(Modifier.height(8.dp))
        if (cves.isEmpty()) {
            Text(
                "No CVEs detected in current articles",
                color = textTertiary(),
                style = TextStyle(fontSize = 11.sp),
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                cves.take(8).forEach { id ->
                    Text(
                        id,
                        color = blue,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(blue.copy(alpha = 0.12f))
                            .border(0.5.dp, blue.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .clickable {
                                val i = Intent(Intent.ACTION_VIEW, "https://nvd.nist.gov/vuln/detail/$id".toUri())
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(i)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
