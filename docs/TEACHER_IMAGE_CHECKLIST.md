# Teacher image implementation checklist

25 October 2026. The two supplied Teacher images and their pasted requirements define this implementation. Exact source, APK checksum and final local checks are in `app/build/teacher-image-delivery/manifest.json`. No remote upload or CI result is claimed.

## Palette and geometry

Canvas #F8F9FC, white surfaces, orange #E85B0B, pressed #C94B08, peach #FFF0E5, text #202735/#647084, fine borders #E7EBF1. Success #18794E and error #B42318 have stable meanings. Mint #EAF6EF, blue #EAF1FC and lavender #F0ECFA support icon/context wells. Dark is deliberately coordinated navy/charcoal. Text/action palette measurements are in `palette-contrast.json`; all measured normal-text pairs exceed 4.5:1.

20dp horizontal margins, 24dp sections, 8-12dp related gaps, restrained 12dp cards, at least 48dp controls. Large-font selectors stack and shortcut/navigation grids wrap. Tablets have a rail and two-column directory. Official RV/company wordmark is retained rather than illustrative reference branding.

| Screen/state | Implementation | Evidence |
|---|---|---|
| Home | New identity/context, next class, compact metrics, six workspace actions, attention, daily schedule | Light/Dark native renders; routed APK captures |
| My classes | Allocated class/subject filter, student counts, compact rows | Native phone/tablet/large-font; routed directory |
| Class detail | Students/Work, search, attendance/assignments and academic links | Native render; installed navigation |
| Attendance | Context selectors, summary, aligned P/A/L rows, retained Excused, search/All present, anchored confirmed save feedback | Native render; Local Demo save and unsaved/saved state on APK |
| Assignments | Class/subject filters, concise published rows and resources | Native Light/Dark and installed Tasks route |
| Creation/detail | Focused fields, attachment and deadline, confirmed publication; expandable destination details | Existing API retained; installed form and keyboard capture |
| Submissions | Explicit unavailable page, no invented received/missing/review data | Existing repository lacks individual records; intentional limitation |
| Marks | Assigned context, editable validated rows, private drafts, partial-confirmation feedback | Range and persisted-draft tests; actual routed editor |
| Individual marks/reports | Existing editor retained, class scope filtered; Teacher publication controls removed | Teacher draft-only and Principal publication tests |
| Timetable | Day/class controls, compact teacher lesson rows | Native renders; installed weekday selection |
| Leave | Own requests, status/detail, request form; no approval actions | Native render; ownership/absence-of-approval test and APK form |
| Notifications | Existing recipient inbox actions under Teacher palette | Native Light/Dark and installed inbox |
| Profile/settings | Existing actions and one persistent Light/Dark/System selector | Native renders; installed appearance checks |
| Empty/error/disabled | Honest empty classes/lessons, unallocated-class denial, unavailable submissions, validation and busy/error feedback | UI tests and rendered examples; no hosted-error simulation claimed |

## Provenance and limits

Production-Compose native Robolectric captures: 393dp, font 1.0, Home 820dp high and destinations 640dp. Adaptive captures: 320dp/150% text and 800dp tablet. Installed APK captures: API 34 emulator at 360dp logical width. These evidence types are labeled separately; compilation is not visual verification.

Only UI/router/state-presentation files and relevant tests changed. Supabase Auth, repositories, RLS, data and payment code were preserved. New routes participate in logout navigation cleanup; role entry does not authorize cloud access. Local Demo is explicitly synthetic and separate from cloud sign-in.

Individual submission review, assignment draft/close APIs and marks submission-for-review are not available in the current interface; the UI does not fake them. Some legacy synthetic subject labels differ from timetable labels. No seeds were changed to disguise gaps. Existing individual report-card screens retain their prior composition. Other portals were preserved, not newly claimed as rebuilt.

No physical device or hosted two-account flow was verified in this batch. No Supabase reset, reseeding, migration, push setup or Razorpay transaction occurred. See README_TEACHER_DELIVERY.md and final logs for exact check results and remaining acceptance.
