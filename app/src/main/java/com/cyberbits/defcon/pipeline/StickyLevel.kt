package com.cyberbits.defcon.pipeline

/**
 * Sticky-level state machine. DEFCON numbering is INVERTED: 1 = highest severity,
 * 5 = lowest. The machine tracks a "floor" (most severe level seen recently) and
 * only allows the displayed level to decrease in severity (numeric value increase)
 * one step per cooldown period. Severity rises take effect immediately.
 *
 * Pure port of iOS `StickyLevel.swift` / docker `db/sticky_level.py`.
 */
object StickyLevel {

    /** 3 hours, matching docker `COOLDOWN`. */
    const val COOLDOWN_MS: Long = 3L * 3600L * 1000L

    data class State(val floor: Int?, val untilAt: Long?)

    data class Result(val displayedLevel: Int, val newFloor: Int, val newUntilAt: Long?)

    private fun isMoreSevere(a: Int, b: Int): Boolean = a < b

    fun computeDisplayedLevel(
        rawLevel: Int,
        state: State,
        evidenceAnchor: Long? = null,
        now: Long = System.currentTimeMillis(),
    ): Result {
        val floor = state.floor
            ?: return Result(displayedLevel = rawLevel, newFloor = rawLevel, newUntilAt = null)

        // Severity rising (or equal) -> snap to raw, clear cooldown.
        if (isMoreSevere(rawLevel, floor) || rawLevel == floor) {
            return Result(displayedLevel = rawLevel, newFloor = rawLevel, newUntilAt = null)
        }

        // raw is LESS severe than floor (numeric raw > floor).
        // Take the EARLIER of stored `untilAt` and the evidence-based estimate so a
        // stale future-dated `untilAt` can't pin the floor indefinitely.
        val evidenceUntil = evidenceAnchor?.let { it + COOLDOWN_MS } ?: Long.MIN_VALUE
        val until: Long = when (val stored = state.untilAt) {
            null -> evidenceUntil
            else -> minOf(stored, evidenceUntil)
        }

        // Cooldown still active -> hold.
        if (now < until) {
            return Result(displayedLevel = floor, newFloor = floor, newUntilAt = until)
        }

        // Cooldown expired -> step floor toward raw (less severe). Catch up one extra
        // step per full cooldown period elapsed since `until`.
        val extraSteps = maxOf(0L, (now - until) / COOLDOWN_MS).toInt()
        var newFloor = floor + 1 + extraSteps
        var newUntil: Long? = now + COOLDOWN_MS
        if (isMoreSevere(newFloor, rawLevel) || newFloor == rawLevel) {
            if (newFloor == rawLevel) newUntil = null
        } else {
            // overshot -> clamp to raw.
            newFloor = rawLevel
            newUntil = null
        }
        return Result(displayedLevel = newFloor, newFloor = newFloor, newUntilAt = newUntil)
    }
}
