package com.example.ui.screens.students.portal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.repository.ErpDataRepository
import com.example.data.datasource.DataMode
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

internal fun studentMoney(paise:Long):String="₹"+NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply{maximumFractionDigits=2}.format(BigDecimal.valueOf(paise,2))

@Composable fun StudentFees(repository:ErpDataRepository,navigate:(String)->Unit) {
 val dark=LocalStudentDark.current
 val student=ownStudent(repository);val invoices by repository.invoices.collectAsState()
 val own=invoices.filter{student!=null&&it.studentId==student.id};var tab by rememberSaveable{mutableIntStateOf(0)}
 val visible=own.filter{if(tab==0)it.pendingAmount>0 else it.pendingAmount==0L}
 LazyColumn(Modifier.fillMaxSize().testTag("student_fees"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{StudentPageHeader("Fees")}
  item{if(LocalStudentDark.current)StudentDarkFeature(studentMoney(own.sumOf{it.pendingAmount}),"Outstanding balance",StudentTone.ORANGE,StudentIcons.Wallet,eyebrow="Total due")else StudentCard(tone=StudentTone.ORANGE){Row(Modifier.padding(StudentLayout.gap),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap)){StudentIcon(StudentIcons.Wallet,StudentTone.ORANGE);Column{Text(studentMoney(own.sumOf{it.pendingAmount}),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Outstanding",color=StudentColors.onOrange)}}}}
  if(dark)item{StudentCard{Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
   Text("Academic account",style=MaterialTheme.typography.titleSmall)
   StudentAmount("Total invoiced",own.sumOf{it.totalAmount});StudentAmount("Already paid",own.sumOf{it.paidAmount});StudentAmount("Remaining",own.sumOf{it.pendingAmount})
  }}}
  item{StudentTabs(listOf("Pending (${own.count{it.pendingAmount>0}})","Paid (${own.count{it.pendingAmount==0L}})"),tab){tab=it}}
  if(visible.isEmpty())item{EmptyStateView(if(tab==0)"No outstanding invoices"else "No paid invoices","Invoices available to your account appear here.")}
  items(visible,key={it.id}){invoice->StudentRow(invoice.title,"Due ${invoice.dueDate}",StudentIcons.Document,if(tab==0)StudentTone.ORANGE else StudentTone.MINT,{navigate(Screen.StudentInvoice.createRoute(invoice.id))},studentMoney(if(tab==0)invoice.pendingAmount else invoice.paidAmount))}
 }
}

@Composable fun StudentInvoice(repository:ErpDataRepository,id:String,pay:(String,String,Long)->Unit,check:(String)->Unit) {
 val student=ownStudent(repository);val invoices by repository.invoices.collectAsState();val fees by repository.feeRecords.collectAsState();val mode by repository.dataMode.collectAsState()
 val invoice=invoices.firstOrNull{it.id==id&&student!=null&&it.studentId==student.id}
 if(invoice==null){EmptyStateView("Invoice unavailable","This invoice is not available to your signed-in student account.");return}
 val fee=fees.firstOrNull{student!=null&&it.id==id&&it.studentId==student.id&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId}
 val transactions=fees.filter{student!=null&&it.id==id&&it.studentId==student.id&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId}.flatMap{it.transactions}.filter{it.status.equals("SUCCESS",true)||it.status.equals("CAPTURED",true)}
 LazyColumn(Modifier.fillMaxSize().testTag("student_invoice"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)){
  item{StudentPageHeader("Invoice")}
  item{StudentCard{Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){StudentBadge(if(mode==DataMode.SUPABASE_DEMO)"Test mode"else "Local sample",StudentTone.ORANGE);StudentBadge(if(invoice.pendingAmount>0)"Unpaid"else "Paid",if(invoice.pendingAmount>0)StudentTone.ERROR else StudentTone.MINT)}
   Text(invoice.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("Invoice ${invoice.invoiceNumber}",style=MaterialTheme.typography.bodySmall)
   if(LocalStudentDark.current)StudentDarkFeature(studentMoney(invoice.pendingAmount),"Amount due",StudentTone.BLUE,StudentIcons.Document,compact=true)
   HorizontalDivider()
   fee?.let{record->listOf("Tuition" to record.tuitionFee,"Examinations" to record.examFee,"Transport" to record.transportFee,"Lab / library" to record.labLibraryFee,"Scholarship / discount" to -record.discountScholarship).filter{it.second!=0L}.forEach{StudentAmount(it.first,it.second)};HorizontalDivider()}
   StudentAmount("Invoice total",invoice.totalAmount);StudentAmount("Paid",invoice.paidAmount);HorizontalDivider();StudentAmount("Balance",invoice.pendingAmount)
   Text("Due ${invoice.dueDate}",style=MaterialTheme.typography.bodyMedium)
   if(invoice.description.isNotBlank())Text(invoice.description,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   if(invoice.pendingAmount>0)StudentAction(if(mode==DataMode.SUPABASE_DEMO)"Pay ${studentMoney(invoice.pendingAmount)}"else "Sign in to pay"){pay(invoice.id,invoice.studentName,invoice.pendingAmount)}
   if(mode==DataMode.SUPABASE_DEMO)OutlinedButton(colors=ButtonDefaults.outlinedButtonColors(contentColor=StudentColors.actionText),shape=RoundedCornerShape(Radius.sm),onClick={check(id)},modifier=Modifier.fillMaxWidth()){Text("Check payment status")}
   Text(if(mode==DataMode.SUPABASE_DEMO)"Razorpay TEST checkout. A receipt appears only after server verification."else "Sample invoice. Cloud sign-in is required for test checkout.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }}}
  item{StudentHeading("Receipts")}
  if(transactions.isEmpty())item{Text("No payment receipt recorded for this invoice.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  items(transactions){transaction->StudentCard(tone=if(LocalStudentDark.current)StudentTone.MINT else null){Column(Modifier.padding(StudentLayout.gap)){Text(studentMoney(transaction.amount),style=MaterialTheme.typography.titleMedium);Text("${transaction.date} · ${transaction.status}",style=MaterialTheme.typography.bodySmall);Text("Receipt ${transaction.receiptNo.ifBlank{"not available"}}",style=MaterialTheme.typography.bodySmall);Text(transaction.method,style=MaterialTheme.typography.bodySmall)}}}
 }
}
@Composable private fun StudentAmount(label:String,paise:Long){Row(Modifier.fillMaxWidth()){Text(label,Modifier.weight(1f),style=if(LocalStudentDark.current)MaterialTheme.typography.bodyMedium else LocalTextStyle.current);Text(studentMoney(paise),fontWeight=FontWeight.SemiBold,style=if(LocalStudentDark.current)MaterialTheme.typography.bodyMedium else LocalTextStyle.current)}}
