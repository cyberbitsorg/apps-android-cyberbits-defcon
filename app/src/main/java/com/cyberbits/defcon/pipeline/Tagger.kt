package com.cyberbits.defcon.pipeline

/**
 * Content-based article tagging.
 *
 * Matches each tag's keyword regex against title and summary, weighs title matches
 * double, and returns the top [maxTags] tag keys ranked by distinct keyword matches.
 * Mirrors the compiled-regex + word-boundary pattern of [Scorer]/[Triggers].
 */
object Tagger {

    const val maxTags: Int = 3
    private const val titleWeight = 2
    private const val summaryWeight = 1

    private val tagRegexes: List<Pair<String, Regex>> =
        Vocabulary.tagKeywords.map { (tag, kws) ->
            tag to cachedRegex(
                kws.joinToString("|") { kw ->
                    if (kw in Vocabulary.tagWordBoundaryRequired) """\b${Regex.escape(kw)}\b"""
                    else Regex.escape(kw)
                }
            )
        }

    /**
     * Return up to [maxTags] category tags, strongest match first.
     *
     * Ties resolve to the earlier tag in the [Vocabulary.tagKeywords] taxonomy order.
     * Articles matching nothing get the fallback tag so every article has at least one tag.
     */
    fun classifyTags(title: String, summary: String): List<String> {
        val titleL = title.lowercase()
        val summaryL = summary.lowercase()

        data class Ranked(val score: Int, val order: Int, val tag: String)

        val ranked = ArrayList<Ranked>()
        for ((order, entry) in tagRegexes.withIndex()) {
            val (tag, regex) = entry
            val score = titleWeight * distinctMatches(regex, titleL) +
                summaryWeight * distinctMatches(regex, summaryL)
            if (score > 0) ranked.add(Ranked(score, order, tag))
        }
        if (ranked.isEmpty()) return listOf(Vocabulary.fallbackTag)
        return ranked
            .sortedWith(compareBy({ -it.score }, { it.order }))
            .take(maxTags)
            .map { it.tag }
    }

    /** Count distinct keywords matched (a keyword repeated twice counts once). */
    private fun distinctMatches(regex: Regex, text: String): Int =
        regex.findAll(text).mapTo(HashSet<String>()) { it.value.lowercase() }.size
}
