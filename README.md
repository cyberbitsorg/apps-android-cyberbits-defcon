# Cyberbits DEFCON — Android

Android companion to the iOS [Defcon Dash](https://github.com/cyberbitsorg/apps-ios-cyberbits-defcon) app. Surfaces the current cyber threat level with supporting indicators and a feed of recent security articles.

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite) for article cache, DataStore for preferences
- WorkManager for periodic refresh
- OkHttp + kotlinx.serialization for the feed API
- Vico for charts
- `minSdk` 26, `targetSdk` 36, JVM 17

## Build & run

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration on an emulator or device.

## Tests

```bash
./gradlew test                  # unit
./gradlew connectedAndroidTest  # instrumented (needs a running device/emulator)
```

## Project layout

- [app/src/main/java/com/cyberbits/defcon/](app/src/main/java/com/cyberbits/defcon/) — application code
- [app/src/main/res/](app/src/main/res/) — resources, including launcher icon derived from the iOS `AppIcon.png`
