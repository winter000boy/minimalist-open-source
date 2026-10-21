package io.github.winter000boy.minimalist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.winter000boy.minimalist.data.AppRepository
import io.github.winter000boy.minimalist.data.HomeAppsStore
import io.github.winter000boy.minimalist.data.LauncherApp
import io.github.winter000boy.minimalist.data.ScreenTime
import io.github.winter000boy.minimalist.data.ScreenTimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val homeAppsStore = HomeAppsStore(application)

    /** Every launchable app, alphabetical. */
    val apps: StateFlow<List<LauncherApp>> = AppRepository(application).apps()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Apps pinned to the home screen, in the order they were added. */
    val homeApps: StateFlow<List<LauncherApp>> = combine(apps, homeAppsStore.keys) { apps, keys ->
        val byKey = apps.associateBy(LauncherApp::key)
        keys.mapNotNull(byKey::get)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val screenTimeRepository = ScreenTimeRepository(application)

    /**
     * Today's screen time. Re-read whenever collection starts, so usage access is re-checked on
     * return from Settings, then on every minute, in step with the clock.
     */
    val screenTime: StateFlow<ScreenTime> = flow {
        while (true) {
            emit(screenTimeRepository.today())
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ScreenTime.Unknown)

    /** Returns false when the home screen is already full. */
    fun addToHome(app: LauncherApp): Boolean {
        val keys = homeAppsStore.keys.value
        if (app.key in keys) return true
        if (homeApps.value.size >= MAX_HOME_APPS) return false
        homeAppsStore.save(keys + app.key)
        return true
    }

    fun removeFromHome(app: LauncherApp) {
        homeAppsStore.save(homeAppsStore.keys.value - app.key)
    }

    companion object {
        /** Kept small on purpose: the home screen is for essentials only. */
        const val MAX_HOME_APPS = 7
    }
}
