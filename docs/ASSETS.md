# Revenex launch assets and references

## Motion/composition references inspected locally
- `20261024-0702-41.9675410.mp4`: 6.03 seconds, 1846×844, 30fps. Repeating short centered wordmark mask/reveal inside a phone composition. Only the controlled reveal rhythm is used; no Netflix logo, type, audio or source frames ship in the app.
- `20261024-0706-03.7567663.mp4`: 6.07 seconds, 598×710, 30fps. Dominant photography above a lower profile/account area with a softly joined surface. Adapted to generic school portal entries, never saved personal profiles or entertainment artwork.
- Both clips were decoded locally at 2fps and inspected as timestamped storyboards. Reference frames remain ignored build evidence under `app/build/launch-reference-analysis/`.

## Bundled real photograph
Photographer: **Haseeb Modi**. Title: *A group of children sitting at desks in a classroom*. Description/location from the photographer: Anjuman Mat. Hr. Sec. School boys' smart classroom, Chennai, Tamil Nadu, India. Published 23 June 2024.

Source: https://unsplash.com/photos/a-group-of-children-sitting-at-desks-in-a-classroom-OcDJhBm8Jnc
License: https://unsplash.com/license — downloaded and reviewed 24 October 2026. The Unsplash License permits downloading, copying, modifying and distributing photographs for commercial/noncommercial uses. The asset is used as an illustrative stock classroom photograph, not as a claim about a customer school or an endorsement.

Bundled asset: `app/src/main/res/drawable-nodpi/school_classroom_chennai.webp`. Optimized from the source JPEG to WebP, with original composition/people retained; no generated faces, overlays, retouching or text burned into the asset. The app crops the photograph responsively and draws a theme-aware fade in Compose. No network image request occurs at launch.

Official Revenex RV mark, company-card wordmark and reveal motion are native/vector UI. No audio or video player is included.

## Official implementation guidance
- https://developer.android.com/develop/ui/views/launch/splash-screen
- https://developer.android.com/develop/ui/views/launch/splash-screen/migrate
- https://developer.android.com/jetpack/androidx/releases/core
- https://developer.android.com/develop/ui/views/theming/darktheme

The AndroidX core-splashscreen dependency is justified solely by consistent API 24+ system-splash handoff; no animation/image/network library is introduced. System splash and Compose use the same dark brand surface. App state is composed/initialized under the brief brand overlay rather than blocked behind a timer.

## Authentication boundary
Portal selection is an entry hint only. `loginWithSupabase` continues to load the protected profile, school, session and child links; it never receives the selected portal role. Supabase demo sessions remain memory-only as configured before this work. Warm activity resume retains an already authorized dashboard; process death requires a fresh sign-in under that existing policy. No passwords or saved-person profiles are persisted by this entry flow.
