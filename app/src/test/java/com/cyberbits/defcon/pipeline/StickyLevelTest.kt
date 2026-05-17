package com.cyberbits.defcon.pipeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StickyLevelTest {

    private val hourMs = 3600L * 1000L

    @Test fun noFloorReturnsRaw() {
        val now = System.currentTimeMillis()
        val r = StickyLevel.computeDisplayedLevel(
            rawLevel = 3,
            state = StickyLevel.State(floor = null, untilAt = null),
            evidenceAnchor = null,
            now = now,
        )
        assertEquals(3, r.displayedLevel)
        assertEquals(3, r.newFloor)
        assertNull(r.newUntilAt)
    }

    @Test fun severityRiseSnapsImmediately() {
        val now = System.currentTimeMillis()
        val r = StickyLevel.computeDisplayedLevel(
            rawLevel = 2,
            state = StickyLevel.State(floor = 4, untilAt = now + hourMs),
            evidenceAnchor = null,
            now = now,
        )
        assertEquals(2, r.displayedLevel)
        assertEquals(2, r.newFloor)
        assertNull(r.newUntilAt)
    }

    @Test fun cooldownActiveHoldsFloor() {
        val now = System.currentTimeMillis()
        val anchor = now - hourMs
        val r = StickyLevel.computeDisplayedLevel(
            rawLevel = 4,
            state = StickyLevel.State(floor = 2, untilAt = anchor + StickyLevel.COOLDOWN_MS),
            evidenceAnchor = anchor,
            now = now,
        )
        assertEquals(2, r.displayedLevel)
        assertEquals(2, r.newFloor)
    }

    @Test fun cooldownExpiredStepsOneLessSevere() {
        val now = System.currentTimeMillis()
        val anchor = now - 4 * hourMs
        val r = StickyLevel.computeDisplayedLevel(
            rawLevel = 5,
            state = StickyLevel.State(floor = 2, untilAt = anchor + StickyLevel.COOLDOWN_MS),
            evidenceAnchor = anchor,
            now = now,
        )
        assertEquals(3, r.displayedLevel)
        assertEquals(3, r.newFloor)
    }

    @Test fun catchUpMultipleStepsWhenIdle() {
        val now = System.currentTimeMillis()
        val anchor = now - 10 * hourMs
        val r = StickyLevel.computeDisplayedLevel(
            rawLevel = 5,
            state = StickyLevel.State(floor = 1, untilAt = anchor + StickyLevel.COOLDOWN_MS),
            evidenceAnchor = anchor,
            now = now,
        )
        assertEquals(4, r.displayedLevel)
        assertEquals(4, r.newFloor)
    }

    @Test fun overshootClampsToRaw() {
        val now = System.currentTimeMillis()
        val anchor = now - 20 * hourMs
        val r = StickyLevel.computeDisplayedLevel(
            rawLevel = 3,
            state = StickyLevel.State(floor = 1, untilAt = anchor + StickyLevel.COOLDOWN_MS),
            evidenceAnchor = anchor,
            now = now,
        )
        assertEquals(3, r.displayedLevel)
        assertEquals(3, r.newFloor)
        assertNull(r.newUntilAt)
    }
}
