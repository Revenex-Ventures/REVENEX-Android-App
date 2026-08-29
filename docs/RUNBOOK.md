# REVENEX (Android) — Setup & Launch Runbook

The steps only you can do to take REVENEX from code to an app your school actually uses. The coding agent (`opencode`) writes and wires the Kotlin code; the tasks below create the real accounts, keys, and cloud services it plugs into. Do them in order. When a step produces a key, hand that one key to the agent when it asks for it by name — nothing more.

> **Golden security rule**: publishable/public keys (the Firebase Android config `google-services.json`, the Google OAuth Web client ID, the Razorpay `key_id`) may live in the app. Secrets (Razorpay `key_secret` + webhook secret, any SMS/WhatsApp tokens) go only into Cloud Functions configuration — never in the app, never in `.env`, never in git, never pasted into the build prompt.

This is a native Android app (Kotlin + Jetpack Compose). There is no iOS build and no Expo — you build it in Android Studio / Gradle and ship via the Google Play Console.

---

## 0. What You'll End Up With
- A Firebase project holding your data (Firestore), files (Storage), logins (Auth), server logic (Cloud Functions), and push (FCM) — the whole backend in one console.
- A Razorpay account (Test first, then Live) for real fee payments, with the secret key living only in Cloud Functions.
- Real seeded data and test logins (Principal, Teacher, Student/Parent).
- The app built as a signed APK/AAB, installed on real phones for testing, then distributed via Play.

**Rough order**: Firebase project → enable Auth/Firestore/Storage/Functions/FCM → `google-services.json` → Google Sign-In OAuth → deploy Functions + rules → Razorpay Test keys + webhook → seed data → build in Android Studio → pilot → go Live (KYC) → Play Store.

---

## 1. Accounts to Create First (~15 minutes)
1. **Google account for the school** (owns Firebase + Google Cloud). Use an official school Workspace/Gmail address — this is also the Principal's login.
2. **Firebase / Google Cloud** — the Blaze (pay-as-you-go) plan is required for Cloud Functions; a single school's usage is typically very low. Add a billing card and set a budget alert.
3. **Razorpay** — sign up at razorpay.com. Build entirely in Test mode immediately; Live payments need business KYC (Section 5).
4. **Google Play Developer account** (one-time ~$25 / ~₹2,000) to distribute the Android app. (No Apple account needed — this is Android-only.)

---

## 2. Firebase (Your Whole Backend)

### 2.1 Create the Project
- `console.firebase.google.com` → Add project → name it e.g. `revenex-dps-noida`.
- Add an Android app to the project. Package name: `com.aistudio.revenexerp.qvnk` (or custom package name). Add SHA-1 fingerprint (get debug SHA-1 via `./gradlew signingReport`).
- Firebase generates `google-services.json`. 🔑 Give this file to the agent — it goes in the app's `app/` folder.

### 2.2 Turn On the Services
- **Auth**: Authentication → Get started → enable Google provider (for Principal). Teacher/Student login uses custom tokens or credentials verified by server.
- **Firestore**: Create database → production mode, region `asia-south1` (Mumbai).
- **Storage**: Get started (report-card PDFs, receipts, homework uploads, avatars).
- **Functions**: Enabled on Blaze plan.
- **FCM**: Cloud Messaging enabled by default.

### 2.3 Deploy the Backend & Security Rules
From project directory:
```bash
npm i -g firebase-tools
firebase login
firebase use --add
firebase deploy --only firestore:rules,firestore:indexes
firebase deploy --only functions
firebase deploy --only storage
```

### 2.4 Principal Allowlist
Add Principal's email to `adminAllowlist` collection in Firestore.

---

## 3. Google Sign-In (Principal Login)
- Registering Android app in Firebase auto-creates OAuth clients in linked Google Cloud project.
- Find Web application OAuth client ID in `console.cloud.google.com` → APIs & Services → Credentials.
- 🔑 Provide Web client ID to agent as `serverClientId`.

---

## 4. Key Mapping Quick Reference

| Credential | Public / Secret | Target Location |
|:---|:---:|:---|
| `google-services.json` | Public | App — `app/google-services.json` |
| Google OAuth Web client ID | Public | App (`serverClientId`) |
| App SHA-1 (debug + release) | Public | Registered in Firebase Console |
| Razorpay `key_id` (`rzp_test_...`) | Public | App (Razorpay Checkout) |
| Razorpay `key_secret` | **Secret** | Cloud Functions config ONLY |
| Razorpay Webhook Secret | **Secret** | Cloud Functions config ONLY |
| SMS / WhatsApp Tokens | **Secret** | Cloud Functions config ONLY |

---

## 5. Razorpay (Fee Payments)

### 5.1 Test Mode
Dashboard → Test Mode → API Keys → Generate Test Key → key_id (`rzp_test_...`) and `key_secret`.
- 🔑 `key_id` → App
- 🔑 `key_secret` → Cloud Functions secret only

### 5.2 Test Loop Verification
Pay fee in app using test card `4111 1111 1111 1111` or test UPI. Signature verified server-side before marking Paid.

### 5.3 Webhooks
Dashboard → Webhooks → Add webhook → URL = Cloud Function HTTPS URL. Events: `payment.captured` & `payment.failed`.

### 5.4 Going Live
Complete Razorpay KYC. Swap Live keys: Live `key_id` → App, Live `key_secret` → Cloud Functions.

---

## 6. Seed Real Data & Test Accounts
Run seed routine for school doc, classes, fee heads, timetable skeleton, and test accounts:
- **Principal**: Allow-listed Google email
- **Teacher**: Employee ID + DOB
- **Student/Parent**: Admission Number + DOB

---

## 7. Build & Sign the App
Build in Android Studio or via CLI:
```bash
./gradlew assembleDebug   # Debug APK
./gradlew bundleRelease   # Play Store Release AAB
```
Set release signing keystore via environment variables: `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`.

---

## 8. Distribution
- **Pilot**: Share debug APK or Play Console Internal testing track.
- **Production**: Play Store listing & rollout.

---

## 9. Pre-Launch Checklist
- [ ] Firebase Auth (Google) on; Firestore + Storage created; Functions & rules deployed.
- [ ] `google-services.json` in `app/`; debug + release SHA-1 registered; Google Sign-In works.
- [ ] Teacher and Student/Parent login verified.
- [ ] Razorpay Test payment works end-to-end (checkout → server-verified → Paid → receipt).
- [ ] Data live across devices; force-quit/reopen preserves data; no fake data paths.
- [ ] RBAC & tenant isolation verified.
- [ ] Report card PDF generation and grade logic verified.
- [ ] Release AAB builds and installs.
