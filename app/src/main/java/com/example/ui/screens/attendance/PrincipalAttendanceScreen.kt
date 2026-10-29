package com.example.ui.screens.attendance

import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalAttendanceScreen(repository: ErpDataRepository,onBack: () -> Unit,initialClass:String?=null,initialDate:String?=null) {
  val students by repository.students.collectAsState()
  val records by repository.classAttendance.collectAsState()
  var cohort by remember {mutableStateOf<String?>(initialClass)}
  var historyStudent by remember {mutableStateOf<Student?>(null)}
  var query by remember {mutableStateOf("")}
  val classes=students.map {it.fullClass}.distinct().sorted()
  val scoped=records.values.filter {(cohort==null||"${it.classGrade}-${it.division}"==cohort)&&(initialDate==null||attendanceDateMatches(it.date,initialDate))}
  Scaffold(topBar={TopAppBar(title={Text(if(cohort==null)"Attendance overview" else "Class $cohort",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick={if(cohort!=null&&initialClass==null){cohort=null;query=""}else onBack()}){Icon(Icons.Default.ArrowBack,"Back")}})}) {padding ->
    LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("principal_attendance_overview"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      item {
        Text("All loaded saved registers",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          SchoolMetric(scoped.size.toString(),"Registers",Icons.Default.CalendarMonth,Modifier.weight(1f))
          SchoolMetric(scoped.sumOf {it.presentCount}.toString(),"Present entries",Icons.Default.CheckCircle,Modifier.weight(1f),SchoolMetricTone.POSITIVE)
          SchoolMetric(scoped.sumOf {it.absentCount}.toString(),"Absent entries",Icons.Default.PersonOff,Modifier.weight(1f),SchoolMetricTone.DANGER)
        }
      }
      item {OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text(if(cohort==null)"Search class" else "Search student or roll")},singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)},shape=RoundedCornerShape(Radius.sm))}
      if(cohort==null) {
        if(classes.isEmpty()) item {EmptyStateView(title="No class records",message="No accessible student classes are loaded.")}
        items(classes.filter {query.isBlank()||it.contains(query,true)}) {section ->
          val saved=records.values.filter {"${it.classGrade}-${it.division}"==section}
          SchoolGroup {SchoolRow("Class $section","${students.count {it.fullClass==section}} students · ${saved.size} saved registers",Icons.Default.Groups,{cohort=section;query=""},if(saved.isEmpty())"No records" else "Review")}
        }
      } else {
        val pupils=students.filter {it.fullClass==cohort&&(query.isBlank()||it.name.contains(query,true)||it.rollNumber.toString()==query)}.sortedBy {it.rollNumber}
        if(pupils.isEmpty()) item {EmptyStateView(title="No matching students",message="Change the search to see other students in this class.")}
        items(pupils,key={it.id}) {student ->
          val entries=scoped.flatMap {it.studentList}.filter {it.studentId==student.id}
          val present=entries.count {it.status==AttendanceStatus.PRESENT||it.status==AttendanceStatus.LATE||it.status==AttendanceStatus.EXCUSED}
          val rate=if(entries.isEmpty()) "No records" else "${(present*100.0/entries.size).toInt()}%"
          SchoolGroup {SchoolRow(student.name,"Roll ${student.rollNumber} · ${student.admissionNumber}",Icons.Default.AccountCircle,{historyStudent=student},rate,Modifier.testTag("student_att_${student.id}"))}
        }
      }
    }
  }
  historyStudent?.let {student ->
    val history=records.values.mapNotNull {record ->record.studentList.firstOrNull {it.studentId==student.id}?.let {Triple(record.date,record.sessionSlot,it.status)}}
    AlertDialog(onDismissRequest={historyStudent=null},title={Text(student.name,style=MaterialTheme.typography.titleMedium)},text={
      if(history.isEmpty()) Text("No saved attendance history is available.")
      else LazyColumn(Modifier.heightIn(max=360.dp),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        items(history){(date,slot,status)->Column {Row(Modifier.fillMaxWidth()){Text(date,Modifier.weight(1f));Text(status.label,fontWeight=FontWeight.SemiBold)};Text(slot,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);HorizontalDivider()}}
      }
    },confirmButton={TextButton(onClick={historyStudent=null}){Text("Close")}})
  }
}
