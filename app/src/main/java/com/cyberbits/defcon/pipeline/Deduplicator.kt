package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.model.RawArticle
import java.security.MessageDigest
import java.text.BreakIterator
import java.util.Locale
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Two-layer deduplication ported from iOS `Deduplicator.swift`:
 *   L1  — SHA-256 fingerprint of normalized title tokens (exact / near-exact).
 *   L2a — Jaccard over token sets (threshold 0.35).
 *   L2b — TF-IDF cosine similarity vs recent titles (threshold 0.55).
 *
 * Temporal conflict (different month OR different year mentioned) bypasses
 * both L2a and L2b for that pair.
 */
object Deduplicator {

    const val L2A_THRESHOLD: Double = 0.35
    const val L2B_THRESHOLD: Double = 0.55
    const val DEDUP_WINDOW: Int = 300

    private const val MIN_TOKEN_LENGTH: Int = 3

    private val MONTHS: Set<String> = setOf(
        "january", "february", "march", "april", "may", "june", "july",
        "august", "september", "october", "november", "december",
    )

    // 4-digit years 19xx or 20xx, word-bounded.
    private val YEAR_REGEX = Regex("""\b((?:19|20)\d{2})\b""")

    // MARK: - Public API

    /** L1 fingerprint: SHA-256 hex of sorted normalized title tokens joined with a space. */
    fun fingerprint(title: String): String {
        val tokens = tokenSet(title).sorted().joinToString(" ")
        val digest = MessageDigest.getInstance("SHA-256").digest(tokens.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** Returns the subset of `incoming` that survives L1 + L2a + L2b dedup. */
    fun deduplicate(
        incoming: List<RawArticle>,
        existingFingerprints: Set<String>,
        recentTitles: List<String>,
    ): List<RawArticle> {
        if (incoming.isEmpty()) return emptyList()

        val seenFingerprints = existingFingerprints.toMutableSet()
        val survivingTitles = recentTitles.toMutableList()
        val out = mutableListOf<RawArticle>()

        for (article in incoming) {
            val title = article.title
            val fp = fingerprint(title)
            if (fp in seenFingerprints) continue

            val newTokens = tokenSet(title)

            // Determine which recent titles are comparable (no temporal conflict).
            val comparable = survivingTitles.filter { !hasTemporalConflict(title, it) }

            // L2a — Jaccard
            var dup = false
            for (existing in comparable) {
                val existingTokens = tokenSet(existing)
                if (jaccard(newTokens, existingTokens) >= L2A_THRESHOLD) {
                    dup = true
                    break
                }
            }
            if (dup) continue

            // L2b — TF-IDF cosine
            if (comparable.isNotEmpty()) {
                val sims = tfidfCosineSimilarities(query = title, corpus = comparable)
                if ((sims.maxOrNull() ?: 0.0) >= L2B_THRESHOLD) continue
            }

            out += article
            seenFingerprints += fp
            survivingTitles.add(0, title)
        }

        return out
    }

    // MARK: - Tokenization

    /**
     * Tokenize via [BreakIterator] word instance (ENGLISH), lowercase each token,
     * keep tokens with length >= 3 that contain at least one letter or digit.
     */
    internal fun tokenSet(text: String): Set<String> {
        val lowered = text.lowercase(Locale.ENGLISH)
        val it = BreakIterator.getWordInstance(Locale.ENGLISH)
        it.setText(lowered)
        val out = HashSet<String>()
        var start = it.first()
        var end = it.next()
        while (end != BreakIterator.DONE) {
            val tok = lowered.substring(start, end)
            if (tok.length >= MIN_TOKEN_LENGTH && tok.any { c -> c.isLetterOrDigit() }) {
                out += tok
            }
            start = end
            end = it.next()
        }
        return out
    }

    private fun jaccard(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val inter = a.intersect(b).size
        val union = a.size + b.size - inter
        return inter.toDouble() / union.toDouble()
    }

    // MARK: - Temporal conflict

    internal fun hasTemporalConflict(a: String, b: String): Boolean {
        val la = a.lowercase(Locale.ENGLISH)
        val lb = b.lowercase(Locale.ENGLISH)

        val monthsA = MONTHS.filter { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(la) }.toSet()
        val monthsB = MONTHS.filter { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(lb) }.toSet()
        if (monthsA.isNotEmpty() && monthsB.isNotEmpty() && monthsA != monthsB) return true

        val yearsA = YEAR_REGEX.findAll(a).map { it.groupValues[1] }.toSet()
        val yearsB = YEAR_REGEX.findAll(b).map { it.groupValues[1] }.toSet()
        if (yearsA.isNotEmpty() && yearsB.isNotEmpty() && yearsA != yearsB) return true

        return false
    }

    // MARK: - TF-IDF cosine

    /**
     * Compute cosine similarities between `query` and each `corpus` title.
     * Builds a fresh TF-IDF vocabulary over (corpus + query). Mirrors iOS:
     *   idf(t) = ln((N + 1) / (df + 1)) + 1
     *   tf(t)  = 1 if term present else 0 (binary presence, matching iOS)
     */
    internal fun tfidfCosineSimilarities(query: String, corpus: List<String>): List<Double> {
        val documents = corpus + query
        val tokenSets = documents.map { tokenSet(it) }
        val vocabulary = tokenSets.fold(HashSet<String>()) { acc, s -> acc.apply { addAll(s) } }
        val n = documents.size.toDouble()

        val idf = HashMap<String, Double>(vocabulary.size)
        for (term in vocabulary) {
            val df = tokenSets.count { it.contains(term) }.toDouble()
            idf[term] = ln((n + 1.0) / (df + 1.0)) + 1.0
        }
        val vocabArray = vocabulary.sorted()

        fun vector(tokens: Set<String>): DoubleArray {
            val v = DoubleArray(vocabArray.size)
            for ((i, term) in vocabArray.withIndex()) {
                if (tokens.contains(term)) v[i] = idf[term] ?: 0.0
            }
            return v
        }
        fun norm(v: DoubleArray): Double {
            var s = 0.0
            for (x in v) s += x * x
            return sqrt(s)
        }

        val queryVec = vector(tokenSets.last())
        val queryNorm = norm(queryVec)
        if (queryNorm == 0.0) return List(corpus.size) { 0.0 }

        return tokenSets.dropLast(1).map { tokens ->
            val docVec = vector(tokens)
            val docNorm = norm(docVec)
            if (docNorm == 0.0) return@map 0.0
            var dot = 0.0
            for (i in queryVec.indices) dot += queryVec[i] * docVec[i]
            dot / (queryNorm * docNorm)
        }
    }
}
