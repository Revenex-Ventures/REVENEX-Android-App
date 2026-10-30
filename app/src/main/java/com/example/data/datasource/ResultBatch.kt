package com.example.data.datasource

import kotlinx.serialization.json.*

/** A single student's publish action submits the configured class/exam batch,
 * including every observed version. Only the database can authorize publication. */
internal fun observedResultBatch(studentId:String,term:String,scopes:List<SupabaseRow>,marks:List<SupabaseRow>):Pair<SupabaseRow,JsonArray> {
  val scope=scopes.singleOrNull{it.term==term&&studentId in it.eligible_student_ids}
    ?: error("A unique exam, eligible roster and required subject scope must be configured before publication.")
  check(scope.required_subjects.isNotEmpty()&&scope.eligible_student_ids.isNotEmpty()){"Result batch configuration is incomplete."}
  val observed=buildJsonArray {
    for(pupil in scope.eligible_student_ids.sorted())for(subject in scope.required_subjects.sorted()) {
      val mark=marks.singleOrNull{it.student_id==pupil&&it.subject==subject&&it.term==scope.term&&
        it.school_id==scope.school_id&&it.session_id==scope.session_id&&it.section_id==scope.section_id}
        ?: error("Required marks are missing. A zero mark is valid; missing marks cannot be published.")
      add(buildJsonObject{put("id",mark.id);put("version",mark.version)})
    }
  }
  return scope to observed
}
