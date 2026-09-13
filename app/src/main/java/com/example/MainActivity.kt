package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.razorpay.Checkout
import com.razorpay.PaymentResultWithDataListener
import com.razorpay.PaymentData
import org.json.JSONObject
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.navArgument
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.NavConfig
import com.example.ui.navigation.Screen
import com.example.ui.screens.academics.ExamsScreen
import com.example.ui.screens.academics.HomeworkScreen
import com.example.ui.screens.academics.ReportCardScreen
import com.example.ui.screens.academics.StudyMaterialScreen
import com.example.ui.screens.academics.TimetableScreen
import com.example.ui.screens.ai.AiAssistantScreen
import com.example.ui.screens.attendance.AttendanceScreen
import com.example.ui.screens.attendance.PrincipalAttendanceScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.communication.NoticesScreen
import com.example.ui.screens.communication.NotificationsScreen
import com.example.ui.screens.dashboard.ParentDashboardScreen
import com.example.ui.screens.dashboard.PrincipalDashboardScreen
import com.example.ui.screens.dashboard.StudentDashboardScreen
import com.example.ui.screens.dashboard.TeacherDashboardScreen
import com.example.ui.screens.fees.FeeManagementScreen
import com.example.ui.screens.operations.*
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reports.ReportsAnalyticsScreen
import com.example.ui.screens.search.GlobalSearchScreen
import com.example.ui.screens.students.StudentDetailScreen
import com.example.ui.screens.students.StudentListScreen
import com.example.ui.screens.teachers.TeacherDetailScreen
import com.example.ui.screens.teachers.TeacherListScreen
import com.example.ui.theme.ScholaTheme

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

  companion object {
    var activeStudentId by mutableStateOf<String?>(null)
    var activeAmount by mutableStateOf(0L)
    var isPaymentProcessing by mutableStateOf(false)
    var isPaymentCompleted by mutableStateOf(false)
    var razorpayPaymentId by mutableStateOf("")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Checkout.preload(applicationContext)
    enableEdgeToEdge()
    setContent {
      ScholaTheme {
        ScholaErpApp(
          isProcessing = isPaymentProcessing,
          paymentCompleted = isPaymentCompleted,
          razorpayPaymentId = razorpayPaymentId,
          onStartPayment = { studentId, amount ->
            startRazorpayPayment(studentId, amount)
          },
          onResetPayment = {
            isPaymentProcessing = false
            isPaymentCompleted = false
            razorpayPaymentId = ""
            activeStudentId = null
            activeAmount = 0L
          }
        )
      }
    }
  }

  private fun startRazorpayPayment(studentId: String, amount: Long) {
    activeStudentId = studentId
    activeAmount = amount
    isPaymentProcessing = true
    isPaymentCompleted = false
    razorpayPaymentId = ""

    val co = Checkout()
    val razorpayKey = runCatching { BuildConfig.RAZORPAY_KEY_ID }.getOrNull()
    if (razorpayKey.isNullOrBlank() || razorpayKey.trim() == "rzp_test_XXXXXXXXXXXXXXXX") {
      android.widget.Toast.makeText(this, "Simulating Stripe/Razorpay payment in Test Sandbox.", android.widget.Toast.LENGTH_SHORT).show()
      onPaymentSuccess("pay_demo_success_${System.currentTimeMillis()}", null)
      return
    }
    co.setKeyID(razorpayKey.trim())
    try {
      val options = JSONObject()
      options.put("name", "ScholaOS")
      options.put("description", "Tuition & Academic Dues")
      options.put("theme.color", "#C2410C")
      options.put("currency", "USD")
      options.put("amount", amount)

      val prefill = JSONObject()
      prefill.put("email", "parent@scholaos.edu")
      prefill.put("contact", "9876543210")
      options.put("prefill", prefill)

      co.open(this, options)
    } catch (e: Exception) {
      android.widget.Toast.makeText(this, "Payment error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
      isPaymentProcessing = false
      activeStudentId = null
      activeAmount = 0L
    }
  }

  override fun onPaymentSuccess(paymentId: String?, paymentData: PaymentData?) {
    isPaymentProcessing = false
    isPaymentCompleted = true
    razorpayPaymentId = paymentId ?: "pay_schola_success"

    val studentId = activeStudentId
    val amount = activeAmount
    if (studentId != null && amount > 0) {
      val repository = ErpDataRepository.getInstance()
      repository.processFeePayment(
        studentId = studentId,
        amountPaid = amount,
        paymentMethod = "Stripe ACH / Card",
        feeHead = "Term 2 Tuition & Practicum Dues",
        razorpayPaymentId = paymentId,
        razorpayOrderId = paymentData?.orderId,
        razorpaySignature = paymentData?.signature
      )
    }
  }

  override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
    isPaymentProcessing = false
    android.widget.Toast.makeText(this, "Payment Cancelled: $response", android.widget.Toast.LENGTH_SHORT).show()
  }
}

