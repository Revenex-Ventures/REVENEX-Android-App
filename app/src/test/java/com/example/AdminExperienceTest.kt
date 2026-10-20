package com.example

import com.example.data.model.UserRole
import com.example.data.datasource.LocalDemoDataSource
import com.example.ui.navigation.NavConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AdminExperienceTest {
  @Test
  fun administrator_has_a_distinct_operational_workspace() {
    val items = NavConfig.getBottomNavItems(UserRole.ADMIN)

    assertEquals("Control", items.first().label)
    assertTrue(items.any { it.label == "Admissions" })
    assertTrue(items.any { it.label == "Schedule" })
    assertTrue(items.any { it.label == "Finance" })
    assertTrue(items.any { it.label == "Modules" })
  }

  @Test
  fun demo_administrator_persona_resolves_before_dashboard_navigation() = runBlocking {
    val source = LocalDemoDataSource()

    source.switchRole(UserRole.ADMIN)

    assertEquals(UserRole.ADMIN, source.getCurrentUserFlow().first().role)
  }
}
