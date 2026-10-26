package com.example

import com.example.data.model.schoolDate
import com.example.data.model.parseSchoolDate
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import java.util.TimeZone

class SchoolDateTest {
  @Test fun indiaSchoolDayDoesNotFollowDeviceTimeZone() {
    val clock=Clock.fixed(Instant.parse("2026-10-25T18:45:00Z"),ZoneOffset.ofHours(-8))
    assertEquals("26 Oct 2026",schoolDate(clock))
  }
  @Test fun legacyAndIsoDatesResolveToSameSchoolDay() {
    assertEquals(parseSchoolDate("2026-10-26"),parseSchoolDate("26 Oct 2026"))
    assertNull(parseSchoolDate("31 Feb 2026"))
  }
  @Test fun formattedWireDateDoesNotDependOnDeviceLanguage() {
    val previous=Locale.getDefault();val zone=TimeZone.getDefault()
    try {
      Locale.setDefault(Locale.JAPAN);TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
      assertEquals("26 Oct 2026",schoolDate(Clock.fixed(Instant.parse("2026-10-26T00:00:00Z"),ZoneOffset.UTC)))
    } finally {Locale.setDefault(previous);TimeZone.setDefault(zone)}
  }
}
