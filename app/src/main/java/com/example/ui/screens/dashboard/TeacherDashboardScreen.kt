package com.example.ui.screens.dashboard
import androidx.compose.runtime.Composable
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.teachers.TeacherHome
@Composable
fun TeacherDashboardScreen(repository:ErpDataRepository,onNavigateTo:(String)->Unit,onShowCreateAssignment:()->Unit,onShowApplyLeave:()->Unit) {
 TeacherHome(repository,onNavigateTo,onShowCreateAssignment,onShowApplyLeave)
}
