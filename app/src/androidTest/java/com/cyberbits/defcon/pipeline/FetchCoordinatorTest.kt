package com.cyberbits.defcon.pipeline

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cyberbits.defcon.data.ArticleRepository
import com.cyberbits.defcon.data.CveRepository
import com.cyberbits.defcon.data.DedupRepository
import com.cyberbits.defcon.data.DefconDatabase
import com.cyberbits.defcon.data.DefconRepository
import com.cyberbits.defcon.data.SettingsRepository
import com.cyberbits.defcon.data.VolumeRepository
import com.cyberbits.defcon.model.FeedSource
import com.cyberbits.defcon.notification.DefconNotifier
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FetchCoordinatorTest {
    private lateinit var db: DefconDatabase
    private lateinit var coordinator: FetchCoordinator

    private val rss = """
<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
  <channel>
    <title>x</title><link>https://x/</link><description>x</description>
    <item>
      <title>Critical RCE in widely-used library — CVE-2024-9001</title>
      <link>https://x/post-1</link>
      <guid>https://x/post-1</guid>
      <description>Unauthenticated remote code execution exploited in the wild.</description>
      <pubDate>Tue, 10 Sep 2024 12:00:00 +0000</pubDate>
    </item>
  </channel>
</rss>"""

    private val atom = """
<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <title>x</title>
  <entry>
    <title>Ransomware campaign breaches hospital network</title>
    <id>urn:1</id>
    <link href="https://x/atom-1"/>
    <published>2024-09-10T12:00:00Z</published>
    <summary>Ransomware threat actor exploited zero-day vulnerability.</summary>
  </entry>
</feed>"""

    private val wp = """[{"id":42,"link":"https://sa/42","date_gmt":"2024-09-10T12:00:00","title":{"rendered":"WP Sample"},"excerpt":{"rendered":"sample"},"categories":[1]}]"""

    private class FakeFetcher(
        private val bodies: Map<FeedSource, String>,
    ) : FeedFetcher() {
        override suspend fun fetch(source: FeedSource): FetchResult {
            val body = bodies[source]
            return FetchResult(source, body, null)
        }
    }

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, DefconDatabase::class.java)
            .allowMainThreadQueries().build()
        val bodies = mapOf(
            FeedSource.BLEEPING_COMPUTER to rss,
            FeedSource.HACKER_NEWS to atom,
            FeedSource.HACKREAD to rss,
            FeedSource.SECURITY_AFFAIRS to wp,
            FeedSource.THE_REGISTER to atom,
        )
        coordinator = FetchCoordinator(
            articles = ArticleRepository(db.articleDao()),
            defcon = DefconRepository(db.defconHistoryDao(), db.stickyLevelDao()),
            dedup = DedupRepository(db.dedupRecordDao()),
            volume = VolumeRepository(db.volumeRecordDao()),
            cves = CveRepository(db.cveRecordDao()),
            settings = SettingsRepository(context),
            notifier = DefconNotifier(context),
            fetcher = FakeFetcher(bodies),
        )
    }

    @After fun tearDown() { db.close() }

    @Test fun runFetchCycleStoresAtLeastOneArticleAndHistory() = runTest {
        val status = coordinator.runFetchCycle()
        assertNotNull(status)
        val recent = db.articleDao().mostRecent(10)
        assertTrue("expected articles after fetch", recent.isNotEmpty())
        assertNotNull(db.defconHistoryDao().latest())
    }
}
