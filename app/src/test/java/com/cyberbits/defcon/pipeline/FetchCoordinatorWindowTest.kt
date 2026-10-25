package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.model.FeedSource
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression: the global-score window must carry each article's stored trigger
 * so the single-event spike carve-out can fire (iOS passes `article.trigger`).
 * Before the fix the window was built with a hardcoded null trigger.
 */
class FetchCoordinatorWindowTest {

    private fun entity(triggerRaw: String?) = ArticleEntity(
        id = "id", guid = "guid", title = "t", summary = "s", url = "u",
        source = FeedSource.HACKREAD, publishedAt = 0L, defconScore = 80.0,
        categories = emptyList(), isRead = false, readAt = null,
        fingerprint = "fp", triggerRaw = triggerRaw,
    )

    @Test fun storedTriggerRidesIntoWindow() {
        val w = entity("active_exploitation").toWindowArticle()
        assertEquals(TriggerType.ACTIVE_EXPLOITATION, w.trigger)
    }

    @Test fun nullTriggerStaysNull() {
        val w = entity(null).toWindowArticle()
        assertEquals(null, w.trigger)
    }

    @Test fun spikeCarveoutFiresThroughEntityWindow() {
        val now = 1_700_000_000_000L
        val entities = listOf(
            entity("active_exploitation").copy(publishedAt = now),
            entity(null).copy(defconScore = 20.0, publishedAt = now),
            entity(null).copy(defconScore = 15.0, publishedAt = now),
        )
        val window = entities.map { it.toWindowArticle() }
        val r = Scorer.computeGlobalScore(window, newCount = 0, baseline = null, now = now, windowHours = 48.0)
        // With the trigger present the spike carve-out keeps the full 80; a null
        // trigger would blend down to 0.6*80 + 0.4*20 = 56.
        assertEquals(80.0, r.weightedMax, 0.1)
        assertEquals(TriggerType.ACTIVE_EXPLOITATION, r.trigger)
    }
}
