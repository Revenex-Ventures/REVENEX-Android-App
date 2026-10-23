package com.example.ui.screens.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaveStatus
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun OperationsHubScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onShowAddStudent: () -> Unit,
  onShowAddTeacher: () -> Unit,
  onShowCreateNotice: () -> Unit,
  onShowApplyLeave: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()

  data class HubModule(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val bgColor: Color,
    val route: String,
    val allowedRoles: Set<UserRole> = UserRole.values().toSet()
  )

  val allRoles = UserRole.values().toSet()
  val adminOnly = setOf(UserRole.PRINCIPAL, UserRole.ADMIN)
  val staffOnly = setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER)
  val noStudents = setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)

  val modules = listOf(
    HubModule("Assignments", "Class work and submissions", Icons.Default.Assignment, SemanticColors.onLavender, SemanticColors.lavenderContainer, Screen.Homework.route, allRoles),
    HubModule("Examinations", "Exam dates and schedules", Icons.Default.EventNote, SemanticColors.onLavender, SemanticColors.lavenderContainer, Screen.Exams.route, allRoles),
    HubModule("Results", "Available student reports", Icons.Default.Assessment, SemanticColors.onLavender, SemanticColors.lavenderContainer, Screen.ReportCard.route, allRoles),
    HubModule("Settings & profile", "Appearance, security and account", Icons.Default.Settings, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer, Screen.Profile.route, allRoles),
    HubModule("My faculty profile", "Personal and professional details", Icons.Default.Person, MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.secondaryContainer, Screen.TeacherProfile.route, setOf(UserRole.TEACHER)),
    HubModule("Student Directory", "Admissions and student details", Icons.Default.Groups, SemanticColors.onPeach, SemanticColors.peachContainer, Screen.Students.route, adminOnly),
    HubModule("Faculty & Staff", "Teacher Workloads & Departments", Icons.Default.School, SemanticColors.onPeach, SemanticColors.peachContainer, Screen.Teachers.route, adminOnly),
    HubModule("Daily Attendance", "Mark and Review Registers", Icons.Default.FactCheck, MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.secondaryContainer, Screen.Attendance.route, allRoles),
    HubModule("Fee Accounts", "Dues, Ledger & Receipts", Icons.Default.AccountBalanceWallet, SemanticColors.onPeach, SemanticColors.peachContainer, Screen.Fees.route, setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.PARENT, UserRole.STUDENT)),
    HubModule("Leave Management", "Staff & Student Absences", Icons.Default.EventBusy, MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.secondaryContainer, Screen.Leaves.route, noStudents),
    HubModule("Transport & Bus", "Routes, Stops & Drivers", Icons.Default.DirectionsBus, SemanticColors.onPeach, SemanticColors.peachContainer, Screen.Transport.route, allRoles),
    HubModule("Digital Library", "Book Catalog, Issue & Return", Icons.Default.LocalLibrary, SemanticColors.onLavender, SemanticColors.lavenderContainer, Screen.Library.route, allRoles),
    HubModule("School Assets", "Smart Boards, Labs & IT", Icons.Default.Inventory, SemanticColors.onPeach, SemanticColors.peachContainer, Screen.Inventory.route, adminOnly),
    HubModule("Executive Reports", "KPI Charts & Export Analytics", Icons.Default.Analytics, MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.secondaryContainer, Screen.Reports.route, adminOnly),
    HubModule("Timetable", "Class Schedule & Periods", Icons.Default.CalendarMonth, SemanticColors.onLavender, SemanticColors.lavenderContainer, Screen.Timetable.route, allRoles),
    HubModule("Notices & Circulars", "School Announcements", Icons.Default.Campaign, MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.secondaryContainer, Screen.Notices.route, allRoles),
    HubModule("Study Material", "Notes, PDFs & Resources", Icons.Default.MenuBook, SemanticColors.onLavender, SemanticColors.lavenderContainer, Screen.StudyMaterial.route, allRoles),
    HubModule("AI Assistant", "Smart School Q&A & Search", Icons.Default.AutoAwesome, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer, Screen.AiAssistant.route, allRoles)
  ).filter { it.allowedRoles.contains(currentUser.role) }

  val moduleColumns = if (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 840) 3 else if (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600) 2 else 1
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("operations_hub_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Text(
            text = "School workspaces",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Your role’s tools, grouped in one place",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
          )
        }
      }
    }

    item {
      Text(
        text = "Available modules",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    items(modules.chunked(moduleColumns)) { pair ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        pair.forEach { mod ->
          Card(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .clickable { onNavigateTo(mod.route) }
              .testTag("module_card_${mod.title.lowercase().replace(" ", "_")}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Row(Modifier.fillMaxWidth().padding(Spacing.md12),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
              Box(Modifier.size(40.dp).background(mod.bgColor, RoundedCornerShape(Radius.sm)), contentAlignment = Alignment.Center) {
                Icon(mod.icon, null, tint = mod.iconTint, modifier = Modifier.size(22.dp))
              }
              Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs4)) {
                Text(mod.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(mod.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
          }
        }
        if (pair.size < moduleColumns) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
fun LeaveManagementScreen(
  repository: ErpDataRepository,
  onShowApplyLeave: () -> Unit
) {
  val leaveRequests by repository.leaveRequests.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  var selectedTab by remember { mutableStateOf(0) }

  val pendingLeaves = leaveRequests.filter { it.status == LeaveStatus.PENDING }
  val historyLeaves = leaveRequests.filter { it.status != LeaveStatus.PENDING }

  Scaffold(
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onShowApplyLeave,
        icon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
        text = { Text("Apply Leave", fontWeight = FontWeight.Bold) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_apply_leave")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("leave_management_screen"),
      contentPadding = PaddingValues(bottom = Spacing.xl24)
    ) {
      item {
        TabRow(
          selectedTabIndex = selectedTab,
          modifier = Modifier.fillMaxWidth()
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Pending (${pendingLeaves.size})", fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Processed History (${historyLeaves.size})", fontWeight = FontWeight.Bold) }
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      val displayList = if (selectedTab == 0) pendingLeaves else historyLeaves

      if (displayList.isEmpty()) {
        item {
          EmptyStateView(
            icon = Icons.Default.EventAvailable,
            title = if (selectedTab == 0) "No Pending Requests" else "No Processed History",
            description = if (selectedTab == 0) "All student and staff leaves have been processed." else "No historical leave applications recorded."
          )
        }
      } else {
        items(displayList, key = { it.id }) { leave ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = leave.applicantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${leave.classOrDept} • ${leave.applicantRole.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                StatusBadge(status = leave.status.label, type = "leave")
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "${leave.leaveType}: ${leave.startDate} to ${leave.endDate} (${leave.daysCount} Day(s))",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = "\"${leave.reason}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              if (leave.approverRemark.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Remark: ${leave.approverRemark}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                  )
                }
              }

              if (leave.status == LeaveStatus.PENDING && (currentUser.role == UserRole.PRINCIPAL || currentUser.role == UserRole.ADMIN)) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  OutlinedButton(
                    onClick = { repository.rejectLeave(leave.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                  ) {
                    Text("Reject")
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Button(
                    onClick = { repository.approveLeave(leave.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                  ) {
                    Text("Approve Leave")
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun TransportScreen(
  repository: ErpDataRepository
) {
  val routes by repository.transportRoutes.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("transport_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "School Transport",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${routes.size} routes • Scheduled stops and driver contacts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StatusSuccess
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ROUTES", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    items(routes, key = { it.id }) { route ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = route.routeNumber,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Text(
              text = "Bus: ${route.vehicleNumber}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = route.routeName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Driver: ${route.driverName} (${route.driverPhone})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Route Stops & Timings:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(4.dp))

          route.stops.forEach { stop ->
            Row(
              modifier = Modifier.padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(modifier = Modifier.size(6.dp).background(RevenexBlue, CircleShape))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = stop, style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
    }
  }
}

@Composable
fun LibraryScreen(
  repository: ErpDataRepository
) {
  val books by repository.libraryBooks.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("library_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(books, key = { it.id }) { book ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Book,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = book.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "By ${book.author}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${book.shelfLocation} • Available: ${book.availableCopies} / ${book.totalCopies}",
              style = MaterialTheme.typography.labelSmall,
              color = if (book.availableCopies > 0) SemanticColors.onSuccess else SemanticColors.onDanger,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          if (book.availableCopies > 0) {
            OutlinedButton(
              onClick = { repository.issueBook(book.id) },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
              Text("Issue", fontSize = 12.sp)
            }
          } else {
            Button(
              onClick = { repository.returnBook(book.id) },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
              Text("Return", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun InventoryScreen(
  repository: ErpDataRepository
) {
  val inventory by repository.inventory.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("inventory_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(inventory, key = { it.id }) { item ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = item.itemName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.weight(1f)
            )
            StatusBadge(status = item.condition)
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Category: ${item.category} • Location: ${item.location}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Units: ${item.quantity}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Estimated Asset Value: ₹${item.estimatedValue.toInt()}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
