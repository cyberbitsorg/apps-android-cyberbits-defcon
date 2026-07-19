package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.model.RawArticle
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Multi-layer deduplication ported byte-for-byte from iOS `Deduplicator.swift`:
 *   L1  — SHA-256 fingerprint of normalised title tokens (exact / near-exact).
 *   L1b — CVE-ID match within 36h TTL (caller-supplied `recentCves`).
 *   L0a — shared quoted phrase (≥ 4 chars) in both titles.
 *   L0b — shared threat-actor token + any other shared meaningful token.
 *   L0c — shared vendor+product pair + any other shared meaningful token.
 *   L2-J — Jaccard over token sets (threshold 0.28).
 *   L2-T — TF-IDF cosine similarity vs comparable titles (threshold 0.45).
 *
 * Temporal conflict (different month OR different year mentioned) bypasses
 * L0a/L0b/L0c, L2-J, and excludes the title from the L2-T corpus.
 *
 * Note: iOS exposes an optional `recentTokens` cache for L2 hot paths; we
 * recompute on the Android side. For v1 corpus size (≤ 300) this is fine.
 */
object Deduplicator {

    const val JACCARD_THRESHOLD: Double = 0.28
    const val TFIDF_THRESHOLD: Double = 0.45
    const val MIN_TOKEN_LENGTH: Int = 3
    const val DEDUP_WINDOW: Int = 300

    private val STOP_WORDS: Set<String> = setOf(
        "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with",
        "by", "from", "is", "are", "was", "were", "be", "been", "has", "have", "had", "it",
        "its", "as", "this", "that", "these", "those", "how", "what", "when", "where",
        "who", "why", "will", "can", "could", "would", "should", "may", "might", "do",
        "does", "did", "not", "no", "up", "out", "so", "than", "into", "over", "after",
        "new", "using", "via", "says", "say", "said", "report", "reports", "found",
        "finds", "warns", "warn", "reveals", "reveal", "shows", "show",
    )

    private val MONTHS: Set<String> = setOf(
        "january", "february", "march", "april", "may", "june", "july", "august",
        "september", "october", "november", "december",
        "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "oct", "nov", "dec",
    )

    /** Known vendor→product pairs. Empty product means vendor-only match. */
    private val VENDOR_PRODUCT_PAIRS: List<Pair<String, String>> = listOf(
        "cisco" to "sd-wan", "cisco" to "ios", "cisco" to "catalyst", "cisco" to "asa",
        "microsoft" to "exchange", "microsoft" to "sharepoint",
        "microsoft" to "azure", "microsoft" to "outlook", "microsoft" to "teams",
        "microsoft" to "bitlocker", "microsoft" to "defender",
        "apple" to "macos", "apple" to "safari",
        "google" to "chrome",
        "fortinet" to "fortigate", "fortinet" to "fortios",
        "fortinet" to "fortimanager", "fortinet" to "fortisandbox",
        "ivanti" to "epmm", "ivanti" to "connect secure", "ivanti" to "endpoint manager",
        "palo alto" to "pan-os", "vmware" to "esxi", "vmware" to "vcenter",
        "vmware" to "fusion",
        "citrix" to "netscaler", "citrix" to "adc",
        "sonicwall" to "sma", "sonicwall" to "firewall",
        "oracle" to "weblogic", "oracle" to "java", "oracle" to "peoplesoft", "oracle" to "e-business",
        "openssh" to "", "openssl" to "", "nginx" to "", "apache" to "struts",
        "wordpress" to "", "drupal" to "",
    )

    // CVE-2024-1234, CVE-2024-12345, CVE 2024 12345 (incl. en/em dashes & spaces).
    private val CVE_REGEX = Regex("""\bCVE[-–— ]?(\d{4})[-–— ]?(\d{3,7})\b""", RegexOption.IGNORE_CASE)

    // Quoted phrases — straight + curly single/double quotes. Content ≥ 4 chars.
    // Quote chars: ' " ' ' " "
    private val QUOTED_PHRASE_REGEX = Regex("""['"‘’“”]([^'"‘’“”]{4,})['"‘’“”]""")

    private val YEAR_REGEX = Regex("""\b(20\d{2})\b""")

    // MARK: - Public API

