package com.example
import com.example.ui.components.compactSchoolMoney
import org.junit.Assert.assertEquals
import org.junit.Test
class CompactSchoolMoneyTest {
 @Test fun dashboardCurrencyKeepsIndianUnitsAndPaisePrecisionForSmallAmounts() {
  assertEquals("₹12.9k",compactSchoolMoney(1285000))
  assertEquals("₹3.8L",compactSchoolMoney(38000000))
  assertEquals("₹1Cr",compactSchoolMoney(1000000000))
  assertEquals("₹99.99",compactSchoolMoney(9999))
  assertEquals("₹0",compactSchoolMoney(0))
 }
}
