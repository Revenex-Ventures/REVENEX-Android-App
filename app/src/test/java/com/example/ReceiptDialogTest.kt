package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.runtime.*
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
    compose.onNodeWithText("Check status").performClick()
    compose.runOnIdle { assertEquals(1,checks);assertEquals(0,starts) }
  }
  @Test fun confirmedStatusReplacesLookupWithReceiptWithoutStartingCharge() {
    var confirmed by mutableStateOf(false)
    var starts=0
    compose.setContent { ScholaTheme {
      FeePaymentDialog("Synthetic student",if(confirmed) 125000L else 0L,false,confirmed,
        if(confirmed) "pay_fixture" else "",{}, { starts++ },
        receiptNo=if(confirmed) "RZP-TEST-fixture" else "",onCheckStatus={confirmed=true})
    } }
    compose.onNodeWithText("Check status").performClick()
    compose.onNodeWithText("RZP-TEST-fixture").assertExists()
    compose.onNodeWithText("Captured · verified (test)").assertExists()
    compose.onNodeWithTag("submit_fee_payment_button").assertDoesNotExist()
    compose.runOnIdle { assertEquals(0,starts) }
  }
}
