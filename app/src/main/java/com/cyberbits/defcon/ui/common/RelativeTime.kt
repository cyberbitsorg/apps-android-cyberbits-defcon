package com.cyberbits.defcon.ui.common

import java.util.concurrent.TimeUnit

object RelativeTime {
    fun format(epochMs: Long, now: Long = System.currentTimeMillis()): String {
        val diff = (now - epochMs).coerceAtLeast(0)
        val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (mins < 1) return "just now"
        if (mins < 60) return "${mins}m ago"
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        if (hours < 24) return "${hours}h ago"
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        if (days < 7) return "${days}d ago"
        return "${days / 7}w ago"
    }
}
