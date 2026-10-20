package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertHasClickAction
import com.example.data.datasource.DataMode
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.communication.NotificationsScreen
import com.example.ui.theme.RevenexTheme
import kotlinx.coroutines.flow.first
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class NotificationInboxInteractionTest {
  @get:Rule val compose=createComposeRule()
  @Test fun rapidNotificationsRemainDistinctAndReadIndependently() = kotlinx.coroutines.runBlocking {
    val source = com.example.data.datasource.LocalDemoDataSource()
    repeat(100) { source.addNotification("Rapid $it", "Message", com.example.data.model.NotificationCategory.ANNOUNCEMENTS, null) }
    val items = source.getNotificationsFlow().first().filter { it.title.startsWith("Rapid ") }
    org.junit.Assert.assertEquals(100, items.map { it.id }.toSet().size)
    source.markNotificationAsRead(items.first().id)
    org.junit.Assert.assertEquals(1, source.getNotificationsFlow().first().count { it.title.startsWith("Rapid ") && it.isRead })
  }
  @Test fun openingUnreadNotificationMarksItRead() {
    val repository=ErpDataRepository.getInstance()
    repository.setDataMode(DataMode.DEMO)
    compose.setContent { RevenexTheme { NotificationsScreen(repository,onBack={}) } }
    compose.waitUntil { repository.notifications.value.any { !it.isRead } }
    val notification=repository.notifications.value.first { !it.isRead }
    compose.onNodeWithText(notification.title,substring=true).assertHasClickAction()
      .performClick()
    ShadowLooper.idleMainLooper()
    compose.waitUntil { repository.notifications.value.first { it.id==notification.id }.isRead }
  }
}
