package com.example

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.example.data.datasource.AppEnvironment
import com.example.data.datasource.DataMode
import com.example.data.datasource.RazorpayCheckoutOrder
import com.example.data.datasource.RazorpayPaymentResult
import com.example.data.datasource.RazorpayPayments
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.razorpay.Checkout
import com.razorpay.PaymentResultWithDataListener
import com.razorpay.PaymentData
import org.json.JSONObject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
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
import com.example.ui.screens.dashboard.AdminDashboardScreen
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
import com.example.ui.screens.teachers.TeacherProfileScreen
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
  companion object {
    var activeAmount by mutableStateOf(0L)
    var isPaymentProcessing by mutableStateOf(false)
    var isPaymentCompleted by mutableStateOf(false)
    var razorpayPaymentId by mutableStateOf("")
    var paymentReceipt by mutableStateOf("")
    var paymentStatusMessage by mutableStateOf("")
    private var activeFeeId: String? = null
    private var activePayerId: String? = null
    private var activeOrder: RazorpayCheckoutOrder? = null
  }
  private val repository get() = ErpDataRepository.getInstance()
  override fun onCreate(savedInstanceState: Bundle?) {
    val appearance = Appearance.parse(getSharedPreferences("revenex_appearance", MODE_PRIVATE).getString("mode", null))
    val systemDark = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
    setTheme(if (appearance.usesDark(systemDark)) R.style.Theme_Revenex_Dark else R.style.Theme_MyApplication)
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ScholaTheme {
        ScholaErpApp(
          isProcessing=isPaymentProcessing,paymentCompleted=isPaymentCompleted,razorpayPaymentId=razorpayPaymentId,
          onStartPayment={ feeId, _ -> startRazorpayPayment(feeId) },
          onResetPayment={ if(!isPaymentProcessing) {
            isPaymentCompleted=false;razorpayPaymentId="";activeAmount=0L;paymentReceipt="";paymentStatusMessage=""
            activeFeeId=null;activePayerId=null;activeOrder=null
          } },
          verifiedPaymentAmount=activeAmount,verifiedReceipt=paymentReceipt,paymentStatus=paymentStatusMessage,
          onCheckPayment={ feeId -> checkPaymentStatus(feeId) }
        )
      }
    }
  }
  private fun sameAccount() = AppEnvironment.currentMode==DataMode.SUPABASE_DEMO &&
    activePayerId==repository.currentUser.value.id
  private fun paymentFailure(message:String) {
    isPaymentProcessing=false;isPaymentCompleted=false;paymentStatusMessage=message
    android.widget.Toast.makeText(this,message,android.widget.Toast.LENGTH_LONG).show()
  }
  private fun startRazorpayPayment(feeId:String) {
    if(isPaymentProcessing)return
    if(AppEnvironment.currentMode!=DataMode.SUPABASE_DEMO) {
      paymentFailure("Use Supabase Demo for Razorpay test checkout. Local demo payments remain simulated.");return
    }
    activeFeeId=feeId;activePayerId=repository.currentUser.value.id;activeOrder=null
    isPaymentProcessing=true;isPaymentCompleted=false;paymentReceipt="";razorpayPaymentId=""
    paymentStatusMessage="Creating a server-verified test order…"
    lifecycleScope.launch {
      try {
        val order=RazorpayPayments.create(feeId)
        check(sameAccount()) { "Account changed. Reopen the fee record." }
        activeOrder=order;activeAmount=order.amount
        val options=JSONObject().apply {
          put("name","Revenex School ERP");put("description","School fees • TEST MODE")
          put("order_id",order.order_id);put("currency",order.currency);put("amount",order.amount)
          put("theme.color",java.lang.String.format(java.util.Locale.US,"#%06X",ScholaTerracotta.toArgb() and 0xFFFFFF))
          put("prefill",JSONObject().apply { put("email",repository.currentUser.value.email) })
        }
        Checkout.preload(applicationContext)
        val checkout=Checkout();checkout.setKeyID(order.key_id)
        paymentStatusMessage="Complete the test payment in secure checkout."
        checkout.open(this@MainActivity,options)
      } catch(e:Exception) {
        if(e is CancellationException)throw e
        paymentFailure("Test checkout is unavailable. Check Supabase payment deployment and server secrets, then retry.")
      }
    }
  }
  override fun onPaymentSuccess(paymentId:String?,paymentData:PaymentData?) {
    val order=activeOrder
    if(order==null || !sameAccount() || paymentId.isNullOrBlank() || paymentData==null) {
      paymentFailure("Payment callback could not be matched. Use Check payment status in Fees.");return
    }
    isPaymentProcessing=true;paymentStatusMessage="Verifying capture with the school payment server…"
    lifecycleScope.launch {
      try {
        applyPaymentResult(RazorpayPayments.verify(order,paymentId,paymentData.orderId.orEmpty(),paymentData.signature.orEmpty()))
      } catch(e:Exception) {
        if(e is CancellationException)throw e
        paymentFailure("Payment is not confirmed yet. Use Check payment status before retrying checkout.")
      } finally { isPaymentProcessing=false }
    }
  }
  private fun checkPaymentStatus(feeId:String) {
    if(isPaymentProcessing)return
    if(AppEnvironment.currentMode!=DataMode.SUPABASE_DEMO) {paymentFailure("Sign in to Supabase Demo to check a test payment.");return}
    activeFeeId=feeId;activePayerId=repository.currentUser.value.id;isPaymentProcessing=true
    lifecycleScope.launch {
      try {
        val result=RazorpayPayments.checkFee(feeId)
        if(result==null) paymentFailure("No test order exists for this fee on your account.") else {
          applyPaymentResult(result)
          android.widget.Toast.makeText(this@MainActivity,if(result.verifiedPaid) "Test payment verified. Ledger refreshed." else if(result.status=="failed") "Payment failed. Fee remains pending; school inbox updated." else "No captured payment yet.",android.widget.Toast.LENGTH_LONG).show()
        }
      } catch(e:Exception) {
        if(e is CancellationException)throw e
        paymentFailure("Payment status could not be checked. Verify sign-in, server setup and network.")
      } finally { isPaymentProcessing=false }
    }
  }
  private suspend fun applyPaymentResult(result:RazorpayPaymentResult) {
    check(sameAccount()) { "Account changed." }
    if(result.verifiedPaid) {
      activeAmount=result.amount;razorpayPaymentId=result.payment_id;paymentReceipt=result.receipt_no
      isPaymentCompleted=true;paymentStatusMessage="Captured and verified by the school payment server."
      try { repository.supabaseDataSource.refreshVerifiedPaymentData() }
      catch(e:Exception) {if(e is CancellationException)throw e;paymentStatusMessage="Payment verified. Reopen Fees to refresh the ledger."}
    } else {
      check(result.status in setOf("pending","failed")) { "Invalid payment confirmation." }
      isPaymentCompleted=false
      try { repository.supabaseDataSource.refreshVerifiedPaymentData() } catch(e:Exception) { if(e is CancellationException) throw e }
      paymentStatusMessage=result.message.take(200).ifBlank { "Payment is pending. Check status again." }
    }
  }
  override fun onPaymentError(code:Int,response:String?,paymentData:PaymentData?) {
    paymentFailure("Checkout was cancelled or failed. Checking provider status; no fee credit was assumed.")
    activeFeeId?.let { checkPaymentStatus(it) }
  }
}

