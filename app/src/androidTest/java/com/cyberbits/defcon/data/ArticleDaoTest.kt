package com.cyberbits.defcon.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cyberbits.defcon.data.entity.ArticleEntity
import com.cyberbits.defcon.model.FeedSource
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticleDaoTest {
    private lateinit var db: DefconDatabase

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DefconDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun tearDown() { db.close() }

    private fun art(id: String, source: FeedSource = FeedSource.HACKREAD, ts: Long) =
        ArticleEntity(
            id = id, guid = "guid-$id", title = "Title $id", summary = "Summary",
            url = "https://x/$id", source = source, publishedAt = ts,
            defconScore = 10.0, categories = emptyList(), isRead = false, readAt = null,
            fingerprint = "fp-$id", triggerRaw = null,
        )

    @Test fun insertAndUpsertByGuid() = runTest {
        val dao = db.articleDao()
        val a = art("a", ts = 1000)
        val aDup = a.copy(id = "b") // same guid → IGNORE
        dao.insertAll(listOf(a))
        dao.insertAll(listOf(aDup))
        assertEquals(1, dao.mostRecent(10).size)
    }

    @Test fun trimByIdsDeletes() = runTest {
        val dao = db.articleDao()
        dao.insertAll((1..5).map { art("a$it", ts = it * 1000L) })
        dao.deleteByIds(listOf("a1", "a2"))
        assertEquals(3, dao.mostRecent(10).size)
    }
}
