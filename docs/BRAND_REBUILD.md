# Revenex brand and presentation rebuild

## Reference decisions

- Image 1: replace the undersized book/wordmark with a prominent RV monogram above the wide REVENEX lettering, on a black brand canvas.
- Image 2: company card is the artwork authority. Native vector contours were traced from the supplied card photograph after perspective analysis; no vector master was supplied. White/charcoal R and restrained gold V variants preserve recognition across themes. The public website currently uses a different R artwork; that asset is not shipped. No card photograph or Netflix artwork/audio is bundled.
- Images 3/4: focused school sign-in, stronger brand/heading hierarchy, clear filled inputs and primary action immediately after credentials. Only configured school email/password sign-in is offered; no fake Google/Apple/sign-up actions. Supabase is explicitly labelled and Local Demo is separate.
- Image 5: accessible two-column persona selection with clear role responsibilities, selected state and a large-text single-column fallback. Student and Parent remain distinct identities.
- Image 6: compact role-specific tasks, semantic pastel accents, register rows, concise invoices and grouped profile/report/settings content. Data-driven counts and unavailable states remain honest.
- Image 7: outgoing brand letters fade on an opaque black stage. The app content remains invisible until that stage is removed, then fades in. There is no wordmark crossfade over Welcome/portal text.

## Scope implemented

Native system splash/launcher icon; shared RV/wordmark in app bars, rails/drawers and account entry; explicit Supabase sign-in; separate offline demo; all routed role dashboards; existing attendance/assignment/fee/inbox/directory/timetable/exam/resource/operations/search/AI presentations; newly rebuilt nested student profile, personal details, academic reports, certificates, marks editor, payment review/pending/error/receipt and search dialog.

Marks save/publish/unpublish, report downloads, attachments, enrollment, invoice/receipt callbacks, Supabase authorization and payment settlement remain attached to the existing interfaces. No schema/RLS reset, reseed, migration replay or server payment change is part of this UI work. Export labels no longer invent a real school affiliation/principal signature; document generation and data inputs are retained.

## Supabase evidence

A read-only request to the configured project's `/auth/v1/settings` returned HTTP 200 with email authentication enabled. This verifies service reachability/configuration, not successful school-profile authentication. The Android entry passes email/password to the existing backend flow; portal choice never grants access. Assigned credentials must still be entered privately on the user's device for hosted acceptance.

## Safe installation

The normal APK keeps package `com.example`. Updating an older differently signed APK still requires its original signing key. An additional `review` build uses `com.example.review` and launcher label **Revenex ERP Review** so it can be installed alongside the old app without deleting its data. Both share the same source, Supabase public configuration and backend security rules; review does not create a separate school/database. No private signing key is put in source or chat. Future updates to an installed review package also need a consistent signing certificate.

CI builds and lints both packages, runs the regular UI/unit workflow tests and verifies both APKs exclude private/server configuration and Firebase dependencies. Final source/artifact/screenshot results will be recorded after verification; pending checks are not declared complete.
