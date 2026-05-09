package com.rltech.rollem.gamestate

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json

object SaveManager {
    private val json = Json {
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

    fun getInt(context: Context, key: String, value: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(key, 0)
    }


    fun <K, V> getMap(
        context: Context,
        key: String,
        keySerializer: KSerializer<K>,
        valueSerializer: KSerializer<V>
    ): Map<K, V> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(key, null) ?: return emptyMap()
        val serializer = MapSerializer(keySerializer, valueSerializer)
        return runCatching { json.decodeFromString(serializer, raw) }
            .getOrDefault(emptyMap())
    }

    fun <K, V> saveMap(
        context: Context,
        key: String,
        value: Map<K, V>,
        keySerializer: KSerializer<K>,
        valueSerializer: KSerializer<V>
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serializer = MapSerializer(keySerializer, valueSerializer)
        val encoded = json.encodeToString(serializer, value)
        prefs.edit { putString(key, encoded) }
    }
}