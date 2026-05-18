package com.cyberbits.defcon.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cyberbits.defcon.ui.theme.textSecondary

/**
 * Uppercase tracked label used by every iOS widget header.
 * iOS: `.caption2.weight(.semibold)` + tracking 1.0 / 1.5.
 */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, tracking: Float = 1f) {
    Text(
        text = text.uppercase(),
        color = textSecondary(),
        modifier = modifier,
        style = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = tracking.sp,
        ),
    )
}
