# Revenex launch and account entry

## Implemented flow

Launcher → Android system book mark → native Compose REVENEX reveal → bundled classroom photograph and portal entry → existing school authentication → backend-authorized dashboard.

The application is composed while the brand overlay runs. The reveal is 1,550ms, includes its settle/fade, has no sound, and has no additional startup sleep. The process-lifetime claim prevents replay on resume, recomposition and activity recreation. Reduced motion skips the reveal after the static system mark. No reference video is played or bundled.

The two supplied clips were inspected locally before implementation. The first informs the controlled wordmark mask; the second informs the photo-over-account composition. No reference branding or footage is reused. Photo credit, license and official Android sources are in [LAUNCH_ASSETS_AND_REFERENCES.md](LAUNCH_ASSETS_AND_REFERENCES.md).

## Presentation and authentication

- Warm ivory `#F8F7F3`, charcoal `#20242B`, decorative orange `#F56600`. Small white action text uses the existing darker accessible orange, rather than low-contrast white on the brand accent.
- Four generic portal entries: Principal, School Administrator, Teacher, Student / Guardian. Selecting one changes the sign-in heading only; no role is submitted to Supabase.
- Local demo has its own secondary screen with separate Principal, Admin, Teacher, Student and Parent personas. It works offline and is labeled synthetic.
- Password visibility, IME submit, keyboard insets, scrolling, busy guards, error feedback and Back navigation remain native Compose controls. Passwords are not saved across activity/process recreation.
- Existing Supabase authentication still resolves protected profile, school, session and linked children. Session restoration uses the existing authenticated `sessionId()` accessor, never a nonempty demo profile. Supabase sessions remain memory-only; a process restart requires sign-in under that existing policy.
- Light remains the default; Light/Dark/System is persisted through the existing appearance preference. AndroidX core-splashscreen supplies the API 24+ handoff. On API 31+, the application-only night override uses NO/YES for Light/Dark and AUTO to clear the override for System. This application setter maps AUTO to `UI_MODE_NIGHT_UNDEFINED` in AOSP, unlike the global time-based `setNightMode` API, which this app never calls. Night resources match the stored dark starting window.
- No Supabase schema, RLS, payment server, payment settlement or private configuration is changed by this launch work.

## Verification record

This file will be updated with the final tested source, APK hash and actual emulator recording after the quality gate completes. Implementation alone is not visual or hosted verification.

Already inspected: fresh Compose/Robolectric renders of photo entry and login in Light/Dark, separate demo selection, narrow 1.5× text, tablet entry and the actual routed Teacher dashboard. The first full launch test run compiled and ran 114 tests; three failed (two obsolete scroll-to-pinned-footer queries and one animation-clock synchronization). These were repaired without skipping tests or reducing assertions.

Pending evidence: final build/test/lint result; actual APK cold launch/warm resume, appearance restart and screen recording. A separate Android 14 emulator was created because the preexisting AVD configuration pointed to an unsupported ARM image. Existing AVD/device data was not reset.

Runtime testing found a System-mode regression in the initial launch implementation: a forced application night mode was read back as if it were the Android setting. Component tests had not caught it. The fix clears the application override instead of mirroring app resources or registering a configuration receiver. Official source verified: [Android 12L UiModeManagerService](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android12L-release/services/core/java/com/android/server/UiModeManagerService.java), [Android 14 UiModeManagerService](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android14-release/services/core/java/com/android/server/UiModeManagerService.java). Final runtime recheck remains required below.

## Device/hosted checks requiring a real account

Successful cloud sign-in must be completed using an assigned synthetic school account on the user's device. No password, OTP, private key or payment details should be sent in chat. Local demo entry does not prove hosted authentication, RLS or payment capture. Physical-device splash timing, OEM behavior and API 24–30 launch-window appearance remain separate checks unless explicitly recorded below.

Use a data-preserving `adb install -r` only when the APK signing certificate matches the installed app. Do not uninstall or clear data to work around a signature mismatch. No physical Android device was connected when this verification began.
