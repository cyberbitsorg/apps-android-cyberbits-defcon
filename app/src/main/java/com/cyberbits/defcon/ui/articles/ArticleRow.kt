package com.cyberbits.defcon.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.pipeline.Scorer
import com.cyberbits.defcon.ui.common.RelativeTime
import com.cyberbits.defcon.ui.theme.DefconColors
import com.cyberbits.defcon.ui.theme.LocalDefconDarkTheme
import com.cyberbits.defcon.ui.theme.accentColor
import com.cyberbits.defcon.ui.theme.adaptiveSurface
import com.cyberbits.defcon.ui.theme.colorHex
import com.cyberbits.defcon.ui.theme.textSecondary
import com.cyberbits.defcon.ui.theme.textTertiary

@Composable
fun ArticleRow(
    article: ArticleEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = Scorer.levelFromScore(article.defconScore)
    val levelColor = DefconColors.colorForLevel(level)
    val isDark = LocalDefconDarkTheme.current
    val levelDisplayColor = if (level == 1 && !isDark) colorHex("111827") else levelColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (article.isRead) 0.7f else 1f)
            .clip(RoundedCornerShape(10.dp))
            .background(adaptiveSurface())
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Vertical accent stripe
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxHeight()
                .width(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(levelColor),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    article.title,
                    color = if (article.isRead) textSecondary() else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (article.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    style = TextStyle(fontSize = 13.sp),
                    maxLines = 3,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(6.dp))
                // Score badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(levelColor.copy(alpha = 0.15f))
                        .border(
                            width = if (level == 1) 1.dp else 0.dp,
                            color = if (level == 1) DefconColors.level2.copy(alpha = 0.3f) else androidx.compose.ui.graphics.Color.Transparent,
                            shape = RoundedCornerShape(5.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        "%.0f".format(article.defconScore),
                        color = levelDisplayColor,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Slate-300 fill, uniform across sources — matches docker fallback;
                // DEFCON-level colored border conveys severity at a glance.
                val badgeColor = colorHex("cbd5e1")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.dp, levelColor.copy(alpha = 0.38f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Text(
                        article.source.abbreviation,
                        color = badgeColor,
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                        ),
                    )
                }
                Text("·", color = textTertiary(), style = TextStyle(fontSize = 10.sp))
                Text(
                    RelativeTime.format(article.publishedAt),
                    color = textSecondary(),
                    style = TextStyle(fontSize = 10.sp),
                )
                Spacer(Modifier.weight(1f))
                if (article.isRead) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Read",
                        tint = textTertiary(),
                        modifier = Modifier.height(12.dp),
                    )
                }
            }
        }
    }
}
