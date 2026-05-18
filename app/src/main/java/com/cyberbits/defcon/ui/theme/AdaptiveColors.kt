package com.cyberbits.defcon.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.cyberbits.defcon.model.FeedSource

/** Hex string ("rrggbb" or "#rrggbb") → Compose Color. */
fun colorHex(hex: String): Color {
    val clean = hex.removePrefix("#")
    val argb = 0xFF000000.toInt() or clean.toLong(16).toInt()
    return Color(argb)
}

/**
 * Whether the current DefconTheme is rendering in dark mode. Provided by
 * `DefconTheme` so widgets get the user-selected appearance (System/Light/Dark)
 * instead of the OS-only `isSystemInDarkTheme()`.
 */
val LocalDefconDarkTheme = compositionLocalOf { false }

@Composable @ReadOnlyComposable
private fun isDark(): Boolean = LocalDefconDarkTheme.current

/** iOS Color.adaptiveBackground — background of scroll views. */
@Composable @ReadOnlyComposable
fun adaptiveBackground(): Color =
    if (isDark()) Color(0xFF0B0B0F) else Color(0xFFF8F8FB)

/** iOS Color.adaptiveSurface — card surface inside widgets. */
@Composable @ReadOnlyComposable
fun adaptiveSurface(): Color =
    if (isDark()) Color(0xFF1A1A1F) else Color(0xFFFFFFFF)

/** iOS Color.adaptiveBorder — subtle card stroke. */
@Composable @ReadOnlyComposable
fun adaptiveBorder(): Color =
    if (isDark()) Color(0xFF2A2A33) else Color(0xFFE5E7EB)

/** iOS .secondary text color. */
@Composable @ReadOnlyComposable
fun textSecondary(): Color =
    if (isDark()) Color(0xFF9CA3AF) else Color(0xFF6B7280)

/** iOS .tertiary text color. */
@Composable @ReadOnlyComposable
fun textTertiary(): Color =
    if (isDark()) Color(0xFF6B7280) else Color(0xFF9CA3AF)

/** Per-feed accent color (matches FeedSource.accentHex). */
fun FeedSource.accentColor(): Color = colorHex(accentHex)