@Composable
fun ScholaErpApp(
  isProcessing: Boolean,
  paymentCompleted: Boolean,
  razorpayPaymentId: String,
  onStartPayment: (feeRecordId: String, amount: Long) -> Unit,
  onResetPayment: () -> Unit,
  verifiedPaymentAmount: Long = 0L,
  verifiedReceipt: String = "",
  paymentStatus: String = "",
  onCheckPayment: (String) -> Unit = {}
) {
  val repository = remember { ErpDataRepository.getInstance() }
  val navController = rememberNavController()

  val currentUser by repository.currentUser.collectAsState()
  val notifications by repository.notifications.collectAsState()
  val backendState by repository.backendState.collectAsState()
  val dataMode by repository.dataMode.collectAsState()
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
  var paymentModalFeeId by remember { mutableStateOf("") }

  LaunchedEffect(currentUser.id, dataMode) {
    showCommandPalette = false
    showAddStudentDialog = false; showAddTeacherDialog = false
    showCreateNoticeDialog = false; showCreateAssignmentDialog = false; showApplyLeaveDialog = false
    showFeePaymentModal = false
    paymentModalStudentName = ""; paymentModalAmount = 0L; paymentModalFeeId = ""
  }

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
    onOpenProfile = {
      val route = Screen.Profile.route
      navController.navigate(route)
    },
    onLogout = {
      repository.logout()
      navController.navigate(Screen.Auth.route) {
        popUpTo(navController.graph.id) { inclusive = true }
      }
    }
  ) { paddingValues ->
    Column(Modifier.fillMaxSize().padding(paddingValues)) {
    if (dataMode == com.example.data.datasource.DataMode.SUPABASE_DEMO && backendState.message.isNotBlank()) {
      Surface(color=if(backendState.failed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
          if(backendState.busy) { CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp);Spacer(Modifier.width(8.dp)) }
          Text(backendState.message,style=MaterialTheme.typography.bodySmall)
        }
      }
    }
    key(currentUser.id, dataMode) {
    NavHost(
      navController = navController,
      startDestination = Screen.Auth.route,
      modifier = Modifier.weight(1f),
      enterTransition = {
        val fromAuth = initialState.destination.route == Screen.Auth.route
        val isLoadingOrDashboard = targetState.destination.route == Screen.Loading.route ||
          targetState.destination.route == Screen.Dashboard.route
        when {
          fromAuth && isLoadingOrDashboard -> fadeIn(animationSpec = tween(220))
          initialState.destination.route == Screen.Loading.route &&
            targetState.destination.route == Screen.Dashboard.route ->
            fadeIn(animationSpec = tween(180))
          else -> slideInHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing)) { it / 6 } +
            fadeIn(animationSpec = tween(150))
        }
      },
      exitTransition = {
        val fromAuth = initialState.destination.route == Screen.Auth.route
        val isLoadingOrDashboard = targetState.destination.route == Screen.Loading.route ||
          targetState.destination.route == Screen.Dashboard.route
        when {
          fromAuth && isLoadingOrDashboard -> fadeOut(animationSpec = tween(220))
          initialState.destination.route == Screen.Loading.route &&
            targetState.destination.route == Screen.Dashboard.route ->
            fadeOut(animationSpec = tween(180))
          else -> slideOutHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing)) { -it / 6 } +
            fadeOut(animationSpec = tween(150))
        }
      },
      popEnterTransition = {
        slideInHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing)) { -it / 6 } +
          fadeIn(animationSpec = tween(150))
      },
      popExitTransition = {
        slideOutHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing)) { it / 6 } +
          fadeOut(animationSpec = tween(150))
      }
    ) {
      composable(Screen.Auth.route) {
        AuthScreen(
          repository = repository,
          onLoginSuccess = {
            navController.navigate(Screen.Loading.route) {
              popUpTo(Screen.Auth.route) { inclusive = true }
            }
          }
        )
      }

      composable(Screen.Loading.route) {
        LoadingScreen(
          onReady = {
            if (navController.currentBackStackEntry?.destination?.route == Screen.Loading.route) {
              navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Loading.route) { inclusive = true }
              }
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
          UserRole.ADMIN -> AdminDashboardScreen(
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
            if(dataMode!=DataMode.SUPABASE_DEMO) {
              repository.setDataMode(DataMode.SUPABASE_DEMO);navController.navigate(Screen.Auth.route)
            } else {
              val feeId=repository.feeRecords.value.filter { it.studentId==studentId && it.pendingAmount==amt }.singleOrNull()?.id
              if(feeId==null) navController.navigate(Screen.Fees.route) else {
                onResetPayment();paymentModalFeeId=feeId;paymentModalStudentName=name;paymentModalAmount=amt;showFeePaymentModal=true
              }
            }
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
        if (currentUser.role == UserRole.PRINCIPAL || currentUser.role == UserRole.ADMIN) {
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
          },
          onPayRazorpay = { feeId, name, amt ->
            if(dataMode!=DataMode.SUPABASE_DEMO) {
              repository.setDataMode(DataMode.SUPABASE_DEMO);navController.navigate(Screen.Auth.route)
            } else {
              onResetPayment();paymentModalFeeId=feeId;paymentModalStudentName=name;paymentModalAmount=amt
              showFeePaymentModal=true
            }
          },
          onCheckRazorpay = onCheckPayment
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

      composable(Screen.TeacherProfile.route) {
        TeacherProfileScreen(repository = repository)
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
          onBack = { navController.popBackStack() },
          onNavigateTo = { navController.navigate(it) { launchSingleTop = true } }
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
          onNavigateTo = { route -> navController.navigate(route) {
            if (route == Screen.Auth.route) popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
          } }
        )
      }
    }
    } // account-scoped navigation content
    } // cloud sync status column
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
      onAddStudent = { student -> repository.addStudent(student) },
      onSubmitStudent = { student, fee -> repository.enrollStudent(student,fee) }
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
      onPublish = { notice -> repository.publishNotice(notice) },
      onSubmitNotice = { repository.publishNoticeConfirmed(it) }
    )
  }

  if (showCreateAssignmentDialog) {
    CreateAssignmentDialog(
      teacherName = currentUser.name,
      repository = repository,
      onDismiss = { showCreateAssignmentDialog = false },
      onCreate = { hw -> repository.createAssignment(hw) },
      onSubmitAssignment = { repository.createAssignmentConfirmed(it) }
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
      amountDue = verifiedPaymentAmount.takeIf { it>0L } ?: paymentModalAmount,
      isProcessing = isProcessing,
      paymentCompleted = paymentCompleted,
      razorpayPaymentId = razorpayPaymentId,
      receiptNo = verifiedReceipt,
      statusMessage = paymentStatus,
      onCheckStatus = { onCheckPayment(paymentModalFeeId) },
      onDismiss = {
        showFeePaymentModal = false
        onResetPayment()
      },
      onStartRazorpay = { _ ->
        if(paymentModalFeeId.isNotBlank()) onStartPayment(paymentModalFeeId,paymentModalAmount)
      }
    )
  }
}

// Branded loading screen shown after role selection so the dashboard's first
// composition settles before it appears (masks the initial jank).
@Composable
private fun LoadingScreen(onReady: () -> Unit) {
  LaunchedEffect(Unit) {
    onReady()
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CircularProgressIndicator(
        modifier = Modifier.size(44.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        strokeWidth = 4.dp
      )
      Spacer(modifier = Modifier.height(Spacing.s3))
      Text(
        text = "Preparing your dashboard…",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
