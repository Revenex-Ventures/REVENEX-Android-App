package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.model.UserRole
import com.example.ui.components.ApplyLeaveDialog
import com.example.ui.theme.ScholaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w600dp-h1000dp-xhdpi")
class ConfirmedLeaveFormTest {
 @get:Rule val compose=createComposeRule()
 @Test fun invalidDatesNeverSubmitAndFailedRequestKeepsOneRetryReference() {
  var dismissed=false;val attempts=mutableListOf<String>()
  compose.setContent{ScholaTheme(false){ApplyLeaveDialog("Synthetic teacher",UserRole.TEACHER,{dismissed=true},
   {_,_,_,_,_->error("Legacy callback must not be used")},onSubmitLeave={id,_,_,_,_->
    attempts.add(id);if(attempts.size==1)error("Synthetic connection failure")
   })}}
  compose.onNodeWithText("From Date").performTextClearance()
  compose.onNodeWithText("From Date").performTextInput("31 Feb 2099")
  compose.onNodeWithText("Reason *").performScrollTo().performTextInput("Synthetic request reason")
  compose.onNodeWithText("Submit request").performClick()
  compose.onNodeWithText("Use valid dates",substring=true).assertExists()
  assertTrue(attempts.isEmpty());assertFalse(dismissed)
  compose.onNodeWithText("From Date").performScrollTo().performTextClearance()
  compose.onNodeWithText("From Date").performTextInput("01 Jan 2099")
  compose.onNodeWithText("To Date").performTextClearance()
  compose.onNodeWithText("To Date").performTextInput("02 Jan 2099")
  compose.onNodeWithText("Submit request").performClick()
  compose.waitUntil{attempts.size==1}
  compose.onNodeWithText("Request was not confirmed",substring=true).assertIsDisplayed()
  assertFalse(dismissed)
  compose.onNodeWithText("Submit request").performClick()
  compose.waitUntil{dismissed}
  assertEquals(2,attempts.size);assertEquals(attempts[0],attempts[1])
 }
}
