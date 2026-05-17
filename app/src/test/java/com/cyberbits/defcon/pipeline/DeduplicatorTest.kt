package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.model.RawArticle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeduplicatorTest {

    private fun art(title: String, guid: String = title): RawArticle = RawArticle(
        guid = guid, title = title, summary = "", url = "https://x",
        source = FeedSource.HACKREAD, publishedAt = 0L, categories = emptyList()
    )

    @Test fun fingerprintIsDeterministic() {
        val a = Deduplicator.fingerprint("Critical zero-day patched in chrome browser")
        val b = Deduplicator.fingerprint("CRITICAL ZERO-DAY PATCHED IN CHROME BROWSER!!!")
        assertEquals(a, b)
    }

    @Test fun fingerprintDifferentForUnrelatedTitles() {
        val a = Deduplicator.fingerprint("Microsoft patches kernel flaw")
        val b = Deduplicator.fingerprint("Major ransomware attack on hospitals")
        assertTrue(a != b)
    }

    @Test fun l1ExactFingerprintDropsDuplicate() {
        val a = art("Critical zero-day patched in chrome browser")
        val b = art("CRITICAL ZERO-DAY PATCHED IN CHROME BROWSER!!!", guid = "b")
        val existing = setOf(Deduplicator.fingerprint(a.title))
        val out = Deduplicator.deduplicate(listOf(b), existing, recentTitles = emptyList())
        assertEquals(0, out.size)
    }

    @Test fun unrelatedTitleSurvivesL1() {
        val a = art("Microsoft releases security update")
        val out = Deduplicator.deduplicate(
            listOf(a),
            existingFingerprints = setOf(Deduplicator.fingerprint("Major ransomware attack on hospitals")),
            recentTitles = listOf("Major ransomware attack on hospitals"),
        )
        assertEquals(1, out.size)
    }

    @Test fun temporalConflictPreventsL2Match() {
        // Same words but different months — must NOT be flagged as duplicate.
        val recent = listOf("Massive breach reported in January 2024")
        val incoming = art("Massive breach reported in March 2024")
        val out = Deduplicator.deduplicate(listOf(incoming), existingFingerprints = emptySet(), recentTitles = recent)
        assertEquals("kept because months differ", 1, out.size)
    }

    @Test fun yearConflictPreventsL2Match() {
        val recent = listOf("Critical breach disclosed at vendor in 2023")
        val incoming = art("Critical breach disclosed at vendor in 2025")
        val out = Deduplicator.deduplicate(listOf(incoming), existingFingerprints = emptySet(), recentTitles = recent)
        assertEquals(1, out.size)
    }
}
