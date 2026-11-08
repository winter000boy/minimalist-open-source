package io.github.winter000boy.minimalist.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppFont { Serif, Sans, Light, Condensed, Mono }

/** Multiplies every text size in the app, on top of the phone's own font size. */
enum class TextSize(val scale: Float) { Small(0.85f), Default(1f), Large(1.15f), ExtraLarge(1.3f) }

data class LauncherSettings(
    val font: AppFont = AppFont.Serif,
    val textSize: TextSize = TextSize.Default,
)

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    fun update(transform: (LauncherSettings) -> LauncherSettings) {
        val settings = transform(_settings.value)
        _settings.value = settings
        prefs.edit {
            putString(KEY_FONT, settings.font.name)
            putString(KEY_TEXT_SIZE, settings.textSize.name)
        }
    }

    private fun load(): LauncherSettings {
        val defaults = LauncherSettings()
        return LauncherSettings(
            font = AppFont.entries.find { it.name == prefs.getString(KEY_FONT, null) } ?: defaults.font,
            textSize = TextSize.entries.find { it.name == prefs.getString(KEY_TEXT_SIZE, null) }
                ?: defaults.textSize,
        )
    }

    private companion object {
        const val KEY_FONT = "font"
        const val KEY_TEXT_SIZE = "text_size"
    }
}
