package com.example.ui.screens.students

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeStatus
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
  studentId: String,
  repository: ErpDataRepository,
  onBack: () -> Unit,
  onPayFee: (studentName: String, amountDue: Long) -> Unit
) {
  val students by repository.students.collectAsState()
  val student = students.firstOrNull { it.id == studentId } ?: students.first()

  Scaffold(
    containerColor = ScholaLinen,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Scholar 360 Profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ScholaTextPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = ScholaLinen
        )
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(Spacing.s4)
    ) {
      Student360DetailPane(
        student = student,
        repository = repository,
        onPayFee = onPayFee
      )
    }
  }
}

/**
 * Reusable Student 360 Detail Pane used both standalone and in Tablet/Desktop split-pane.
 */
@Composable
fun Student360DetailPane(
  student: Student,
  repository: ErpDataRepository,
  onPayFee: (studentName: String, amountDue: Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val invoices by repository.invoices.collectAsState()
  val reportCards by repository.reportCards.collectAsState()
  val gradebookEntries by repository.gradebookEntries.collectAsState()

  val studentInvoice = invoices.firstOrNull { it.studentId == student.id }
  val studentReportCard = reportCards.firstOrNull { it.studentId == student.id }
  val studentGrades = gradebookEntries.filter { it.studentId == student.id || it.cohort == student.fullClass }

  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("Overview", "Attendance", "Ledger", "Transcript")

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("student_360_detail_pane"),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    // 1. HERO IDENTITY SURFACE (Dark Onyx, 22dp corners)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.hero))
          .background(ScholaOnyx)
          .border(1.dp, ScholaOnyxBorder, RoundedCornerShape(Radius.hero))
          .padding(Spacing.cardPaddingLarge)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Avatar(name = student.name, size = 48.dp, background = ScholaTerracotta)
              Spacer(modifier = Modifier.width(Spacing.s3))
              Column {
                Text(
                  text = student.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = ScholaOnyxText
                )
                Text(
                  text = "Class ${student.fullClass} • Roll #${student.rollNumber} • Adm #${student.admissionNumber}",
                  style = MaterialTheme.typography.bodySmall,
                  color = ScholaOnyxMuted,
                  fontSize = 11.sp
                )
              }
            }
            ScholaPillBadge(status = student.status)
          }

          Spacer(modifier = Modifier.height(Spacing.s4))

          // Attendance Ring & GPA Meter Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            VitalRing(
              value = student.attendancePercent.toFloat(),
              label = "Attendance",
              size = VitalRingSize.MEDIUM,
              gradientStart = ScholaTerracotta,
              trackColor = ScholaOnyxBorder,
              textColor = Color.White
            )

            Box(
              modifier = Modifier
                .width(1.dp)
                .height(48.dp)
                .background(ScholaOnyxBorder)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "CUMULATIVE GPA",
                style = MaterialTheme.typography.labelSmall,
                color = ScholaOnyxMuted,
                letterSpacing = TypeTokens.trackingMicroLabel,
                fontSize = 9.sp
              )
              Text(
                text = "${student.gpa} / 4.0",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ScholaGold
              )
              Text(
                text = "Class Rank: #${student.rank}",
                style = MaterialTheme.typography.bodySmall,
                color = ScholaOnyxMuted,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    // 2. SEGMENTED TAB SELECTOR
    item {
      Surface(
        shape = RoundedCornerShape(Radius.pill),
        color = ScholaSurface,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.pill))
          .border(1.dp, ScholaBorder, RoundedCornerShape(Radius.pill))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(Radius.pill))
                .background(if (isSelected) ScholaTerracotta else Color.Transparent)
                .clickable { selectedTab = index }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ScholaTextSecondary,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }

    // 3. TAB CONTENT
    when (selectedTab) {
      0 -> {
        // OVERVIEW: Parent credentials & Demographics
        item {
          AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Parent / Guardian Credentials", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.s2))
            InfoRow("Guardian Name", student.parentName)
            InfoRow("Emergency Phone", student.parentPhone)
            InfoRow("Verified Email", student.parentEmail)
            InfoRow("Residential Address", student.address.ifBlank { "Lake View Residency, Pune" })
          }
        }
        item {
          AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Demographics & Medical Record", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.s2))
            InfoRow("Gender / DOB", "${student.gender} • ${student.dob}")
            InfoRow("Blood Group", student.bloodGroup)
            InfoRow("Admission Date", student.admissionDate.ifBlank { "10 Jun 2022" })
          }
        }
      }

      1 -> {
        // ATTENDANCE: Roll Call verification details
        item {
          AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Attendance Eligibility Threshold", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.s2))
            Text(
              text = "Scholar presence is verified at ${student.attendancePercent}%. Examination eligibility requires minimum 75.0% verified presence.",
              style = MaterialTheme.typography.bodyMedium,
              color = ScholaTextSecondary
            )
            Spacer(modifier = Modifier.height(Spacing.s3))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
              ScholaPillBadge(status = "Verified Present")
              ScholaPillBadge(status = "Eligible")
            }
          }
        }
      }

      2 -> {
        // LEDGER: Tuition invoice breakdown
        item {
          AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Tuition & Activity Ledger", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                  text = "Invoice: ${studentInvoice?.invoiceNumber ?: "INV-2026-0842"}",
                  style = MaterialTheme.typography.bodySmall,
                  color = ScholaMuted
                )
              }
              ScholaPillBadge(status = student.feeStatus.label)
            }
            Spacer(modifier = Modifier.height(Spacing.s3))
            val total = studentInvoice?.totalAmount ?: 450000L
            val paid = studentInvoice?.paidAmount ?: 305000L
            val due = (total - paid).coerceAtLeast(0L)

            InfoRow("Total Demanded", "$${total / 100L}")
            InfoRow("Paid Realized", "$${paid / 100L}")
            InfoRow("Outstanding Due", "$${due / 100L}")

            if (due > 0) {
              Spacer(modifier = Modifier.height(Spacing.s3))
              AppButton(
                text = "Collect Outstanding Dues ($${due / 100L})",
                onClick = { onPayFee(student.name, due) },
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }

      3 -> {
        // TRANSCRIPT: Assessment grades & scores
        if (studentGrades.isEmpty()) {
          item {
            EmptyStateView(
              title = "No Grades Published Yet",
              message = "Assessments for this cohort will appear once published by faculty."
            )
          }
        } else {
          items(studentGrades, key = { it.id }) { grd ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(grd.assessmentTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                  Text("${grd.subject} • ${grd.instructorName}", style = MaterialTheme.typography.bodySmall, color = ScholaMuted)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("${grd.score} / ${grd.maxScore}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ScholaTerracotta)
                  ScholaPillBadge(status = grd.letterGrade)
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
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = ScholaMuted)
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ScholaTextPrimary)
  }
}
