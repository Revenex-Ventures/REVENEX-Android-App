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
  val student = students.firstOrNull { it.id == studentId }
  if (student == null) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Spacing.s4)) {
      TextButton(onClick = onBack) { Text("Back") }
      EmptyStateView(title = "Student unavailable", message = "This record is not loaded or your account cannot access it.")
    }
    return
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Student profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
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
fun Student360DetailPane(student: Student, repository: ErpDataRepository,
  onPayFee: (studentName: String, amountDue: Long) -> Unit, modifier: Modifier = Modifier) {
  val invoices by repository.invoices.collectAsState()
  val reports by repository.reportCards.collectAsState()
  val grades by repository.gradebookEntries.collectAsState()
  val cloud=repository.dataMode.collectAsState().value==com.example.data.datasource.DataMode.SUPABASE_DEMO
  val invoice=invoices.firstOrNull {it.studentId==student.id}
  val assessments=grades.filter {it.studentId==student.id}
  val cards=reports.filter {it.studentId==student.id}
  var contact by remember(student.id) {mutableStateOf(false)}
  var personal by remember(student.id) {mutableStateOf(false)}
  LazyColumn(modifier.fillMaxSize().testTag("student_360_detail_pane"),verticalArrangement=Arrangement.spacedBy(Spacing.xl24),contentPadding=PaddingValues(bottom=Spacing.xl24)) {
    item {SchoolGroup {
      Row(Modifier.padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
        Avatar(student.name,size=Spacing.xxl32,background=SemanticColors.lavenderContainer,textColor=SemanticColors.onLavender)
        Column(Modifier.weight(1f)) {
          Text(student.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
          Text("Class ${student.fullClass} · Roll ${student.rollNumber}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          Text(student.admissionNumber,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }}
    item {SchoolSection("Academics") {SchoolGroup {
      Column(Modifier.padding(Spacing.md12)) {
        InfoRow("Class & section",student.fullClass)
        InfoRow("Academic session",student.sessionId)
        InfoRow("Student status",student.status)
        if(!cloud) InfoRow("Local demo GPA / rank","${student.gpa} / 10 · Rank ${student.rank}")
      }
    }}}
    item {SchoolSection("Guardian",if(contact)"Hide" else "Details",{contact=!contact}) {SchoolGroup {
      Column(Modifier.padding(Spacing.md12)) {
        InfoRow("Guardian",student.parentName)
        if(contact) {
          InfoRow("Phone",student.parentPhone);InfoRow("Email",student.parentEmail);InfoRow("Address",student.address)
        }
      }
    }}}
    item {SchoolSection("Admission",if(personal)"Hide" else "Details",{personal=!personal}) {SchoolGroup {
      Column(Modifier.padding(Spacing.md12)) {
        InfoRow("Admission date",student.admissionDate)
        if(personal){InfoRow("Gender / date of birth","${student.gender} · ${student.dob}");InfoRow("Blood group",student.bloodGroup)}
      }
    }}}
    item {SchoolSection("Attendance") {SchoolGroup {
      Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        Text("${student.attendancePercent}% recorded attendance",style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
        Text("Attendance eligibility is determined by the school's policies. This summary does not certify examination eligibility.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }}}
    item {SchoolSection("Fees") {SchoolGroup {
      Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        if(invoice==null) Text("No fee record available to this account.",style=MaterialTheme.typography.bodySmall)
        else {
          Text(invoice.title,style=MaterialTheme.typography.titleSmall)
          InfoRow("Invoice",invoice.invoiceNumber)
          InfoRow("Billed",profileCurrency(invoice.totalAmount));InfoRow("Paid",profileCurrency(invoice.paidAmount));InfoRow("Outstanding",profileCurrency(invoice.pendingAmount))
          if(invoice.pendingAmount>0) AppButton(text="Pay",onClick={onPayFee(student.name,invoice.pendingAmount)},modifier=Modifier.fillMaxWidth())
          if(cloud) Text("Razorpay TEST checkout. Receipt confirmation is handled by the server.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }}}
    item {SchoolSection("Results") {SchoolGroup {
      if(assessments.isEmpty()) Text("No assessment results available.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
      assessments.forEach {grade ->
        Row(Modifier.fillMaxWidth().padding(Spacing.md12),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          Column(Modifier.weight(1f)){Text(grade.assessmentTitle,style=MaterialTheme.typography.titleSmall);Text("${grade.subject} · ${grade.instructorName}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
          Text("${grade.score}/${grade.maxScore} · ${grade.letterGrade}",style=MaterialTheme.typography.labelLarge)
        }
      }
    }}}
    item {SchoolSection("Documents") {SchoolGroup {
      Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        val documents=cards.flatMap {card ->card.scores}.filter {it.checkedPaperUrl.isNotBlank()||it.answerKeyUrl.isNotBlank()}
        if(documents.isEmpty()) Text("No marked-paper documents available.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        documents.forEach {score ->
          Text(score.subjectName,style=MaterialTheme.typography.titleSmall)
          if(score.checkedPaperUrl.isNotBlank()) SchoolDocumentButton(score.checkedPaperUrl,"Checked paper")
          if(score.answerKeyUrl.isNotBlank()) SchoolDocumentButton(score.answerKeyUrl,"Answer key")
        }
      }
    }}}
  }
}

private fun profileCurrency(paise: Long): String = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(java.math.BigDecimal(paise).movePointLeft(2))

@Composable
private fun InfoRow(label: String, value: String) {SchoolDetailRow(label,value)}
