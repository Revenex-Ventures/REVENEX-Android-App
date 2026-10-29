package com.example

import android.app.Application
import com.example.data.local.StudentProfileStore
import com.example.data.local.TeacherProfileStore

class RevenexApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    com.example.ui.theme.AppearanceSystemBars.install(this)
    StudentProfileStore.init(this)
    TeacherProfileStore.init(this)
    // Demo uses the database inbox only; do not initialize FCM or device push.
  }
}
