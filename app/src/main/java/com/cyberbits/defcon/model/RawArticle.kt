package com.cyberbits.defcon.model

data class RawArticle(
    val guid: String,
    val title: String,
    val summary: String,
    val url: String,
    val source: FeedSource,
    val publishedAt: Long,        // epoch millis
    val categories: List<String>,
)
