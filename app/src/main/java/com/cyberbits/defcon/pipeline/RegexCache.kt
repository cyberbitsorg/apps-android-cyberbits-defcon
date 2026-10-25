package com.cyberbits.defcon.pipeline

private val cache: MutableMap<String, Regex> = mutableMapOf()

/** Shared case-insensitive regex cache for the scoring/trigger patterns. */
internal fun cachedRegex(pattern: String): Regex {
    cache[pattern]?.let { return it }
    val r = Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    cache[pattern] = r
    return r
}
