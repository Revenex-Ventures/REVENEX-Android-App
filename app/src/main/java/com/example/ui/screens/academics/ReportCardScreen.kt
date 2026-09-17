package com.example.ui.screens.academics

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.ReportCard
import com.example.data.model.Student
import com.example.data.model.SubjectScore
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.Avatar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

// ============================================================================
// STUDENT PROFILE / ACCOUNT — replaces the old generic Report Card tab.
//   • Profile hub: identity card + aligned account options
//   • Personal Details (edit contact & profile info)
//   • Academic Reports (year-wise grade cards + PDF download)
// ============================================================================

private enum class ProfileSection { HOME, DETAILS, REPORTS, LEAVING, BONAFIDE }

@Composable
fun ReportCardScreen(
  repository: ErpDataRepository
) {
  val context = LocalContext.current
  val students by repository.students.collectAsState()
  val reportCards by repository.reportCards.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()

  val student = remember(students, selectedStudentId) {
    students.firstOrNull { it.id == selectedStudentId.ifBlank { "stu_1001" } }
      ?: students.firstOrNull()
  } ?: return

  val annualReports = remember(reportCards, student.id) {
    buildAnnualReports(student, reportCards)
  }

  var section by remember { mutableStateOf(ProfileSection.HOME) }

  // Profile image picker → copy to internal storage → update Student in repository.
  val saveScope = rememberCoroutineScope()
  val imagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult
    saveScope.launch {
      val savedPath = withContext(Dispatchers.IO) {
        try {
          val dir = File(context.filesDir, "avatars").apply { mkdirs() }
          val out = File(dir, "${student.id}.jpg")
          context.contentResolver.openInputStream(uri)?.use { input ->
            val bmp = BitmapFactory.decodeStream(input)
            out.outputStream().use { fos -> bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, fos) }
          }
          out.absolutePath
        } catch (e: Exception) {
          null
        }
      }
      if (savedPath != null) {
        repository.updateStudent(student.copy(avatarUrl = savedPath))
        Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
      }
    }
  }

  BackHandler(enabled = section != ProfileSection.HOME) { section = ProfileSection.HOME }

  val latestReport = annualReports.lastOrNull()

  when (section) {
    ProfileSection.HOME -> ProfileHub(
      student = student,
      summaryLabel = latestReport?.academicYear.orEmpty(),
      summaryValue = latestReport?.let { "Grade ${it.overallGrade}" }.orEmpty(),
      onPickPhoto = { imagePicker.launch("image/*") },
      onOpenDetails = { section = ProfileSection.DETAILS },
      onOpenReports = { section = ProfileSection.REPORTS },
      onOpenLeaving = { section = ProfileSection.LEAVING },
      onOpenBonafide = { section = ProfileSection.BONAFIDE }
    )
    ProfileSection.DETAILS -> DetailsPage(
      student = student,
      onSave = { updated -> repository.updateStudent(updated) },
      onBack = { section = ProfileSection.HOME }
    )
    ProfileSection.REPORTS -> ReportsPage(
      student = student,
      annualReports = annualReports,
      onBack = { section = ProfileSection.HOME }
    )
    ProfileSection.LEAVING -> LeavingCertificatePage(
      student = student,
      report = latestReport,
      onBack = { section = ProfileSection.HOME }
    )
    ProfileSection.BONAFIDE -> BonafideCertificatePage(
      student = student,
      report = latestReport,
      onBack = { section = ProfileSection.HOME }
    )
  }
}

// ── Profile hub: identity card + aligned account options ─────────────────────
@Composable
private fun ProfileHub(
  student: Student,
  summaryLabel: String = "",
  summaryValue: String = "",
  onPickPhoto: () -> Unit,
  onOpenDetails: () -> Unit,
  onOpenReports: () -> Unit,
  onOpenLeaving: () -> Unit,
  onOpenBonafide: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      ProfileHeaderCard(student = student, onPickPhoto = onPickPhoto, onEditDetails = onOpenDetails)
    }

    item { SectionLabel("Academic") }

    item {
      ProfileMenuCard(
        icon = Icons.Filled.ReceiptLong,
        title = "Academic Performance",
        subtitle = "Year-wise marks, grades & PDF report",
        containerColor = ScholaTerracottaContainer,
        contentColor = RevenexOnPrimaryContainer,
        summaryLabel = summaryLabel,
        summaryValue = summaryValue,
        onClick = onOpenReports
      )
    }

    item { SectionLabel("Certificates") }

    item {
      ProfileMenuCard(
        icon = Icons.Filled.Article,
        title = "Leaving Certificate",
        subtitle = "School leaving / transfer certificate",
        containerColor = ScholaGoldContainer,
        contentColor = ScholaGoldText,
        onClick = onOpenLeaving
      )
    }

    item {
      ProfileMenuCard(
        icon = Icons.Filled.VerifiedUser,
        title = "Bonafide Certificate",
        subtitle = "Proof of current enrolment",
        containerColor = StatusSuccessBg,
        contentColor = StatusSuccessText,
        onClick = onOpenBonafide
      )
    }
  }
}

// ── Section label for grouped menu ───────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text.uppercase(Locale.US),
    style = MaterialTheme.typography.labelSmall,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    letterSpacing = 1.2.sp,
    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
  )
}

