package com.example.ui.screens.principal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.datasource.DataMode
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

internal fun principalMoney(paise:Long):String=NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply{minimumFractionDigits=0;maximumFractionDigits=2}.format(BigDecimal.valueOf(paise,2))
@Composable fun PrincipalFees(repository:ErpDataRepository,receipt:(String)->Unit) {
 val principalDark=LocalPrincipalDark.current
 val invoices by repository.invoices.collectAsState();val fees by repository.feeRecords.collectAsState()
 val actor by repository.currentUser.collectAsState();val mode by repository.dataMode.collectAsState()
 var cohort by rememberSaveable(actor.id){mutableStateOf("All classes")};var tab by rememberSaveable(actor.id){mutableIntStateOf(0)}
 var selected by rememberSaveable(actor.id){mutableStateOf<String?>(null)}
 val visible=invoices.filter{cohort=="All classes"||it.cohort==cohort}
 val dues=visible.filter{it.pendingAmount>0}
 val history=fees.filter{f->visible.any{it.id==f.id}}.flatMap{f->f.transactions.map{f to it}}.distinctBy{it.second.transactionId}
 LazyColumn(Modifier.fillMaxSize().testTag("principal_fees"),contentPadding=PaddingValues(horizontal=if(principalDark)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  item {PrincipalPageHeader("Fees");PrincipalSelect(cohort,listOf("All classes")+invoices.map{it.cohort}.distinct().sorted(),{cohort=it})}
  if(principalDark)item {Column(verticalArrangement=Arrangement.spacedBy(Spacing.md12)){
   PrincipalSummary("Collected",principalMoney(visible.sumOf{it.paidAmount}),"Loaded school session ledger",PrincipalIcons.Fees,PrincipalColors.peach,PrincipalColors.peachInk)
   PrincipalSummary("Outstanding",principalMoney(dues.sumOf{it.pendingAmount}),"${dues.size} invoices",Icons.Outlined.Description,PrincipalColors.blue,PrincipalColors.blueInk)
  }}
  if(!principalDark)item {Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   PrincipalSummary("Due",principalMoney(dues.sumOf{it.pendingAmount}),"${dues.size} invoices",Icons.Outlined.CurrencyRupee,PrincipalColors.peach,PrincipalColors.peachInk,Modifier.weight(1f))
   PrincipalSummary("Collected",principalMoney(visible.sumOf{it.paidAmount}),"${visible.size} invoices",Icons.Outlined.VerifiedUser,PrincipalColors.mint,PrincipalColors.mintInk,Modifier.weight(1f))
  }}
  item {PrincipalTabs(listOf("Dues (${dues.size})","Receipts (${history.size})"),tab,{tab=it})}
  if(tab==0) {
   if(dues.isEmpty())item {EmptyStateView("No dues","No outstanding invoices match this class.")}
   items(dues,key={it.id}) {invoice->PrincipalRecordRow(invoice.studentName,invoice.cohort,{selected=invoice.id},Modifier.testTag("principal_invoice_${invoice.id}")){Text(principalMoney(invoice.pendingAmount),style=MaterialTheme.typography.labelLarge,fontWeight=FontWeight.Bold)}}
  } else {
   if(history.isEmpty())item {EmptyStateView("No receipts","Receipts appear after a confirmed payment is recorded.")}
   items(history,key={it.second.transactionId}) {(fee,tx)->PrincipalRecordRow(fee.studentName,"${tx.date} · ${tx.receiptNo}",{selected=fee.id}){Text(principalMoney(tx.amount),style=MaterialTheme.typography.labelMedium)}}
  }
  item {Text(if(mode==DataMode.DEMO)"Local Demo ledger · sample payments" else "Loaded school session · Razorpay TEST payments",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
 invoices.firstOrNull{it.id==selected}?.let {invoice->
  val record=fees.firstOrNull{it.id==invoice.id}
  AlertDialog(onDismissRequest={selected=null},title={Text("Invoice")},text={Column(Modifier.heightIn(max=PrincipalLayout.historyHeight).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   Text(invoice.studentName,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
   SchoolDetailRow("Class",invoice.cohort);SchoolDetailRow("Invoice",invoice.invoiceNumber);SchoolDetailRow("Due date",invoice.dueDate)
   record?.let{SchoolDetailRow("Tuition",principalMoney(it.tuitionFee));SchoolDetailRow("Exams",principalMoney(it.examFee));SchoolDetailRow("Transport",principalMoney(it.transportFee));SchoolDetailRow("Lab / library",principalMoney(it.labLibraryFee));SchoolDetailRow("Scholarship",principalMoney(it.discountScholarship))}
   HorizontalDivider();SchoolDetailRow("Total",principalMoney(invoice.totalAmount));SchoolDetailRow("Collected",principalMoney(invoice.paidAmount));SchoolDetailRow("Outstanding",principalMoney(invoice.pendingAmount));StatusBadge(invoice.status.label)
   Text(invoice.description,style=MaterialTheme.typography.bodySmall)
   record?.transactions?.forEach{tx->HorizontalDivider();SchoolDetailRow(tx.receiptNo,principalMoney(tx.amount));Text("${tx.date} · ${tx.status}",style=MaterialTheme.typography.bodySmall)}
   if(mode==DataMode.SUPABASE_DEMO)TextButton(onClick={selected=null;receipt(invoice.id)}){Text("Check payment / receipt")}
   Text("Payment is made from the student's or guardian's account.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }},confirmButton={TextButton(onClick={selected=null}){Text("Done")}})
 }
}
