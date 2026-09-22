package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeStatus
import com.example.data.model.LeaveStatus
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun ParentDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onShowPaymentDialog: (studentName: String, amountDue: Long) -> Unit,
  onShowApplyLeave: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()
  val students by repository.students.collectAsState()
  val feeRecords by repository.feeRecords.collectAsState()
  val leaveRequests by repository.leaveRequests.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()
  val notices by repository.notices.collectAsState()

  val children = remember(students, currentUser) {
    students.filter { st ->
      st.parentPhone.replace("+91", "").trim().replace(" ", "") == currentUser.phone.replace("+91", "").trim().replace(" ", "") ||
      st.id == currentUser.associatedStudentId ||
      currentUser.associatedChildNames.any { name -> st.name.contains(name.substringBefore(" (").trim(), ignoreCase = true) }
    }
  }

  val activeChild = remember(children, selectedStudentId) {
    children.firstOrNull { it.id == selectedStudentId }
      ?: children.firstOrNull()
      ?: Student(
        id = "stu_1001",
        admissionNumber = "REV-2026-1001",
        rollNumber = 3,
        name = "Aarav Patel",
        classGrade = "10",
        division = "A",
        dob = "15/08/2009",
        gender = "Male",
        bloodGroup = "A+",
        parentName = currentUser.name,
        parentPhone = currentUser.phone,
        parentEmail = currentUser.email,
        address = "",
        admissionDate = "",
        attendancePercent = 96.4,
        feeStatus = FeeStatus.PENDING,
        feePendingAmount = 1450000L,
        rank = 3,
        gpa = 9.4
      )
  }

  val childFee = feeRecords.firstOrNull { it.studentId == activeChild.id }
  val childPendingFee = childFee?.pendingAmount ?: activeChild.feePendingAmount

  // Dynamic today's attendance check
  val childTodayAttendance = remember(classAttendanceMap, activeChild.id) {
    repository.getStudentTodayAttendance(activeChild.id)
  }

  val isPresent = childTodayAttendance == AttendanceStatus.PRESENT || childTodayAttendance == AttendanceStatus.LATE
  val attendanceLabel = childTodayAttendance.label
  val attendanceSublabel = when (childTodayAttendance) {
    AttendanceStatus.PRESENT -> "Recorded in Morning Register"
    AttendanceStatus.ABSENT -> "Reported Absent by Class Teacher"
    AttendanceStatus.LATE -> "Marked Late Entry (08:45 AM)"
    AttendanceStatus.EXCUSED -> "Officially Excused Absence"
    AttendanceStatus.LEAVE -> "Approved Medical Leave"
  }

  val childLeaves = leaveRequests.filter { it.applicantName.contains(activeChild.name, ignoreCase = true) || it.applicantId == activeChild.id }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("parent_dashboard_list"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Parent Header & Multi-Child Switcher
    item {
      AnimatedFadeIn(delayMillis = 0) {
        val parentShape = RoundedCornerShape(Radius.hero)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(parentShape)
            .background(ScholaSlateNavyDark, parentShape)
            .drawBehind {
              drawRect(
                brush = Brush.linearGradient(
                  colors = listOf(
                    Color(0xFF261D18),
                    ScholaSlateNavy,
                    InkBlack
                  ),
                  start = Offset(0f, 0f),
                  end = Offset(size.width, size.height)
                )
              )
              val gw = size.width
              val glowCenter = Offset(gw * 0.85f, size.height * 0.20f)
              val glowRadius = gw * 0.9f
              drawCircle(
                brush = Brush.radialGradient(
                  0f to ScholaTerracotta.copy(alpha = 0.12f),
                  0.5f to ScholaTerracotta.copy(alpha = 0.03f),
                  1f to Color.Transparent,
                  center = glowCenter,
                  radius = glowRadius
                ),
                radius = glowRadius,
                center = glowCenter
              )
              drawRoundRect(
                brush = Brush.horizontalGradient(
                  colors = listOf(
                    ScholaTerracotta,
                    ScholaTerracottaLight.copy(alpha = 0.6f),
                    Color.Transparent
                  )
                ),
                topLeft = Offset(0f, 0f),
                size = Size(gw * 0.75f, 2.5f * density),
                cornerRadius = CornerRadius(1.5f * density, 1.5f * density)
              )
            }
            .border(
              width = 1.dp,
              brush = Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.24f),
                0.5f to Color.White.copy(alpha = 0.08f),
                1f to Color.White.copy(alpha = 0.14f)
              ),
              shape = parentShape
            )
            .shadow(16.dp, parentShape, clip = false)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(Spacing.cardPaddingLarge)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(5.dp)
                      .background(Color(0xFF10B981), CircleShape)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "PARENT DESK",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.1.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = currentUser.name,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
              Surface(
                shape = RoundedCornerShape(Radius.pill),
                color = ScholaTerracotta.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, ScholaTerracotta.copy(alpha = 0.4f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.FamilyRestroom,
                    contentDescription = null,
                    tint = ScholaTerracottaLight,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "GUARDIAN",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color.White
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "SELECT WARD / SCHOLAR",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              letterSpacing = 1.0.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White.copy(alpha = 0.70f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              children.forEach { child ->
                val isSelected = child.id == activeChild.id
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = if (isSelected) ScholaTerracotta.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                  border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) ScholaTerracotta else Color.White.copy(alpha = 0.14f)
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { repository.setSelectedStudentId(child.id) }
                    .testTag("child_selector_${child.name.lowercase().replace(" ", "_")}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .background(
                          if (isSelected) ScholaTerracotta else Color.White.copy(alpha = 0.15f),
                          CircleShape
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = child.name.take(1),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = child.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                      Text(
                        text = "Class ${child.fullClass}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (isSelected) ScholaTerracottaLight else Color.White.copy(alpha = 0.70f)
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Child Live Status Pill
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White.copy(alpha = 0.06f),
              border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.10f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .background(if (isPresent) Color(0xFF10B981) else Color(0xFFF59E0B), CircleShape)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Today: $attendanceLabel",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                  )
                }
                Text(
                  text = if (childPendingFee > 0L) "Dues: ₹${childPendingFee / 100}" else "Fees: Cleared",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (childPendingFee > 0L) Color(0xFFFBBF24) else Color(0xFF34D399)
                )
              }
            }
          }
        }
      }
    }

    // Fee Dues Reminder Card (If Pending)
    if (childPendingFee > 0L) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = StatusWarningContainer)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Term 2 Fee Dues Pending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StatusWarningText
              )
              Text(
                text = "Amount: ₹${childPendingFee / 100} • Due by 15th Sep 2026",
                style = MaterialTheme.typography.bodySmall,
                color = StatusWarningText.copy(alpha = 0.9f)
              )
            }
            Button(
              onClick = { onShowPaymentDialog(activeChild.name, childPendingFee) },
              colors = ButtonDefaults.buttonColors(containerColor = ScholaTerracotta),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
              modifier = Modifier.testTag("pay_fees_quick_button")
            ) {
              Text("Pay Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
      }
    }

    // Parent Quick Actions
    item {
      SectionHeader(title = "Parent Self-Service")
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          QuickActionButton(
            title = "Fee Ledger",
            icon = Icons.Default.AccountBalanceWallet,
            iconTint = ScholaSlateNavy,
            backgroundColor = ScholaSlateContainer,
            modifier = Modifier.width(115.dp),
            onClick = { onNavigateTo(Screen.Fees.route) }
          )
        }
        item {
          QuickActionButton(
            title = "Apply Leave",
            icon = Icons.Default.EventBusy,
            iconTint = ScholaSlateNavy,
            backgroundColor = ScholaSlateContainer,
            modifier = Modifier.width(115.dp),
            onClick = onShowApplyLeave
          )
        }
        item {
          QuickActionButton(
            title = "Report Card",
            icon = Icons.Default.Grade,
            iconTint = ScholaSlateNavy,
            backgroundColor = ScholaSlateContainer,
            modifier = Modifier.width(115.dp),
            onClick = { onNavigateTo(Screen.ReportCard.route) }
          )
        }
        item {
          QuickActionButton(
            title = "Bus Tracking",
            icon = Icons.Default.DirectionsBus,
            iconTint = ScholaTerracotta,
            backgroundColor = ScholaTerracottaContainer,
            modifier = Modifier.width(115.dp),
            onClick = { onNavigateTo(Screen.Transport.route) }
          )
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // Live Child Status & Attendance (Dynamically linked)
    item {
      SectionHeader(title = "${activeChild.name}'s Daily Status")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        StatCard(
          title = "Today's Attendance",
          value = attendanceLabel,
          sublabel = attendanceSublabel,
          icon = if (isPresent) Icons.Default.CheckCircle else Icons.Default.Cancel,
          iconTint = if (isPresent) StatusSuccess else StatusError,
          iconBackground = if (isPresent) StatusSuccessContainer else StatusErrorContainer,
          trendText = "${activeChild.attendancePercent}% Term",
          isPositiveTrend = isPresent,
          modifier = Modifier.weight(1f)
        )
        StatCard(
          title = "Class Rank",
          value = "#${activeChild.rank}",
          sublabel = "Top 10% in Class ${activeChild.fullClass}",
          icon = Icons.Default.MilitaryTech,
          iconTint = RevenexGold,
          iconBackground = RevenexGoldContainer,
          trendText = "GPA ${activeChild.gpa}",
          isPositiveTrend = true,
          modifier = Modifier.weight(1f)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Leave Requests Status Card (If any applied)
    if (childLeaves.isNotEmpty()) {
      item {
        SectionHeader(title = "Recent Leave Applications")
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          childLeaves.forEach { leave ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${leave.leaveType} (${leave.daysCount} Days)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  StatusBadge(status = leave.status.label, type = "leave")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Dates: ${leave.startDate} to ${leave.endDate}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (leave.approverRemark.isNotBlank()) {
                  Text(
                    text = "Remarks: ${leave.approverRemark}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (leave.status == LeaveStatus.APPROVED) StatusSuccess else StatusErrorText
                  )
                }
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // Class Teacher Quick Connect
    item {
      SectionHeader(title = "Class Teacher Contact")
      AppCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = RevenexPrimaryContainer,
            modifier = Modifier.size(48.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.School, contentDescription = null, tint = RevenexBlue)
            }
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (activeChild.classGrade == "10") "Prof. Sunita Rao" else "Mrs. Shweta Kadam",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Class Teacher (${activeChild.fullClass}) • Mon-Fri 2-4 PM",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(
            onClick = { /* Teacher connect */ },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
          ) {
            Icon(Icons.Default.Call, contentDescription = "Call Teacher", tint = MaterialTheme.colorScheme.primary)
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Recent Notices
    item {
      SectionHeader(
        title = "School Circulars for Parents",
        actionText = "View All",
        onActionClick = { onNavigateTo(Screen.Notices.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        notices.take(2).forEach { notice ->
          AppCard(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateTo(Screen.Notices.route) }
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Text(
                text = notice.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = notice.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
              )
            }
          }
        }
      }
    }
  }
}