// ── Aligned menu card: icon + title + subtitle + (summary chip | chevron) ─────
@Composable
private fun ProfileMenuCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  containerColor: Color,
  contentColor: Color,
  summaryLabel: String = "",
  summaryValue: String = "",
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(contentColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(23.dp))
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ScholaTextSecondary)
      }
      if (summaryValue.isNotBlank()) {
        Column(horizontalAlignment = Alignment.End) {
          Text(summaryLabel, style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          Surface(shape = RoundedCornerShape(8.dp), color = contentColor) {
            Text(
              text = summaryValue,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelMedium,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }
      } else {
        Icon(
          Icons.Filled.ChevronRight,
          contentDescription = null,
          tint = contentColor.copy(alpha = 0.6f),
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}

// ── Shared sub-page scaffold: back bar + scrollable content ──────────────────
@Composable
private fun SubPageScaffold(
  title: String,
  onBack: () -> Unit,
  content: @Composable ColumnScope.() -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 6.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
      }
      Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
      content = content
    )
  }
}

// ── Personal details page ─────────────────────────────────────────────────────
@Composable
private fun DetailsPage(
  student: Student,
  onSave: (Student) -> Unit,
  onBack: () -> Unit
) {
  SubPageScaffold(title = "Personal Details", onBack = onBack) {
    EditableProfileCard(student = student, onSave = onSave)
  }
}

// ── Academic reports page ─────────────────────────────────────────────────────
@Composable
private fun ReportsPage(
  student: Student,
  annualReports: List<ReportCard>,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val years = annualReports.map { it.academicYear }
  var selectedYear by remember { mutableStateOf(years.lastOrNull() ?: "") }
  val activeReport = annualReports.firstOrNull { it.academicYear == selectedYear } ?: annualReports.last()

  SubPageScaffold(title = "Academic Reports", onBack = onBack) {
    // 1. Year selector — aligned chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      years.forEach { year ->
        FilterChip(
          selected = year == selectedYear,
          onClick = { selectedYear = year },
          label = { Text(year, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RevenexNavy,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.weight(1f)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Professional report (selected year)
    ProfessionalReportView(report = activeReport, student = student)

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Download PDF
    Button(
      onClick = projectPdfDownload(context, student, activeReport),
      colors = ButtonDefaults.buttonColors(containerColor = ScholaTerracotta),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      shape = RoundedCornerShape(16.dp)
    ) {
      Icon(Icons.Default.Download, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Download ${activeReport.academicYear} Report (PDF)", fontWeight = FontWeight.Bold)
    }
  }
}

// ── Leaving certificate page ─────────────────────────────────────────────────
@Composable
private fun LeavingCertificatePage(
  student: Student,
  report: ReportCard?,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val rows = listOf(
    "Name of the Pupil" to student.name,
    "Father's / Guardian's Name" to student.parentName,
    "Nationality" to "Indian",
    "Date of Birth" to student.dob,
    "Class Last Attended" to student.fullClass,
    "Admission Number" to student.admissionNumber,
    "Date of Admission" to student.admissionDate,
    "Date of Leaving School" to (report?.issueDate ?: "As per school records"),
    "Class in which Studying" to student.fullClass,
    "Result / Class Passed" to (report?.let { "Passed — Grade ${it.overallGrade}" } ?: "Promoted"),
    "Conduct & Character" to "Good",
    "Reason for Leaving" to "On request of parent / guardian",
    "Remarks" to (if (student.feePendingAmount == 0L) "No dues pending" else "Fee dues as per records")
  )
  val note = "Certified that the above particulars are true and correct as per the official records of this institution."
  val issueDate = report?.issueDate ?: student.admissionDate

  SubPageScaffold(title = "Leaving Certificate", onBack = onBack) {
    CertificatePaper(title = "LEAVING CERTIFICATE") {
      rows.forEachIndexed { index, row -> CertRow(index + 1, row.first, row.second) }
      Text(
        text = note,
        style = MaterialTheme.typography.bodySmall,
        color = ScholaTextSecondary,
        textAlign = TextAlign.Justify,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
      )
      CertificateFooter(issueDate = issueDate)
    }
    CertificateDownloadButton(
      onClick = certificatePdfDownload(
        context = context,
        fileName = "Leaving_Certificate_${student.name.replace(" ", "_")}",
        title = "LEAVING CERTIFICATE",
        rows = rows,
        paragraphs = listOf(note),
        issueDate = issueDate
      )
    )
  }
}

// ── Bonafide certificate page ────────────────────────────────────────────────
@Composable
private fun BonafideCertificatePage(
  student: Student,
  report: ReportCard?,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val year = report?.academicYear ?: "2025-2026"
  val isFemale = student.gender.equals("Female", ignoreCase = true)
  val pronoun = if (isFemale) "She" else "He"
  val possessive = if (isFemale) "her" else "his"
  val paragraphs = listOf(
    "This is to certify that ${student.name}, ${if (isFemale) "daughter" else "son"} of ${student.parentName}, is a bona fide student of this school, studying in Class ${student.fullClass} during the academic year $year.",
    "As per the records of this institution, ${possessive} date of birth is ${student.dob} and the admission number is ${student.admissionNumber}. ${pronoun} is currently enrolled in the school and attending regular classes.",
    "This certificate is issued on the request of the student / parent for official purposes. It must not be used for any other purpose."
  )
  val issueDate = report?.issueDate ?: student.admissionDate

  SubPageScaffold(title = "Bonafide Certificate", onBack = onBack) {
    CertificatePaper(title = "BONAFIDE CERTIFICATE") {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
          text = "TO WHOMSOEVER IT MAY CONCERN",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = ScholaTextSecondary,
          letterSpacing = 1.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(14.dp))
        paragraphs.forEachIndexed { index, paragraph ->
          Text(
            text = paragraph,
            style = MaterialTheme.typography.bodyMedium,
            color = ScholaTextPrimary,
            textAlign = TextAlign.Justify,
            lineHeight = 20.sp
          )
          if (index < paragraphs.size - 1) Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ScholaSurfaceWarm)
            .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("STUDENT", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
            Text(student.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ScholaTextPrimary)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text("CLASS", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
            Text(student.fullClass, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ScholaTextPrimary)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text("SESSION", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
            Text(year, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ScholaTextPrimary)
          }
        }
      }
      CertificateFooter(issueDate = issueDate)
    }
    CertificateDownloadButton(
      onClick = certificatePdfDownload(
        context = context,
        fileName = "Bonafide_Certificate_${student.name.replace(" ", "_")}",
        title = "BONAFIDE CERTIFICATE",
        rows = emptyList(),
        paragraphs = paragraphs,
        issueDate = issueDate
      )
    )
  }
}

// ── Shared certificate paper (fixed light document look) ─────────────────────
@Composable
private fun CertificatePaper(
  title: String,
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(Color.White)
      .border(1.2.dp, RevenexNavy.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(RevenexNavy),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 14.dp)
      ) {
        Text(
          text = "REVENEX PUBLIC SCHOOL",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          letterSpacing = 1.sp
        )
        Text(
          text = "Affiliated to CBSE • Affiliation No. 2130167",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.85f)
        )
        Text(
          text = "Sector 21, New Delhi - 110075",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.7f)
        )
      }
    }

    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = RevenexNavy,
      letterSpacing = 2.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
    )
    Box(
      modifier = Modifier
        .padding(top = 8.dp)
        .align(Alignment.CenterHorizontally)
        .width(64.dp)
        .height(3.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(ScholaGold)
    )
    Spacer(modifier = Modifier.height(14.dp))
    content()
    Spacer(modifier = Modifier.height(6.dp))
  }
}

