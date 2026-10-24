package com.example.ui.screens.fees

import kotlinx.coroutines.launch

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
fun FeeManagementScreen(repository: ErpDataRepository,
  onShowPaymentModal: (studentName: String, amountDue: Long) -> Unit,
  onPayRazorpay: (String,String,Long) -> Unit = {_,name,amount -> onShowPaymentModal(name,amount)},
  onCheckRazorpay: (String) -> Unit = {}) {
  val invoices by repository.invoices.collectAsState()
  val fees by repository.feeRecords.collectAsState()
  val user by repository.currentUser.collectAsState()
  val mode by repository.dataMode.collectAsState()
  val cloud=mode==com.example.data.datasource.DataMode.SUPABASE_DEMO
  var filter by remember(user.id) {mutableStateOf("Pending")}
  var expanded by remember(user.id) {mutableStateOf<String?>(null)}
  var filterMenu by remember {mutableStateOf(false)}
  val unpaid=invoices.filter {it.pendingAmount>0}
  val paid=invoices.filter {it.status==FeeStatus.PAID}
  val visible=when(filter){"Pending"->unpaid;"Paid"->paid;"Overdue"->invoices.filter {it.status==FeeStatus.OVERDUE};"Partial"->invoices.filter {it.status==FeeStatus.PARTIAL};else->invoices}
  val history=fees.flatMap {fee -> fee.transactions.map {fee to it}}
  val list=androidx.compose.foundation.lazy.rememberLazyListState()
  val scope=rememberCoroutineScope()
  LazyColumn(Modifier.fillMaxSize().testTag("fee_management_screen"),state=list,contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    item {WorkspacePageHeader("Fees & payments")}
    item {Surface(shape=RoundedCornerShape(Radius.md),color=SemanticColors.peachContainer) {
      Column(Modifier.fillMaxWidth().padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        Row(Modifier.fillMaxWidth()) {
          Column(Modifier.weight(1f)) {
            Text("Outstanding amount",style=MaterialTheme.typography.bodySmall,color=SemanticColors.onPeach)
            Text(feeCurrency(unpaid.sumOf {it.pendingAmount}),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=SemanticColors.onPeach)
          }
          Text(if(cloud) "TEST mode" else "Local sample",style=MaterialTheme.typography.labelSmall,color=SemanticColors.onPeach)
        }
        Text(if(user.role in setOf(UserRole.STUDENT,UserRole.PARENT)) "Invoices available to your account" else "School invoices · choose an invoice to review",style=MaterialTheme.typography.bodySmall,color=SemanticColors.onPeach)
        Button(onClick={
          if(unpaid.size==1) unpaid.first().let {onPayRazorpay(it.id,it.studentName,it.pendingAmount)}
          else {filter="Pending";scope.launch {list.animateScrollToItem(3)}}
        },enabled=unpaid.isNotEmpty(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(Radius.sm)) {Text(if(unpaid.size==1) "Pay" else "View pending invoices")}
      }
    }}
    item {
      TabRow(selectedTabIndex=if(filter=="Paid")1 else 0,containerColor=MaterialTheme.colorScheme.background) {
        Tab(selected=filter!="Paid",onClick={filter="Pending"},text={Text("Pending (${unpaid.size})")})
        Tab(selected=filter=="Paid",onClick={filter="Paid"},text={Text("Paid (${paid.size})")})
      }
      Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        Text("Invoices · $filter",Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
        Box {
          TextButton(onClick={filterMenu=true}) {Text("Filter");Icon(Icons.Default.ExpandMore,null)}
          DropdownMenu(filterMenu,{filterMenu=false}) {listOf("Pending","Paid","All invoices","Overdue","Partial").forEach {value ->
            DropdownMenuItem(text={Text(value)},onClick={filter=value;filterMenu=false})
          }}
        }
      }
    }
    if(visible.isEmpty()) item {EmptyStateView(title="No matching invoices",message="There are no invoices in this view.")}
    items(visible,key={it.id}) {invoice ->
      SchoolGroup {
        Row(Modifier.fillMaxWidth().clickable {expanded=if(expanded==invoice.id)null else invoice.id}.padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          Icon(Icons.Default.ReceiptLong,null,tint=MaterialTheme.colorScheme.primary)
          Column(Modifier.weight(1f)) {
            Text(invoice.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
            Text("${invoice.studentName} · ${invoice.cohort}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Due ${invoice.dueDate}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Column(horizontalAlignment=Alignment.End) {
            Text(feeCurrency(if(invoice.pendingAmount>0)invoice.pendingAmount else invoice.totalAmount),style=MaterialTheme.typography.titleSmall)
            if(invoice.pendingAmount>0) TextButton(onClick={onPayRazorpay(invoice.id,invoice.studentName,invoice.pendingAmount)}) {Text("Pay")}
            else if(cloud) TextButton(onClick={onPayRazorpay(invoice.id,invoice.studentName,0L)}) {Text("Receipt")}
            else StatusBadge(status="Paid")
          }
        }
        androidx.compose.animation.AnimatedVisibility(visible=expanded==invoice.id) {
          Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
            HorizontalDivider()
            Text(invoice.invoiceNumber,style=MaterialTheme.typography.labelMedium)
            Text(invoice.description,style=MaterialTheme.typography.bodyMedium)
            Text("${feeCurrency(invoice.paidAmount)} paid of ${feeCurrency(invoice.totalAmount)}",style=MaterialTheme.typography.bodySmall)
            Text("Issued ${invoice.issueDate} · ${invoice.paymentChannel}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            StatusBadge(status=invoice.status.label)
            if(cloud) TextButton(onClick={onCheckRazorpay(invoice.id)}) {Text(if(invoice.pendingAmount>0) "Check payment status" else "View receipt / status")}
          }
        }
      }
    }
    item {Spacer(Modifier.height(Spacing.md12));Text("Payment history",style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)}
    if(history.isEmpty()) item {Text("No recorded payments. Receipts appear after server-confirmed capture.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    items(history,key={(fee,tx)->"${fee.id}_${tx.transactionId}"}) {(fee,tx)->
      SchoolGroup {
        Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
          Row(Modifier.fillMaxWidth()) {Text(fee.studentName,Modifier.weight(1f),style=MaterialTheme.typography.titleSmall);Text(feeCurrency(tx.amount),style=MaterialTheme.typography.titleSmall)}
          Text("${tx.date} · ${tx.status}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          Text(tx.receiptNo,style=MaterialTheme.typography.labelSmall)
          if(cloud) TextButton(onClick={onCheckRazorpay(fee.id)}) {Text("Check invoice receipt")}
        }
      }
    }
    item {Text("Billed ${feeCurrency(invoices.sumOf {it.totalAmount})} · Collected ${feeCurrency(invoices.sumOf {it.paidAmount})}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
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

private fun feeCurrency(paise: Long): String = java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
  currency = java.util.Currency.getInstance("INR")
}.format(java.math.BigDecimal.valueOf(paise, 2))
