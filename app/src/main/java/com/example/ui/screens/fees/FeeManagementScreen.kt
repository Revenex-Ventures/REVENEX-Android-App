package com.example.ui.screens.fees

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.FeeRecord
import com.example.data.model.FeeStatus
import com.example.data.model.UserRole
import com.example.data.model.Student
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun FeeManagementScreen(
  repository: ErpDataRepository,
  onShowPaymentModal: (studentName: String, amountDue: Double) -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()
  val feeRecords by repository.feeRecords.collectAsState()
  val students by repository.students.collectAsState()

  var selectedFilter by remember { mutableStateOf("All") }
  var selectedTab by remember { mutableStateOf(0) } // 0: Student Dues, 1: Transaction Receipts

  val scopedFeeRecords = remember(feeRecords, currentUser, selectedStudentId) {
    if (currentUser.role == UserRole.PARENT || currentUser.role == UserRole.STUDENT) {
      feeRecords.filter { it.studentId == selectedStudentId }
    } else {
      feeRecords
    }
  }

  val totalDemanded = scopedFeeRecords.sumOf { it.totalFee }
  val totalCollected = scopedFeeRecords.sumOf { it.paidAmount }
  val totalPending = scopedFeeRecords.sumOf { it.pendingAmount }

  val filteredRecords = remember(scopedFeeRecords, selectedFilter) {
    when (selectedFilter) {
      "Pending" -> scopedFeeRecords.filter { it.status == FeeStatus.PENDING || it.status == FeeStatus.OVERDUE }
      "Overdue" -> scopedFeeRecords.filter { it.status == FeeStatus.OVERDUE }
      "Paid" -> scopedFeeRecords.filter { it.status == FeeStatus.PAID }
      else -> scopedFeeRecords
    }
  }

  val allTransactions = remember(scopedFeeRecords) {
    scopedFeeRecords.flatMap { it.transactions }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("fee_management_screen"),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    // Top Financial KPI Grid
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = RevenexNavy)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Text(
              text = if (currentUser.role == UserRole.STUDENT || currentUser.role == UserRole.PARENT)
                "My Fee Summary" else "Fee Collection & Accounts",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = if (currentUser.role == UserRole.STUDENT || currentUser.role == UserRole.PARENT)
                "Academic Session 2026-2027 • Personal Fee Ledger"
              else "Academic Session 2026-2027 • Central Accounts Ledger",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("TOTAL REVENUE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("₹${(totalDemanded / 1000).toInt()}k", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("COLLECTED (PAID)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("₹${(totalCollected / 1000).toInt()}k", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = StatusSuccessContainer)
              }
              Column {
                Text("PENDING OUTSTANDING", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("₹${(totalPending / 1000).toInt()}k", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = StatusErrorContainer)
              }
            }
          }
        }
      }
    }

    // Tabs
    item {
      TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Student Ledgers", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Payment Receipts (${allTransactions.size})", fontWeight = FontWeight.Bold) }
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    if (selectedTab == 0) {
      // Filter Chips
      item {
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("All", "Pending", "Overdue", "Paid").forEach { filter ->
            item {
              FilterChip(
                selected = selectedFilter == filter,
                onClick = { selectedFilter = filter },
                label = { Text(filter) }
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      items(filteredRecords, key = { it.id }) { record ->
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
                  text = record.studentName,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Class ${record.classGrade}-${record.division} • Due Date: ${record.dueDate}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              StatusBadge(status = record.status.label, type = "fee")
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Total Fee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${record.totalFee.toInt()}", fontWeight = FontWeight.SemiBold)
              }
              Column {
                Text("Paid Amount", style = MaterialTheme.typography.labelSmall, color = StatusSuccessText)
                Text("₹${record.paidAmount.toInt()}", color = StatusSuccessText, fontWeight = FontWeight.Bold)
              }
              Column {
                Text("Pending Balance", style = MaterialTheme.typography.labelSmall, color = StatusErrorText)
                Text("₹${record.pendingAmount.toInt()}", color = StatusErrorText, fontWeight = FontWeight.Bold)
              }
            }

            if (record.pendingAmount > 0) {
              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = { onShowPaymentModal(record.studentName, record.pendingAmount) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("collect_fee_btn_${record.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue)
              ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Collect / Pay Online (₹${record.pendingAmount.toInt()})")
              }
            }
          }
        }
      }
    } else {
      // Transaction Receipts List
      items(allTransactions, key = { it.transactionId }) { txn ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .background(StatusSuccessContainer, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Receipt, contentDescription = null, tint = StatusSuccess)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Receipt #${txn.receiptNo}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${txn.feeHead} • ${txn.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Method: ${txn.method} (Txn: ${txn.transactionId})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "₹${txn.amount.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StatusSuccessText
              )
              StatusBadge(status = "SUCCESS")
            }
          }
        }
      }
    }
  }
}