@Composable
private fun CertRow(number: Int, label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text("$number.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = ScholaTextSecondary, modifier = Modifier.width(24.dp))
    Text(label, style = MaterialTheme.typography.bodySmall, color = ScholaTextSecondary, modifier = Modifier.weight(1f))
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = ScholaTextPrimary,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(1f)
    )
  }
  HorizontalDivider(
    color = ScholaBorder,
    thickness = 0.5.dp,
    modifier = Modifier.padding(horizontal = 16.dp)
  )
}

@Composable
private fun CertificateFooter(issueDate: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom
  ) {
    Column {
      Text("Place: New Delhi", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
      Text("Date: $issueDate", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
    }
    Column(horizontalAlignment = Alignment.End) {
      Text("Dr. Arvind Sharma", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ScholaTextPrimary)
      Text("Principal", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
      Spacer(modifier = Modifier.height(6.dp))
      Text("(School Seal)", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
    }
  }
}

// ── Download button shared by both certificate pages ─────────────────────────
@Composable
private fun CertificateDownloadButton(onClick: () -> Unit) {
  Spacer(modifier = Modifier.height(18.dp))
  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(containerColor = ScholaTerracottaDark),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(50.dp)
  ) {
    Icon(Icons.Default.Download, contentDescription = null)
    Spacer(modifier = Modifier.width(8.dp))
    Text("Download Certificate (PDF)", fontWeight = FontWeight.Bold)
  }
}

// ── Profile header: picture (tappable) + identity ────────────────────────────
@Composable
private fun ProfileHeaderCard(
  student: Student,
  onPickPhoto: () -> Unit,
  onEditDetails: () -> Unit = {}
) {
  val accent = ScholaTerracotta
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = ScholaTerracottaContainer),
    border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(modifier = Modifier.size(96.dp)) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(Color.White)
              .border(1.dp, accent.copy(alpha = 0.25f), CircleShape)
              .clickable(onClick = onPickPhoto),
            contentAlignment = Alignment.Center
          ) {
            Avatar(
              name = student.name,
              size = 84.dp,
              imagePath = student.avatarUrl.ifBlank { null },
              background = accent,
              textColor = Color.White
            )
          }
          // Camera badge — drawn outside the clipped avatar so it is never cut off.
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(32.dp)
              .clip(CircleShape)
              .background(ScholaTerracottaDark)
              .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Filled.CameraAlt,
              contentDescription = "Change photo",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = student.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ScholaTextPrimary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Class ${student.fullClass} • Roll #${student.rollNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = ScholaTextSecondary
          )
          Text(
            text = "Adm. No. ${student.admissionNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = ScholaOnTerracottaContainer,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))
          StatusBadge(status = student.status)
        }
      }

      // Edit pencil — overlays the top-right of the card
      IconButton(
        onClick = onEditDetails,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(6.dp)
      ) {
        Icon(
          Icons.Filled.Edit,
          contentDescription = "Edit details",
          tint = ScholaOnTerracottaContainer,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

// ── Editable personal details ────────────────────────────────────────────────
@Composable
private fun EditableProfileCard(
  student: Student,
  onSave: (Student) -> Unit
) {
  var name by remember(student.id) { mutableStateOf(student.name) }
  var dob by remember(student.id) { mutableStateOf(student.dob) }
  var bloodGroup by remember(student.id) { mutableStateOf(student.bloodGroup) }
  var parentPhone by remember(student.id) { mutableStateOf(student.parentPhone) }
  var parentEmail by remember(student.id) { mutableStateOf(student.parentEmail) }
  var address by remember(student.id) { mutableStateOf(student.address) }
  var saved by remember(student.id) { mutableStateOf(true) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Personal Details",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
        Text(
          text = if (saved) "Saved" else "Unsaved changes",
          style = MaterialTheme.typography.labelSmall,
          color = if (saved) StatusSuccessText else ScholaTerracotta,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      OutlinedTextField(
        value = name,
        onValueChange = { name = it; saved = false },
        label = { Text("Full Name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = dob,
          onValueChange = { dob = it; saved = false },
          label = { Text("Date of Birth") },
          singleLine = true,
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = bloodGroup,
          onValueChange = { bloodGroup = it; saved = false },
          label = { Text("Blood Group") },
          singleLine = true,
          modifier = Modifier.weight(1f)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))

      OutlinedTextField(
        value = parentPhone,
        onValueChange = { parentPhone = it; saved = false },
        label = { Text("Parent Contact") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))

      OutlinedTextField(
        value = parentEmail,
        onValueChange = { parentEmail = it; saved = false },
        label = { Text("Parent Email") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))

      OutlinedTextField(
        value = address,
        onValueChange = { address = it; saved = false },
        label = { Text("Address") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(14.dp))

      Button(
        onClick = {
          onSave(
            student.copy(
              name = name.trim(),
              dob = dob.trim(),
              bloodGroup = bloodGroup.trim(),
              parentPhone = parentPhone.trim(),
              parentEmail = parentEmail.trim(),
              address = address.trim()
            )
          )
          saved = true
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (saved) StatusSuccessContainer else ScholaTerracotta,
          contentColor = if (saved) StatusSuccessText else Color.White
        ),
        enabled = !saved,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(if (saved) "Details Saved" else "Save Changes", fontWeight = FontWeight.Bold)
      }
    }
}

// ── Student report sheet (simple, real result-card format) ───────────────────
@Composable
private fun ProfessionalReportView(
  report: ReportCard,
  student: Student
) {
  val totalMax = report.scores.sumOf { it.maxMarks }
  val totalObt = report.scores.sumOf { it.obtainedMarks }
  val pct = if (totalMax > 0) totalObt * 100.0 / totalMax else 0.0

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(Color.White)
      .border(1.2.dp, RevenexNavy.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
  ) {
    // ── 1. School header band
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(RevenexNavy),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
          text = "REVENEX PUBLIC SCHOOL",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          letterSpacing = 1.sp
        )
        Text(
          text = "Affiliated to Central Board of Secondary Education",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.85f)
        )
        Text(
          text = "STUDENT ACADEMIC REPORT",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          modifier = Modifier.padding(top = 4.dp)
        )
        Text(
          text = "${report.term}   •   Academic Year ${report.academicYear}",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.85f)
        )
      }
    }

    // ── 2. Student particulars (simple label/value grid)
    Row(modifier = Modifier.fillMaxWidth()) {
      ReportField("Student Name", report.studentName, Modifier.weight(2f))
      ReportField("Class", "${report.classGrade}-${report.division}", Modifier.weight(1f))
    }
    Row(modifier = Modifier.fillMaxWidth()) {
      ReportField("Roll No.", "${report.rollNumber}", Modifier.weight(1f))
      ReportField("Adm. No.", student.admissionNumber, Modifier.weight(1f))
      ReportField("Attendance", "${String.format(Locale.US, "%.1f", report.attendanceRate)}%", Modifier.weight(1f))
    }

    // ── 3. Marks table
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(RevenexNavy)
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Text("SUBJECT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(2.2f))
      Text("MAX", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
      Text("OBTAINED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
      Text("GRADE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
    }

    report.scores.forEachIndexed { index, score ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (index % 2 == 0) Color.White else ScholaSurfaceWarm)
          .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(score.subjectName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ScholaTextPrimary, modifier = Modifier.weight(2.2f))
        Text("${score.maxMarks}", style = MaterialTheme.typography.bodyMedium, color = ScholaTextSecondary, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
        Text("${score.obtainedMarks}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RevenexBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Surface(
          shape = RoundedCornerShape(5.dp),
          color = StatusSuccessContainer,
          modifier = Modifier.weight(0.8f)
        ) {
          Text(score.grade, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StatusSuccessText, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 2.dp))
        }
      }
    }

    // ── 4. Grand total + overall grade
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(RevenexNavy)
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("GRAND TOTAL", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        Text(
          "$totalObt / $totalMax   (${String.format(Locale.US, "%.1f", pct)}%)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
      Text("GRADE ${report.overallGrade}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }

    // ── 5. Rank
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text("CLASS RANK", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
        Text("#${report.rankInClass} / ${report.totalStudents}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ScholaTextPrimary)
      }
      Column(horizontalAlignment = Alignment.End) {
        Text("OVERALL GRADE", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
        Text(report.overallGrade, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ScholaTextPrimary)
      }
    }

    // ── 6. Principal remark
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
      Text("PRINCIPAL'S REMARKS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ScholaTextSecondary)
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = "\u201C${report.principalRemark}\u201D",
        style = MaterialTheme.typography.bodyMedium,
        color = ScholaTextPrimary
      )
    }

    // ── 7. Signatures
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Column {
        Text("Issue Date: ${report.issueDate}", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
        Text("Digitally issued by Revenex", style = MaterialTheme.typography.labelSmall, color = StatusSuccessText, fontWeight = FontWeight.Bold)
      }
      Column(horizontalAlignment = Alignment.End) {
        Text("Dr. Arvind Sharma", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ScholaTextPrimary)
        Text("Principal & Academic Director", style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
      }
    }
  }
}

@Composable
private fun ReportField(label: String, value: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Text(label.uppercase(Locale.US), style = MaterialTheme.typography.labelSmall, color = ScholaTextSecondary)
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ScholaTextPrimary)
  }
}

@Composable
private fun RowScope.StudentInfoCell(label: String, value: String, weight: Float) {
  Column(
    modifier = Modifier
      .weight(weight)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Text(label.uppercase(Locale.US), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ScholaTextPrimary)
  }
}

@Composable
private fun InfoPill(label: String, value: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(ScholaSurfaceWarm)
      .padding(horizontal = 12.dp, vertical = 10.dp)
  ) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ScholaTextPrimary)
  }
}

