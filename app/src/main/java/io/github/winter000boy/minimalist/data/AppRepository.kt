package io.github.winter000boy.minimalist.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import io.github.winter000boy.minimalist.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.text.Collator

/** Reads launchable apps from every profile and re-reads them when packages change. */
class AppRepository(context: Context) {

    private val context = context.applicationContext
    private val launcherApps = this.context.getSystemService(LauncherApps::class.java)
    private val userManager = this.context.getSystemService(UserManager::class.java)

    fun apps(): Flow<List<LauncherApp>> = packageChanges()
        .conflate()
        .map { loadApps() }
        .flowOn(Dispatchers.Default)

    /** Emits once immediately, then on every install, uninstall, or update. */
    private fun packageChanges(): Flow<Unit> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) {
                trySend(Unit)
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) {
                trySend(Unit)
            }
        }
        trySend(Unit)
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { launcherApps.unregisterCallback(callback) }
    }

    private fun loadApps(): List<LauncherApp> {
        val mainUser = Process.myUserHandle()
        val collator = Collator.getInstance()
        return launcherApps.profiles.flatMap { user ->
            // A locked or paused profile can refuse access; just skip it.
            val activities = runCatching { launcherApps.getActivityList(null, user) }
                .getOrDefault(emptyList())
            val serial = userManager.getSerialNumberForUser(user)
            activities.mapNotNull { info ->
                if (info.componentName.packageName == context.packageName) return@mapNotNull null
                val label = info.label.toString().trim().ifEmpty { info.componentName.packageName }
                val flags = info.applicationInfo.flags
                val isSystemApp = flags and ApplicationInfo.FLAG_SYSTEM != 0
                LauncherApp(
                    label = if (user == mainUser) label else context.getString(R.string.work_app_label, label),
                    component = info.componentName,
                    user = user,
                    userSerial = serial,
                    firstInstallTime = info.firstInstallTime,
                    isSystemApp = isSystemApp,
                    // Updated system apps can still roll back to the factory version.
                    canUninstall = !isSystemApp || flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0,
                )
            }
        }.sortedWith(compareBy(collator) { it.label })
    }
}
