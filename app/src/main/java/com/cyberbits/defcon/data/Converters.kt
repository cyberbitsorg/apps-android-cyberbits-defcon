package com.cyberbits.defcon.data

import androidx.room.TypeConverter
import com.cyberbits.defcon.model.FeedSource
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }
    private val stringListSerializer = ListSerializer(String.serializer())

    @TypeConverter fun stringListToJson(value: List<String>): String =
        json.encodeToString(stringListSerializer, value)

    @TypeConverter fun jsonToStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else json.decodeFromString(stringListSerializer, value)

    @TypeConverter fun feedSourceToString(value: FeedSource): String = value.name
    @TypeConverter fun stringToFeedSource(value: String): FeedSource = FeedSource.valueOf(value)
}
