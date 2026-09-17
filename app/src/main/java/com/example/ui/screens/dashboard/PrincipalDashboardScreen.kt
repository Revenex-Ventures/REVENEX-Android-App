package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun PrincipalDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onShowAddStudent: () -> Unit,
  onShowAddTeacher: () -> Unit,
  onShowCreateNotice: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val invoices by repository.invoices.collectAsState()
  val auditLogs by repository.auditLogs.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()
  val notices by repository.notices.collectAsState()
  val examSchedules by repository.examSchedules.collectAsState()

  val totalStudents = students.size
  val totalFaculty = teachers.size

  val totalBilled = invoices.sumOf { it.totalAmount }
  val totalCollected = invoices.sumOf { it.paidAmount }
  val totalReceivables = invoices.sumOf { it.pendingAmount }
  val overdueInvoices = invoices.filter { it.status == FeeStatus.OVERDUE }

  val overallAttendance = remember(classAttendanceMap, students) {
    repository.getOverallSchoolAttendanceRate()
  }
  val formattedAttendance = String.format(Locale.US, "%.1f", overallAttendance)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(GlassBgTransparent)
      .testTag("principal_dashboard_list"),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = 100.dp)
  ) {
    // 1. OPERATIONS HERO CARD (Dark Onyx Surface, 22dp corners, 1px border)
    item {
      AnimatedFadeIn(delayMillis = 0) {
        FeatureHeroCard(
          cycleTitle = "Academic Cycle 2026-2027 • Executive Desk",
          categoryBadge = "Q3 Operations Active",
          primaryStatistic = "$${String.format(Locale.US, "%,.1f", (totalCollected / 100000.0))}k",
          statisticLabel = "Net Tuition Revenue Realized",
          secondaryKeyMetric = "$formattedAttendance%",
          secondaryMetricLabel = "Daily Roll Call Rate",
          actionButtonText = "Review Institutional Ledger",
          onActionClick = { onNavigateTo(Screen.Fees.route) },
          badgeBgColor = ScholaTerracottaContainer,
          badgeTextColor = ScholaOnTerracottaContainer,
          heroHighlightColor = ScholaTerracotta,
          modifier = Modifier.padding(bottom = Spacing.s4)
        )
      }
    }

    // 2. HORIZONTAL WORKFLOW PILLS
    item {
      AnimatedFadeIn(delayMillis = 100) {
        Column(modifier = Modifier.padding(bottom = Spacing.s4)) {
          SectionHeader(title = "Workflow Shortcuts")
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
          ) {
            item {
              WorkflowQuickPill(
                title = "Command Palette",
                icon = Icons.Default.Terminal,
                onClick = { onNavigateTo(Screen.GlobalSearch.route) },
                isHighlighted = true
              )
            }
            item {
              WorkflowQuickPill(
                title = "Take Roll Call",
                icon = Icons.Default.FactCheck,
                onClick = { onNavigateTo(Screen.Attendance.route) }
              )
            }
            item {
              WorkflowQuickPill(
                title = "Enroll Scholar",
                icon = Icons.Default.PersonAdd,
                onClick = onShowAddStudent
              )
            }
            item {
              WorkflowQuickPill(
                title = "View Ledger",
                icon = Icons.Default.AccountBalanceWallet,
                onClick = { onNavigateTo(Screen.Fees.route) }
              )
            }
            item {
              WorkflowQuickPill(
                title = "Broadcast Alert",
                icon = Icons.Default.Campaign,
                onClick = onShowCreateNotice
              )
            }
          }
        }
      }
    }

    // 3. 2x2 GRID OF COMPACT STAT TILES
    item {
      AnimatedFadeIn(delayMillis = 200) {
        Column(modifier = Modifier.padding(bottom = Spacing.s4)) {
          SectionHeader(title = "Operational Health Matrix")

          // Row 1: Active Scholars & Daily Attendance
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
          ) {
            CompactStatTile(
              categoryLabel = "Active Scholars",
              statCounter = "1,420",
              icon = Icons.Default.Groups,
              iconTint = ScholaTerracotta,
              iconContainerColor = ScholaTerracottaContainer,
              deltaText = "+4.2% YoY",
              isPositiveDelta = true,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Students.route) }
            )

            CompactStatTile(
              categoryLabel = "Daily Attendance",
              statCounter = "$formattedAttendance%",
              icon = Icons.Default.HowToReg,
              iconTint = Color(0xFF15803D),
              iconContainerColor = StatusSuccessBg,
              deltaText = "96.8% Target",
              isPositiveDelta = true,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Attendance.route) }
            )
          }

          Spacer(modifier = Modifier.height(Spacing.s3))

          // Row 2: Outstanding Receivables & Institutional GPA
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
          ) {
            CompactStatTile(
              categoryLabel = "Receivables Due",
              statCounter = "$${totalReceivables / 100000L}k",
              icon = Icons.Default.ReceiptLong,
              iconTint = StatusDangerText,
              iconContainerColor = StatusDangerBg,
              deltaText = "${overdueInvoices.size} Overdue",
              isPositiveDelta = false,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.Fees.route) }
            )

            CompactStatTile(
              categoryLabel = "Institutional GPA",
              statCounter = "3.84 / 4.0",
              icon = Icons.Default.Grade,
              iconTint = ScholaGold,
              iconContainerColor = ScholaGoldContainer,
              deltaText = "Top 5% Nat'l",
              isPositiveDelta = true,
              modifier = Modifier.weight(1f),
              onClick = { onNavigateTo(Screen.ReportCard.route) }
            )
          }
        }
      }
    }

    // 4. OVERDUE ALERTS CARD
    if (overdueInvoices.isNotEmpty()) {
      item {
        AnimatedFadeIn(delayMillis = 250) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = Spacing.s4)
              .clip(RoundedCornerShape(Radius.lg))
              .background(StatusDangerBg)
              .border(1.dp, StatusDangerText.copy(alpha = 0.3f), RoundedCornerShape(Radius.lg))
              .padding(Spacing.cardPadding)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(StatusDangerText, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                  )
                }
                Spacer(modifier = Modifier.width(Spacing.s3))
                Column {
                  Text(
                    text = "${overdueInvoices.size} Accounts Overdue (>10 Days)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = StatusDangerText
                  )
                  Text(
                    text = "$${overdueInvoices.sumOf { it.pendingAmount } / 100000L}k outstanding tuition requiring bursar review.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusDangerText.copy(alpha = 0.85f),
                    fontSize = 11.sp
                  )
                }
              }
              Surface(
                shape = RoundedCornerShape(Radius.pill),
                color = StatusDangerText,
                modifier = Modifier
                  .clip(RoundedCornerShape(Radius.pill))
                  .clickable { onNavigateTo(Screen.Fees.route) }
              ) {
                Text(
                  text = "Review",
                  color = Color.White,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = Spacing.s3, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
    }

    // 5. ATTENDANCE & ACADEMIC TREND CHARTS
    item {
      AnimatedFadeIn(delayMillis = 300) {
        Column(modifier = Modifier.padding(bottom = Spacing.s4)) {
          SectionHeader(
            title = "Cohort Attendance Analytics",
            actionText = "Full Matrix",
            onActionClick = { onNavigateTo(Screen.Attendance.route) }
          )
          SimpleBarChart(
            title = "Weekly Attendance Trend (Mon - Fri)",
            data = listOf(
              "Mon" to 96.8f,
              "Tue" to 97.4f,
              "Wed" to 96.2f,
              "Thu" to 95.8f,
              "Fri" to 97.1f
            ),
            barColor = ScholaTerracotta
          )
        }
      }
    }

    // 6. RECENT AUDIT STREAM FEED
    item {
      AnimatedFadeIn(delayMillis = 350) {
        Column(modifier = Modifier.padding(bottom = Spacing.s4)) {
          SectionHeader(
            title = "Real-Time Institutional Audit Stream",
            actionText = "Audit Hub",
            onActionClick = { onNavigateTo(Screen.Notices.route) }
          )

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.s2)
          ) {
            auditLogs.take(4).forEach { log ->
              AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(CircleShape)
                      .background(
                        when (log.actionType) {
                          "ROLL_CALL" -> StatusSuccessBg
                          "FEE_PAYMENT" -> ScholaTerracottaContainer
                          "GRADEBOOK" -> Color(0xFFEDE9FE)
                          else -> ScholaSlateContainer
                        }
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = when (log.actionType) {
                        "ROLL_CALL" -> Icons.Default.CheckCircle
                        "FEE_PAYMENT" -> Icons.Default.AttachMoney
                        "GRADEBOOK" -> Icons.Default.Grade
                        else -> Icons.Default.HistoryEdu
                      },
                      contentDescription = null,
                      tint = when (log.actionType) {
                        "ROLL_CALL" -> StatusSuccessText
                        "FEE_PAYMENT" -> ScholaTerracotta
                        "GRADEBOOK" -> ScholaTerracottaDark
                        else -> ScholaSlateNavy
                      },
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(Spacing.s3))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = log.actionSummary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = log.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = ScholaMuted,
                        fontSize = 10.sp
                      )
                    }
                    Text(
                      text = "${log.actorName} (${log.actorRole}) • ${log.details}",
                      style = MaterialTheme.typography.bodySmall,
                      color = ScholaTextSecondary,
                      fontSize = 11.sp,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
                    )
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