// ── PDF render + save (Android PdfDocument → Downloads) ──────────────────────
private fun projectPdfDownload(
  context: android.content.Context,
  student: Student,
  report: ReportCard
): () -> Unit = {
  val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default)
  scope.launch {
    try {
      val uri = ReportPdfRenderer.renderAndSave(context, student, report)
      withContext(Dispatchers.Main) {
        Toast.makeText(
          context,
          if (uri != null) "Saved to Downloads → ${uri.lastPathSegment}" else "Could not save report",
          Toast.LENGTH_LONG
        ).show()
      }
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
      }
    }
  }
}

// ── Certificate PDF render + save (Android PdfDocument → Downloads) ──────────
private fun certificatePdfDownload(
  context: Context,
  fileName: String,
  title: String,
  rows: List<Pair<String, String>>,
  paragraphs: List<String>,
  issueDate: String
): () -> Unit = {
  val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default)
  scope.launch {
    try {
      val uri = CertificatePdfRenderer.renderAndSave(context, fileName, title, rows, paragraphs, issueDate)
      withContext(Dispatchers.Main) {
        Toast.makeText(
          context,
          if (uri != null) "Saved to Downloads → ${uri.lastPathSegment}" else "Could not save certificate",
          Toast.LENGTH_LONG
        ).show()
      }
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
      }
    }
  }
}

