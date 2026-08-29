# REVENEX SCHOOL ERP — TROUBLESHOOTING GUIDE

## Common Build & Runtime Issues

### 1. "SDK location not found"
- **Symptom**: Gradle build fails with `SDK location not found. Define a valid SDK location...`
- **Fix**: Ensure `local.properties` exists in the project root with the correct path:
  ```properties
  sdk.dir=C\:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
  ```

### 2. "There is not enough space on the disk"
- **Symptom**: Gradle fails during dependency download with disk space error.
- **Fix**: Free up at least 3 GB on the drive. Clean Gradle caches if needed:
  ```bash
  .\gradlew.bat clean
  ```

### 3. "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"
- **Symptom**: `gradlew.bat` execution fails immediately.
- **Fix**: Ensure `gradle/wrapper/gradle-wrapper.jar` exists. If missing, restore it from Gradle release assets.

### 4. "Firebase Auth / Firestore Not Initialized"
- **Symptom**: Logcat shows `Cloud Firestore not initialized` or authentication fails in Production Mode.
- **Fix**: Ensure `google-services.json` is placed in `app/` directory and contains valid Firebase project credentials.

---

## Support & Diagnostics

To run full diagnostic checks:
```bash
# Check Gradle environment
.\gradlew.bat --version

# Run debug unit test suite
.\gradlew.bat :app:testDebugUnitTest --info
```
