# Cyberbits Defcon Dash (Android)

Android companion to the iOS [Defcon Dash](https://github.com/cyberbitsorg/apps-ios-cyberbits-defcon) app. Surfaces the current cyber threat level with supporting indicators and a feed of recent security articles.

> [!NOTE]
> **Not yet on the Play Store.** The app is feature-complete and builds/runs on a device, but the actual Play Store release (Play Console enrollment, app signing, screenshots, review submission) is still a TODO. It's deferred for now given the time, effort, and cost involved, and will be done at a later date.

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite) for article cache, DataStore for preferences
- WorkManager for periodic refresh
- OkHttp + kotlinx.serialization for the feed API
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

## License

GNU General Public License v3.0. See [LICENSE](LICENSE) for details.
