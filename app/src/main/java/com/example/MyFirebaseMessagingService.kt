package com.example

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger

/** Displays received notifications. Server delivery and user/device registration are separate. */
class MyFirebaseMessagingService : FirebaseMessagingService() {
  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    val title = message.notification?.title ?: message.data["title"]
    val body = message.notification?.body ?: message.data["body"]
    if (title.isNullOrBlank() && body.isNullOrBlank()) return
    showNotification(this, title ?: "Revenex School ERP", body.orEmpty(), CHANNEL_DEFAULT)
  }

  companion object {
    const val CHANNEL_DEFAULT = "revenex_erp_default"
    private val notificationIds = AtomicInteger(0)

    fun createNotificationChannel(context: Context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(
          CHANNEL_DEFAULT, "School updates", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Attendance, fees, exams and school announcements" })
      }
    }

    fun showNotification(context: Context, title: String, body: String, channelId: String) {
      createNotificationChannel(context)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
      val manager = NotificationManagerCompat.from(context)
      if (!manager.areNotificationsEnabled()) return
      val openApp = PendingIntent.getActivity(context, 0,
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
      val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        .setContentIntent(openApp)
        .setAutoCancel(true)
        .build()
      manager.notify(notificationIds.incrementAndGet(), notification)
    }
  }
}