private object CertificatePdfRenderer {

  private const val W = 595
  private const val H = 842
  private const val M = 52

  private val ONYX = android.graphics.Color.rgb(17, 17, 17)
  private val ORANGE = android.graphics.Color.rgb(255, 90, 31)
  private val INK = android.graphics.Color.rgb(17, 17, 17)
  private val MUTED = android.graphics.Color.rgb(110, 110, 110)
  private val LIGHT = android.graphics.Color.rgb(245, 245, 245)
  private val WHITE = android.graphics.Color.WHITE

  fun renderAndSave(
    context: Context,
    fileName: String,
    title: String,
    rows: List<Pair<String, String>>,
    paragraphs: List<String>,
    issueDate: String
  ): Uri? {
    val doc = PdfDocument()
    val page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, 1).create())
    draw(page.canvas, title, rows, paragraphs, issueDate)
    doc.finishPage(page)
    val uri = try {
      saveToDownloads(context, doc, fileName)
    } finally {
      doc.close()
    }
    return uri
  }

  private fun draw(
    c: android.graphics.Canvas,
    title: String,
    rows: List<Pair<String, String>>,
    paragraphs: List<String>,
    issueDate: String
  ) {
    // Institutional header (black + orange rule)
    c.drawRect(0f, 0f, W.toFloat(), 108f, solid(ONYX))
    c.drawRect(0f, 108f, W.toFloat(), 112f, solid(ORANGE))
    centered(c, "REVENEX PUBLIC SCHOOL", 44f, text(WHITE, 22f, bold = true, spacing = 0.4f))
    centered(c, "Affiliated to CBSE • Affiliation No. 2130167", 68f, text(WHITE, 10f))
    centered(c, "Sector 21, New Delhi - 110075", 84f, text(WHITE, 9f))

    // Title + orange underline
    centered(c, title, 158f, text(INK, 15f, bold = true, spacing = 1.5f))
    c.drawRoundRect(RectF(W / 2f - 34f, 170f, W / 2f + 34f, 174f), 2f, 2f, solid(ORANGE))

    var y = 216f
    if (rows.isNotEmpty()) {
      rows.forEachIndexed { index, row ->
        if (index % 2 == 1) {
          c.drawRect(M.toFloat() - 8f, y - 14f, (W - M).toFloat() + 8f, y + 8f, solid(LIGHT))
        }
        c.drawText(row.first, M.toFloat(), y, text(MUTED, 10f))
        val valuePaint = text(INK, 10f, bold = true)
        c.drawText(row.second, (W - M) - valuePaint.measureText(row.second), y, valuePaint)
        y += 26f
      }
      y += 12f
    } else {
      y += 4f
    }

    paragraphs.forEach { paragraph ->
      y = wrapped(c, paragraph, M.toFloat(), y, (W - 2 * M).toFloat(), text(INK, 11f), 17f) + 16f
    }

    y = maxOf(y + 30f, 620f)
    c.drawLine(M.toFloat(), y, M + 190f, y, text(INK, 0f))
    c.drawText("Dr. Arvind Sharma", M.toFloat(), y + 18f, text(INK, 11f, bold = true))
    c.drawText("Principal & Academic Director", M.toFloat(), y + 33f, text(MUTED, 9f))

    c.drawLine((W - M) - 190f, y, W - M.toFloat(), y, text(INK, 0f))
    c.drawText("Place: New Delhi", (W - M) - 190f, y + 18f, text(INK, 11f, bold = true))
    c.drawText("Date: $issueDate", (W - M) - 190f, y + 33f, text(MUTED, 9f))
    c.drawText("(School Seal)", (W - M) - 190f, y + 50f, text(MUTED, 9f))

    centered(c, "This is a computer-generated document and does not require a physical signature.", 792f, text(MUTED, 8f))
    centered(c, "REVENEX PUBLIC SCHOOL • reckoning excellence since 2006", 806f, text(MUTED, 7f))
  }

  private fun wrapped(c: android.graphics.Canvas, text: String, x: Float, y: Float, maxWidth: Float, p: Paint, lineHeight: Float): Float {
    val words = text.split(" ")
    var line = ""
    var ty = y
    for (w in words) {
      val candidate = if (line.isEmpty()) w else "$line $w"
      if (p.measureText(candidate) > maxWidth) {
        c.drawText(line, x, ty, p)
        line = w
        ty += lineHeight
      } else {
        line = candidate
      }
    }
    if (line.isNotEmpty()) c.drawText(line, x, ty, p)
    return ty
  }

  private fun centered(c: android.graphics.Canvas, text: String, y: Float, p: Paint) {
    c.drawText(text, (W - p.measureText(text)) / 2f, y, p)
  }

  private fun text(color: Int, size: Float, bold: Boolean = false, spacing: Float = 0f): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      this.color = color
      if (size > 0f) textSize = size
      typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
      if (spacing > 0f) letterSpacing = spacing
    }

  private fun solid(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    style = Paint.Style.FILL
  }

  private fun saveToDownloads(context: Context, doc: PdfDocument, fileName: String): Uri? {
    val values = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, "$fileName.pdf")
      put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
      put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Revenex")
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
    resolver.openOutputStream(uri)?.use { os ->
      doc.writeTo(os)
    } ?: return null
    return uri
  }
}

