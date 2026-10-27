package com.example.ui.screens.principal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.screens.attendance.parseSchoolRecordDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun PrincipalAttendanceOverview(repository:ErpDataRepository,openClass:(String,String)->Unit,report:()->Unit) {
 val students by repository.students.collectAsState();val saved by repository.classAttendance.collectAsState()
 val dates=saved.values.mapNotNull {parseSchoolRecordDate(it.date)}.distinct().sortedDescending()
 var selected by remember {mutableStateOf<LocalDate?>(null)}
 val date=selected ?: dates.firstOrNull() ?: LocalDate.now()
 val onDate=saved.values.filter {parseSchoolRecordDate(it.date)==date}
 val slots=onDate.map {it.sessionSlot}.distinct().sorted()
 var chosenSlot by remember(date){mutableStateOf<String?>(null)}
 val slot=chosenSlot ?: slots.firstOrNull {it.contains("Morning",true)} ?: slots.firstOrNull()
 val records=onDate.filter {it.sessionSlot==slot}
 var info by remember {mutableStateOf(false)}
 val total=records.sumOf {it.totalStudents};val present=records.sumOf {it.presentCount+it.lateCount+it.excusedCount};val absent=records.sumOf {it.absentCount}
 val format=DateTimeFormatter.ofPattern("dd MMM uuuu",Locale.getDefault())
 val choices=(listOf(LocalDate.now())+dates).distinct().sortedDescending()
 Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={Surface(color=MaterialTheme.colorScheme.background){Button(onClick=report,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.xs),modifier=Modifier.fillMaxWidth().padding(horizontal=Spacing.screenPadding,vertical=Spacing.sm8)){Icon(Icons.Outlined.BarChart,null);Spacer(Modifier.width(Spacing.sm8));Text("View report")}}}) {padding->
 LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("principal_attendance_overview"),contentPadding=PaddingValues(horizontal=if(LocalPrincipalDark.current)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  item {Column(verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
   PrincipalPageHeader("Attendance"){IconButton(onClick={info=true}){Icon(Icons.Outlined.Info,"Attendance scope")}}
   PrincipalSelect(date.format(format),choices.map {it.format(format)},{value->selected=choices.first {it.format(format)==value}})
   if(slots.size>1)PrincipalSelect(slot.orEmpty(),slots,{chosenSlot=it})
   PrincipalTabs(listOf("Students"),0,{})
   Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
    PrincipalSummary("Present",if(total==0)"—" else String.format(Locale.US,"%.0f%%",present*100.0/total),"($present/$total)",PrincipalIcons.Students,PrincipalColors.mint,PrincipalColors.mintInk,Modifier.weight(1f))
    PrincipalSummary("Absent",if(total==0)"—" else String.format(Locale.US,"%.0f%%",absent*100.0/total),"($absent/$total)",Icons.Outlined.PersonOff,SemanticColors.dangerContainer,SemanticColors.onDanger,Modifier.weight(1f))
   }
  }}
  items(students.map {it.fullClass}.distinct().sortedWith(compareByDescending<String> {c->records.any {"${it.classGrade}-${it.division}"==c}}.thenBy {it})) {cohort->
   val rows=records.filter {"${it.classGrade}-${it.division}"==cohort};val count=rows.sumOf {it.totalStudents};val p=rows.sumOf {it.presentCount+it.lateCount+it.excusedCount}
   Column(Modifier.fillMaxWidth().clickable {openClass(cohort,date.toString())}.padding(vertical=Spacing.sm8).testTag("principal_class_$cohort"),verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
     Text("Class $cohort",Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
     Text(if(count==0)"No records" else "$p / $count",style=MaterialTheme.typography.bodySmall)
     Text(if(count==0)"—" else String.format(Locale.US,"%.0f%%",p*100.0/count),style=MaterialTheme.typography.labelLarge,color=PrincipalColors.mintInk)
     Icon(Icons.Outlined.ChevronRight,null)
    }
    LinearProgressIndicator(progress={if(count==0)0f else p.toFloat()/count},modifier=Modifier.fillMaxWidth(),color=PrincipalColors.mintInk,trackColor=PrincipalColors.mint)
   }
  }
  if(students.isEmpty())item {EmptyStateView("No classes","No student classes are available to your account.")}
 }
 }
 if(info)AlertDialog(onDismissRequest={info=false},title={Text("Attendance scope")},text={Text("Saved student registers for $date · ${slot ?: "no slot available"}. Staff attendance and corrections are not configured.")},confirmButton={TextButton(onClick={info=false}){Text("Done")}})
}


@Composable fun PrincipalClassHistory(repository:ErpDataRepository,cohort:String,date:String,onBack:()->Unit) {
 val students by repository.students.collectAsState();val registers by repository.classAttendance.collectAsState()
 var selected by remember {mutableStateOf<String?>(null)};var query by remember {mutableStateOf("")}
 val day=registers.values.filter {"${it.classGrade}-${it.division}"==cohort&&parseSchoolRecordDate(it.date)?.toString()==date}
 val pupils=students.filter {it.fullClass==cohort&&(it.name.contains(query,true)||it.rollNumber.toString().contains(query))}.sortedBy {it.rollNumber}
 LazyColumn(Modifier.fillMaxSize().testTag("principal_class_history"),contentPadding=PaddingValues(if(LocalPrincipalDark.current)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  item {PrincipalPageHeader("Class $cohort");Text(date,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  item {OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Search students")},leadingIcon={Icon(Icons.Outlined.Search,null)})}
  if(pupils.isEmpty())item {EmptyStateView("No students","No accessible students match this class or search.")}
  items(pupils,key={it.id}){student->
   val entries=day.flatMap {it.studentList}.filter {it.studentId==student.id}
   PrincipalRecordRow(student.name,"Roll ${student.rollNumber}",{selected=student.id},Modifier.testTag("principal_history_${student.id}")) {
    Text(entries.joinToString {it.status.label}.ifBlank {"No record"},style=MaterialTheme.typography.labelSmall)
   }
  }
 }
 selected?.let {id->
  val student=students.firstOrNull {it.id==id}
  val rows=registers.values.mapNotNull {r->r.studentList.firstOrNull {it.studentId==id}?.let {Triple(r.date,r.sessionSlot,it.status)}}.sortedByDescending {parseSchoolRecordDate(it.first)}
  AlertDialog(onDismissRequest={selected=null},title={Text(student?.name ?: "Attendance history")},text={LazyColumn(Modifier.heightIn(max=PrincipalLayout.historyHeight),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   if(rows.isEmpty())item {Text("No saved history")}
   items(rows){(day,slot,status)->Column {SchoolDetailRow(day,status.label);Text(slot,style=MaterialTheme.typography.bodySmall);HorizontalDivider()}}
  }},confirmButton={TextButton(onClick={selected=null}){Text("Done")}})
 }
}
