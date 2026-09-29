# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An Android app ("Hide Notifications", package `com.martinservold.hidenotifications`) that auto-dismisses specific notifications from an app, such as that one app whose recurring notification can't be turned off on its own. Kotlin, Jetpack Compose (Material 3), Room via KSP. minSdk 26, target/compile SDK 34, JDK 17, Gradle 8.7 wrapper, AGP 8.5.2, Kotlin 1.9.24. It is released under CC0, but the Gradle wrapper files keep their Apache 2.0 license.

## Build

```
./gradlew assembleDebug      # debug APK, applicationId gets a ".debug" suffix so it installs alongside release
./gradlew installDebug       # install to a connected device/emulator
./gradlew assembleRelease    # signed only if keystore.properties exists
```

- Release signing reads `keystore.properties` at the repo root (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`). That file and `app/keystore/` are git-ignored. Without it, the release build is produced unsigned.
- There are no tests and no lint setup beyond the Gradle defaults. You verify changes on a real device, which also needs notification access granted in system settings.

## Architecture

The UI and the listener service run in one process and share state through two singletons:

- **`service/NotificationBlockerService`** (a `NotificationListenerService`): this does the core work. On every post it:
  - skips its own package
  - checks the notification against `cachedRules` and cancels it if a rule matches (the rule's `dismissCount` goes up)
  - drops group summaries
  - otherwise mirrors the notification into `NotificationRepository`

  `cachedRules` is kept current by collecting the Room `Flow`, so matching stays synchronous inside the callback. Every 60s a reconcile loop prunes list entries the OS no longer reports, to cover missed removal events. A static `instance` exposes `cancelNow(key)` so the UI can dismiss notifications right away.
- **`repository/NotificationRepository`**: an in-memory `StateFlow<List<ActiveNotification>>` object that holds the live list shown on the Notifications tab. It is not persisted.
- **`repository/RuleRepository`**: a singleton over Room that is the single source of truth for rules. `findMatch`/`titleMatches` hold the matching semantics. A `null` `titleMatch` blocks the whole package, `EXACT` compares case-sensitively and `CONTAINS` ignores case. Both the service and the UI call these, so keep matching logic here.
- **`data/`**: the `NotificationRule` entity and `AppDatabase`. When you change the entity, bump the `@Database` version and add a `Migration` to `addMigrations(...)`. `exportSchema = false`, so no schema JSON is written. Enums are stored as text through `Converters`.
- **UI** (`MainActivity` with two bottom-nav tabs, `ui/screens`): there are no ViewModels. Screens get repositories through `getInstance(context)` inside `remember`, and launch writes on `rememberCoroutineScope`.
  - A long press on a notification opens `BlockNotificationDialog`. On confirm, the UI cancels matching live notifications, inserts the rule, and adds the matched count to its `dismissCount`.
  - The snackbar's Undo only deletes the rule. Notifications that were already cancelled are not restored.
- `util/` covers checking notification-listener access and the battery-optimization exemption prompt (shown as a banner). An optimized app can have its listener killed.
