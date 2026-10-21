package io.github.winter000boy.minimalist.data

import android.content.ComponentName
import android.os.UserHandle

/** One launchable activity, for one user profile (personal or work). */
data class LauncherApp(
    val label: String,
    val component: ComponentName,
    val user: UserHandle,
    /** Stable across reboots, unlike the user id, so it is safe to persist. */
    val userSerial: Long,
    val firstInstallTime: Long,
    /** Came with the phone, so it was never really "installed" by the user. */
    val isSystemApp: Boolean,
    val canUninstall: Boolean,
) {
    val key: String = "${component.flattenToString()}#$userSerial"
    val searchKey: String = label.normalizedForSearch()
    val indexLetter: Char = indexLetterOf(searchKey)
}

private const val RECENT_WINDOW_MS = 3L * 24 * 60 * 60 * 1000
private const val RECENT_MAX = 3

/** Up to three apps the user installed in the last three days, newest first. */
fun recentlyInstalled(
    apps: List<LauncherApp>,
    now: Long = System.currentTimeMillis(),
): List<LauncherApp> = apps
    .filter { !it.isSystemApp && now - it.firstInstallTime in 0 until RECENT_WINDOW_MS }
    .sortedByDescending { it.firstInstallTime }
    .take(RECENT_MAX)

/** Filters [this] (already alphabetical) by [query], best matches first. */
fun List<LauncherApp>.search(query: String): List<LauncherApp> {
    val normalized = query.normalizedForSearch()
    if (normalized.isEmpty()) return this
    return mapNotNull { app -> matchRank(app.searchKey, normalized)?.let { it to app } }
        .sortedBy { it.first }
        .map { it.second }
}
