package com.navin.kidsdrawing.product.freedraw

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.navin.kidsdrawing.drawing.domain.DrawingBrushPreset
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import kotlinx.coroutines.flow.first

private val Context.freeDrawToolDataStore by preferencesDataStore(name = "free_draw_tool_state")

internal interface FreeDrawToolSettingsPersistence {
    suspend fun load(): DrawingToolSettings
    suspend fun save(settings: DrawingToolSettings)
}

internal class AndroidFreeDrawToolSettingsStore(
    context: Context,
) : FreeDrawToolSettingsPersistence {
    private val appContext = context.applicationContext

    override suspend fun load(): DrawingToolSettings {
        val prefs = appContext.freeDrawToolDataStore.data.first()
        val preset = prefs[Keys.BRUSH_PRESET]
            ?.let { raw -> runCatching { DrawingBrushPreset.valueOf(raw) }.getOrNull() }
            ?: DrawingBrushPreset.PENCIL
        val tool = prefs[Keys.TOOL]
            ?.let { raw -> runCatching { DrawingTool.valueOf(raw) }.getOrNull() }
            ?: DrawingTool.PENCIL
        val color = prefs[Keys.COLOR_ARGB] ?: DrawingToolSettings.DEFAULT_PENCIL_COLOR_ARGB
        val fallbackWidth = if (tool == DrawingTool.ERASER) {
            DrawingToolSettings.DEFAULT_ERASER_WIDTH
        } else {
            preset.defaultWidth
        }
        val width = (prefs[Keys.WIDTH] ?: fallbackWidth)
            .coerceIn(DrawingToolSettings.MIN_TOOL_WIDTH, DrawingToolSettings.MAX_TOOL_WIDTH)
        return DrawingToolSettings(
            tool = tool,
            brushPreset = preset,
            colorArgb = color,
            width = width,
        )
    }

    override suspend fun save(settings: DrawingToolSettings) {
        appContext.freeDrawToolDataStore.edit { prefs ->
            prefs[Keys.TOOL] = settings.tool.name
            prefs[Keys.BRUSH_PRESET] = settings.brushPreset.name
            prefs[Keys.COLOR_ARGB] = settings.colorArgb
            prefs[Keys.WIDTH] = settings.width
        }
    }

    private object Keys {
        val TOOL = stringPreferencesKey("tool")
        val BRUSH_PRESET = stringPreferencesKey("brush_preset")
        val COLOR_ARGB = intPreferencesKey("color_argb")
        val WIDTH = floatPreferencesKey("width")
    }
}
