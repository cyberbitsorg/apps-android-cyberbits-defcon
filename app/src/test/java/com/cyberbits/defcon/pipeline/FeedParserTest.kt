package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.model.FeedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedParserTest {
    private fun load(name: String): String =
        javaClass.classLoader!!.getResource("fixtures/$name")!!.readText()

    @Test fun parsesRss() {
        val items = FeedParser.parseXml(FeedSource.HACKREAD, load("sample-rss.xml"))
        assertEquals(2, items.size)
        assertEquals("Sample RSS Title 1", items[0].title)
        assertEquals("https://example.com/post-1", items[0].url)
        assertTrue(items[0].summary.contains("&"))
        assertTrue(items[0].categories.contains("security"))
    }

    @Test fun parsesAtom() {
        val items = FeedParser.parseXml(FeedSource.THE_REGISTER, load("sample-atom.xml"))
        assertEquals(2, items.size)
        assertEquals("Sample Atom Title 1", items[0].title)
        assertEquals("https://example.com/atom-1", items[0].url)
        assertTrue(items[0].categories.contains("security"))
    }

    @Test fun parsesWordPressJson() {
        val items = FeedParser.parseWordPressJson(FeedSource.SECURITY_AFFAIRS, load("sample-wp.json"))
        assertEquals(2, items.size)
        assertEquals("WP Title 1 & more", items[0].title)
        assertEquals("101", items[0].guid)
        assertTrue(items[0].summary.contains("WP excerpt one"))
    }
}
