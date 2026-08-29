package com.example.ui.screens.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.data.datasource.FirebaseStorageManager
import com.example.data.model.ReportCard
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ReportCardScreen(
  repository: ErpDataRepository
) {
  val reportCards by repository.reportCards.collectAsState()
  val students by repository.students.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val activeReport = reportCards.firstOrNull { it.studentId == selectedStudentId } ?: ReportCard(
    id = "rep_sample",
    studentId = "stu_1001",
    studentName = "Aarav Patel",
    classGrade = "10",
    division = "A",
    rollNumber = 14,
    term = "Term 1 Summative Assessment 2026",
    academicYear = "2026-2027",
    scores = emptyList(),
    attendanceRate = 94.8,
    rankInClass = 3,
    totalStudents = 42,
    principalRemark = "Exemplary performance across analytical and computational subjects.",
    issueDate = "24 Aug 2026"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("report_card_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Official School Header Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RevenexNavy)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "REVENEX PUBLIC SCHOOL",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 1.sp
          )
          Text(
            text = "Affiliated to Central Board of Secondary Education (CBSE)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
          )
          Text(
            text = "OFFICIAL STATEMENT OF MARKS & EVALUATION",
            style = MaterialTheme.typography.labelSmall,
            color = RevenexGoldLight,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Student: ${activeReport.studentName}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Class: ${activeReport.classGrade}-${activeReport.division} (Roll #${activeReport.rollNumber})", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Rank: #${activeReport.rankInClass} / ${activeReport.totalStudents}", color = RevenexGoldLight, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Session: ${activeReport.academicYear}", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
    }

    // Subject Scores Table
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Scholastic Performance (Term 1)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Table Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
              .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Subject", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
            Text("Max", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
            Text("Obt.", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
            Text("Grade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
          }

          Spacer(modifier = Modifier.height(6.dp))

          activeReport.scores.forEach { score ->
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(2f)) {
                  Text(score.subjectName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                  Text(score.remarks, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
                Text("${score.maxMarks}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                Text("${score.obtainedMarks}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RevenexBlue, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = StatusSuccessContainer,
                  modifier = Modifier.weight(0.8f)
                ) {
                  Text(
                    text = score.grade,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = StatusSuccessText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 2.dp)
                  )
                }
              }

              // Checked Paper & Answer Key Actions Row
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (currentUser.role == UserRole.TEACHER || currentUser.role == UserRole.PRINCIPAL) {
                  var showPaperUploadDialog by remember { mutableStateOf(false) }
                  Text(
                    text = if (score.checkedPaperUrl.isEmpty()) "Upload Checked Paper" else "Paper: ${score.checkedPaperUrl}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showPaperUploadDialog = true }.testTag("upload_paper_${score.subjectName.replace(" ", "_")}")
                  )
                  
                  if (showPaperUploadDialog) {
                    val context = LocalContext.current
                    val uploadScope = rememberCoroutineScope()
                    var paperUri by remember { mutableStateOf<Uri?>(null) }
                    var paperMime by remember { mutableStateOf("") }
                    var paperName by remember { mutableStateOf("") }
                    var paperError by remember { mutableStateOf<String?>(null) }
                    var isPaperUploading by remember { mutableStateOf(false) }

                    val paperPicker = rememberLauncherForActivityResult(
                      contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                      paperError = null
                      if (uri != null) {
                        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                        if (!FirebaseStorageManager.isAllowedDocumentType(mime)) {
                          paperError = "Only PDF, DOC or DOCX files are accepted as a checked paper."
                          paperUri = null
                        } else {
                          paperUri = uri
                          paperMime = mime
                          paperName = uri.lastPathSegment?.substringAfterLast("/")
                            ?.takeIf { it.contains(".") }
                            ?: when (mime.lowercase()) {
                              "application/pdf" -> "checked_paper.pdf"
                              "application/msword" -> "checked_paper.doc"
                              else -> "checked_paper.docx"
                            }
                        }
                      } else {
                        paperUri = null
                      }
                    }

                    AlertDialog(
                      onDismissRequest = { showPaperUploadDialog = false },
                      title = { Text("Upload Student Checked Paper", fontWeight = FontWeight.Bold) },
                      text = {
                        Column {
                          Text(
                            text = if (paperUri == null) "Select the checked paper file (PDF / DOC / DOCX) for ${score.subjectName}." else "Selected: $paperName",
                            style = MaterialTheme.typography.bodySmall
                          )
                          Spacer(modifier = Modifier.height(10.dp))
                          OutlinedTextField(
                            value = if (paperUri == null) "No file selected" else paperName,
                            onValueChange = {},
                            readOnly = true,
                            isError = paperError != null,
                            supportingText = {
                              if (paperError != null) {
                                Text(paperError.orEmpty(), color = MaterialTheme.colorScheme.error)
                              }
                            },
                            trailingIcon = {
                              if (isPaperUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                              } else {
                                IconButton(onClick = { paperPicker.launch("*/*") }) {
                                  Icon(Icons.Default.AttachFile, contentDescription = "Choose File")
                                }
                              }
                            },
                            modifier = Modifier.fillMaxWidth()
                          )
                        }
                      },
                      confirmButton = {
                        Button(
                          enabled = paperUri != null && !isPaperUploading,
                          onClick = {
                            val uri = paperUri ?: return@Button
                            isPaperUploading = true
                            paperError = null
                            uploadScope.launch {
                              try {
                                val result = repository.uploadAttachment(
                                  context = context,
                                  folder = "checked_papers/${activeReport.studentId}",
                                  sourceUri = uri,
                                  displayName = paperName.ifBlank { null },
                                  mimeType = paperMime.ifBlank { "application/pdf" }
                                )
                                repository.saveCheckedPaper(
                                  studentId = activeReport.studentId,
                                  term = activeReport.term,
                                  subjectName = score.subjectName,
                                  checkedPaperUrl = result.url
                                )
                                showPaperUploadDialog = false
                              } catch (e: Exception) {
                                paperError = e.message ?: "Upload failed. Try again."
                              } finally {
                                isPaperUploading = false
                              }
                            }
                          }
                        ) {
                          Text("Upload")
                        }
                      },
                      dismissButton = {
                        TextButton(onClick = { showPaperUploadDialog = false }) {
                          Text("Cancel")
                        }
                      }
                    )
                  }

                  var showKeyUploadDialog by remember { mutableStateOf(false) }
                  Text(
                    text = if (score.answerKeyUrl.isEmpty()) "Upload Answer Key" else "Key: ${score.answerKeyUrl}",
                    color = Color(0xFF0F766E),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showKeyUploadDialog = true }.testTag("upload_key_${score.subjectName.replace(" ", "_")}")
                  )

                  if (showKeyUploadDialog) {
                    val context = LocalContext.current
                    val uploadScope = rememberCoroutineScope()
                    var keyUri by remember { mutableStateOf<Uri?>(null) }
                    var keyMime by remember { mutableStateOf("") }
                    var keyName by remember { mutableStateOf("") }
                    var keyError by remember { mutableStateOf<String?>(null) }
                    var isKeyUploading by remember { mutableStateOf(false) }

                    val keyPicker = rememberLauncherForActivityResult(
                      contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                      keyError = null
                      if (uri != null) {
                        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                        if (!FirebaseStorageManager.isAllowedDocumentType(mime)) {
                          keyError = "Only PDF, DOC or DOCX files are accepted as an answer key."
                          keyUri = null
                        } else {
                          keyUri = uri
                          keyMime = mime
                          keyName = uri.lastPathSegment?.substringAfterLast("/")
                            ?.takeIf { it.contains(".") }
                            ?: when (mime.lowercase()) {
                              "application/pdf" -> "answer_key.pdf"
                              "application/msword" -> "answer_key.doc"
                              else -> "answer_key.docx"
                            }
                        }
                      } else {
                        keyUri = null
                      }
                    }

                    AlertDialog(
                      onDismissRequest = { showKeyUploadDialog = false },
                      title = { Text("Upload Exam Answer Key (Class-wide)", fontWeight = FontWeight.Bold) },
                      text = {
                        Column {
                          Text(
                            text = if (keyUri == null) "Select the answer key file (PDF / DOC / DOCX) for ${activeReport.classGrade}-${activeReport.division} ${score.subjectName}." else "Selected: $keyName",
                            style = MaterialTheme.typography.bodySmall
                          )
                          Spacer(modifier = Modifier.height(10.dp))
                          OutlinedTextField(
                            value = if (keyUri == null) "No file selected" else keyName,
                            onValueChange = {},
                            readOnly = true,
                            isError = keyError != null,
                            supportingText = {
                              if (keyError != null) {
                                Text(keyError.orEmpty(), color = MaterialTheme.colorScheme.error)
                              }
                            },
                            trailingIcon = {
                              if (isKeyUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                              } else {
                                IconButton(onClick = { keyPicker.launch("*/*") }) {
                                  Icon(Icons.Default.AttachFile, contentDescription = "Choose File")
                                }
                              }
                            },
                            modifier = Modifier.fillMaxWidth()
                          )
                        }
                      },
                      confirmButton = {
                        Button(
                          enabled = keyUri != null && !isKeyUploading,
                          onClick = {
                            val uri = keyUri ?: return@Button
                            isKeyUploading = true
                            keyError = null
                            uploadScope.launch {
                              try {
                                val result = repository.uploadAttachment(
                                  context = context,
                                  folder = "answer_keys/${activeReport.classGrade}-${activeReport.division}",
                                  sourceUri = uri,
                                  displayName = keyName.ifBlank { null },
                                  mimeType = keyMime.ifBlank { "application/pdf" }
                                )
                                repository.saveAnswerKey(
                                  classGrade = activeReport.classGrade,
                                  term = activeReport.term,
                                  subjectName = score.subjectName,
                                  answerKeyUrl = result.url
                                )
                                showKeyUploadDialog = false
                              } catch (e: Exception) {
                                keyError = e.message ?: "Upload failed. Try again."
                              } finally {
                                isKeyUploading = false
                              }
                            }
                          }
                        ) {
                          Text("Upload")
                        }
                      },
                      dismissButton = {
                        TextButton(onClick = { showKeyUploadDialog = false }) {
                          Text("Cancel")
                        }
                      }
                    )
                  }
                } else {
                  if (score.checkedPaperUrl.isNotEmpty()) {
                    var showPaperViewer by remember { mutableStateOf(false) }
                    Text(
                      text = "View Checked Paper",
                      color = MaterialTheme.colorScheme.primary,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.clickable { showPaperViewer = true }.testTag("view_paper_${score.subjectName.replace(" ", "_")}")
                    )
                    if (showPaperViewer) {
                      AlertDialog(
                        onDismissRequest = { showPaperViewer = false },
                        title = { Text("Checked Exam Paper: ${score.subjectName}", fontWeight = FontWeight.Bold) },
                        text = {
                          Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("File: ${score.checkedPaperUrl} • Scanned Portrait", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                              modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color.LightGray, RoundedCornerShape(8.dp)),
                              contentAlignment = Alignment.Center
                            ) {
                              Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.DarkGray)
                            }
                          }
                        },
                        confirmButton = {
                          TextButton(onClick = { showPaperViewer = false }) {
                            Text("Close")
                          }
                        }
                      )
                    }
                  }
                  
                  if (score.answerKeyUrl.isNotEmpty()) {
                    var showKeyViewer by remember { mutableStateOf(false) }
                    Text(
                      text = "View Answer Key",
                      color = Color(0xFF0F766E),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.clickable { showKeyViewer = true }.testTag("view_key_${score.subjectName.replace(" ", "_")}")
                    )
                    if (showKeyViewer) {
                      AlertDialog(
                        onDismissRequest = { showKeyViewer = false },
                        title = { Text("Exam Answer Key: ${score.subjectName}", fontWeight = FontWeight.Bold) },
                        text = {
                          Column {
                            Text("File: ${score.answerKeyUrl} • Verified Answer Booklet", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Content Overview:\nSection A: Q1 - A, Q2 - C, Q3 - D\nSection B: Full derivation of standard formulas.\nSection C: Case study analysis outline.", style = MaterialTheme.typography.bodySmall)
                          }
                        },
                        confirmButton = {
                          TextButton(onClick = { showKeyViewer = false }) {
                            Text("Close")
                          }
                        }
                      )
                    }
                  }
                }
              }
            }
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Total Summary Row
          val totalMax = activeReport.scores.sumOf { it.maxMarks }
          val totalObt = activeReport.scores.sumOf { it.obtainedMarks }
          val overallPct = if (totalMax > 0) (totalObt.toDouble() / totalMax.toDouble()) * 100.0 else 0.0

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(RevenexPrimaryContainer, RoundedCornerShape(10.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Grand Total Marks", style = MaterialTheme.typography.labelSmall, color = RevenexOnPrimaryContainer)
              Text("$totalObt / $totalMax (${String.format("%.1f", overallPct)}%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RevenexOnPrimaryContainer)
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StatusSuccess
            ) {
              Text("Grade A1", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
          }
        }
      }
    }

    // Principal Sign & Remark
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Principal's Remarks & Endorsement:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "\"${activeReport.principalRemark}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Column {
              Text("Issue Date: ${activeReport.issueDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("Revenex Digitally Signed", style = MaterialTheme.typography.labelSmall, color = StatusSuccessText, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Dr. Arvind Sharma", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("Principal & Academic Director", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}
