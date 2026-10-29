package com.example

import com.example.data.datasource.*
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class SupabaseReconciliationTest {
 private val actor=UserProfile("teacher","Teacher","teacher@example.com",UserRole.TEACHER,"","","T",schoolId="school")
 private val profile=SupabaseRow("teacher",school_id="school",session_id="session",role="TEACHER")
 @Test fun verifiedEmptySnapshotIsValidButMissingProfileIsNot() {
  validateReconciledScope(actor,"session",profile,emptyMap())
  assertTrue(runCatching{validateReconciledScope(actor,"session",null,emptyMap())}.isFailure)
 }
 @Test fun changedIdentityRoleSchoolOrSessionRejectsSnapshot() {
  for(changed in listOf(profile.copy(id="another"),profile.copy(role="PRINCIPAL"),profile.copy(school_id="other"),profile.copy(session_id="old"))){
   assertTrue(runCatching{validateReconciledScope(actor,"session",changed,emptyMap())}.isFailure)
  }
 }
 @Test fun mixedSchoolOrSessionRowsCannotPopulateAnAccount() {
  for(row in listOf(SupabaseRow("student",school_id="other",session_id="session"),SupabaseRow("student",school_id="school",session_id="old"))){
   assertTrue(runCatching{validateReconciledScope(actor,"session",profile,mapOf("students" to listOf(row)))}.isFailure)
  }
 }
 @Test fun teacherClassesComeFromCurrentBackendAllocationsNotPayload() {
  val tables=mapOf(
   "classes" to listOf(SupabaseRow("c10",grade="10"),SupabaseRow("c9",grade="9")),
   "sections" to listOf(SupabaseRow("10a",class_id="c10",division="A"),SupabaseRow("9b",class_id="c9",division="B")),
   "section_teachers" to listOf(SupabaseRow("allocation",section_id="10a",profile_id="teacher"),SupabaseRow("other",section_id="9b",profile_id="another"))
  )
  assertEquals(listOf("10-A"),authoritativeTeacherClasses("teacher",tables))
  assertEquals(emptyList<String>(),authoritativeTeacherClasses("teacher",tables+("section_teachers" to emptyList())))
 }
}
