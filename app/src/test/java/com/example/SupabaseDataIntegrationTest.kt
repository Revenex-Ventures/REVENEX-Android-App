package com.example

import com.example.data.datasource.*
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class SupabaseDataIntegrationTest {
  private val dispatcher=StandardTestDispatcher()
  @Before fun setup() { Dispatchers.setMain(dispatcher);AppEnvironment.setMode(DataMode.DEMO) }
  @After fun cleanup() { ErpDataRepository.getInstance().setDataMode(DataMode.DEMO);dispatcher.scheduler.runCurrent();Dispatchers.resetMain() }

  @Test fun cloudStartsEmptyAndCannotSwitchIdentity()=runTest(dispatcher) {
    val source=SupabaseDataSource()
    assertTrue(source.getStudentsFlow().first().isEmpty())
    assertTrue(source.getNoticesFlow().first().isEmpty())
    assertTrue(source.getNotificationsFlow().first().isEmpty())
    assertTrue(source.getFeeRecordsFlow().first().isEmpty())
    assertTrue(runCatching { source.switchRole(UserRole.PRINCIPAL) }.isFailure)
    assertTrue(runCatching { source.login("someone@example.com",UserRole.PRINCIPAL,"Fake") }.isFailure)
    assertTrue(runCatching { source.markAllNotificationsAsRead() }.isFailure)
  }
  @Test fun modeSwitchClearsSamplesAndLogoutClearsCloudState()=runTest(dispatcher) {
    val repository=ErpDataRepository.getInstance()
    repository.setDataMode(DataMode.DEMO);runCurrent()
    assertTrue(repository.students.value.isNotEmpty())
    repository.setDataMode(DataMode.SUPABASE_DEMO);runCurrent()
    assertTrue(repository.students.value.isEmpty())
    assertTrue(repository.teachers.value.isEmpty())
    assertTrue(repository.feeRecords.value.isEmpty())
    assertTrue(repository.invoices.value.isEmpty())
    assertTrue(repository.gradebookEntries.value.isEmpty())
    assertEquals("",repository.selectedStudentId.value)
    repository.logout();runCurrent()
    assertTrue(repository.students.value.isEmpty())
    assertEquals("",repository.currentUser.value.id)
    repository.setDataMode(DataMode.DEMO);runCurrent()
    assertTrue(repository.students.value.isNotEmpty())
  }
  @Test fun documentReferencesAreValidatedWithoutUsingCloudCredentials()=runTest(dispatcher) {
    assertEquals("",SupabaseFileStore.resolveUrl(""))
    assertEquals("https://example.com/other.pdf",SupabaseFileStore.resolveUrl("https://example.com/other.pdf"))
    assertNull(SupabaseFileStore.documentPath("https://foreign.example/erp-documents/fake"))
    val path="school/session/00000000-0000-0000-0000-000000000001/00000000-0000-0000-0000-000000000002"
    assertEquals(path,SupabaseFileStore.documentPath("supabase://erp-documents/$path"))
    assertTrue(runCatching { SupabaseFileStore.documentPath("supabase://erp-documents/../../secret") }.isFailure)
    assertTrue(runCatching { SupabaseFileStore.resolveUrl("http://example.com/file") }.isFailure)
    assertTrue(runCatching { SupabaseFileStore.resolveUrl("javascript:alert(1)") }.isFailure)
  }
  @Test fun firebaseComponentsAreAbsent() {
    val context=androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    val flags=android.content.pm.PackageManager.GET_PROVIDERS or android.content.pm.PackageManager.GET_SERVICES or android.content.pm.PackageManager.GET_RECEIVERS
    val info=context.packageManager.getPackageInfo(context.packageName,flags)
    val names=info.providers.orEmpty().map { it.name } + info.services.orEmpty().map { it.name } + info.receivers.orEmpty().map { it.name }
    assertTrue(names.none { it.startsWith("com.google.firebase.") || it.endsWith("MyFirebaseMessagingService") })
    assertTrue(runCatching { Class.forName("com.google.firebase.auth.FirebaseAuth") }.isFailure)
  }
}
