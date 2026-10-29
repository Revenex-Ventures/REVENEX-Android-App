package com.example

import com.example.data.model.UserRole
import com.example.data.datasource.LocalDemoDataSource
import com.example.ui.navigation.NavConfig
import com.example.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AdminExperienceTest {
  @Test
  fun administrator_has_no_mobile_workspace() {
    val items = NavConfig.getBottomNavItems(UserRole.ADMIN)

    assertTrue(items.isEmpty())
  }

  @Test fun familyScheduleKeepsItsTimetableDestination() {
    assertEquals(Screen.Timetable.route,NavConfig.getBottomNavItems(UserRole.STUDENT).single {it.label=="Schedule"}.route)
    assertEquals(Screen.Timetable.route,NavConfig.getBottomNavItems(UserRole.PARENT).single {it.label=="Timetable"}.route)
    assertEquals(listOf("Home","Learn","Schedule","Inbox","More"),NavConfig.getBottomNavItems(UserRole.STUDENT).map{it.label})
  }

  @Test fun nestedDestinationsRetainTheirPrimarySelection() {
    assertEquals(Screen.StudentLearn.route,NavConfig.selectedPrimaryRoute(UserRole.STUDENT,Screen.Exams.route))
    assertEquals(null, NavConfig.selectedPrimaryRoute(UserRole.ADMIN, Screen.TeacherDetail.route))
    assertEquals(null, NavConfig.selectedPrimaryRoute(UserRole.ADMIN, Screen.Homework.route))
    assertEquals(Screen.OperationsHub.route, NavConfig.selectedPrimaryRoute(UserRole.PRINCIPAL, Screen.PrincipalAttendance.route))
    assertEquals(Screen.OperationsHub.route, NavConfig.selectedPrimaryRoute(UserRole.PARENT, Screen.Notifications.route))
  }

  @Test
  fun backend_administrator_identity_is_retained_without_mobile_navigation() = runBlocking {
    val source = LocalDemoDataSource()

    source.switchRole(UserRole.ADMIN)

    assertEquals(UserRole.ADMIN, source.getCurrentUserFlow().first().role)
    assertTrue(NavConfig.getBottomNavItems(source.getCurrentUserFlow().first().role).isEmpty())
  }
}
