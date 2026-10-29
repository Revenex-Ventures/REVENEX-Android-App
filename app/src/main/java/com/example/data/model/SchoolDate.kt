package com.example.data.model

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

private val indiaSchoolZone=ZoneId.of("Asia/Kolkata")
private val schoolWireDate=DateTimeFormatter.ofPattern("dd MMM uuuu",Locale.US).withResolverStyle(ResolverStyle.STRICT)

/** Stable legacy wire format, independent of the phone's language and time zone. */
fun schoolDate(clock:Clock=Clock.systemUTC()):String=LocalDate.now(clock.withZone(indiaSchoolZone)).format(schoolWireDate)

fun parseSchoolDate(value:String):LocalDate? = listOf(DateTimeFormatter.ISO_LOCAL_DATE,schoolWireDate)
  .firstNotNullOfOrNull { format -> runCatching { LocalDate.parse(value,format) }.getOrNull() }
