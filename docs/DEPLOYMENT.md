# REVENEX SCHOOL ERP — DEPLOYMENT & RELEASE GUIDE

## 1. Production Build & Packaging
To build the release APK or Android App Bundle (AAB):
```bash
gradle :app:assembleRelease
# Or for Play Store submission:
gradle :app:bundleRelease
```

## 2. Environment Variables & Secret Configuration
- In accordance with enterprise best practices, secrets (such as Google API keys, Razorpay Merchant IDs, and Gemini API keys) are configured through the AI Studio Secrets panel and injected via `BuildConfig` and `.env`.
- Do **not** commit private credentials or `local.properties` to version control.