private object ReportPdfRenderer {

  private const val W = 595
  private const val H = 842
  private const val M = 42

  private val NAVY = android.graphics.Color.rgb(11, 31, 58)
  private val GOLD = android.graphics.Color.rgb(196, 158, 32)
  private val INK = android.graphics.Color.rgb(26, 30, 38)
  private val MUTED = android.graphics.Color.rgb(108, 116, 128)
  private val LIGHT = android.graphics.Color.rgb(242, 244, 247)
  private val GREEN = android.graphics.Color.rgb(17, 122, 87)
  private val WHITE = android.graphics.Color.WHITE

  fun renderAndSave(context: Context, student: Student, report: ReportCard): Uri? {
    val doc = PdfDocument()
    val page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, 1).create())
    drawSheet(page.canvas, student, report)
    doc.finishPage(page)

    val uri = try {
      saveToDownloads(context, doc, student, report)
    } finally {
      doc.close()
    }
    return uri
  }

  private fun drawSheet(c: android.graphics.Canvas, student: Student, report: ReportCard) {
    // Header band
    c.drawRect(0f, 0f, W.toFloat(), 118f, paintSolid(NAVY))
    c.drawRect(0f, 118f, W.toFloat(), 121f, paintSolid(GOLD))
    drawCentered(c, "REVENEX PUBLIC SCHOOL", 44f, paintText(WHITE, 24f, bold = true, spacing = 0.5f))
    drawCentered(c, "Affiliated to Central Board of Secondary Education (CBSE)", 64f, paintText(WHITE, 10f))
    drawCentered(c, "STATEMENT OF MARKS & EVALUATION", 86f, paintText(GOLD, 11f, bold = true, spacing = 0.5f))

    drawCentered(c, report.term.uppercase(Locale.US), 148f, paintText(INK, 14f, bold = true))
    drawCentered(c, "Academic Year ${report.academicYear}   •   Class ${report.classGrade}-${report.division}", 168f, paintText(MUTED, 10f))

    // Student info box
    c.drawRoundRect(M.toFloat(), 190f, (W - M).toFloat(), 300f, 10f, 10f, paintText(LIGHT, 0f, fill = true))
    val info = listOf(
      "Student Name" to report.studentName,
      "Admission Number" to student.admissionNumber,
      "Roll Number" to "${report.rollNumber}",
      "Date of Birth" to student.dob,
      "Blood Group" to student.bloodGroup,
      "Attendance Rate" to "${String.format(Locale.US, "%.1f", report.attendanceRate)}%"
    )
    var ty = 218f
    info.chunked(2).forEach { pair ->
      columnValue(c, M + 18f, ty, pair.getOrNull(0))
      columnValue(c, W / 2f + 18f, ty, pair.getOrNull(1))
      ty += 28f
    }

    // Scores table
    val tableTop = 322f
    val headerH = 26f
    val cols = tableColumns()
    c.drawRect(M.toFloat(), tableTop, (W - M).toFloat(), tableTop + headerH, paintSolid(NAVY))
    rowCell(c, "SUBJECT", cols[0], tableTop, headerH, paintText(WHITE, 9f, bold = true), left = true)
    rowCell(c, "MAX", cols[1], tableTop, headerH, paintText(WHITE, 9f, bold = true))
    rowCell(c, "MARKS OBTAINED", cols[2], tableTop, headerH, paintText(WHITE, 9f, bold = true))
    rowCell(c, "GRADE", cols[3], tableTop, headerH, paintText(WHITE, 9f, bold = true), right = true)

    var rowY = tableTop + headerH
    report.scores.forEachIndexed { index, score ->
      if (index % 2 == 1) c.drawRect(M.toFloat(), rowY, (W - M).toFloat(), rowY + 26f, paintSolid(LIGHT))
      rowCell(c, score.subjectName, cols[0], rowY, 26f, paintText(INK, 9f, bold = true), left = true)
      rowCell(c, "${score.maxMarks}", cols[1], rowY, 26f, paintText(INK, 9f))
      rowCell(c, "${score.obtainedMarks}", cols[2], rowY, 26f, paintText(INK, 9f, bold = true))
      val gx = cols[3]
      val gradePaint = paintText(GREEN, 9f, bold = true)
      val gradeBox = RectF(
        gx.first + 2f, rowY + 7f, gx.second - 2f, rowY + 20f
      )
      c.drawRoundRect(gradeBox, 4f, 4f, paintSolid(android.graphics.Color.rgb(228, 241, 236)))
      drawCenteredIn(c, score.grade, gradeBox, gradePaint)
      rowY += 26f
    }

    // Totals row
    val totalMax = report.scores.sumOf { it.maxMarks }
    val totalObt = report.scores.sumOf { it.obtainedMarks }
    val pct = if (totalMax > 0) totalObt * 100.0 / totalMax else 0.0
    c.drawRect(M.toFloat(), rowY, (W - M).toFloat(), rowY + 26f, paintSolid(GOLD))
    rowCell(c, "GRAND TOTAL", cols[0], rowY, 26f, paintText(WHITE, 9f, bold = true), left = true)
    rowCell(c, "$totalMax", cols[1], rowY, 26f, paintText(WHITE, 9f, bold = true))
    rowCell(c, "$totalObt (${String.format(Locale.US, "%.1f", pct)}%)", cols[2], rowY, 26f, paintText(WHITE, 9f, bold = true))
    rowCell(c, report.overallGrade, cols[3], rowY, 26f, paintText(WHITE, 9f, bold = true), right = true)
    rowY += 26f

    // Rank + summary line
    val summary = "Class Rank: #${report.rankInClass} / ${report.totalStudents}     •     ${report.scores.size} Subjects     •     Overall Grade: ${report.overallGrade}"
    drawWrapped(c, summary, M.toFloat(), rowY + 22f, (W - 2 * M).toFloat(), paintText(MUTED, 9f))

    // Principal remark
    val remarkY = rowY + 58f
    c.drawText("PRINCIPAL'S REMARKS", M.toFloat(), remarkY, paintText(MUTED, 8f, bold = true))
    drawWrapped(
      c, "\u201C${report.principalRemark}\u201D", M.toFloat(), remarkY + 16f, (W - 2 * M).toFloat(),
      paintText(INK, 10f)
    )

    // Signatures
    c.drawLine(M.toFloat(), 690f, M + 200f, 690f, paintText(INK, 0f))
    c.drawText("Dr. Arvind Sharma", M.toFloat(), 706f, paintText(INK, 10f, bold = true))
    c.drawText("Principal & Academic Director", M.toFloat(), 719f, paintText(MUTED, 8f))

    c.drawLine(W - M - 200f, 690f, W - M.toFloat(), 690f, paintText(INK, 0f))
    c.drawText("Date: ${report.issueDate}", W - M - 200f, 706f, paintText(INK, 10f, bold = true))
    c.drawText("Class Teacher / Co-ordinator", W - M - 200f, 719f, paintText(MUTED, 8f))

    drawCentered(c, "This is a computer-generated document and does not require a physical signature.", 800f, paintText(MUTED, 8f))
    drawCentered(c, "REVENEX PUBLIC SCHOOL • reckoning excellence since 2006", 814f, paintText(MUTED, 7f))
  }

  private fun tableColumns(): List<Pair<Float, Float>> {
    val left = M.toFloat()
    val right = (W - M).toFloat()
    val total = right - left
    return listOf(
      left to left + total * 0.46f,
      left + total * 0.46f to left + total * 0.60f,
      left + total * 0.60f to left + total * 0.82f,
      left + total * 0.82f to right
    )
  }

  private fun columnValue(c: android.graphics.Canvas, x: Float, y: Float, kv: Pair<String, String>?) {
    if (kv == null) return
    c.drawText(kv.first.uppercase(Locale.US), x, y, paintText(MUTED, 7f, bold = true))
    c.drawText(kv.second, x, y + 13f, paintText(INK, 9.5f, bold = true))
  }

  private fun rowCell(c: android.graphics.Canvas, text: String, col: Pair<Float, Float>, top: Float, h: Float, p: Paint, left: Boolean = false, right: Boolean = false) {
    val cx = (col.first + col.second) / 2f
    val cy = top + h / 2f + p.textSize * 0.35f
    if (left) c.drawText(text, col.first + 10f, top + h / 2f + p.textSize * 0.35f, p)
    else if (right) {
      c.drawText(text, col.second - 10f - p.measureText(text), top + h / 2f + p.textSize * 0.35f, p)
    } else drawCenteredIn(c, text, RectF(col.first, top, col.second, top + h), p)
  }

  private fun drawCentered(c: android.graphics.Canvas, text: String, y: Float, p: Paint) {
    val x = (W - p.measureText(text)) / 2f
    c.drawText(text, x, y, p)
  }

  private fun drawCenteredIn(c: android.graphics.Canvas, text: String, rect: RectF, p: Paint) {
    val x = rect.centerX() - p.measureText(text) / 2f
    val y = rect.centerY() + p.textSize * 0.35f
    c.drawText(text, x, y, p)
  }

  private fun drawWrapped(c: android.graphics.Canvas, text: String, x: Float, y: Float, maxWidth: Float, p: Paint) {
    val words = text.split(" ")
    var line = ""
    var ty = y
    for (w in words) {
      val test = if (line.isEmpty()) w else "$line $w"
      if (p.measureText(test) > maxWidth) {
        c.drawText(line, x, ty, p)
        line = w
        ty += p.textSize * 1.4f
      } else {
        line = test
      }
    }
    if (line.isNotEmpty()) c.drawText(line, x, ty, p)
  }

  private fun paintText(color: Int, size: Float, bold: Boolean = false, fill: Boolean = false, spacing: Float = 0f): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      this.color = color
      if (size > 0f) textSize = size
      typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
      if (fill) style = Paint.Style.FILL
      if (spacing > 0f) letterSpacing = spacing
    }

  private fun paintSolid(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    style = Paint.Style.FILL
  }

  private fun saveToDownloads(context: Context, doc: PdfDocument, student: Student, report: ReportCard): Uri? {
    val name = "${student.name.replace(" ", "_")}_${report.academicYear}.pdf"
    val values = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, name)
      put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
      put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Revenex")
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
    resolver.openOutputStream(uri)?.use { os ->
      doc.writeTo(os)
    } ?: return null
    return uri
  }
}

