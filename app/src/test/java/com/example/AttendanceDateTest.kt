package com.example

import com.example.ui.screens.attendance.attendanceDateMatches
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class AttendanceDateTest {
  @Test fun savedPostgresDateMatchesRegisterSelectorWithoutTimezoneShift() {
    assertTrue(attendanceDateMatches("2026-10-24", "24 Oct 2026", Locale.ENGLISH))
    assertTrue(attendanceDateMatches("24 Oct 2026", "2026-10-24", Locale.ENGLISH))
    assertFalse(attendanceDateMatches("2026-10-23", "24 Oct 2026", Locale.ENGLISH))
  }
  @Test fun localizedAndLocalDemoDisplayDatesMatch() {
    assertTrue(attendanceDateMatches("2026-10-24", "24 oct. 2026", Locale.FRANCE))
    assertTrue(attendanceDateMatches("24 Oct 2026", "24 Oct 2026", Locale.ENGLISH))
  }
  @Test fun invalidDatesNeverProduceASavedRegisterMatch() {
    assertFalse(attendanceDateMatches("2026-02-30", "02 Mar 2026", Locale.ENGLISH))
    assertFalse(attendanceDateMatches("not recorded", "not recorded", Locale.ENGLISH))
  }
}
