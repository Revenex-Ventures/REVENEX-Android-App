package com.example

import com.example.data.datasource.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CompletePaginationTest {
  @Test fun lowerServerCapDoesNotTurnAShortPageIntoATruncatedSuccess() = runBlocking {
    val expected = (1..1005).map { it.toString().padStart(5, '0') }
    val actual = readCompleteRows<String>(pageSize=500, id={it}) { after, _ ->
      val remaining = expected.filter { after==null || it>after }
      SchoolPage(remaining.take(37), remaining.size.toLong())
    }
    assertEquals(expected, actual)
  }

  @Test fun missingCountEarlyEmptyDuplicateOrChangedCountCannotPublishPartialRows() = runBlocking {
    for (fault in listOf("missing", "empty", "duplicate", "drift")) {
      var calls=0
      val result=runCatching {
        readCompleteRows<String>(id={it}) { _, _ ->
          calls++
          when {
            fault=="missing" -> SchoolPage(listOf("a"), null)
            calls==1 -> SchoolPage(listOf("a"), 2)
            fault=="empty" -> SchoolPage(emptyList(), 1)
            fault=="duplicate" -> SchoolPage(listOf("a"), 1)
            else -> SchoolPage(listOf("b"), 2)
          }
        }
      }
      assertTrue(fault, result.isFailure)
    }
  }

  @Test fun finalCountChangeAndSafetyLimitCannotBecomeSuccessfulSnapshot() = runBlocking {
    var calls=0
    assertTrue(runCatching {
      readCompleteRows<String>(id={it}) { _, _ ->
        calls++; SchoolPage(listOf("a"), if(calls==1)1 else 2)
      }
    }.isFailure)
    assertTrue(runCatching {
      readCompleteRows<String>(maxRows=1, id={it}) { _, _ -> SchoolPage(listOf("a"), 2) }
    }.isFailure)
  }
}
