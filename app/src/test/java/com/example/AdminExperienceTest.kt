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
  fun administrator_has_a_distinct_operational_workspace() {
    val items = NavConfig.getBottomNavItems(UserRole.ADMIN)

    assertEquals("Home", items.first().label)
    assertTrue(items.any { it.label == "People" })
    assertEquals(Screen.Academics.route,items.single {it.label=="Academics"}.route)
    assertTrue(items.any { it.label == "Finance" })
    assertTrue(items.any { it.label == "More" })
  }

  @Test fun familyScheduleKeepsItsTimetableDestination() {
    listOf(UserRole.STUDENT,UserRole.PARENT).forEach {role ->
      assertEquals(Screen.Timetable.route,NavConfig.getBottomNavItems(role).single {it.label=="Timetable"}.route)
    }
  }

  @Test fun nestedDestinationsRetainTheirPrimarySelection() {
    assertEquals(Screen.Students.route, NavConfig.selectedPrimaryRoute(UserRole.ADMIN, Screen.TeacherDetail.route))
    assertEquals(Screen.Academics.route, NavConfig.selectedPrimaryRoute(UserRole.ADMIN, Screen.Homework.route))
    assertEquals(Screen.OperationsHub.route, NavConfig.selectedPrimaryRoute(UserRole.PRINCIPAL, Screen.PrincipalAttendance.route))
    assertEquals(Screen.OperationsHub.route, NavConfig.selectedPrimaryRoute(UserRole.PARENT, Screen.Notifications.route))
  }

  @Test
  fun demo_administrator_persona_resolves_before_dashboard_navigation() = runBlocking {
    val source = LocalDemoDataSource()

    source.switchRole(UserRole.ADMIN)

    assertEquals(UserRole.ADMIN, source.getCurrentUserFlow().first().role)
  }
}
