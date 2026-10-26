package com.example

import com.example.data.datasource.*
import com.example.data.model.UserRole
import com.example.data.repository.SampleData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SchoolWorkflowAvailabilityTest {
 @Test fun cloudAvailabilityStartsUnknownWithoutPretendingSetupIsComplete()=runBlocking {
  val source=SupabaseDataSource()
  val state=source.getBackendCapabilitiesFlow().first()
  assertFalse(state.loaded);assertFalse(state.assignments);assertFalse(state.submissions);assertFalse(state.marks)
 }
 @Test fun localDraftCanBePublishedWithoutLeakingOrDuplicatingIt()=runBlocking {
  val source=LocalDemoDataSource();source.switchRole(UserRole.TEACHER)
  val work=SampleData.initialAssignments.first().copy(id="workflow_draft_fixture",publicationState="DRAFT")
  source.createAssignment(work)
  assertEquals(1,source.getAssignmentsFlow().first().count{it.id==work.id})
  source.switchRole(UserRole.STUDENT)
  assertFalse(source.getAssignmentsFlow().first().any{it.id==work.id})
  source.switchRole(UserRole.TEACHER);source.createAssignment(work.copy(publicationState="PUBLISHED"))
  source.switchRole(UserRole.STUDENT)
  assertEquals(1,source.getAssignmentsFlow().first().count{it.id==work.id})
 }
 @Test fun pickerFormatsMatchThePrivateBucketInsteadOfAdvertisingUnsupportedOfficeTypes() {
  for(mime in listOf("application/pdf","image/jpeg","image/png","application/vnd.openxmlformats-officedocument.wordprocessingml.document"))assertTrue(DocumentFiles.isAllowedDocumentType(mime))
  for(mime in listOf("application/msword","application/vnd.ms-excel","text/html","application/octet-stream",""))assertFalse(DocumentFiles.isAllowedDocumentType(mime))
 }
 @Test fun localNoticeTargetingAndRetryDoNotLeakOrDuplicateTheAnnouncement()=runBlocking {
  val source=LocalDemoDataSource();source.switchRole(UserRole.PRINCIPAL)
  val notice=SampleData.initialNotices.first().copy(id="targeted_local_fixture",targetAudience="Teachers")
  source.publishNotice(notice);source.publishNotice(notice)
  source.switchRole(UserRole.TEACHER)
  assertEquals(1,source.getNoticesFlow().first().count{it.id==notice.id})
  assertEquals(1,source.getNotificationsFlow().first().count{it.actionableId==notice.id})
  source.switchRole(UserRole.STUDENT)
  assertFalse(source.getNoticesFlow().first().any{it.id==notice.id})
  assertFalse(source.getNotificationsFlow().first().any{it.actionableId==notice.id})
 }
 @Test fun localLeaveRetryPreservesOneRequestAndAnExistingDecision()=runBlocking {
  val source=LocalDemoDataSource();source.switchRole(UserRole.TEACHER)
  source.requestLeave("leave_retry_fixture","Medical Leave","01 Jan 2099","02 Jan 2099","Synthetic leave")
  source.requestLeave("leave_retry_fixture","Medical Leave","01 Jan 2099","02 Jan 2099","Synthetic leave")
  assertEquals(1,source.getLeaveRequestsFlow().first().count{it.id=="leave_retry_fixture"})
  assertEquals(2,source.getLeaveRequestsFlow().first().single{it.id=="leave_retry_fixture"}.daysCount)
  source.switchRole(UserRole.PRINCIPAL);source.updateLeaveStatus("leave_retry_fixture",com.example.data.model.LeaveStatus.APPROVED,"Checked")
  source.switchRole(UserRole.TEACHER);source.requestLeave("leave_retry_fixture","Medical Leave","01 Jan 2099","02 Jan 2099","Synthetic leave")
  assertEquals(com.example.data.model.LeaveStatus.APPROVED,source.getLeaveRequestsFlow().first().single{it.id=="leave_retry_fixture"}.status)
 }
}
