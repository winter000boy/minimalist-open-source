# Minimalist: project context

Read this before touching the code. It is the map of what exists and why, so a new session doesn't
need to re-read every file. **Keep it current: update it in the same commit as any feature change.**

## What it is

An icon-free Android home screen (launcher) to cut down screen time. Black screen, big clock, app
names as plain text. Kotlin + Jetpack Compose, single activity, no network permission.

- Repo: `github.com/winter000boy/minimalist-open-source` (personal, MIT). It is **not** a Privacera/Trust3
  repo: there's no JIRA project and no GitLab MR flow, so commits need no JIRA key.
- Package / applicationId: `io.github.winter000boy.minimalist` (can't change once released).
- Users install from `https://github.com/winter000boy/minimalist-open-source/releases/latest/download/Minimalist.apk`.

## Features and where they live

All paths below are under `app/src/main/java/io/github/winter000boy/minimalist/`.

| Feature | Where |
|---|---|
| Home: clock (tap → alarms), date ("Tuesday, 20th October"), minute tick while visible | `ui/HomeScreen.kt` (`Clock`), `ui/Formatting.kt` |
| Screen-time ring around the clock (full = 2 h), "1h 12m today"; needs Usage access | `ui/HomeScreen.kt` (`screenTimeRing`), `data/ScreenTime.kt`, `LauncherViewModel.screenTime` |
| Up to 7 pinned home apps (`MAX_HOME_APPS`) | `LauncherViewModel.kt`, `data/HomeAppsStore.kt` |
| Phone / camera shortcuts, "Set as home app" prompt | `ui/HomeScreen.kt`, `system/Intents.kt` |
| Double-tap empty home space to lock | `ui/HomeScreen.kt` (`detectTapGestures`), `ui/LauncherScreen.kt` (`onDoubleTap`), `system/LockScreenService.kt` |
| App list on the **right** of home (swipe left to open) | `ui/LauncherScreen.kt` (`HOME_PAGE = 0`, `DRAWER_PAGE = 1`) |
| List: names only, accent-insensitive search (Go opens first match), "Recently installed" (user apps, 3 days, max 3), A–Z bar, settings gear | `ui/AppDrawer.kt`, `data/LauncherApp.kt`, `data/Search.kt` |
| Long-press menu: add/remove home, App info, Uninstall | `ui/AppRow.kt` |
| Work-profile apps, live update on install/uninstall | `data/AppRepository.kt` (`LauncherApps` + callback) |
| Settings: font (Serif, Sans, Light, Condensed, Mono), text size (0.85–1.3×), double-tap to lock, double-tap-to-wake info, change home app | `ui/SettingsScreen.kt`, `data/SettingsStore.kt`, `ui/Theme.kt` |

Persistence is SharedPreferences: `home` (pinned keys, `HomeAppsStore`) and `settings` (`SettingsStore`).
An app's key is `component.flattenToString()#userSerial`.

## How it fits together

- `MainActivity` (singleTask HOME activity) builds `LauncherViewModel`, applies `MinimalistTheme(font, textSize)`,
  and forwards Home presses (`onNewIntent`) to the UI as a `homePresses` flow.
- `LauncherScreen` holds a 2-page `HorizontalPager` [home, list] and, when open, the settings screen
  in place of the pager.
- `LauncherViewModel` exposes `apps`, `homeApps`, `screenTime` and `settings` as `StateFlow`s.

## Decisions that aren't obvious from the code (don't undo without reason)

- **Always back to the clock.** `ON_STOP` → `goHome()` (pager to home, search cleared), except while settings
  are open, so a trip to Accessibility settings comes back to them. A Home press closes settings too.
- **Pager ignores bring-into-view** (`NeverBringIntoView` in `LauncherScreen`), and the search field can only take
  focus while the list is the settled page. Without both, after a hardware key press Android re-focuses the
  off-screen search field when the launcher returns, and the pager scrolls back to the list with the keyboard up.
- **Text size** works by overriding `LocalDensity`'s `fontScale` in `MinimalistTheme`, which scales every `sp` at
  once. At Default the density isn't touched, so Android 14's non-linear font scaling still applies.
- **Fonts are system families only** (no bundled font files), so every option works on every phone.
- **Lock = accessibility service** (`GLOBAL_ACTION_LOCK_SCREEN`, Android 9+). Device admin `lockNow()` was rejected
  because it disables fingerprint/face unlock until the PIN is entered. The service config
  (`res/xml/lock_screen_service.xml`) subscribes to no events and can't read window content. On Android 13+,
  browser-installed apps must first get **App info → ⋮ → Allow restricted settings**; the settings screen says so.
- **Double-tap to wake can't be built**: apps get no touches while the screen is off. Settings points to the
  phone's own option.
- **targetSdk stays 36** (lint warns) until Android 37 behaviour changes are tested; compileSdk 37 is required
  by the AndroidX versions in use.

## Build, test, release

- Toolchain: AGP 9.4.1 (built-in Kotlin, so no `kotlin-android` plugin), Kotlin compose plugin 2.4.21,
  Gradle wrapper 9.8.2, compileSdk 37 / targetSdk 36 / minSdk 26. Versions are in `gradle/libs.versions.toml`.
- On this Mac: SDK at `~/Library/Android/sdk` (`local.properties`), and JDK
  `JAVA_HOME=~/Library/Java/JavaVirtualMachines/corretto-21.0.10/Contents/Home`.
- `./gradlew assembleDebug testDebugUnitTest lintDebug`. Unit tests (`app/src/test`) cover pure logic only:
  search ranking, date/time/duration formatting, screen-time interval maths.
- **Release:** pushing to `main` (app/Gradle/workflow paths) runs `.github/workflows/android-apk.yml`, which signs
  with repo secrets and publishes release `v<versionName>-build<versionCode>` with `Minimalist.apk`.
  **Bump `versionCode` and `versionName`** in `app/build.gradle.kts` for every user-visible change, or the
  push just replaces the APK in the existing release. The signing key lives in `~/minimalist-signing/`, outside
  the repo: never read, print or commit its contents.

## Testing on the emulator

- AVD `minimalist_test` (Pixel 7, Android 16):
  `~/Library/Android/sdk/emulator/emulator -avd minimalist_test -no-window -no-audio -gpu swiftshader_indirect`.
- Make it the home app: `adb shell cmd role add-role-holder android.app.role.HOME io.github.winter000boy.minimalist`.
- Turn the lock service on without the UI:
  `adb shell settings put secure enabled_accessibility_services io.github.winter000boy.minimalist/io.github.winter000boy.minimalist.system.LockScreenService`.
- Double-tap via adb: `adb shell "input tap X Y & (sleep 0.15; input tap X Y); wait"`. Check the screen with
  `adb shell dumpsys power | grep mWakefulness`.
- `adb shell input keyevent KEYCODE_ENTER` takes the device out of touch mode, which changes focus behaviour.
  Test both that and on-screen keyboard taps when touching focus or navigation code.
- A release APK can't be installed over a debug one (different signing keys): uninstall first.
