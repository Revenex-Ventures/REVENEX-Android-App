# Student timetable — India-time rail

This change is limited to the Student timetable presentation. It preserves the existing authorized school/session/class timetable data and all other pages.

- One connected vertical rail, aligned start/end times, subject, teacher, room and period number.
- India time is explicitly `Asia/Kolkata`, calculated from the device's current instant, independently of its display timezone. Enable Android automatic date/time for an accurate device clock; this is not an external time-synchronization service.
- The default selection follows today's Indian weekday, including midnight rollover. Other weekday selections refer to the current Indian calendar week; **Today** restores automatic selection.
- Green check nodes mean a scheduled interval has ended; orange highlights the current interval with a moving dot/tick; hollow nodes mean upcoming intervals. These are time-based statuses, never attendance or completion records.
- Actual gaps between lessons remain connected and display their duration. Invalid/missing interval times show an honest unavailable status.
- The clock refreshes every second while the screen is started, stops in the background and refreshes on resume. No looping decorative animation or additional libraries are used.
- Existing Light/Dark palettes, semantic colors, scalable text, scrolling, accessible row descriptions and system-safe navigation remain in use.

Implementation: `StudentTimetableTimeline.kt`; the old `StudentTimetable` implementation is removed from `StudentAcademics.kt` so the existing route calls the new implementation. Shared dimensions are in `Tokens.kt`.

Verification evidence and APK are under `app/build/student-timeline-delivery/`. Controlled-clock native renders demonstrate active lessons and gaps; emulator screenshots demonstrate the actual device clock and existing Local Demo data. These must not be described as hosted sign-in or physical-device verification.

Time tests cover India date rollover, device-zone independence, 12/24-hour input, noon/midnight, invalid times, interval boundaries and fractional progress. The native render test checks active lesson/gap states in Light/Dark and asserts that attendance data is unchanged.

Lifecycle reference: [Android lifecycle-aware coroutines](https://developer.android.com/topic/libraries/architecture/lifecycle).
