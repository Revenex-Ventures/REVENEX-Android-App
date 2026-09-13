# app/src/test/

JVM unit tests and screenshot tests. These run on the host machine without a device or emulator.

## Directories

- `java/com/example/` — Kotlin test source files
- `screenshots/` — Golden PNG screenshots used by Roborazzi for visual regression testing

## Run all tests

```bash
.\gradlew.bat test
```

Current status: **15/15 tests passing (100%)**.
