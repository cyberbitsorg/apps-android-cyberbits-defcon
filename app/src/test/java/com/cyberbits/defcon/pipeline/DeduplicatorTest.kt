package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.model.RawArticle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeduplicatorTest {

    private fun art(title: String, summary: String = "", guid: String = title) = RawArticle(
        guid = guid, title = title, summary = summary, url = "https://x",
        source = FeedSource.HACKREAD, publishedAt = 0L, categories = emptyList()
    )

    @Test fun fingerprintIsDeterministicAndCaseInsensitive() {
        val a = Deduplicator.fingerprint("Critical zero-day patched in chrome browser")
        val b = Deduplicator.fingerprint("CRITICAL ZERO-DAY PATCHED IN CHROME BROWSER!!!")
        assertEquals(a, b)
    }

    @Test fun l1ExactFingerprintDropsDuplicate() {
        val a = art("Critical zero-day patched in chrome browser")
        val b = art("CRITICAL ZERO-DAY PATCHED IN CHROME BROWSER!!!", guid = "b")
        val existing = setOf(Deduplicator.fingerprint(a.title))
        val out = Deduplicator.deduplicate(listOf(b), existing, recentTitles = emptyList())
        assertEquals(0, out.size)
    }

    @Test fun extractCveIdsCanonical() {
        val cves = Deduplicator.extractCVEs("Fix for cve-2024-1234 and CVE 2024 56789 released")
        assertTrue("CVE-2024-1234" in cves)
        assertTrue("CVE-2024-56789" in cves)
    }

    @Test fun l1bCveMatchDropsDuplicate() {
        val article = art("New patch released", summary = "Fixes CVE-2024-1234 critical bug")
        val out = Deduplicator.deduplicate(
            listOf(article),
            existingFingerprints = emptySet(),
            recentTitles = emptyList(),
            recentCves = setOf("CVE-2024-1234"),
        )
        assertEquals(0, out.size)
    }

    @Test fun temporalConflictKeepsBoth() {
        val recent = listOf("Massive breach reported in January 2024")
        val incoming = art("Massive breach reported in March 2024")
        val out = Deduplicator.deduplicate(listOf(incoming), emptySet(), recent)
        assertEquals(1, out.size)
    }

    @Test fun yearConflictKeepsBoth() {
        val recent = listOf("Critical breach disclosed at vendor in 2023")
        val incoming = art("Critical breach disclosed at vendor in 2025")
        val out = Deduplicator.deduplicate(listOf(incoming), emptySet(), recent)
        assertEquals(1, out.size)
    }

    @Test fun jaccardThresholdDropsNearDuplicate() {
        val recent = listOf("Microsoft patches critical exchange server vulnerability today")
        val incoming = art("Microsoft patches critical exchange server vulnerability now")
        val out = Deduplicator.deduplicate(listOf(incoming), emptySet(), recent)
        assertEquals("near-duplicate dropped by Jaccard >= 0.28", 0, out.size)
    }
}
