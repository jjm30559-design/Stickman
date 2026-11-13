package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.StickmanBodyStyle
import com.example.model.StickmanGlowTheme
import com.example.model.StrokeWeightOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studioDataStore: DataStore<Preferences> by preferencesDataStore(name = "stickman_studio_prefs")

data class StudioPreferences(
    val bodyStyle: StickmanBodyStyle = StickmanBodyStyle.YOUTUBE_SOLID,
    val glowTheme: StickmanGlowTheme = StickmanGlowTheme.CHOSEN_ORANGE,
    val strokeWeight: StrokeWeightOption = StrokeWeightOption.BOLD,
    val fps: Float = 8f,
    val onionSkinEnabled: Boolean = true,
    val showJointHandles: Boolean = true,
    val isLooping: Boolean = true,
    val showFace: Boolean = true,
)

class StudioPreferencesRepository(private val context: Context) {
    private object Keys {
        val BODY_STYLE = stringPreferencesKey("body_style")
        val GLOW_THEME = stringPreferencesKey("glow_theme")
        val STROKE_WEIGHT = stringPreferencesKey("stroke_weight")
        val FPS = floatPreferencesKey("fps")
        val ONION_SKIN = booleanPreferencesKey("onion_skin")
        val SHOW_JOINTS = booleanPreferencesKey("show_joints")
        val IS_LOOPING = booleanPreferencesKey("is_looping")
        val SHOW_FACE = booleanPreferencesKey("show_face")
    }

    val preferencesFlow: Flow<StudioPreferences> = context.studioDataStore.data.map { prefs ->
        StudioPreferences(
            bodyStyle = prefs[Keys.BODY_STYLE]
                ?.let { name -> StickmanBodyStyle.entries.firstOrNull { it.name == name } }
                ?: StickmanBodyStyle.YOUTUBE_SOLID,
            glowTheme = prefs[Keys.GLOW_THEME]
                ?.let { name -> StickmanGlowTheme.entries.firstOrNull { it.name == name } }
                ?: StickmanGlowTheme.CHOSEN_ORANGE,
            strokeWeight = prefs[Keys.STROKE_WEIGHT]
                ?.let { name -> StrokeWeightOption.entries.firstOrNull { it.name == name } }
                ?: StrokeWeightOption.BOLD,
            fps = (prefs[Keys.FPS] ?: 8f).coerceIn(2f, 24f),
            onionSkinEnabled = prefs[Keys.ONION_SKIN] ?: true,
            showJointHandles = prefs[Keys.SHOW_JOINTS] ?: true,
            isLooping = prefs[Keys.IS_LOOPING] ?: true,
            showFace = prefs[Keys.SHOW_FACE] ?: true,
        )
    }

    suspend fun setBodyStyle(style: StickmanBodyStyle) {
        context.studioDataStore.edit { it[Keys.BODY_STYLE] = style.name }
    }

    suspend fun setGlowTheme(theme: StickmanGlowTheme) {
        context.studioDataStore.edit { it[Keys.GLOW_THEME] = theme.name }
    }

    suspend fun setStrokeWeight(weight: StrokeWeightOption) {
        context.studioDataStore.edit { it[Keys.STROKE_WEIGHT] = weight.name }
    }

    suspend fun setFps(fps: Float) {
        context.studioDataStore.edit { it[Keys.FPS] = fps }
    }

    suspend fun setOnionSkin(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.ONION_SKIN] = enabled }
    }

    suspend fun setShowJoints(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.SHOW_JOINTS] = enabled }
    }

    suspend fun setLooping(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.IS_LOOPING] = enabled }
    }

    suspend fun setShowFace(show: Boolean) {
        context.studioDataStore.edit { it[Keys.SHOW_FACE] = show }
    }
}
