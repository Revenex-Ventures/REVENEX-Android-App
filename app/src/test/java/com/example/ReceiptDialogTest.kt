package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.components.FeePaymentDialog
import com.example.ui.theme.ScholaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class ReceiptDialogTest {
  @get:Rule val compose = createComposeRule()
  @Test fun receiptLookupCannotStartAnotherCharge() {
    var starts=0; var checks=0
    compose.setContent { ScholaTheme {
      FeePaymentDialog("Demo student",0L,false,false,"",{}, { starts++ },onCheckStatus={checks++})
    } }
    compose.onNodeWithTag("submit_fee_payment_button").assertDoesNotExist()
    compose.onNodeWithText("Check payment status").performClick()
    compose.runOnIdle { assertEquals(1,checks);assertEquals(0,starts) }
  }
}
