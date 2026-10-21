package io.github.winter000boy.minimalist.system

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import io.github.winter000boy.minimalist.R
import io.github.winter000boy.minimalist.data.LauncherApp

fun Context.launchApp(app: LauncherApp) = tryOrToast {
    getSystemService(LauncherApps::class.java).startMainActivity(app.component, app.user, null, null)
}

fun Context.openAppInfo(app: LauncherApp) = tryOrToast {
    getSystemService(LauncherApps::class.java).startAppDetailsActivity(app.component, app.user, null, null)
}

fun Context.uninstall(app: LauncherApp) = tryOrToast {
    startActivity(
        Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.component.packageName, null))
            .putExtra(Intent.EXTRA_USER, app.user),
    )
}

fun Context.openDialer() = tryOrToast { startActivity(Intent(Intent.ACTION_DIAL)) }

fun Context.openCamera() = tryOrToast { startActivity(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) }

fun Context.openAlarms() = tryOrToast { startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS)) }

fun Context.openHomeSettings() = tryOrToast { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }

/** Straight to our own "Usage access" switch where Settings supports it, else the full list. */
fun Context.openUsageAccessSettings() = tryOrToast {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    try {
        startActivity(Intent(intent).setData(Uri.fromParts("package", packageName, null)))
    } catch (_: ActivityNotFoundException) {
        startActivity(intent)
    }
}

fun Context.isDefaultLauncher(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_HOME)
    }
    val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolved = packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
    return resolved?.activityInfo?.packageName == packageName
}

/** The system "use as home app?" dialog (Android 10+), or null to fall back to settings. */
fun Context.homeRoleRequestIntent(): Intent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    val roleManager = getSystemService(RoleManager::class.java)
    if (!roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) return null
    return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
}

private inline fun Context.tryOrToast(block: () -> Unit) {
    try {
        block()
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show()
    }
}
