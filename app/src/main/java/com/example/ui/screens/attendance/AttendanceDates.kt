package com.example.ui.screens.attendance

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

/** PostgreSQL date values are ISO; register selectors use localized display dates. */
fun attendanceDateMatches(recordDate: String, selectedDate: String, locale: Locale = Locale.getDefault()): Boolean {
  val formats = listOf(DateTimeFormatter.ISO_LOCAL_DATE,
    DateTimeFormatter.ofPattern("dd MMM uuuu", locale).withResolverStyle(ResolverStyle.STRICT))
  fun parse(value: String): LocalDate? = formats.firstNotNullOfOrNull { format ->
    runCatching { LocalDate.parse(value, format) }.getOrNull()
  }
  val recorded = parse(recordDate) ?: return false
  return recorded == parse(selectedDate)
}
