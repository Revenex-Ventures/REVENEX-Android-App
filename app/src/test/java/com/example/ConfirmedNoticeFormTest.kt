package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.components.CreateNoticeDialog
import com.example.ui.theme.ScholaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], qualifiers="w600dp-h1000dp-xhdpi")
class ConfirmedNoticeFormTest {
  @get:Rule val compose = createComposeRule()
  @Test fun failureRetainsFormAndRetryUsesSameRecordUntilConfirmed() {
    var dismissed = false
    val attemptedIds = mutableListOf<String>()
    val attemptedAudiences = mutableListOf<String>()
    compose.setContent { ScholaTheme(false) {
      CreateNoticeDialog(onDismiss={ dismissed=true }, onPublish={ error("Async callback must not be used") },
        onSubmitNotice={ notice ->
          attemptedIds.add(notice.id)
          attemptedAudiences.add(notice.targetAudience)
          if (attemptedIds.size == 1) error("Network unavailable")
        })
    } }
    compose.onNodeWithText("Notice Title *").performTextInput("School timetable update")
    compose.onNodeWithText("Notice message *").performTextInput("Please review the new timetable.")
    compose.onNodeWithText("Teachers").performScrollTo().performClick()
    compose.onNodeWithTag("submit_publish_notice_button").assertIsDisplayed().performClick()
    compose.waitUntil { attemptedIds.size == 1 }
    compose.onNodeWithText("Publication could not be confirmed.", substring=true).assertIsDisplayed()
    assertFalse(dismissed)
    compose.onNodeWithTag("submit_publish_notice_button").assertIsDisplayed().performClick()
    compose.waitUntil { dismissed }
    assertEquals(2, attemptedIds.size)
    assertEquals(attemptedIds[0], attemptedIds[1])
    assertEquals(listOf("Teachers","Teachers"),attemptedAudiences)
  }
}