    /** L1 fingerprint: SHA-256 hex of sorted token set joined by space. */
    fun fingerprint(title: String): String {
        val tokens = tokenSet(title).sorted().joinToString(" ")
        val digest = MessageDigest.getInstance("SHA-256").digest(tokens.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** Extract canonical CVE IDs (e.g. "CVE-2024-1234") from text. */
    fun extractCVEs(text: String): Set<String> {
        val out = HashSet<String>()
        for (m in CVE_REGEX.findAll(text)) {
            val year = m.groupValues[1]
            val num = m.groupValues[2]
            out += "CVE-$year-$num"
        }
        return out
    }

    /** Token set — mirrors iOS `tokenSet`. */
    fun tokenSet(text: String): Set<String> {
        val lowered = text.lowercase(Locale.ENGLISH)
        val sb = StringBuilder(lowered.length)
        for (ch in lowered) {
            if (Character.isLetterOrDigit(ch) || ch == '_' || ch == ' ' || ch == '\t' || ch == '\n') {
                sb.append(ch)
            } else {
                sb.append(' ')
            }
        }
        val out = HashSet<String>()
        for (tok in sb.toString().split(' ', '\t', '\n')) {
            if (tok.isEmpty()) continue
            if (tok.length >= MIN_TOKEN_LENGTH && tok !in STOP_WORDS) {
                out += tok
            }
        }
        return out
    }

    /** Returns true if `a` and `b` mention different months OR different years. */
    fun hasTemporalConflict(a: String, b: String): Boolean {
        val la = a.lowercase(Locale.ENGLISH).split(' ')
        val lb = b.lowercase(Locale.ENGLISH).split(' ')
        val monthsA = la.filter { it in MONTHS }.toSet()
        val monthsB = lb.filter { it in MONTHS }.toSet()
        if (monthsA.isNotEmpty() && monthsB.isNotEmpty() && monthsA != monthsB) return true

        val yearsA = YEAR_REGEX.findAll(a).map { it.groupValues[1] }.toSet()
        val yearsB = YEAR_REGEX.findAll(b).map { it.groupValues[1] }.toSet()
        if (yearsA.isNotEmpty() && yearsB.isNotEmpty() && yearsA != yearsB) return true

        return false
    }

    /**
     * Deduplicate `incoming` against the given state.
     * Survivors are returned in input order. A surviving article does NOT
     * get added to the recent set within a single call (iOS semantics).
     */
    fun deduplicate(
        incoming: List<RawArticle>,
        existingFingerprints: Set<String>,
        recentTitles: List<String>,
        recentCves: Set<String> = emptySet(),
    ): List<RawArticle> {
        if (incoming.isEmpty()) return emptyList()
        val out = ArrayList<RawArticle>(incoming.size)
        for (article in incoming) {
            if (!isDuplicate(article, existingFingerprints, recentTitles, recentCves)) {
                out += article
            }
        }
        return out
    }

    // MARK: - Internals

    private fun isDuplicate(
        article: RawArticle,
        existingFingerprints: Set<String>,
        recentTitles: List<String>,
        recentCves: Set<String>,
    ): Boolean {
        val fp = fingerprint(article.title)
        if (fp in existingFingerprints) return true

        // L1b — CVE match
        if (recentCves.isNotEmpty()) {
            val articleText = "${article.title} ${article.summary}"
            for (cve in extractCVEs(articleText)) {
                if (cve in recentCves) return true
            }
        }

        val newTokens = tokenSet(article.title)

        for (existing in recentTitles) {
            if (hasTemporalConflict(article.title, existing)) continue

            // L0a — shared quoted phrase
            if (sharedNamedPhrase(article.title, existing)) return true

            // L0b — shared threat actor + any other token
            if (sharedActorAndToken(article.title, existing)) return true

            // L0c — shared vendor+product pair + any other token (excluding pair tokens)
            val pair = sharedVendorProduct(article.title, existing)
            if (pair != null) {
                val existingTokens = tokenSet(existing)
                val pairTokens = pair.split(' ').filter { it.isNotEmpty() }.toSet()
                val sharedExtra = (newTokens - pairTokens) intersect (existingTokens - pairTokens)
                if (sharedExtra.isNotEmpty()) return true
            }

            // L2-J — Jaccard
            val existingTokens = tokenSet(existing)
            if (jaccard(newTokens, existingTokens) >= JACCARD_THRESHOLD) return true
        }

        // L2-T — TF-IDF cosine over comparable (non-temporal-conflicting) titles
        val comparable = recentTitles.filter { !hasTemporalConflict(article.title, it) }
        if (comparable.isNotEmpty()) {
            val sims = tfidfCosineSimilarities(article.title, comparable)
            if ((sims.maxOrNull() ?: 0.0) >= TFIDF_THRESHOLD) return true
        }

        return false
    }

    private fun jaccard(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val inter = a.intersect(b).size
        val union = a.size + b.size - inter
        return inter.toDouble() / union.toDouble()
    }

    private fun sharedNamedPhrase(a: String, b: String): Boolean {
        val la = a.lowercase(Locale.ENGLISH)
        val lb = b.lowercase(Locale.ENGLISH)
        for (s in listOf(a, b)) {
            for (m in QUOTED_PHRASE_REGEX.findAll(s)) {
                val phrase = m.groupValues[1].lowercase(Locale.ENGLISH).trim()
                if (phrase.length >= 4 && la.contains(phrase) && lb.contains(phrase)) {
                    return true
                }
            }
        }
        return false
    }

    private fun sharedActorAndToken(a: String, b: String): Boolean {
        val la = a.lowercase(Locale.ENGLISH)
        val lb = b.lowercase(Locale.ENGLISH)
        for (actor in Vocabulary.threatActors) {
            if (!la.contains(actor) || !lb.contains(actor)) continue
            val actorTokens = actor.split(' ').filter { it.isNotEmpty() }.toSet()
            val extra = (tokenSet(a) - actorTokens) intersect (tokenSet(b) - actorTokens)
            if (extra.isNotEmpty()) return true
        }
        return false
    }

    private fun sharedVendorProduct(a: String, b: String): String? {
        val la = a.lowercase(Locale.ENGLISH)
        val lb = b.lowercase(Locale.ENGLISH)
        for ((vendor, product) in VENDOR_PRODUCT_PAIRS) {
            if (!la.contains(vendor) || !lb.contains(vendor)) continue
            if (product.isNotEmpty()) {
                if (!la.contains(product) || !lb.contains(product)) continue
            }
            return if (product.isEmpty()) vendor else "$vendor $product"
        }
        return null
    }

    /**
     * Compute cosine similarities between `query` and each `corpus` title.
     * Builds a fresh TF-IDF vocabulary over (corpus + query). Mirrors iOS:
     *   idf(t) = ln((N + 1) / (df + 1)) + 1
     *   tf(t)  = 1 if term present else 0 (binary presence)
     */
    internal fun tfidfCosineSimilarities(query: String, corpus: List<String>): List<Double> {
        val documents = corpus + query
        val tokenSets = documents.map { tokenSet(it) }
        val vocabulary = HashSet<String>()
        for (s in tokenSets) vocabulary.addAll(s)
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
