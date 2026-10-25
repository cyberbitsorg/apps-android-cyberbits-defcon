package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.model.RawArticle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.TimeZone
import javax.xml.parsers.SAXParserFactory

object FeedParser {

    private val json = Json { ignoreUnknownKeys = true }

    private val rssFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = true
        }
    }

    fun parseXml(source: FeedSource, body: String): List<RawArticle> {
        val handler = FeedHandler(source)
        val factory = SAXParserFactory.newInstance().apply {
            isNamespaceAware = false
        }
        val parser = factory.newSAXParser()
        parser.parse(ByteArrayInputStream(body.toByteArray(Charsets.UTF_8)), handler)
        return handler.articles
    }

    fun parseWordPressJson(source: FeedSource, body: String): List<RawArticle> {
        val posts = json.decodeFromString<List<WpPost>>(body)
        return posts.mapNotNull { post ->
            val date = parseIso8601(post.dateGmt + "Z") ?: return@mapNotNull null
            RawArticle(
                guid = post.id.toString(),
                title = post.title.rendered.htmlStripped(),
                summary = post.excerpt.rendered.htmlStripped(),
                url = post.link,
                source = source,
                publishedAt = date,
                categories = post.categories.map { it.toString() },
            )
        }
    }

    internal fun parseRfc822(s: String): Long? = try {
        if (s.isEmpty()) null else rssFormat.parse(s)?.time
    } catch (_: Throwable) {
        null
    }

    internal fun parseIso8601(s: String): Long? {
        if (s.isEmpty()) return null
        return try {
            OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    @Serializable
    private data class WpPost(
        val id: Int,
        val link: String,
        @SerialName("date_gmt") val dateGmt: String,
        val title: WpRendered,
        val excerpt: WpRendered,
        val categories: List<Int> = emptyList(),
    )

    @Serializable
    private data class WpRendered(val rendered: String)

    private class FeedHandler(private val source: FeedSource) : DefaultHandler() {
        val articles = mutableListOf<RawArticle>()

        // Feeds must never reference external entities: resolving to an empty source
        // keeps a crafted feed from reading local files or probing the network (XXE).
        override fun resolveEntity(publicId: String?, systemId: String?): InputSource =
            InputSource(StringReader(""))

        private var current = ""
        private var isAtom = false
        private var inItem = false

        private var title = StringBuilder()
        private var link = StringBuilder()
        private var guid = StringBuilder()
        private var summary = StringBuilder()
        private var pubDate = StringBuilder()
        private val categories = mutableListOf<String>()
        private var pendingAtomLink = ""

        override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
            current = qName.lowercase(Locale.US)
            if (current == "feed") isAtom = true
            if (current == "item" || current == "entry") {
                inItem = true
                title = StringBuilder(); link = StringBuilder(); guid = StringBuilder()
                summary = StringBuilder(); pubDate = StringBuilder()
                categories.clear()
                pendingAtomLink = ""
            }
            if (inItem && isAtom && current == "link") {
                val href = attributes.getValue("href")
                if (!href.isNullOrEmpty()) pendingAtomLink = href
            }
            if (inItem && current == "category") {
                val term = attributes.getValue("term")
                if (!term.isNullOrEmpty()) categories.add(term)
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            if (!inItem) return
            val s = String(ch, start, length).trim()
            if (s.isEmpty()) return
            when (current) {
                "title" -> title.append(s)
                "link" -> if (!isAtom) link.append(s)
                "guid", "id" -> guid.append(s)
                "description", "summary", "content:encoded", "content" -> summary.append(s)
                "pubdate", "published", "updated" -> if (pubDate.isEmpty()) pubDate.append(s)
                "category" -> categories.add(s)
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String) {
            val lower = qName.lowercase(Locale.US)
            if (lower == "item" || lower == "entry") {
                if (inItem) {
                    var linkStr = link.toString()
                    if (isAtom && linkStr.isEmpty()) linkStr = pendingAtomLink
                    var guidStr = guid.toString()
                    if (guidStr.isEmpty()) guidStr = linkStr
                    val titleStr = title.toString().trim()
                    val pubDateStr = pubDate.toString().trim()

                    if (titleStr.isNotEmpty() && linkStr.isNotEmpty() && guidStr.isNotEmpty()) {
                        val publishedAt = if (isAtom) parseIso8601(pubDateStr) else parseRfc822(pubDateStr)
                        if (publishedAt != null) {
                            articles.add(
                                RawArticle(
                                    guid = guidStr,
                                    title = titleStr.htmlStripped(),
                                    summary = summary.toString().trim().htmlStripped(),
                                    url = linkStr,
                                    source = source,
                                    publishedAt = publishedAt,
                                    categories = categories.toList(),
                                )
                            )
                        }
                    }
                    inItem = false
                }
            }
            current = ""
        }
    }
}

private val HTML_TAG = Regex("<[^>]+>")

private fun String.htmlStripped(): String =
    replace(HTML_TAG, "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&nbsp;", " ")
        .trim()
