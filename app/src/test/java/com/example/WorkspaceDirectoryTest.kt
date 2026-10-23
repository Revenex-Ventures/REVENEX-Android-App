package com.example

import com.example.data.model.UserRole
import com.example.ui.navigation.Screen
import com.example.ui.screens.operations.WorkspaceArea
import com.example.ui.screens.operations.WorkspaceDirectory
import org.junit.Assert.*
import org.junit.Test

class WorkspaceDirectoryTest {
  @Test fun familyHasFeesButNeverStaffDirectories() {
    for (role in listOf(UserRole.PARENT, UserRole.STUDENT)) {
      val routes = WorkspaceDirectory.visible(role).map { it.route }
      assertTrue(Screen.Fees.route in routes)
      assertFalse(Screen.Students.route in routes)
      assertFalse(Screen.Teachers.route in routes)
      assertFalse(Screen.Inventory.route in routes)
    }
  }

  @Test fun searchFindsMetadataAndRespectsAreaAndRole() {
    assertEquals(listOf(Screen.Fees.route), WorkspaceDirectory.visible(UserRole.ADMIN, " receipts ").map { it.route })
    assertTrue(WorkspaceDirectory.visible(UserRole.TEACHER, "receipts").isEmpty())
    assertTrue(WorkspaceDirectory.visible(UserRole.ADMIN, "receipts", WorkspaceArea.ACADEMICS).isEmpty())
  }

  @Test fun everyRoleHasUniqueRoutesAndInboxSettingsAndSearch() {
    for (role in UserRole.entries) {
      val routes = WorkspaceDirectory.visible(role).map { it.route }
      assertEquals(routes.size, routes.distinct().size)
      assertTrue(routes.containsAll(listOf(Screen.Notifications.route, Screen.Profile.route, Screen.GlobalSearch.route)))
    }
  }
}
