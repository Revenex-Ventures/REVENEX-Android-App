package com.example.ui.screens.principal

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.screens.attendance.parseSchoolRecordDate
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

/** Escape both CSV syntax and spreadsheet formula cells. No remote export service. */
internal fun principalCsv(rows:List<List<String>>):String=rows.joinToString("\r\n") {row->row.joinToString(",") {cell->
 val safe=if(cell.trimStart().firstOrNull() in listOf('=','+','-','@')||cell.startsWith('\t')||cell.startsWith('\r'))"'$cell" else cell
 "\"${safe.replace("\"","\"\"")}\""
}}+"\r\n"
private data class SavedReport(val name:String,val uri:Uri,val bytes:Int,val date:String)
private data class PendingReport(val actor:String,val name:String,val csv:String)

@Composable fun PrincipalReports(repository:ErpDataRepository) {
 val students by repository.students.collectAsState();val staff by repository.teachers.collectAsState()
 val fees by repository.feeRecords.collectAsState();val registers by repository.classAttendance.collectAsState()
 val cards by repository.reportCards.collectAsState();val actor by repository.currentUser.collectAsState()
 var period by rememberSaveable(actor.id){mutableStateOf("All records")}
 var category by rememberSaveable(actor.id){mutableStateOf("Attendance")};var detail by rememberSaveable(actor.id){mutableStateOf(false)}
 val context=LocalContext.current;val scope=rememberCoroutineScope();val tableScroll=rememberScrollState()
 var pending by remember(actor.id){mutableStateOf<PendingReport?>(null)}
 var busy by remember(actor.id){mutableStateOf(false)};var message by remember(actor.id){mutableStateOf<String?>(null)}
 val recent=remember(actor.id){mutableStateListOf<SavedReport>()}
 fun inPeriod(value:String):Boolean {
  if(period=="All records")return true
  val date=parseSchoolRecordDate(value) ?: return false;val now=LocalDate.now()
  val start=if(period=="This week")now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))else now.withDayOfMonth(1)
  return !date.isBefore(start)&&!date.isAfter(now)
 }
 val rows:List<List<String>> = when(category) {
  "Attendance"->listOf(listOf("Date","Class","Slot","Present / total"))+registers.values.filter{inPeriod(it.date)}.sortedByDescending{parseSchoolRecordDate(it.date)}.map{listOf(it.date,"${it.classGrade}-${it.division}",it.sessionSlot,"${it.presentCount+it.lateCount+it.excusedCount} / ${it.totalStudents}")}
  "Results"->listOf(listOf("Student","Class","Term","Score","Status"))+cards.map{listOf(it.studentName,"${it.classGrade}-${it.division}",it.term,"${it.totalObtained} / ${it.totalMax}",if(it.published)"Published"else"Draft")}
  "Fees"->listOf(listOf("Student","Class","Billed INR","Collected INR","Due INR"))+fees.map{listOf(it.studentName,"${it.classGrade}-${it.division}",java.math.BigDecimal.valueOf(it.totalFee,2).toPlainString(),java.math.BigDecimal.valueOf(it.paidAmount,2).toPlainString(),java.math.BigDecimal.valueOf(it.pendingAmount,2).toPlainString())}
  "Admissions"->listOf(listOf("Student","Class","Admission","Date","Status"))+students.filter{inPeriod(it.admissionDate)}.map{listOf(it.name,it.fullClass,it.admissionNumber,it.admissionDate,it.status)}
  else->listOf(listOf("Staff","Department","Classes","Status"))+staff.map{listOf(it.name,it.department,it.assignedClasses.joinToString(),it.status)}
 }
 val scopeLabel=if(category in listOf("Attendance","Admissions"))period else "Loaded school session"
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) {uri->
  val request=pending;pending=null
  if(uri==null){busy=false;message="Export cancelled"}
  else if(request==null||repository.currentUser.value.id!=request.actor){busy=false;message="Export stopped: account changed"}
  else scope.launch {
   try {
    check(repository.currentUser.value.id==request.actor)
    val bytes=request.csv.toByteArray(Charsets.UTF_8)
    withContext(Dispatchers.IO){context.contentResolver.openOutputStream(uri,"w")?.use{it.write(bytes)} ?: error("No document writer")}
    if(repository.currentUser.value.id==request.actor){recent.add(0,SavedReport(request.name,uri,bytes.size,LocalDate.now().toString()));message="Report saved"}
   } catch(e:CancellationException){throw e}
   catch(e:Exception){message="Could not save report. Check the selected location and retry."}
   finally {busy=false}
  }
 }
 BackHandler(detail){detail=false}
 val back=LocalWorkspaceBack.current
 CompositionLocalProvider(LocalWorkspaceBack provides if(detail)({detail=false})else back) {
 Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={Column(Modifier.padding(horizontal=Spacing.screenPadding,vertical=Spacing.sm8)) {
  Text("$category · $scopeLabel",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  message?.let{Text(it,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  Button(onClick={if(!busy){val name="Revenex-${category.lowercase()}-${LocalDate.now()}.csv";pending=PendingReport(actor.id,name,principalCsv(rows));busy=true;message=null;try{launcher.launch(name)}catch(e:Exception){pending=null;busy=false;message="No document picker available"}}},enabled=!busy&&rows.size>1,modifier=Modifier.fillMaxWidth().testTag("principal_export"),shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.xs)){Icon(Icons.Outlined.FileUpload,null);Spacer(Modifier.width(Spacing.sm8));Text(if(busy)"Saving…"else"Export $category")}
 }}) {padding->
 LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("principal_reports"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
  item {PrincipalPageHeader(if(detail)category else "Reports"){if(category in listOf("Attendance","Admissions"))PrincipalSelect(period,listOf("All records","This week","This month"),{period=it},Modifier.width(PrincipalLayout.contextWidth))else Text("Loaded session",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
  if(!detail) {
   item {PrincipalModules(listOf(PrincipalModule("Attendance",PrincipalIcons.Students,"Attendance",0),PrincipalModule("Results",PrincipalIcons.Reports,"Results",2),PrincipalModule("Fees",Icons.Outlined.CurrencyRupee,"Fees",3),PrincipalModule("Admissions",Icons.Outlined.PersonAdd,"Admissions",3),PrincipalModule("Staff",Icons.Outlined.People,"Staff",0)),{category=it;detail=true;message=null},compact=true)}
   item {PrincipalHeading("Recent") {
    if(recent.isEmpty())Text("No reports exported in this visit.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    recent.forEach {file->SchoolRow(file.name,"CSV · ${file.bytes} bytes · ${file.date}",Icons.Outlined.Description,{
     try{context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(file.uri,"text/csv").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))}catch(e:Exception){message="No CSV reader installed. Open the file from your document app."}
    })}
   }}
  } else {
   item {Text("$scopeLabel · ${rows.size-1} records",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   if(rows.size==1)item {EmptyStateView("No records","No authorized records match this report scope.")}
   itemsIndexed(rows) {index,row->
    Row(Modifier.fillMaxWidth().horizontalScroll(tableScroll).padding(vertical=Spacing.md12)) {
     row.forEach {cell->Text(cell,Modifier.width(PrincipalLayout.reportColumn).padding(end=Spacing.sm8),style=MaterialTheme.typography.bodyMedium,fontWeight=if(index==0)FontWeight.Bold else FontWeight.Normal)}
    }
    HorizontalDivider()
   }
  }
 }
 }
 }
}
