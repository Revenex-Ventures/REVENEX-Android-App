# Student timetable: connected India-time rail

The Student dark timetable now uses the connected rail already present in Light
mode, while retaining its approved colored lesson cards. Start/end times align
with each lesson. Completed segments, upcoming nodes and the current position
remain visually distinct. Gaps between lessons receive their own connected
segment and a break label, so the current marker continues through a break.

The existing clock converts the current instant to `Asia/Kolkata` and refreshes
once per second while the screen is started. Selecting another day never shows
an incorrect live lesson. This is a device clock converted to IST, not a trusted
network time service; incorrect device time can still affect the position.

Lesson status is display-only and never marks attendance or writes completion.
Teacher names remain available by expanding lesson cards. The shell owns system
and keyboard insets; the lesson list remains scrollable above the bottom dock.

Verification includes elapsed-seconds progress, India midnight rollover under a
different device timezone, AM/PM parsing, invalid intervals, past/future days,
and actual native Compose captures of a live lesson and a live break. Final
results and APK metadata are recorded in the delivery folder after checks finish.
