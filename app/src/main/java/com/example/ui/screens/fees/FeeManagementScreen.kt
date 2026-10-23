package com.example.ui.screens.fees

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeStatus
import com.example.data.model.LedgerInvoice
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun FeeManagementScreen(
  repository: ErpDataRepository,
  onShowPaymentModal: (studentName: String, amountDue: Long) -> Unit,
  onPayRazorpay: (String, String, Long) -> Unit = { _, name, amount -> onShowPaymentModal(name, amount) },
  onCheckRazorpay: (String) -> Unit = {}
) {
  val invoices by repository.invoices.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val dataMode by repository.dataMode.collectAsState()

  var selectedFilter by remember { mutableStateOf("Pending") }
  val filters = listOf("Pending", "All Invoices", "Overdue", "Partial", "Paid")

  // Selected invoice for payment dialog
  var selectedInvoiceForPayment by remember { mutableStateOf<LedgerInvoice?>(null) }

  val totalBilled = invoices.sumOf { it.totalAmount }
  val totalCollected = invoices.sumOf { it.paidAmount }
  val totalReceivables = invoices.sumOf { it.pendingAmount }
  val collectionPercentage = if (totalBilled > 0) ((totalCollected.toDouble() / totalBilled.toDouble()) * 100.0).toInt() else 0

  val filteredInvoices = remember(invoices, selectedFilter) {
    when (selectedFilter) {
      "Pending" -> invoices.filter { it.pendingAmount>0 }
      "Overdue" -> invoices.filter { it.status == FeeStatus.OVERDUE }
      "Partial" -> invoices.filter { it.status == FeeStatus.PARTIAL }
      "Paid" -> invoices.filter { it.status == FeeStatus.PAID }
      else -> invoices
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("fee_management_screen"),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    // 1. NET REVENUE REALIZATION HERO CARD (Dark Onyx Surface, 22dp corners)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.hero))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.hero))
          .padding(Spacing.cardPaddingLarge)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "FEES & PAYMENTS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = TypeTokens.trackingMicroLabel
              )
              Text(
                text = "Balances & invoices",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
            }
            Surface(
              shape = RoundedCornerShape(Radius.pill),
              color = SemanticColors.warningContainer
            ) {
              Text(
                text = "$collectionPercentage% Collected",
                color = SemanticColors.onWarning,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(Spacing.s4))

          // Collection Progress Bar
          LinearProgressIndicator(
            progress = { (collectionPercentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(Radius.pill)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
          )

          Spacer(modifier = Modifier.height(Spacing.s4))

          // Metrics row: Billed Total, Collected, Outstanding Receivables
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("BILLED TOTAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = TypeTokens.trackingMicroLabel, fontSize = 12.sp)
              Text("₹${totalBilled / 100L}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("COLLECTED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = TypeTokens.trackingMicroLabel, fontSize = 12.sp)
              Text("₹${totalCollected / 100L}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SemanticColors.onSuccess)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("RECEIVABLES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = TypeTokens.trackingMicroLabel, fontSize = 12.sp)
              Text("₹${totalReceivables / 100L}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SemanticColors.onDanger)
            }
          }
        }
      }
    }

    // 2. STATUS FILTER CHIPS
    item {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
      ) {
        items(filters) { flt ->
          val isSelected = selectedFilter == flt
          Surface(
            shape = RoundedCornerShape(Radius.pill),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            modifier = Modifier
              .clip(RoundedCornerShape(Radius.pill))
              .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.pill))
              .clickable { selectedFilter = flt }
          ) {
            Text(
              text = flt,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.heightIn(min = 48.dp).wrapContentHeight(Alignment.CenterVertically).padding(horizontal = Spacing.s3, vertical = 6.dp)
            )
          }
        }
      }
    }

    // 3. LEDGER INVOICE CARDS
    item {
      SectionHeader(title = "Invoices · $selectedFilter (${filteredInvoices.size})")
    }

    if (filteredInvoices.isEmpty()) {
      item {
        EmptyStateView(
          title = "No Invoices Matching Filter",
          message = "No financial records found in '$selectedFilter'."
        )
      }
    } else {
      items(filteredInvoices, key = { it.id }) { invoice ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = invoice.studentName,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "(${invoice.cohort})",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 12.sp
                )
              }
              Text(
                text = "${invoice.invoiceNumber} • ${invoice.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
              Text(
                text = "Due: ${invoice.dueDate} • Channel: ${invoice.paymentChannel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }

            ScholaPillBadge(status = invoice.status.label)
          }

          Spacer(modifier = Modifier.height(Spacing.s3))

          // Payment Progress Bar
          LinearProgressIndicator(
            progress = { invoice.progressFraction },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(Radius.pill)),
            color = if (invoice.status == FeeStatus.OVERDUE) SemanticColors.onDanger else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
          )

          Spacer(modifier = Modifier.height(Spacing.s3))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "₹${invoice.paidAmount / 100L} paid of ₹${invoice.totalAmount / 100L}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )

            if (invoice.pendingAmount > 0 && dataMode==com.example.data.datasource.DataMode.SUPABASE_DEMO) {
              Column(horizontalAlignment = Alignment.End) {
                AppButton(text="Razorpay Test", onClick={ onPayRazorpay(invoice.id, invoice.studentName, invoice.pendingAmount) })
                TextButton(onClick={ onCheckRazorpay(invoice.id) }) { Text("Check payment status") }
              }
            } else if (invoice.pendingAmount > 0) {
              AppButton(
                text = "Razorpay Test (₹${invoice.pendingAmount / 100L})",
                onClick = { onPayRazorpay(invoice.id,invoice.studentName,invoice.pendingAmount) },
                containerColor = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                minHeight = 38.dp
              )
            } else {
              Surface(
                shape = RoundedCornerShape(Radius.pill),
                color = SemanticColors.successContainer
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = Spacing.s2, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = SemanticColors.onSuccess, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Settled in Full", color = SemanticColors.onSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }

  // Payment Collection Dialog with Presets & Channels
  if (selectedInvoiceForPayment != null) {
    ScholaPaymentCollectionDialog(
      invoice = selectedInvoiceForPayment!!,
      onDismiss = { selectedInvoiceForPayment = null },
      onConfirmPayment = { amount, channel ->
        repository.processInvoicePayment(selectedInvoiceForPayment!!.id, amount, channel)
        selectedInvoiceForPayment = null
      }
    )
  }
}

/**
 * Payment Collection Dialog with custom amount input, quick presets, and payment channel chips.
 */
@Composable
fun ScholaPaymentCollectionDialog(
  invoice: LedgerInvoice,
  onDismiss: () -> Unit,
  onConfirmPayment: (amountPaid: Long, channel: String) -> Unit
) {
  var customAmountInput by remember { mutableStateOf((invoice.pendingAmount / 100L).toString()) }
  var selectedPreset by remember { mutableStateOf("Full Due") }
  var selectedChannel by remember { mutableStateOf("Stripe ACH") }

  val channels = listOf("Stripe ACH", "Wire Transfer", "Apple Pay")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text("Collect Tuition Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
          text = "Scholar: ${invoice.studentName} (${invoice.invoiceNumber})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.s3)
      ) {
        // Invoice Summary Card
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Total Demanded:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("₹${invoice.totalAmount / 100L}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Outstanding Balance:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("₹${invoice.pendingAmount / 100L}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SemanticColors.onDanger)
          }
        }

        // Quick Presets Row
        Text("Quick Preset Amount:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
          listOf("Full Due", "50% Split", "Custom").forEach { preset ->
            val isSelected = selectedPreset == preset
            Surface(
              shape = RoundedCornerShape(Radius.pill),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
              modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.pill))
                .clickable {
                  selectedPreset = preset
                  customAmountInput = when (preset) {
                    "Full Due" -> (invoice.pendingAmount / 100L).toString()
                    "50% Split" -> ((invoice.pendingAmount / 2) / 100L).toString()
                    else -> customAmountInput
                  }
                }
            ) {
              Text(
                text = preset,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.heightIn(min = 48.dp).wrapContentHeight(Alignment.CenterVertically).padding(horizontal = Spacing.s3, vertical = 6.dp)
              )
            }
          }
        }

        // Custom Amount Input Field
        ScholaInputField(
          value = customAmountInput,
          onValueChange = { customAmountInput = it },
          label = "Amount to Charge (₹)",
          placeholder = "e.g. 1450",
          keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )

        // Payment Channel Chips (Stripe ACH, Wire, Apple Pay)
        Text("Payment Processing Channel:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
          channels.forEach { ch ->
            val isSelected = selectedChannel == ch
            Surface(
              shape = RoundedCornerShape(Radius.sm),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
              modifier = Modifier
                .clip(RoundedCornerShape(Radius.sm))
                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.sm))
                .clickable { selectedChannel = ch }
            ) {
              Text(
                text = ch,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = (customAmountInput.toLongOrNull() ?: (invoice.pendingAmount / 100L)) * 100L
          onConfirmPayment(amt, selectedChannel)
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Process Payment", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    },
    containerColor = MaterialTheme.colorScheme.surface
  )
}
