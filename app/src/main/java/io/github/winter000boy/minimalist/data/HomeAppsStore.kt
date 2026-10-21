package io.github.winter000boy.minimalist.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The ordered list of [LauncherApp.key]s pinned to the home screen. */
class HomeAppsStore(context: Context) {

    private val prefs = context.getSharedPreferences("home", Context.MODE_PRIVATE)
    private val _keys = MutableStateFlow(load())
    val keys: StateFlow<List<String>> = _keys.asStateFlow()

    fun save(keys: List<String>) {
        _keys.value = keys
        prefs.edit { putString(KEY_HOME_APPS, keys.joinToString(SEPARATOR)) }
    }

    private fun load(): List<String> =
        prefs.getString(KEY_HOME_APPS, null)
            ?.split(SEPARATOR)
            ?.filter(String::isNotEmpty)
            .orEmpty()

    private companion object {
        const val KEY_HOME_APPS = "home_apps"
        const val SEPARATOR = "\n"
    }
}
