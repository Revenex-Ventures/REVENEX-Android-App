package com.example
import com.example.data.datasource.*
import com.example.data.repository.ErpDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class FeeLedgerConsistencyTest {
 private val dispatcher=StandardTestDispatcher()
 @Before fun setup(){Dispatchers.setMain(dispatcher)}
 @After fun cleanup(){dispatcher.scheduler.runCurrent();Dispatchers.resetMain()}
 @Test fun demoDashboardAndInvoiceBalancesUseTheSameFeeRecords()=runTest(dispatcher) {
  val repository=ErpDataRepository.getInstance();repository.setDataMode(DataMode.SUPABASE_DEMO);runCurrent();repository.setDataMode(DataMode.DEMO);runCurrent()
  val fees=repository.feeRecords.value;val invoices=repository.invoices.value.associateBy {it.id}
  assertTrue(fees.isNotEmpty());assertEquals(fees.size,invoices.size)
  fees.forEach {fee-> val invoice=invoices.getValue(fee.id)
   assertEquals(fee.studentId,invoice.studentId);assertEquals(fee.totalFee,invoice.totalAmount)
   assertEquals(fee.paidAmount,invoice.paidAmount);assertEquals(fee.pendingAmount,invoice.pendingAmount)
  }
 }
}
