# app/src/test/java/com/example/

Kotlin test classes for unit, Robolectric, contract, and Roborazzi screenshot tests.

## Test Suites (21 Test Files)

### 1. Core Domain & Business Logic
- **`ErpProductionUnitTest.kt`**: Attendance calculation (`calculateStudentAttendancePercentage`), CBSE grade computation, fee status transitions, and role permissions.
- **`ReportDataIntegrityTest.kt`**: Validates report card metrics, GPA calculations, and aggregate data integrity.
- **`ExampleUnitTest.kt`**: Basic JUnit environment verification.

### 2. Supabase Cloud Integration & Auth
- **`SupabaseConfigurationTest.kt`**: Verifies URL parsing, public key reading, and public-only properties constraint (rejects service role keys).
- **`SupabaseDataIntegrationTest.kt`**: Tests cloud model mapping, PostgreSQL row conversion, and session state caching.
- **`SupabaseSignInDiagnosticsTest.kt`**: Diagnostic validation for Auth errors, connection failures, and unlinked profiles.
- **`SupabaseStudentSummaryTest.kt`**: Regression checks ensuring student summaries derive fees solely from permitted records without payload leaks.

### 3. UI, Theming & Interaction (Robolectric)
- **`AppearanceTest.kt`**: Theme mode persistence (Light, Dark, System) and dynamic contrast switching.
- **`ThemeContrastTest.kt`**: WCAG AA color contrast ratio verification across background, surface, text, and role accent tokens.
- **`AdminExperienceTest.kt`**: Administrator portal navigation, role switching, and control actions.
- **`ConfirmedNoticeFormTest.kt`**: Notice creation workflow, required fields, and validation before submission.
- **`NotificationInboxInteractionTest.kt`**: Unread notification count badge updates and mark-as-read interaction verification.
- **`ExampleRobolectricTest.kt`**: JVM-based Android Context tests for resources and component lifecycles.

### 4. Backend Contracts & Server AI
- **`ServerAssistantRequestTest.kt`**: Verifies client boundary constraint (Android sends only bounded query and JWT; backend derives all permissions).
- **`AssistantReplyEngineTest.kt`**: Deterministic local fallback assistant rules, role boundaries, and write refusal logic.
- **`RazorpayContractTest.kt`**: Verifies payment payload structure, amount validation, and order contracts.
- **`PdfExportStorageTest.kt`**: PDF snapshot generation and scoped storage access tests.

### 5. Visual Regression & Roborazzi Screenshot Tests
- **`GreetingScreenshotTest.kt`**: Visual snapshot test of the authentication and welcome composable.
- **`ScholaOSPreviewScreenshotTest.kt`**: Visual snapshots of 10 primary dashboard and workflow screens.
- **`RevenexRedesignScreenshotTest.kt`**: Snapshot verification of the redesigned Warm-White/Ink/Teal design system.
- **`SecondaryScreensScreenshotTest.kt`**: Visual tests for detail views, modals, and split-pane tablet layouts.

## Running Tests

```bash
# Run all unit and Robolectric tests
.\gradlew.bat test

# Run a specific test suite
.\gradlew.bat testDebugUnitTest --tests com.example.SupabaseDataIntegrationTest

# Record updated Roborazzi screenshot goldens
.\gradlew.bat recordRoborazziDebug
```

