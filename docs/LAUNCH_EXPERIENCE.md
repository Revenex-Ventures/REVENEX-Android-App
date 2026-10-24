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

## Verified delivery — 24 October 2026

- Tested source: `bfc5dc16bff87216405849ff5820e1853768316d` on `codex/revenex-reference-redesign`.
- CI build merge commit: `7663858da7e962e7820001d534f19a3421adfeee`.
- [CI run 40080849234](https://github.com/Revenex-Ventures/REVENEX-Android-App/actions/runs/40080849234): Android and server security jobs passed; deployment skipped. No database or server deployment was performed.
- Android: **116 tests, 0 failures, 0 errors, 0 skipped**. Lint: **0 errors, 71 warnings**. **129** fresh Compose/Robolectric PNGs. These counts supersede historical 81/82/97/104/114 counts for this delivery.
- CI commands: `assembleDebug testDebugUnitTest lintDebug verifyNoFirebaseDependencies`, with `-Proborazzi.test.record=true`; APK private-configuration boundary check also passed. Local Windows Gradle attempts were blocked by existing cache locks; these results are from Linux CI/JDK 21, not a successful local Windows build.
- APK: `D:/VS Code/Revenex Android/app/build/launch-entry-delivery/apk/app-debug.apk`, **25,966,525 bytes**.
- APK SHA-256: `b999c86ecd10cfb1c1d71fd38dd9dc64c282876ade36b5d7820697d8d43d5040`.

### Actual Android runtime verification

Installed this exact APK on a separate Android 14 / API 34 Pixel 6 emulator. The existing AVD was not reset: its broken ARM configuration was bypassed with dedicated verification AVDs. No physical device was connected. Normal phone viewport: 1080×2400 px, density 420; large text: 1.5×. Tablet check resized the dedicated emulator to 1600×2560 px at density 320 (800dp wide), then restored the phone viewport.

| Check | Actual result |
|---|---|
| Fresh installation | Light photo/account entry reached from the real launcher icon |
| Native splash → brand → entry | Continuous runtime recording inspected; original book, mask/wordmark reveal and photo entry rendered |
| Portal selection and Back | Principal, Admin, Teacher and Student/Guardian sign-in pages reached and returned correctly |
| Local Demo | All five distinct Principal/Admin/Teacher/Student/Parent dashboards reached and captured |
| Offline behavior | Cloud sign-in showed `Cannot reach Supabase. Check internet access and try again.`; no dashboard granted; Parent Local Demo still worked offline |
| Warm resume | HOME → actual launcher icon returned to the Parent dashboard without entry/reveal replay |
| Appearance | Light, Dark, and System verified; System followed Android light → dark → light; explicit Light stayed light while Android was dark |
| Restart | Stored Dark and Light survived process restart; process restart returns to entry under the existing memory-only sign-in policy |
| Reduced motion | Zero animator scale recorded in Dark: static native/brief brand presentation followed by photo entry, without the moving reveal |
| Interrupted startup | HOME interruption followed by launcher resume reached entry without hanging |
| Large text and keyboard | Actual 1.5× keyboard-open form inspected; fields/action/help remained scrollable above the IME |
| Tablet width | Actual photo/entry columns and focused login inspected at 800dp width |
| Crash check | No AndroidRuntime errors from the final running app process |

Evidence under `app/build/launch-entry-delivery/`:
- `final-runtime/Revenex_actual_launch_complete.mp4`: continuous actual app recording, **24.91 seconds**, H.264 720×1600, **no audio**. It shows launcher icon → brand → photo entry → Teacher sign-in UI → **Local Demo** persona selection → Teacher dashboard. It does **not** demonstrate successful hosted Supabase authentication.
- `final-runtime/complete_recording_storyboard.jpg`: extracted frames, inspected through the final dashboard.
- `final-runtime/Dark_reduced_motion_startup.mp4` and `dark_reduced_motion_storyboard.jpg`: actual reduced-motion/Dark startup.
- `final-runtime/actual_role_dashboards_gallery.jpg`: actual Principal/Admin/Teacher/Student/Parent screens.
- `final-runtime/actual_entry_states_gallery.jpg`: actual light/dark entry, login, offline failure, keyboard and tablet examples.
- `final-runtime/appearance_verified_gallery.jpg`: actual appearance states; individual original PNGs are adjacent.
- `launch-compose-gallery.jpg`: fresh Compose/Robolectric entry/login/demo/narrow/tablet/routed Teacher renders, separate from emulator captures.
- `verification.json`: source, merge commit, APK hash and test/lint counts.

The 1,550ms value is the Compose reveal duration. It is not a claim that total debug cold startup, including Android/process initialization, takes 1,550ms on every device.

### Corrections found during verification

The first 114-test launch run had three failures: two tests attempted to scroll an action that is now pinned, and one began measuring animation time before committing the native-exit state. Tests now assert the pinned action is visible and synchronize the controlled animation clock. No tests were skipped or removed.

Runtime testing then found a genuine System-mode regression not caught by those component tests: forced app night resources were being read back as though they were Android's setting. The fix clears the application override; no configuration receiver or time-based global night setting is used. A new regression test verifies the application setting is cleared while Android's setting is unchanged. An API 28 night-resource test also checks explicit Light/Dark starting palettes.

Official implementation checked: [Android 12L UiModeManagerService](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android12L-release/services/core/java/com/android/server/UiModeManagerService.java), [Android 14 UiModeManagerService](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android14-release/services/core/java/com/android/server/UiModeManagerService.java). In the application setter, AUTO maps to `UI_MODE_NIGHT_UNDEFINED`; this differs from the global `setNightMode` API. Actual System light/dark/light captures confirmed the fix.

## Device/hosted checks requiring a real account

Successful cloud sign-in must be completed using an assigned synthetic school account on the user's device. No password, OTP, private key or payment details should be sent in chat. Local demo entry does not prove hosted authentication, RLS or payment capture. Physical-device splash timing, OEM behavior and API 24–30 launch-window appearance remain separate checks unless explicitly recorded below.

### Confirmed signing/upgrade blocker

`apksigner verify --print-certs` confirmed different certificates:
- This APK: `992225b13a3c145e1560b422461a4069d05bac40d3011719e4ccfb513bce6b31`.
- Historical `app/build/admin-upgrade-delivery/apk/app-debug.apk`: `1d87c2c8ffa4650d5e5d07026e01472815a63c4e2f9bd28cc5f69a2d2a145145`.

A data-preserving replacement of that historical build is blocked until the new APK can be signed with the original key. CI currently generates separate debug certificates across runners. Do not uninstall, clear data, bypass signature verification or reset a device to work around this. The delivered APK was tested on a fresh isolated emulator. Use a separate test device for a fresh installation, or configure the original matching signing key locally/through secure CI secrets before `adb install -r`. Never place private signing material in chat or Git. See [Android app signing](https://developer.android.com/studio/publish/app-signing).

### Scope and remaining checks

Successful hosted Supabase sign-in using an assigned account is not verified in this run. On your own device, select the relevant portal and sign in yourself; the backend must resolve the actual role/school/session/children before dashboard access. Do not send credentials. Physical OEM behavior, first installation while Android is already dark, and API 24–30 device startup continuity still need device checks; API 28 theme resources were unit-tested, not device-tested. No Razorpay transaction or new push integration was tested or added by this launch task.

The launch/account-entry work is implemented and tested within these limits. It does not declare the earlier whole-ERP redesign complete: nested report/certificate/marks-editor and some payment-dialog presentation work remain in the broader checklist. Supabase, RLS, payment verification, existing data and unrelated user README edits were preserved.


## Source provenance and handoff

The tested APK was built from the GitHub source/merge commits above. Local `git rev-parse HEAD` remains `ce80da2d73283bb62846449cb9b434ff8e800cff`: existing Windows Git/index and Gradle cache locks prevented normal local staging/building. Exact allowlisted changes were committed to the existing GitHub branch through the Git Data API, with expected-parent checks and no force update. Unrelated local README changes were preserved. Do not describe the local HEAD as the tested build, reset the working tree, delete another process's locks, or blindly pull over user edits.

For the next agent: preserve this launch flow and the Supabase/payment contracts; obtain a real-account/device test without collecting credentials in chat; resolve original-key signing before promising an in-place upgrade; continue the explicitly unfinished nested presentation checklist separately. Do not infer hosted authentication/payment success from Local Demo, CI or screenshots.
