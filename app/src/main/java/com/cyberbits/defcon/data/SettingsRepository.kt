package com.cyberbits.defcon.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "defcon_settings")

enum class AppearanceMode(val value: Int, val label: String) {
    SYSTEM(0, "System"),
    LIGHT(1, "Light"),
    DARK(2, "Dark");

    companion object {
        fun from(value: Int): AppearanceMode =
            entries.firstOrNull { it.value == value } ?: SYSTEM
    }
}

class SettingsRepository(private val context: Context) {

    data class Settings(
        val appearance: AppearanceMode = AppearanceMode.SYSTEM,
        val refreshIntervalMinutes: Int = 60,
        val defconNotificationsEnabled: Boolean = true,
        val notificationThreshold: Int = 3,
        val newArticleNotificationsEnabled: Boolean = true,
        val lastRefreshedAt: Long? = null,
    )

    private object Keys {
        val appearance = intPreferencesKey("appearance")
        val refreshIntervalMinutes = intPreferencesKey("refresh_interval_minutes")
        val defconNotificationsEnabled = booleanPreferencesKey("defcon_notifications_enabled")
        val notificationThreshold = intPreferencesKey("notification_threshold")
        val newArticleNotificationsEnabled = booleanPreferencesKey("new_article_notifications_enabled")
        val lastRefreshedAt = longPreferencesKey("last_refreshed_at")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            appearance = AppearanceMode.from(prefs[Keys.appearance] ?: 0),
            refreshIntervalMinutes = prefs[Keys.refreshIntervalMinutes] ?: 60,
            defconNotificationsEnabled = prefs[Keys.defconNotificationsEnabled] ?: true,
            notificationThreshold = prefs[Keys.notificationThreshold] ?: 3,
            newArticleNotificationsEnabled = prefs[Keys.newArticleNotificationsEnabled] ?: true,
            lastRefreshedAt = prefs[Keys.lastRefreshedAt],
        )
    }

    suspend fun current(): Settings = settings.first()

    suspend fun setAppearance(value: AppearanceMode) =
        context.dataStore.edit { it[Keys.appearance] = value.value }

    suspend fun setRefreshIntervalMinutes(value: Int) =
        context.dataStore.edit { it[Keys.refreshIntervalMinutes] = value }

    suspend fun setDefconNotificationsEnabled(value: Boolean) =
        context.dataStore.edit { it[Keys.defconNotificationsEnabled] = value }

    suspend fun setNotificationThreshold(value: Int) =
        context.dataStore.edit { it[Keys.notificationThreshold] = value }

    suspend fun setNewArticleNotificationsEnabled(value: Boolean) =
        context.dataStore.edit { it[Keys.newArticleNotificationsEnabled] = value }

    suspend fun setLastRefreshedAt(value: Long) =
        context.dataStore.edit { it[Keys.lastRefreshedAt] = value }

    suspend fun needsRefresh(now: Long): Boolean {
        val s = current()
        val last = s.lastRefreshedAt ?: return true
        return now - last > s.refreshIntervalMinutes * 60_000L
    }
}
