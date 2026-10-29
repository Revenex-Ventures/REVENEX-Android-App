package com.example
import com.example.ui.screens.principal.principalCsv
import com.example.ui.screens.principal.principalMoney
import org.junit.Test
import org.junit.Assert.*
class PrincipalReportExportTest {
 @Test fun csvEscapesMultilineQuotesAndNeutralizesSpreadsheetFormulas() {
  val output=principalCsv(listOf(listOf("name","value"),listOf("Doe, A\n\"B\""," =HYPERLINK(\"x\")"),listOf("@SUM(A1)","\t+1")))
  assertTrue(output.contains("\"Doe, A\n\"\"B\"\"\""))
  assertTrue(output.contains("\"' =HYPERLINK(\"\"x\"\")\""))
  assertTrue(output.contains("\"'@SUM(A1)\",\"'\t+1\""))
  assertTrue(output.endsWith("\r\n"))
 }
 @Test fun currencyRetainsPaiseWithoutFloatingPointRounding() {
  assertTrue(principalMoney(123456789L).contains("1,234,567.89"))
  assertTrue(principalMoney(1L).contains("0.01"))
 }
}
