package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Student
import org.json.JSONObject

/**
 * Persists the user-editable parts of a [Student] profile — including the saved avatar
 * file path — so profile changes survive app restarts.
 *
 * Demo mode rebuilds all ERP state from [com.example.data.repository.SampleData] on every
 * launch, which previously discarded the in-memory `avatarUrl` even though the image file
 * itself stayed on disk. Only the editable subset is stored and merged back over the
 * sample record by id, so non-editable fields keep tracking the canonical sample data.
 */
object StudentProfileStore {

  private const val PREFS_NAME = "revenex_student_profiles"

  @Volatile
  private var prefs: SharedPreferences? = null

  fun init(context: Context) {
    if (prefs == null) {
      synchronized(this) {
        if (prefs == null) {
          prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
      }
    }
  }

  fun save(student: Student) {
    val store = prefs ?: return
    val json = JSONObject().apply {
      put("name", student.name)
      put("dob", student.dob)
      put("bloodGroup", student.bloodGroup)
      put("parentPhone", student.parentPhone)
      put("parentEmail", student.parentEmail)
      put("address", student.address)
      put("avatarUrl", student.avatarUrl)
    }
    store.edit().putString(student.id, json.toString()).apply()
  }

  fun remove(studentId: String) {
    prefs?.edit()?.remove(studentId)?.apply()
  }

  fun applyOverrides(students: List<Student>): List<Student> {
    val store = prefs ?: return students
    if (store.all.isEmpty()) return students
    return students.map { student ->
      val raw = store.getString(student.id, null) ?: return@map student
      runCatching {
        val json = JSONObject(raw)
        student.copy(
          name = json.optString("name", student.name),
          dob = json.optString("dob", student.dob),
          bloodGroup = json.optString("bloodGroup", student.bloodGroup),
          parentPhone = json.optString("parentPhone", student.parentPhone),
          parentEmail = json.optString("parentEmail", student.parentEmail),
          address = json.optString("address", student.address),
          avatarUrl = json.optString("avatarUrl", student.avatarUrl)
        )
      }.getOrDefault(student)
    }
  }
}
