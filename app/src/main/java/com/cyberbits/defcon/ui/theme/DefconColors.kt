package com.cyberbits.defcon.ui.theme

import androidx.compose.ui.graphics.Color

/** Brand colors for the 5 DEFCON levels (matches DefconStatus.colorHexFor). */
object DefconColors {
    val level1 = Color(0xFFFFFFFF) // Cocked Pistol — white
    val level2 = Color(0xFFDC2626) // Fast Pace — red
    val level3 = Color(0xFFEAB308) // Round House — yellow
    val level4 = Color(0xFF22C55E) // Double Take — green
    val level5 = Color(0xFF3B82F6) // Fade Out — blue
    val unknown = Color(0xFF808080)

    fun colorForLevel(level: Int): Color = when (level) {
        1 -> level1
        2 -> level2
        3 -> level3
        4 -> level4
        5 -> level5
        else -> unknown
    }
}
