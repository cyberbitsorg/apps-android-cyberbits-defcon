package com.cyberbits.defcon.ui.articles

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.pipeline.Scorer
import com.cyberbits.defcon.ui.common.RelativeTime
import com.cyberbits.defcon.ui.theme.DefconColors

@Composable
fun ArticleRow(
    article: ArticleEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = Scorer.levelFromScore(article.defconScore)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(DefconColors.colorForLevel(level))
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(
                article.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                fontWeight = if (!article.isRead) FontWeight.SemiBold else FontWeight.Normal,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(article.source.displayName, style = MaterialTheme.typography.labelSmall)
                Text("·", style = MaterialTheme.typography.labelSmall)
                Text(RelativeTime.format(article.publishedAt), style = MaterialTheme.typography.labelSmall)
                Text("·", style = MaterialTheme.typography.labelSmall)
                Text("${"%.0f".format(article.defconScore)}", style = MaterialTheme.typography.labelSmall)
            }
        }
        if (!article.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(12.dp))
        } else {
            Spacer(Modifier.width(20.dp))
        }
    }
}
