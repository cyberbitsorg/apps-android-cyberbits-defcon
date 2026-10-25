package com.cyberbits.defcon.pipeline

import com.cyberbits.defcon.model.FeedSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

open class FeedFetcher(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
) {
    data class FetchResult(val source: FeedSource, val body: String?, val error: Throwable?)

    open suspend fun fetch(source: FeedSource): FetchResult = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url(source.feedUrl)
                .header("User-Agent", "CyberbitsDefcon/1.0 (Android)")
                .header("Accept", "application/rss+xml, application/atom+xml, application/json;q=0.9, */*;q=0.1")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    FetchResult(source, null, RuntimeException("HTTP ${resp.code}"))
                } else {
                    FetchResult(source, resp.body.string(), null)
                }
            }
        } catch (t: Throwable) {
            FetchResult(source, null, t)
        }
    }
}
