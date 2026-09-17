package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Teacher
import org.json.JSONObject

/**
 * Persists the user-editable parts of a [Teacher] profile — including the saved avatar
 * file path — so faculty profile changes survive app restarts.
 *
 * Demo mode rebuilds all ERP state from [com.example.data.repository.SampleData] on every
 * launch, which previously discarded the in-memory `avatarUrl` even though the image file
 * itself stayed on disk. Only the editable subset is stored and merged back over the
 * sample record by id, so non-editable fields keep tracking the canonical sample data.
 */
object TeacherProfileStore {

  private const val PREFS_NAME = "revenex_teacher_profiles"

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

  fun save(teacher: Teacher) {
    val store = prefs ?: return
    val json = JSONObject().apply {
      put("name", teacher.name)
      put("dob", teacher.dob)
      put("phone", teacher.phone)
      put("email", teacher.email)
      put("department", teacher.department)
      put("designation", teacher.designation)
      put("qualification", teacher.qualification)
      put("avatarUrl", teacher.avatarUrl)
    }
    store.edit().putString(teacher.id, json.toString()).apply()
  }

  fun remove(teacherId: String) {
    prefs?.edit()?.remove(teacherId)?.apply()
  }

  fun applyOverrides(teachers: List<Teacher>): List<Teacher> {
    val store = prefs ?: return teachers
    if (store.all.isEmpty()) return teachers
    return teachers.map { teacher ->
      val raw = store.getString(teacher.id, null) ?: return@map teacher
      runCatching {
        val json = JSONObject(raw)
        teacher.copy(
          name = json.optString("name", teacher.name),
          dob = json.optString("dob", teacher.dob),
          phone = json.optString("phone", teacher.phone),
          email = json.optString("email", teacher.email),
          department = json.optString("department", teacher.department),
          designation = json.optString("designation", teacher.designation),
          qualification = json.optString("qualification", teacher.qualification),
          avatarUrl = json.optString("avatarUrl", teacher.avatarUrl)
        )
      }.getOrDefault(teacher)
    }
  }
}
