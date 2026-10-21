# minimalist-open-source

A minimalist Android home screen app (launcher) built to cut down screen time.
No icons, no widgets, no colours to pull you in. Just the time, and the names of your apps when you actually need one.

## What it does

**Home screen**
- Pure black, with a large clock and the date ("Tuesday, 20th October"). Tap the clock to open your alarms.
- Today's screen time: a thin white ring around the clock fills up as you use your phone, with "1h 12m today" under the date.
  A full ring means 2 hours. Time on the home screen itself doesn't count.
  It needs usage access: tap "Show today's screen time" and turn on **Permit access to app usage data**.
  The numbers come from Android's own usage stats and never leave the phone (the app has no internet permission).
- Up to 7 pinned apps, shown as plain text. The limit is deliberate.
- Phone shortcut bottom-left, camera bottom-right.

**App list** (swipe right from home)
- Every installed app, alphabetical, names only, no icons.
- Search bar at the top. Press Go on the keyboard to open the first match.
- "Recently installed" section at the top for apps you installed in the last 3 days.
- A–Z bar on the right: tap or drag to jump.
- Long-press any app for **Add to home / Remove from home**, **App info**, or **Uninstall**.

Swipe left, press Back, or press Home to return to the clock. Leaving an app always brings you back to the clock, not the list.

Work-profile apps are included and marked "(Work)".

## Install on your phone

1. On the phone, open this link. It always downloads the newest version:

   **https://github.com/winter000boy/minimalist-open-source/releases/latest/download/Minimalist.apk**

2. Open the downloaded file. Allow **Install unknown apps** for your browser when Android asks.
   Play Protect may warn that the app isn't from the Play Store; tap **Install anyway**.
3. Press the Home button. Android asks which home app to use. Pick **Minimalist** and choose **Always**.
   You can also open Minimalist and tap "Set Minimalist as your home app" at the bottom of the screen,
   or go to **Settings → Apps → Default apps → Home app**.

To update, open the same link again and install over the old version. Your pinned apps are kept.

To go back to your old launcher, pick it again under **Settings → Apps → Default apps → Home app**.

## Build

Requirements: JDK 17 or newer, and the Android SDK (platform 36). Android Studio includes both.

```bash
./gradlew assembleDebug
```

The APK ends up in `app/build/outputs/apk/debug/`. Run the unit tests with:

```bash
./gradlew testDebugUnitTest
```

If you're not using Android Studio, point Gradle at your SDK in `local.properties`:

```properties
sdk.dir=/path/to/Android/sdk
```

## Publishing a release

[`.github/workflows/android-apk.yml`](.github/workflows/android-apk.yml) builds a signed APK on every push to `main`
that changes the app, and attaches it to a GitHub Release as `Minimalist.apk`. You can also run it by hand:
**Actions → Android APK → Run workflow**.

**One-time setup.** Every release must be signed with the same key, or phones refuse to install updates.
The key lives outside the repo in `~/minimalist-signing/`. **Back up that folder** (for example in your password
manager): if it's lost, nobody can install an update over the existing app without uninstalling it first.

Add three repository secrets under **Settings → Secrets and variables → Actions → New repository secret**.
Each value is the full contents of the matching file in `~/minimalist-signing/`:

| Secret name | Copy from file |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `ANDROID_KEYSTORE_BASE64.txt` (one very long line) |
| `ANDROID_KEYSTORE_PASSWORD` | `ANDROID_KEYSTORE_PASSWORD.txt` |
| `ANDROID_KEY_ALIAS` | `ANDROID_KEY_ALIAS.txt` (`upload`) |

**For each new version**, raise `versionCode` (by 1) and `versionName` in `app/build.gradle.kts`.
The release is tagged from them, e.g. `v0.1.0-build1`. Pushing without changing them replaces the APK in the
existing release.

## Project layout

```
app/src/main/java/io/github/winter000boy/minimalist/
├── MainActivity.kt          Home activity; forwards Home-button presses to the UI
├── LauncherViewModel.kt     App list, pinned home apps, today's screen time
├── data/
│   ├── AppRepository.kt     Reads launchable apps (all profiles), live-updates on install/uninstall
│   ├── HomeAppsStore.kt     Persists pinned apps
│   ├── LauncherApp.kt       App model, search, "recently installed"
│   ├── ScreenTime.kt        Today's screen time from UsageStatsManager, usage-access check
│   └── Search.kt            Accent-insensitive matching and A–Z grouping
├── system/Intents.kt        Launching apps, app info, uninstall, dialer, camera, usage access, default-launcher check
└── ui/
    ├── LauncherScreen.kt    Two-page pager: [app list] ← swipe → [home]
    ├── HomeScreen.kt        Clock, date, screen-time ring, pinned apps, phone/camera
    ├── AppDrawer.kt         Search, list, A–Z bar
    ├── AppRow.kt            One app name + long-press menu
    ├── Formatting.kt        Time, date and "1h 12m" text
    └── Theme.kt             Black and white, serif type
```

Built with Kotlin and Jetpack Compose. Minimum Android 8.0 (API 26).

## License

[MIT](LICENSE)
