# app/src/test/

JVM unit tests and screenshot tests. These run on the host machine without a device or emulator.

## Directories

- `java/com/example/` — Kotlin test source files
- `screenshots/` — Golden PNG screenshots used by Roborazzi for visual regression testing

## Run all tests

```bash
.\gradlew.bat test
```

See the current verification record in `docs/ERP_AUDIT.md`; historical test counts are not evidence for the current checkout. Notification tests cover received data messages, private notification display and denied permission. Screenshot captures write to `app/build/reports/screenshots`; capture alone is not an asserted visual regression test.
For the current Robolectric Android 16 native loader, use a `GRADLE_USER_HOME` path without spaces. Test runtime artifacts are cached there under `caches/robolectric-m2`; JVM temporary files are under the app build directory. This avoids both the native loader's encoded-space path bug and exhaustion of the system TEMP drive.