@Composable
fun ScholaErpApp(
  isProcessing: Boolean,
  paymentCompleted: Boolean,
  razorpayPaymentId: String,
  onStartPayment: (studentId: String, amount: Long) -> Unit,
  onResetPayment: () -> Unit
) {
  val repository = remember { ErpDataRepository.getInstance() }
  val navController = rememberNavController()

  val currentUser by repository.currentUser.collectAsState()
  val notifications by repository.notifications.collectAsState()
  val unreadNotificationsCount = notifications.count { !it.isRead }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  // Global Command Palette (⌘K) Modal State
  var showCommandPalette by remember { mutableStateOf(false) }

  // Dialog & Modal States
  var showAddStudentDialog by remember { mutableStateOf(false) }
  var showAddTeacherDialog by remember { mutableStateOf(false) }
  var showCreateNoticeDialog by remember { mutableStateOf(false) }
  var showCreateAssignmentDialog by remember { mutableStateOf(false) }
  var showApplyLeaveDialog by remember { mutableStateOf(false) }

  var showFeePaymentModal by remember { mutableStateOf(false) }
  var paymentModalStudentName by remember { mutableStateOf("") }
  var paymentModalAmount by remember { mutableStateOf(0L) }

  ScholaResponsiveScaffold(
    currentRoute = currentRoute,
    currentUser = currentUser,
    unreadNotificationsCount = unreadNotificationsCount,
    onNavigateToRoute = { route ->
      if (currentRoute != route) {
        navController.navigate(route) {
          popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
          }
          launchSingleTop = true
          restoreState = true
        }
      }
    },
    onOpenCommandPalette = { showCommandPalette = true },
    onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
    onOpenProfile = { navController.navigate(Screen.Profile.route) },
    onLogout = {
      repository.logout()
      navController.navigate(Screen.Auth.route) {
        popUpTo(0) { inclusive = true }
      }
    }
  ) { paddingValues ->
    NavHost(
      navController = navController,
      startDestination = Screen.Auth.route,
      modifier = Modifier.padding(paddingValues),
      enterTransition = { fadeIn(animationSpec = tween(250)) + slideInHorizontally(initialOffsetX = { 200 }, animationSpec = tween(250, easing = FastOutSlowInEasing)) },
      exitTransition = { fadeOut(animationSpec = tween(250)) + slideOutHorizontally(targetOffsetX = { -200 }, animationSpec = tween(250, easing = FastOutSlowInEasing)) },
      popEnterTransition = { fadeIn(animationSpec = tween(250)) + slideInHorizontally(initialOffsetX = { -200 }, animationSpec = tween(250, easing = FastOutSlowInEasing)) },
      popExitTransition = { fadeOut(animationSpec = tween(250)) + slideOutHorizontally(targetOffsetX = { 200 }, animationSpec = tween(250, easing = FastOutSlowInEasing)) }
    ) {
      composable(Screen.Auth.route) {
        AuthScreen(
          repository = repository,
          onLoginSuccess = {
            navController.navigate(Screen.Dashboard.route) {
              popUpTo(Screen.Auth.route) { inclusive = true }
            }
          }
        )
      }

      composable(Screen.Dashboard.route) {
        when (currentUser.role) {
          UserRole.PRINCIPAL -> PrincipalDashboardScreen(
            repository = repository,
            onNavigateTo = { navController.navigate(it) },
            onShowAddStudent = { showAddStudentDialog = true },
            onShowAddTeacher = { showAddTeacherDialog = true },
            onShowCreateNotice = { showCreateNoticeDialog = true }
          )
          UserRole.TEACHER -> TeacherDashboardScreen(
            repository = repository,
            onNavigateTo = { navController.navigate(it) },
            onShowCreateAssignment = { showCreateAssignmentDialog = true },
            onShowApplyLeave = { showApplyLeaveDialog = true }
          )
          UserRole.STUDENT -> StudentDashboardScreen(
            repository = repository,
            onNavigateTo = { navController.navigate(it) }
          )
          UserRole.PARENT -> StudentDashboardScreen(
            repository = repository,
            onNavigateTo = { navController.navigate(it) }
          )
        }
      }

      // Student Directory & 360 View
      composable(Screen.Students.route) {
        StudentListScreen(
          repository = repository,
          onNavigateToStudentDetail = { studentId ->
            navController.navigate(Screen.StudentDetail.createRoute(studentId))
          },
          onShowAddStudent = { showAddStudentDialog = true }
        )
      }

      composable(
        route = Screen.StudentDetail.route,
        arguments = listOf(navArgument("studentId") { type = NavType.StringType })
      ) { backStackEntry ->
        val studentId = backStackEntry.arguments?.getString("studentId") ?: "stu_1001"
        StudentDetailScreen(
          studentId = studentId,
          repository = repository,
          onBack = { navController.popBackStack() },
          onPayFee = { name, amt ->
            paymentModalStudentName = name
            paymentModalAmount = amt
            showFeePaymentModal = true
          }
        )
      }

      // Faculty Directory & Details
      composable(Screen.Teachers.route) {
        TeacherListScreen(
          repository = repository,
          onNavigateToTeacherDetail = { teacherId ->
            navController.navigate(Screen.TeacherDetail.createRoute(teacherId))
          },
          onShowAddTeacher = { showAddTeacherDialog = true }
        )
      }

      composable(
        route = Screen.TeacherDetail.route,
        arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
      ) { backStackEntry ->
        val teacherId = backStackEntry.arguments?.getString("teacherId") ?: "tch_201"
        TeacherDetailScreen(
          teacherId = teacherId,
          repository = repository,
          onBack = { navController.popBackStack() }
        )
      }

      // Attendance Matrix
      composable(
        route = Screen.Attendance.route,
        arguments = listOf(
          navArgument("classGrade") { type = NavType.StringType; nullable = true },
          navArgument("division") { type = NavType.StringType; nullable = true }
        )
      ) { backStackEntry ->
        val classGrade = backStackEntry.arguments?.getString("classGrade")
        val division = backStackEntry.arguments?.getString("division")
        if (currentUser.role == UserRole.PRINCIPAL) {
          PrincipalAttendanceScreen(
            repository = repository,
            onBack = { navController.popBackStack() }
          )
        } else {
          AttendanceScreen(
            repository = repository,
            initialClassGrade = classGrade,
            initialDivision = division,
            onBack = { navController.popBackStack() }
          )
        }
      }

      // Bursar & Financial Ledger
      composable(Screen.Fees.route) {
        FeeManagementScreen(
          repository = repository,
          onShowPaymentModal = { name, amt ->
            paymentModalStudentName = name
            paymentModalAmount = amt
            showFeePaymentModal = true
          }
        )
      }

      // Academics & Gradebook
      composable(Screen.Timetable.route) {
        TimetableScreen(repository = repository)
      }

      composable(Screen.Homework.route) {
        HomeworkScreen(
          repository = repository,
          onShowCreateAssignment = { showCreateAssignmentDialog = true }
        )
      }

      composable(Screen.StudyMaterial.route) {
        StudyMaterialScreen(repository = repository)
      }

      composable(Screen.ReportCard.route) {
        ReportCardScreen(repository = repository)
      }

      composable(Screen.Exams.route) {
        ExamsScreen(repository = repository)
      }

      // Communication & Audit Trail
      composable(Screen.Notices.route) {
        NoticesScreen(
          repository = repository,
          onShowCreateNotice = { showCreateNoticeDialog = true }
        )
      }

      composable(Screen.Notifications.route) {
        NotificationsScreen(
          repository = repository,
          onBack = { navController.popBackStack() }
        )
      }

      // Operations
      composable(Screen.Leaves.route) {
        LeaveManagementScreen(
          repository = repository,
          onShowApplyLeave = { showApplyLeaveDialog = true }
        )
      }

      composable(Screen.Transport.route) {
        TransportScreen(repository = repository)
      }

      composable(Screen.Library.route) {
        LibraryScreen(repository = repository)
      }

      composable(Screen.Inventory.route) {
        InventoryScreen(repository = repository)
      }

      composable(Screen.OperationsHub.route) {
        OperationsHubScreen(
          repository = repository,
          onNavigateTo = { navController.navigate(it) },
          onShowAddStudent = { showAddStudentDialog = true },
          onShowAddTeacher = { showAddTeacherDialog = true },
          onShowCreateNotice = { showCreateNoticeDialog = true },
          onShowApplyLeave = { showApplyLeaveDialog = true }
        )
      }

      // Reports & AI
      composable(Screen.Reports.route) {
        ReportsAnalyticsScreen(repository = repository)
      }

      composable(Screen.AiAssistant.route) {
        AiAssistantScreen(
          repository = repository,
          onBack = { navController.popBackStack() }
        )
      }

      composable(Screen.GlobalSearch.route) {
        GlobalSearchScreen(
          repository = repository,
          onNavigateTo = { navController.navigate(it) },
          onBack = { navController.popBackStack() }
        )
      }

      composable(Screen.Profile.route) {
        ProfileScreen(
          repository = repository,
          onBack = { navController.popBackStack() },
          onNavigateTo = { navController.navigate(it) }
        )
      }
    }
  }

  // Global Command Palette (⌘K) Modal Overlay
  if (showCommandPalette) {
    GlobalCommandPaletteModal(
      repository = repository,
      onDismiss = { showCommandPalette = false },
      onNavigateTo = { route -> navController.navigate(route) },
      onTriggerAction = { action ->
        when (action) {
          "admit_student" -> showAddStudentDialog = true
          "create_notice" -> showCreateNoticeDialog = true
          "create_assignment" -> showCreateAssignmentDialog = true
        }
      }
    )
  }

  // Modals & Interactive Dialogs
  if (showAddStudentDialog) {
    val studentsList by repository.students.collectAsState()
    AddStudentDialog(
      existingStudentIds = studentsList.map { it.id },
      existingAdmissionNumbers = studentsList.map { it.admissionNumber },
      onDismiss = { showAddStudentDialog = false },
      onAddStudent = { student -> repository.addStudent(student) }
    )
  }

  if (showAddTeacherDialog) {
    val teachersList by repository.teachers.collectAsState()
    AddTeacherDialog(
      existingEmployeeIds = teachersList.map { it.employeeId },
      onDismiss = { showAddTeacherDialog = false },
      onAddTeacher = { teacher -> repository.addTeacher(teacher) }
    )
  }

  if (showCreateNoticeDialog) {
    CreateNoticeDialog(
      onDismiss = { showCreateNoticeDialog = false },
      onPublish = { notice -> repository.publishNotice(notice) }
    )
  }

  if (showCreateAssignmentDialog) {
    CreateAssignmentDialog(
      teacherName = currentUser.name,
      repository = repository,
      onDismiss = { showCreateAssignmentDialog = false },
      onCreate = { hw -> repository.createAssignment(hw) }
    )
  }

  if (showApplyLeaveDialog) {
    ApplyLeaveDialog(
      applicantName = currentUser.name,
      applicantRole = currentUser.role,
      onDismiss = { showApplyLeaveDialog = false },
      onApply = { type, start, end, days, reason ->
        repository.applyLeave(
          applicantName = currentUser.name,
          applicantRole = currentUser.role,
          applicantId = currentUser.id,
          classOrDept = currentUser.associatedClass.ifBlank { "Staff" },
          leaveType = type,
          startDate = start,
          endDate = end,
          daysCount = days,
          reason = reason
        )
      }
    )
  }

  if (showFeePaymentModal) {
    FeePaymentDialog(
      studentName = paymentModalStudentName,
      amountDue = paymentModalAmount,
      isProcessing = isProcessing,
      paymentCompleted = paymentCompleted,
      razorpayPaymentId = razorpayPaymentId,
      onDismiss = {
        showFeePaymentModal = false
        onResetPayment()
      },
      onStartRazorpay = { method ->
        val student = repository.students.value.firstOrNull { it.name == paymentModalStudentName }
        if (student != null) {
          onStartPayment(student.id, paymentModalAmount)
        }
      }
    )
  }
}
