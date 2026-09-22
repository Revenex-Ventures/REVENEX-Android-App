# REVENEX Android App — Rules

## UI/Design Focus

Design and UI work touches ONLY the files below. Do not read, search, or analyze other
files (`data/`, `repository/`, services, MainActivity, etc.) unless a task explicitly requires it.

### Theme tokens
- `app/src/main/java/com/example/ui/theme/Color.kt`
- `app/src/main/java/com/example/ui/theme/Tokens.kt`
- `app/src/main/java/com/example/ui/theme/Type.kt`
- `app/src/main/java/com/example/ui/theme/Theme.kt`

### Reusable components
- `app/src/main/java/com/example/ui/components/DesignSystem.kt` (hero card `AnimatedAttendanceHero`, `AttendanceBead`, `CountUpNumber`, buttons, etc.)
- `app/src/main/java/com/example/ui/components/CommonComponents.kt`
- `app/src/main/java/com/example/ui/components/Dialogs.kt`
- `app/src/main/java/com/example/ui/components/CommandPalette.kt`
- `app/src/main/java/com/example/ui/components/ScheduleTimeline.kt`
- `app/src/main/java/com/example/ui/components/ResponsiveScaffold.kt`

### Screens (UI layout)
- `app/src/main/java/com/example/ui/screens/**` (auth, dashboard, profile, academics, attendance, fees, teachers, students, operations, reports, search, ai, communication)

Everything under `app/src/main/java/com/example/ui/` is UI. Everything else is logic to skip.

## Commands
- Build: `.\gradlew.bat assembleDebug`
- Install: `adb install -r app\build\outputs\apk\debug\app-debug.apk`
- Relaunch: `adb shell am force-stop com.example; adb shell am start -n com.example/com.example.MainActivity`

## Conventions
- Reuse existing tokens (`ScholaTerracotta`, `Radius.hero`, `Spacing.*`) from the theme files; do not hardcode new values inline.
- Preserve the public API of shared components like `AnimatedAttendanceHero` unless the caller updates.
- Design fanout (screens that host a shared component): keep each dashboard's hero call in sync.