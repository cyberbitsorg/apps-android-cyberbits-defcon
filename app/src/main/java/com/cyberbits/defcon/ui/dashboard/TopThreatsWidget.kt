package com.cyberbits.defcon.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.pipeline.Scorer
import com.cyberbits.defcon.ui.common.SectionHeader
import com.cyberbits.defcon.ui.theme.DefconColors
import com.cyberbits.defcon.ui.theme.LocalDefconDarkTheme
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.colorHex

@Composable
fun TopThreatsWidget(articles: List<ArticleEntity>, modifier: Modifier = Modifier) {
    val top = articles.take(20).sortedByDescending { it.defconScore }.take(5)
    val context = LocalContext.current

    val isDark = LocalDefconDarkTheme.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(adaptiveSurface())
            .padding(16.dp),
    ) {
        SectionHeader("Top threats", tracking = 1f)
        Spacer(Modifier.height(8.dp))
        if (top.isEmpty()) {
            Text("No articles yet", style = TextStyle(fontSize = 11.sp))
        } else {
            top.forEach { article ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable {
                            val i = Intent(Intent.ACTION_VIEW, article.url.toUri())
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(i)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val level = Scorer.levelFromScore(article.defconScore)
                    val barColor = if (level == 1 && !isDark) colorHex("111827") else DefconColors.colorForLevel(level)
                    Box(
                        modifier = Modifier
                            .size(width = 3.dp, height = 22.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        article.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        style = TextStyle(fontSize = 12.sp),
                    )
                }
            }
        }
    }
}

