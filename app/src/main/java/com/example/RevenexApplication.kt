package com.example

import android.app.Application
import com.example.data.local.StudentProfileStore
import com.example.data.local.TeacherProfileStore

class RevenexApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    StudentProfileStore.init(this)
    TeacherProfileStore.init(this)
  }
}
