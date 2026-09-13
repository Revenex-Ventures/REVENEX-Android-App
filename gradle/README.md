# gradle/

Gradle build tooling configuration.

## Files

### `libs.versions.toml`

The centralized version catalog for all project dependencies. Instead of hardcoding versions scattered across build files, every library version is declared here and referenced by alias.

Key dependency groups:

| Category | Key Libraries |
|---|---|
| **UI** | Jetpack Compose BOM 2024.09, Material3, Material Icons Extended, Navigation Compose 2.8.9 |
| **Firebase** | Firebase BOM 34.17.0 → Firestore, Auth, Storage, Firebase AI (Gemini), App Check |
| **Networking** | Retrofit 2.12, OkHttp 4.10, Moshi 1.15.2 |
| **Lifecycle** | ViewModel Compose, Runtime Compose, Lifecycle KTX 2.8.7 |
| **Local Storage** | Room 2.7.0, DataStore Preferences 1.1.7 |
| **Camera** | CameraX 1.5.0 (camera2, lifecycle, view, core) |
| **Testing** | Robolectric 4.16.1, Roborazzi 1.59.0, Coroutines Test 1.10.2 |
| **Auth** | Credentials 1.5.0, Google Identity |
| **Build plugins** | AGP 9.1.1, Kotlin 2.2.10, KSP 2.3.5, Secrets Gradle Plugin 2.0.1 |

To add a new dependency, declare its version here first, then reference the alias in `app/build.gradle.kts`.

---

## `wrapper/`

Gradle wrapper JAR and properties file. Pins the Gradle version used by this project. Do not modify manually — update via:

```bash
.\gradlew.bat wrapper --gradle-version <version>
```
