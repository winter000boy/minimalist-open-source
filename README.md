# minimalist-open-source

A minimalist Android home screen app (launcher) built to cut down screen time.
No icons, no widgets, no colours to pull you in. Just the time, and the names of your apps when you actually need one.

## What it does

**Home screen**
- Pure black, with a large clock and the date ("Tuesday, 20th October"). Tap the clock to open your alarms.
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

1. Build the APK (see below), or download one from Releases once they exist.
2. Copy `app/build/outputs/apk/debug/app-debug.apk` to the phone and open it. You may need to allow installs from that source.
   With USB debugging on, you can do this instead:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Press the Home button. Android asks which home app to use. Pick **Minimalist** and choose **Always**.
   You can also open Minimalist and tap "Set Minimalist as your home app" at the bottom of the screen,
   or go to **Settings → Apps → Default apps → Home app**.

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

## Project layout

```
app/src/main/java/io/github/winter000boy/minimalist/
├── MainActivity.kt          Home activity; forwards Home-button presses to the UI
├── LauncherViewModel.kt     App list + pinned home apps
├── data/
│   ├── AppRepository.kt     Reads launchable apps (all profiles), live-updates on install/uninstall
│   ├── HomeAppsStore.kt     Persists pinned apps
│   ├── LauncherApp.kt       App model, search, "recently installed"
│   └── Search.kt            Accent-insensitive matching and A–Z grouping
├── system/Intents.kt        Launching apps, app info, uninstall, dialer, camera, default-launcher check
└── ui/
    ├── LauncherScreen.kt    Two-page pager: [app list] ← swipe → [home]
    ├── HomeScreen.kt        Clock, date, pinned apps, phone/camera
    ├── AppDrawer.kt         Search, list, A–Z bar
    ├── AppRow.kt            One app name + long-press menu
    ├── Formatting.kt        Time and date text
    └── Theme.kt             Black and white, serif type
```

Built with Kotlin and Jetpack Compose. Minimum Android 8.0 (API 26).

## License

[MIT](LICENSE)
