package com.example

import com.example.ui.screens.students.portal.*
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class StudentTimetableTimeTest {
 private val date=LocalDate.of(2026,10,26)
 private fun now(time:String)=ZonedDateTime.of(date,LocalTime.parse(time),ZoneId.of("Asia/Kolkata"))
 @Test fun indiaClockIgnoresDeviceTimeZoneAndRollsDate(){
  val clock=Clock.fixed(Instant.parse("2026-10-25T20:00:00Z"),ZoneId.of("America/Los_Angeles"))
  val india=studentIndiaNow(clock)
  assertEquals(date,india.toLocalDate());assertEquals(LocalTime.of(1,30),india.toLocalTime());assertEquals("Asia/Kolkata",india.zone.id)
 }
 @Test fun twentyFourHourAndMeridiemTimesAreUnambiguous(){
  assertEquals(LocalTime.of(13,0),studentLessonTime("13:00"))
  assertEquals(LocalTime.of(13,0),studentLessonTime("01:00PM"))
  assertEquals(LocalTime.MIDNIGHT,studentLessonTime("12:00 AM"))
  assertEquals(LocalTime.NOON,studentLessonTime("12:00 PM"))
  assertNull(studentLessonTime("13:75"));assertNull(studentLessonTime("99:00"))
 }
 @Test fun liveStrokeMovesByActualElapsedSeconds(){
  val progress=requireNotNull(studentLessonProgress(date,now("10:22:30"),"10:00","10:45"))
  assertEquals(StudentLessonPhase.LIVE,progress.phase);assertEquals(.5f,progress.fraction,.0001f)
  assertEquals(StudentLessonPhase.LIVE,studentLessonProgress(date,now("10:00"),"10:00","10:45")!!.phase)
  assertEquals(StudentLessonPhase.ENDED,studentLessonProgress(date,now("10:45"),"10:00","10:45")!!.phase)
 }
 @Test fun pastAndFutureDaysNeverDisplayAnIncorrectLiveMarker(){
  assertEquals(StudentLessonPhase.ENDED,studentLessonProgress(date.minusDays(1),now("10:22"),"10:00","10:45")!!.phase)
  assertEquals(StudentLessonPhase.UPCOMING,studentLessonProgress(date.plusDays(1),now("10:22"),"10:00","10:45")!!.phase)
 }
 @Test fun invalidIntervalsCannotBeMarkedEnded(){
  assertNull(studentLessonProgress(date,now("10:22"),"bad","10:45"))
  assertNull(studentLessonProgress(date,now("10:22"),"11:00","10:00"))
  assertNull(studentLessonProgress(date,now("10:22"),"10:00","10:00"))
 }
 @Test fun gapCursorUsesElapsedTimeWithoutMarkingAttendance(){
  val progress=studentLessonProgress(date,now("10:10"),"10:00","10:15")!!
  assertEquals(StudentLessonPhase.LIVE,progress.phase);assertEquals(2f/3f,progress.fraction,.0001f)
 }
}
