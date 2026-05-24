package com.cyberbits.defcon.model

data class DefconBreakdown(
    val weightedMax: Double,            // 0–100
    val volumeBonus: Double,            // 0–10
    val triggerLabel: String? = null,   // e.g. "Active Exploitation" (filled in by FetchCoordinator)
    val triggerArticleTitle: String? = null,
    val triggerArticlePublishedAt: Long? = null,
) {
    val total: Double get() = (weightedMax + volumeBonus).coerceAtMost(100.0)
}

data class DefconStatus(
    val score: Double,
    val level: Int,           // 1 (highest threat) .. 5 (lowest), official DEFCON convention
    val label: String,
    val colorHex: String,
    val breakdown: DefconBreakdown,
    val trend: Trend,
    val computedAt: Long,     // epoch millis
) {
    companion object {
        /** Official DEFCON callsigns (matches docker constants.ts + iOS DefconStatus.swift). */
        fun labelFor(level: Int): String = when (level) {
            1 -> "COCKED PISTOL"
            2 -> "FAST PACE"
            3 -> "ROUND HOUSE"
            4 -> "DOUBLE TAKE"
            5 -> "FADE OUT"
            else -> "UNKNOWN"
        }

        fun shortLabel(level: Int): String = when (level) {
            in 1..5 -> "DEFCON $level"
            else -> "—"
        }

        /** Matches iOS Color.defcon(_:). */
        fun colorHexFor(level: Int): String = when (level) {
            1 -> "#ffffff"  // Cocked Pistol — white
            2 -> "#dc2626"  // Fast Pace — red
            3 -> "#eab308"  // Round House — yellow
            4 -> "#22c55e"  // Double Take — green
            5 -> "#3b82f6"  // Fade Out — blue
            else -> "#808080"
        }
    }
}
