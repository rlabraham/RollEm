package com.rltech.rollem.game.save

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

object SaveManager {
    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        // Needed if you ever use non-primitive/complex keys in maps.
        allowStructuredMapKeys = true
    }


    fun saveLong(context: Context, key: String, value: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(key, value) }
    }

    fun getLong(context: Context, key: String): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(key, 0L)
    }

    fun saveInt(context: Context, key: String, value: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(key, value) }
    }

    fun getInt(context: Context, key: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(key, 0)
    }

    inline fun <reified T> saveList(
        context: Context,
        key: String,
        value: List<T>
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serializer = ListSerializer(serializer<T>())
        val encoded = json.encodeToString(serializer, value)
        prefs.edit { putString(key, encoded) }
    }

    inline fun <reified T> getList(
        context: Context,
        key: String
    ): List<T> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(key, null) ?: return emptyList()
        val serializer = ListSerializer(serializer<T>())
        return runCatching { json.decodeFromString(serializer, raw) }
            .getOrDefault(emptyList())
    }
}