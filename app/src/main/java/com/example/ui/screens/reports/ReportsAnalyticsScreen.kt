package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.SectionHeader
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.SimpleProgressRing
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun ReportsAnalyticsScreen(
  repository: ErpDataRepository
) {
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val feeRecords by repository.feeRecords.collectAsState()

  val totalStudents = students.size
  val totalCollected = feeRecords.sumOf { it.paidAmount }
  val totalPending = feeRecords.sumOf { it.pendingAmount }
  val totalDemanded = feeRecords.sumOf { it.totalFee }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("reports_analytics_screen"),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RevenexNavy)
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
                text = "Executive ERP Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Term 1 Institutional Performance Report",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
              )
            }
            IconButton(
              onClick = { /* Export PDF */ },
              modifier = Modifier.background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            ) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = "Export Report", tint = Color.White)
            }
          }
        }
      }
    }

    item {
      SectionHeader(title = "Core Operational Metrics")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        StatCard(
          title = "Fee Realization",
          value = "${((totalCollected / totalDemanded) * 100).toInt()}%",
          sublabel = "₹${(totalCollected / 1000).toInt()}k Collected",
          icon = Icons.Default.AccountBalanceWallet,
          iconTint = ScholaSlateNavy,
          iconBackground = ScholaSlateContainer,
          trendText = "+8.4% YoY",
          isPositiveTrend = true,
          modifier = Modifier.weight(1f)
        )
        StatCard(
          title = "Average Attendance",
          value = "95.6%",
          sublabel = "Across 10 Classes",
          icon = Icons.Default.FactCheck,
          iconTint = RevenexBlue,
          iconBackground = RevenexPrimaryContainer,
          trendText = "98% Target",
          isPositiveTrend = true,
          modifier = Modifier.weight(1f)
        )
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    item {
      SectionHeader(title = "Subject-Wise Academic Mastery")
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        SimpleBarChart(
          title = "Average Subject Scores (Term 1 Class 10)",
          data = listOf(
            "Math" to 92.4f,
            "Physics" to 88.6f,
            "English" to 91.0f,
            "Social" to 87.5f,
            "CS/AI" to 96.2f,
            "Hindi" to 86.8f
          ),
          barColor = RevenexPrimary
        )

        SimpleBarChart(
          title = "Grade-wise Fee Realization Ratio",
          data = listOf(
            "G-10" to 84.0f,
            "G-9" to 92.5f,
            "G-8" to 78.0f,
            "G-7" to 95.0f,
            "G-6" to 90.2f
          ),
          barColor = Color(0xFF059669)
        )
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    item {
      SectionHeader(title = "Institutional Health Summary")
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Revenex ERP Automated Audit Findings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• 98.4% Teacher on-time attendance with 0 unscheduled classroom vacancies.\n• Fee collection is 14% higher than corresponding cycle in 2025.\n• 100% of homework assignments submitted and graded digitally on portal.\n• GPS bus tracking active on all 3 transport routes with 0 delay escalations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
          )
        }
      }
    }
  }
}
