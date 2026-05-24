package com.cyberbits.defcon.pipeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScorerTest {

    @Test fun emptyInputScoresZero() {
        val r = Scorer.computeArticleScore("", "")
        assertEquals(0.0, r.score, 0.0)
        assertEquals(null, r.trigger)
    }

    @Test fun triggerArticleUsesTrackAWhenHigher() {
        val r = Scorer.computeArticleScore(
            "Critical zero-day actively exploited in the wild",
            "Attackers are exploiting CVE-2024-9999 affecting millions of users."
        )
        assertTrue("score should be >= activeExploitation base 70", r.score >= 70.0)
        assertEquals(TriggerType.ACTIVE_EXPLOITATION, r.trigger)
    }

    @Test fun newsletterTitleSuppressesTrigger() {
        val r = Scorer.computeArticleScore(
            "Weekly Roundup: actively exploited zero-day in chrome",
            "Body mentions exploitation."
        )
        assertEquals(null, r.trigger)
    }

    @Test fun cvssMaxSeverityPhraseScores9() {
        // "max severity" / "maximum severity" → infers CVSS 9.0 → cveScore 27
        val r = Scorer.computeArticleScore(
            "Ubiquiti patches three max severity unifi os vulnerabilities",
            ""
        )
        assertEquals(27.0, r.cve, 0.1)
    }

    @Test fun cvssExplicitWindowExtracts() {
        val r = Scorer.computeArticleScore(
            "Vendor patches bug",
            "CVSS: 9.8 critical flaw in widget."
        )
        // cveRaw = 9.8 → cveScore = 9.8/10 * 30 = 29.4
        assertEquals(29.4, r.cve, 0.1)
    }

    @Test fun levelThresholds() {
        assertEquals(1, Scorer.levelFromScore(85.0))
        assertEquals(2, Scorer.levelFromScore(60.0))
        assertEquals(3, Scorer.levelFromScore(40.0))
        assertEquals(4, Scorer.levelFromScore(20.0))
        assertEquals(5, Scorer.levelFromScore(0.0))
    }

    @Test fun globalScoreCombinesWeightedMaxAndVolume() {
        val now = 1_700_000_000_000L
        val h = 3600_000L
        val window = listOf(
            Scorer.WindowArticle("title-a", 60.0, null, now - 1L * h),
            Scorer.WindowArticle("title-b", 80.0, null, now - 18L * h),
        )
        val r = Scorer.computeGlobalScore(window, newCount = 20, baseline = 10.0, now = now, windowHours = 24.0)
        assertTrue("score <= 100", r.score <= 100.0)
        assertTrue("score > weightedMax-only", r.score > r.weightedMax - 0.001)
    }

    @Test fun currentWindowHoursWeekendVsWeekday() {
        // 2026-05-23 is a Saturday (UTC). 2026-05-25 is a Monday (UTC).
        val saturdayUtc = java.time.ZonedDateTime.of(2026, 5, 23, 12, 0, 0, 0, java.time.ZoneOffset.UTC)
            .toInstant().toEpochMilli()
        val mondayUtc = java.time.ZonedDateTime.of(2026, 5, 25, 12, 0, 0, 0, java.time.ZoneOffset.UTC)
            .toInstant().toEpochMilli()
        assertEquals(72.0, Scorer.currentWindowHours(saturdayUtc), 0.0)
        assertEquals(48.0, Scorer.currentWindowHours(mondayUtc), 0.0)
    }

    @Test fun globalScoreHonoursExplicitWindowHours() {
        val now = 1_700_000_000_000L
        val h = 3600_000L
        val window = listOf(
            Scorer.WindowArticle("old-high", 90.0, null, now - 50L * h),
        )
        // 48h window → 50h-old article is fully decayed → 0
        val r48 = Scorer.computeGlobalScore(window, newCount = 0, baseline = null, now = now, windowHours = 48.0)
        assertEquals(0.0, r48.weightedMax, 0.001)
        // 72h window → weight = 1 - 50/72 = 22/72, score = 90 * 22/72 ≈ 27.5
        val r72 = Scorer.computeGlobalScore(window, newCount = 0, baseline = null, now = now, windowHours = 72.0)
        assertEquals(90.0 * 22.0 / 72.0, r72.weightedMax, 0.1)
    }

    @Test fun triggerArticlePublishedAtPropagates() {
        val now = 1_700_000_000_000L
        val h = 3600_000L
        val publishedAt = now - 2L * h
        val window = listOf(
            Scorer.WindowArticle("winner", 80.0, null, publishedAt),
            Scorer.WindowArticle("loser", 30.0, null, now - 1L * h),
        )
        val r = Scorer.computeGlobalScore(window, newCount = 0, baseline = null, now = now, windowHours = 48.0)
        assertEquals(publishedAt, r.triggerArticlePublishedAt)
    }

    @Test fun triggerArticlePublishedAtNullForEmptyWindow() {
        val r = Scorer.computeGlobalScore(emptyList(), newCount = 0, baseline = null, now = 1L, windowHours = 48.0)
        assertEquals(null, r.triggerArticlePublishedAt)
    }
}
