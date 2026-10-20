package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.messaging.RemoteMessage
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class MessagingNotificationTest {
  @Test
  fun `data message displays a private notification with a launch action`() {
    val service = Robolectric.buildService(MyFirebaseMessagingService::class.java).create().get()
    service.onMessageReceived(RemoteMessage.Builder("sender")
      .setMessageId("notice-1")
      .setData(mapOf("title" to "School update", "body" to "A new notice is available"))
      .build())
    val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val posted = shadowOf(manager).allNotifications.single()
    assertEquals("School update", posted.extras.getCharSequence("android.title"))
    assertEquals(android.app.Notification.VISIBILITY_PRIVATE, posted.visibility)
    assertNotNull(posted.contentIntent)
    assertNotNull(manager.getNotificationChannel(MyFirebaseMessagingService.CHANNEL_DEFAULT))
  }

  @Test
  fun `empty data message does not create a blank notification`() {
    val service = Robolectric.buildService(MyFirebaseMessagingService::class.java).create().get()
    service.onMessageReceived(RemoteMessage.Builder("sender").build())
    val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    assertTrue(shadowOf(manager).allNotifications.isEmpty())
  }

  @Test
  @Config(sdk = [33])
  fun `denied notification permission does not post or crash`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    shadowOf(application).denyPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    val service = Robolectric.buildService(MyFirebaseMessagingService::class.java).create().get()
    service.onMessageReceived(RemoteMessage.Builder("sender")
      .setData(mapOf("title" to "School update", "body" to "A new notice is available"))
      .build())
    val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    assertTrue(shadowOf(manager).allNotifications.isEmpty())
  }
}
