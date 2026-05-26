package com.cyberbits.defcon.pipeline

import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/** Result of scoring a single article (v2). */
data class ArticleScoreResult(
    val score: Double,
    val trigger: TriggerType?,
    val trackA: Double,
    val trackB: Double,
    val cve: Double,
    val impact: Double,
    val keywords: Double,
)

/** Result of computing the global DEFCON score (v2). */
data class GlobalScoreResult(
    val score: Double,
    val weightedMax: Double,
    val volumeBonus: Double,
    val trigger: TriggerType?,
    val triggerArticleTitle: String?,
    val triggerArticlePublishedAt: Long? = null,
)

object Scorer {

    /** Each entry represents an article in the 24h scoring window. */
    data class WindowArticle(
        val title: String,
        val score: Double,
        val trigger: TriggerType?,
        val publishedAt: Long, // epoch millis
    )

    private const val WINDOW_HOURS_WEEKDAY: Double = 48.0
    private const val WINDOW_HOURS_WEEKEND: Double = 72.0
    private const val volumeBonusCap: Double = 10.0
    private const val impactCap: Double = 12.0
    private const val densityCap: Double = 2.0

    // MARK: - Per-article (v2)

    fun computeArticleScore(title: String, summary: String): ArticleScoreResult {
        val text = "$title $summary".lowercase()
        if (text.trim().isEmpty()) {
            return ArticleScoreResult(0.0, null, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val (cve, impact, keywords) = trackBUnclamped(text)

        // Title keyword bonus
        val titleRaw = keywordRaw(title.lowercase())
        val titleBoost = min(titleRaw.toDouble() / 8.0, 1.0) * 20.0

        val trackBUnc = cve + impact + keywords + titleBoost
        var trackB = min(trackBUnc, 80.0)

        val isNewsletter = Triggers.isNewsletterTitle(title)
        if (isNewsletter) {
            // Recaps bundle many unrelated threats; keyword density inflates Track B
            // even though no single item carries a dense article's severity.
            trackB *= 0.5
        }

        var triggerMatch = Triggers.detect(text)
        if (triggerMatch != null && isNewsletter) {
            triggerMatch = null
        }

        if (triggerMatch != null) {
            val base = triggerMatch.trigger.baseScore
            val bonus = min(trackBUnc, 10.0)
            val trackA = min(base + bonus, 100.0)
            val final = max(trackA, trackB)
            return ArticleScoreResult(
                score = round2(final),
                trigger = triggerMatch.trigger,
                trackA = round2(trackA),
                trackB = round2(trackB),
                cve = round2(cve),
                impact = round2(impact),
                keywords = round2(keywords),
            )
        }

        return ArticleScoreResult(
            score = round2(trackB),
            trigger = null,
            trackA = 0.0,
            trackB = round2(trackB),
            cve = round2(cve),
            impact = round2(impact),
            keywords = round2(keywords),
        )
    }

    // MARK: - Global (v2)

    /**
     * Weekend-aware default decay window. Saturday/Sunday in UTC uses 72h,
     * weekdays use 48h. Lets news from late Friday survive the quiet weekend.
     */
    fun currentWindowHours(nowMillis: Long = System.currentTimeMillis()): Double {
        val day = java.time.Instant.ofEpochMilli(nowMillis)
            .atZone(java.time.ZoneOffset.UTC).dayOfWeek
        return if (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY)
            WINDOW_HOURS_WEEKEND else WINDOW_HOURS_WEEKDAY
    }

    fun computeGlobalScore(
        window: List<WindowArticle>,
        newCount: Int,
        baseline: Double?,
        now: Long = System.currentTimeMillis(),
        windowHours: Double? = null,
    ): GlobalScoreResult {
        val activeWindow = windowHours ?: currentWindowHours(now)
        var weightedMax = 0.0
        var winner: WindowArticle? = null
        for (art in window) {
            val ageHours = (now - art.publishedAt).toDouble() / 3_600_000.0
            val weight = max(0.0, 1.0 - ageHours / activeWindow)
            val contribution = art.score * weight
            if (contribution > weightedMax) {
                weightedMax = contribution
                winner = art
            }
        }

        val volumeBonus: Double = if (baseline != null && baseline > 0) {
            val ratio = newCount.toDouble() / baseline
            max(0.0, min((ratio - 1.0) * 10.0, volumeBonusCap))
        } else 0.0

        val total = min(weightedMax + volumeBonus, 100.0)
        return GlobalScoreResult(
            score = round2(total),
            weightedMax = round2(weightedMax),
            volumeBonus = round2(volumeBonus),
            trigger = winner?.trigger,
            triggerArticleTitle = winner?.title,
            triggerArticlePublishedAt = winner?.publishedAt,
        )
    }

    /** Convert a 0–100 score to an official DEFCON level (1=highest threat, 5=lowest). */
    fun levelFromScore(score: Double): Int {
        if (score >= 80) return 1
        if (score >= 60) return 2
        if (score >= 40) return 3
        if (score >= 20) return 4
        return 5
    }

    // MARK: - Track B sub-scores

    private fun trackBUnclamped(text: String): Triple<Double, Double, Double> {
        val cveRaw = extractCVSS(text)
        val cveScore = (cveRaw / 10.0) * 30.0
        val impactScore = extractImpactScore(text)
        val keywordScore = extractKeywordScoreNormalized(text)
        return Triple(cveScore, impactScore, keywordScore)
    }

    private const val cveIdPattern = """\bCVE[-–— ]?\d{4}[-–— ]?\d{3,}\b"""
    private val severityWords: List<Pair<String, Double>> = listOf(
        "critical" to 9.0,
        "high" to 7.0,
        "medium" to 5.0,
        "low" to 2.5,
    )

    private fun extractCVSS(text: String): Double {
        // 1) Explicit CVSS scores within 50-char window
        val scores = mutableListOf<Double>()
        val cvssWindow = cachedRegex("""cvss[^\n]{0,50}""")
        val numRe = cachedRegex("""(?<![.\w])(\d+\.?\d*)(?!\w)""")
        for (m in cvssWindow.findAll(text)) {
            val window = m.value
            for (nm in numRe.findAll(window)) {
                val v = nm.groupValues[1].toDoubleOrNull() ?: continue
                if (v in 0.0..10.0) scores.add(v)
            }
        }
        scores.maxOrNull()?.let { return it }

        // "max severity" / "maximum severity" — vendor-press shorthand for CVSS ~10.
        if (cachedRegex("""\bmax(?:imum)?\s+severity\b""").containsMatchIn(text)) return 9.0

        // 2) CVE id + severity word co-occurrence
        if (cachedRegex(cveIdPattern).containsMatchIn(text)) {
            val matched = severityWords
                .filter { cachedRegex("""\b${it.first}\b""").containsMatchIn(text) }
                .map { it.second }
            matched.maxOrNull()?.let { return it }
        }

        // 3) Prose-described vulnerabilities with strong scope/qualifier
        val scope = Triggers.hasScopeAmplifier(text)
        if (cachedRegex("""\bzero[- ]?day\b""").containsMatchIn(text) && scope) return 8.0
        if (cachedRegex("""\b(remote code execution|RCE)\b""").containsMatchIn(text)) {
            val unauth = cachedRegex("""\bunauthenticated\b""").containsMatchIn(text)
            if (unauth || scope) return 8.0
        }
        if (cachedRegex("""\b(root|kernel)\b""").containsMatchIn(text) && scope) return 7.5

        return 0.0
    }

    private fun extractImpactScore(text: String): Double {
        var raw = 0.0
        if (cachedRegex("""power grid|hospital|water treatment|government|military|critical infrastructure""")
                .containsMatchIn(text)) raw += 5
        if (cachedRegex("""(?:\d[\d,.]*\s*)?millions?\b""").containsMatchIn(text)) raw += 4
        cachedRegex("""(\d+)\s*countries""").find(text)?.let { m ->
            m.groupValues[1].toIntOrNull()?.let { n -> if (n > 5) raw += 4 }
        }
        if (cachedRegex("""data breach|breached|breaches|\bbreach\b|leaked|exposed|exposing""")
                .containsMatchIn(text)) raw += 3
        if (cachedRegex("""(?:\d{6,}|\d{3,}[kK])\s*(users|records|devices|systems)""")
                .containsMatchIn(text)) raw += 3
        if (cachedRegex("""hundreds of\s+(colleges?|universities|schools|organizations|companies|agencies)""")
                .containsMatchIn(text)) raw += 3
        if (cachedRegex("""\b(all|most)\s+(major|popular|common)\s+(linux|windows|macos|android|ios|mobile\s+)?(distributions?|versions?|systems?|platforms?|devices?|distros?)\b""")
                .containsMatchIn(text)) raw += 3
        if (cachedRegex("""\bbillions?\s+of\s+(devices|users)\b""").containsMatchIn(text)) raw += 5
        if (cachedRegex("""\bevery\s+(version|release)\b""").containsMatchIn(text)) raw += 4
        if (cachedRegex("""\bany\s+(linux|windows|macos|android|ios)\s+(system|device|user)\b""")
                .containsMatchIn(text)) raw += 4
        return min(raw / impactCap, 1.0) * 25.0
    }

    private fun keywordRaw(text: String): Int {
        var raw = 0
        val tiers: List<Pair<List<String>, Int>> = listOf(
            Vocabulary.tier1 to 8,
            Vocabulary.tier2 to 4,
            Vocabulary.tier3 to 1,
        )
        for ((tier, pts) in tiers) {
            for (kw in tier) {
                if (Vocabulary.wordBoundaryRequired.contains(kw)) {
                    val pattern = """\b${Regex.escape(kw)}\b"""
                    if (cachedRegex(pattern).containsMatchIn(text)) raw += pts
                } else {
                    if (text.contains(kw)) raw += pts
                }
            }
        }
        return raw
    }

    private fun extractKeywordScoreNormalized(text: String): Double {
        val raw = keywordRaw(text)
        if (raw == 0) return 0.0
        val tokenCount = max(1, text.split(' ').size)
        // log2(x) = ln(x)/ln(2)
        val denom = max(1.0, ln((tokenCount + 1).toDouble()) / ln(2.0))
        val density = raw.toDouble() / denom
        return min(density / densityCap, 1.0) * 25.0
    }

    // MARK: - Helpers

    private fun round2(v: Double): Double = round(v * 100) / 100

    private val cache: MutableMap<String, Regex> = mutableMapOf()

    private fun cachedRegex(pattern: String): Regex {
        cache[pattern]?.let { return it }
        val r = Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        cache[pattern] = r
        return r
    }
}
