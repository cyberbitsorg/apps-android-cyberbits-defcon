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
        val r = Scorer.computeGlobalScore(window, newCount = 20, baseline = 10.0, now = now)
        assertTrue("score <= 100", r.score <= 100.0)
        assertTrue("score > weightedMax-only", r.score > r.weightedMax - 0.001)
    }
}
