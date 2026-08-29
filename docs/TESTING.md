# REVENEX SCHOOL ERP — TESTING STRATEGY & VERIFICATION

## 1. Automated Test Architecture
Revenex ERP includes comprehensive automated unit tests, Robolectric JVM tests, and visual screenshot verification:

- **Robolectric Local JVM Tests**: Tests navigation, role switching, attendance calculation, fee receipt generation, and leave approval workflows without requiring an emulator.
- **Compose UI Tests**: Tests key test tags (`submit_login_button`, `role_select_principal`, `pay_fees_quick_button`, etc.) to guarantee interactive accessibility and touch target validation.

## 2. Running Local Tests
To execute standard unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```

To run Roborazzi screenshot verification:
```bash
gradle :app:verifyRoborazziDebug
```
