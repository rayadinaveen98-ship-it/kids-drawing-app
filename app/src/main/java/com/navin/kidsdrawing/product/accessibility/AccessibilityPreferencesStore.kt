package com.navin.kidsdrawing.product.accessibility

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.accessibilityPreferencesDataStore by preferencesDataStore(
    name = "accessibility_preferences",
)

data class AccessibilityPreferences(
    val reduceMotion: Boolean = false,
)

/**
 * Device-local accessibility preferences owned by the product shell.
 *
 * These settings deliberately live outside ChildProfile: they describe how this device should
 * present the app, not an assessment or attribute of the child. The store is offline-only and has
 * a corruption-safe default that preserves full product access.
 */
class AccessibilityPreferencesStore(
    private val context: Context,
) {
    val preferences: Flow<AccessibilityPreferences> = context.accessibilityPreferencesDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            AccessibilityPreferences(
                reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            )
        }

    suspend fun load(): AccessibilityPreferences = preferences.first()

    suspend fun setReduceMotion(enabled: Boolean) {
        context.accessibilityPreferencesDataStore.edit { prefs ->
            prefs[Keys.REDUCE_MOTION] = enabled
        }
    }

    private object Keys {
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
    }
}
