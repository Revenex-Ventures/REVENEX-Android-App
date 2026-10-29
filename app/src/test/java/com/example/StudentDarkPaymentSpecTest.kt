package com.example

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.components.FeePaymentDialog
import com.example.ui.theme.ScholaTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File

/** Presentation fixtures only: these are not captured-provider transaction evidence. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w393dp-h852dp-xhdpi")
class StudentDarkPaymentSpecTest {
 @get:Rule val compose=createComposeRule()
 @Test fun unconfirmedLookupAndCancellationNeverDisplayVerifiedReceipt(){
  var state by mutableIntStateOf(0);var starts=0;var checks=0
  compose.setContent{ScholaTheme(true,student=true){FeePaymentDialog(
   "Synthetic test student",if(state==0)0L else 450000L,state==1,state==3,
   if(state==3)"pay_fixture"else "",{}, {starts++},
   receiptNo=if(state==3)"TEST-FIXTURE"else "",
   statusMessage=if(state==2)"Checkout was cancelled. Check payment status for a confirmed outcome."else "",onCheckStatus={checks++})}}
  compose.onNodeWithText("Amount not loaded").assertExists()
  compose.onNodeWithText("Captured · verified (test)").assertDoesNotExist()
  compose.onNodeWithText("Check status").performClick()
  compose.runOnIdle{assertEquals(0,starts);assertEquals(1,checks)}
  File("build/reports/student-dark/nested").mkdirs()
  val names=listOf("lookup","payment_pending","payment_cancelled","verified_receipt_fixture")
  for(i in names.indices){
   compose.runOnIdle{state=i};compose.waitForIdle()
   if(i!=3)compose.onNodeWithText("Captured · verified (test)").assertDoesNotExist()
   compose.onRoot().captureRoboImage("build/reports/student-dark/nested/${names[i]}.png")
  }
  compose.onNodeWithText("TEST-FIXTURE").assertExists()
  compose.runOnIdle{assertEquals(0,starts)}
 }
}