// ── Year-wise report seeding (demo) ──────────────────────────────────────────
private fun buildAnnualReports(student: Student, seeded: List<ReportCard>): List<ReportCard> {
  val own = seeded.filter { it.studentId == student.id }
  val years = listOf("2024-2025", "2025-2026", "2026-2027")
  return years.map { year ->
    own.firstOrNull { it.academicYear == year } ?: sampleReportFor(student, year)
  }
}

private fun sampleReportFor(student: Student, year: String): ReportCard {
  val base = listOf(
    Triple("Mathematics", 96, "Exceptional analytical clarity and precision."),
    Triple("Physics & Chemistry", 92, "Strong conceptual depth and lab work."),
    Triple("English Literature", 88, "Creative writing is vibrant; refine grammar."),
    Triple("Social Science", 90, "Excellent grasp of history and civics."),
    Triple("Computer Science (Python)", 98, "Top scorer in algorithms and logic."),
    Triple("Hindi (Second Language)", 85, "Good comprehension and expression.")
  )
  val yearIndex = (year.take(4).toIntOrNull() ?: 2024) % 3
  val drift = intArrayOf(3, -2, 0)[yearIndex]
  val maxMarks = 100
  val scores = base.map { (subject, marks, remark) ->
    val obtained = (marks + drift).coerceIn(80, 99)
    SubjectScore(subject, maxMarks, obtained, SubjectScore.calculateCbseGrade(obtained, maxMarks), remark)
  }
  val className = when (year) {
    "2024-2025" -> "8"
    "2025-2026" -> "9"
    else -> student.classGrade
  }
  val term = if (year == "2026-2027") "Term 1 Summative Assessment 2026" else "Annual Examination"
  val issueDate = when (year) {
    "2024-2025" -> "05 May 2025"
    "2025-2026" -> "18 May 2026"
    else -> "24 Aug 2026"
  }
  return ReportCard(
    id = "rep_${student.id}_${year.removePrefix("20").replace("-", "")}",
    studentId = student.id,
    studentName = student.name,
    classGrade = className,
    division = student.division,
    rollNumber = student.rollNumber,
    term = term,
    academicYear = year,
    scores = scores,
    attendanceRate = listOf(93.5, 95.2, 96.8)[yearIndex],
    rankInClass = listOf(6, 4, 3)[yearIndex],
    totalStudents = 42,
    principalRemark = "${student.name} demonstrates consistent diligence across all disciplines. An outstanding all-round academic profile — keep up the high standard!",
    issueDate = issueDate,
    published = true
  )
}