# Cyberbits Defcon Dash (Android)

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

- [app/src/main/java/com/cyberbits/defcon/](app/src/main/java/com/cyberbits/defcon/) App entry point (`DefconApp`, `MainActivity`, `AppContainer`, `AppState`)
- [app/src/main/java/com/cyberbits/defcon/ui/](app/src/main/java/com/cyberbits/defcon/ui/) Compose screens & theming
- [app/src/main/java/com/cyberbits/defcon/model/](app/src/main/java/com/cyberbits/defcon/model/) Domain models
- [app/src/main/java/com/cyberbits/defcon/pipeline/](app/src/main/java/com/cyberbits/defcon/pipeline/) Fetch / parse / score pipeline
- [app/src/main/java/com/cyberbits/defcon/data/](app/src/main/java/com/cyberbits/defcon/data/) Room + DataStore persistence
- [app/src/main/java/com/cyberbits/defcon/notification/](app/src/main/java/com/cyberbits/defcon/notification/) Threat-level notifications
- [app/src/main/java/com/cyberbits/defcon/work/](app/src/main/java/com/cyberbits/defcon/work/) WorkManager periodic refresh
- [app/src/main/res/](app/src/main/res/) Resources, including launcher icon derived from the iOS `AppIcon.png`
- [app/src/test/](app/src/test/) Unit tests
- [app/src/androidTest/](app/src/androidTest/) Instrumented tests
