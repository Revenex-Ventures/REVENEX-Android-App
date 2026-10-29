package com.example.data.datasource

import com.example.data.model.UserProfile

/** A changed protected profile requires fresh authentication, never an in-place UI role switch. */
internal class SchoolAccessChanged : IllegalStateException("School access changed. Sign in again to reload your permissions.")

internal fun validateReconciledScope(actor:UserProfile,session:String,profile:SupabaseRow?,snapshot:Map<String,List<SupabaseRow>>) {
 if(actor.id.isBlank() || session.isBlank() || profile==null || profile.id!=actor.id ||
  profile.school_id!=actor.schoolId || profile.session_id!=session || profile.role!=actor.role.name ||
  snapshot.values.any{rows->rows.any{it.school_id!=actor.schoolId || it.session_id!=session}})throw SchoolAccessChanged()
}

/** Protected allocation rows replace the historical assignedClasses display payload. RLS remains authority. */
internal fun authoritativeTeacherClasses(profileId:String,tables:Map<String,List<SupabaseRow>>):List<String> {
 val sectionIds=tables["section_teachers"].orEmpty().filter{it.profile_id==profileId}.mapNotNull{it.section_id}.toSet()
 val classes=tables["classes"].orEmpty().associateBy{it.id}
 return tables["sections"].orEmpty().filter{it.id in sectionIds}.mapNotNull{section->
  classes[section.class_id]?.grade?.takeIf{it.isNotBlank()}?.let{"$it-${section.division}"}
 }.distinct().sorted()
}
