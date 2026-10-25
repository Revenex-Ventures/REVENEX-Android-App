package com.example
import com.example.data.datasource.confirmInSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SessionConfirmationTest {
 @Test fun confirmedActionReloadsAndReturnsOnlyForItsInitiatingAccount()=runBlocking {
  var reloaded=false
  val result=confirmInSession({7L},{true},{"server confirmed"},{reloaded=true})
  assertTrue(reloaded);assertEquals("server confirmed",result)
 }
 @Test fun accountSwitchDuringRequestSkipsReloadAndCannotBecomeSuccess()=runBlocking {
  var revision=7L;var reloaded=false
  val result=runCatching {confirmInSession({revision},{true},{revision=8L},{reloaded=true})}
  assertTrue(result.isFailure);assertFalse(reloaded)
 }
 @Test fun logoutDuringReloadCannotBecomeSuccess()=runBlocking {
  var active=true
  val result=runCatching {confirmInSession({7L},{active},{Unit},{active=false})}
  assertTrue(result.isFailure)
 }
 @Test fun rejectedPersistenceDoesNotReloadOrSwallowTheFailure()=runBlocking {
  var reloaded=false
  val result=runCatching {confirmInSession({7L},{true},{error("Rejected by server")},{reloaded=true})}
  assertEquals("Rejected by server",result.exceptionOrNull()?.message);assertFalse(reloaded)
 }
 @Test fun signedOutActionNeverStartsPersistence()=runBlocking {
  var persisted=false
  val result=runCatching {confirmInSession({7L},{false},{persisted=true},{})}
  assertTrue(result.isFailure);assertFalse(persisted)
 }
}
