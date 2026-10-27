package com.example.ui.screens.students.portal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.*
import java.time.format.DateTimeFormatter

internal data class StudentRegisterEntry(val date:LocalDate,val session:String,val status:AttendanceStatus)
internal fun studentAttendanceEntries(student:Student,records:List<ClassAttendanceRecord>,today:LocalDate=studentToday()):List<StudentRegisterEntry> = records.filter{it.schoolId==student.schoolId&&it.sessionId==student.sessionId&&it.classGrade==student.classGrade&&it.division==student.division}.mapNotNull{record->
 val day=studentDate(record.date)?:return@mapNotNull null
 if(day.isAfter(today))return@mapNotNull null
 record.studentList.firstOrNull{it.studentId==student.id}?.let{StudentRegisterEntry(day,record.sessionSlot,it.status)}
}.distinctBy{it.date to it.session}.sortedByDescending{it.date}

@Composable fun StudentAttendance(repository:ErpDataRepository) {
 val dark=LocalStudentDark.current
 val student=ownStudent(repository);val registers by repository.classAttendance.collectAsState()
 if(student==null){EmptyStateView("Student link unavailable","Attendance requires your verified student record.");return}
 var monthValue by rememberSaveable(student.id){mutableStateOf(YearMonth.from(studentToday()).toString())}
 val month=YearMonth.parse(monthValue);val all=studentAttendanceEntries(student,registers.values.toList());val entries=all.filter{YearMonth.from(it.date)==month}
 var selected by remember{mutableStateOf<LocalDate?>(null)}
 selected?.let{day->AlertDialog(onDismissRequest={selected=null},title={Text(day.toString())},text={Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){val details=entries.filter{it.date==day};if(details.isEmpty())Text("No recorded attendance.");details.forEach{Text("${it.session}: ${it.status.label}")}}},confirmButton={TextButton(onClick={selected=null}){Text("Close")}})}
 val present=entries.count{it.status in setOf(AttendanceStatus.PRESENT,AttendanceStatus.LATE,AttendanceStatus.EXCUSED)}
 LazyColumn(Modifier.fillMaxSize().testTag("student_attendance"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)){
  item{StudentPageHeader("Attendance")}
  if(dark)item{StudentDarkFeature(if(entries.isEmpty())"N/A"else "${present*100/entries.size}%","Recorded attendance this month",StudentTone.MINT,StudentIcons.Calendar,compact=true)}
  item{StudentCard{
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick={monthValue=month.minusMonths(1).toString()}){Icon(Icons.Outlined.ChevronLeft,"Previous month")};Text(month.format(DateTimeFormatter.ofPattern("MMMM uuuu")),Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold);IconButton(onClick={monthValue=month.plusMonths(1).toString()}){Icon(Icons.Outlined.ChevronRight,"Next month")}}
   Box(Modifier.horizontalScroll(rememberScrollState())){Column(Modifier.widthIn(min=336.dp).padding(Spacing.xs4)){
    Row{listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach{Text(it,Modifier.width(48.dp),style=MaterialTheme.typography.labelSmall,textAlign=androidx.compose.ui.text.style.TextAlign.Center)}}
    val cells=(1 until month.atDay(1).dayOfWeek.value).map{0}+(1..month.lengthOfMonth()).toList()
    cells.chunked(7).forEach{week->Row{week.forEach{day->
     if(day==0)Spacer(Modifier.size(48.dp))else{
      val date=month.atDay(day);val statuses=entries.filter{it.date==date}.map{it.status}
      val status=when{AttendanceStatus.ABSENT in statuses->AttendanceStatus.ABSENT;AttendanceStatus.LEAVE in statuses->AttendanceStatus.LEAVE;statuses.isNotEmpty()->statuses.first();else->null}
      val tone=when(status){AttendanceStatus.ABSENT->StudentTone.ERROR;AttendanceStatus.LEAVE,AttendanceStatus.LATE->StudentTone.BLUE;null->StudentTone.NEUTRAL;else->StudentTone.MINT}
      Surface(onClick={selected=date},enabled=!date.isAfter(studentToday()),modifier=Modifier.size(48.dp).semantics{contentDescription="$date: ${status?.label?:"No recorded attendance"}"},shape=CircleShape,color=androidx.compose.ui.graphics.Color.Transparent){Box(Modifier.padding(Spacing.xs4).fillMaxSize().background(if(status==null)MaterialTheme.colorScheme.surface else StudentColors.colors(tone).first,CircleShape),contentAlignment=Alignment.Center){Text(day.toString(),style=MaterialTheme.typography.bodySmall,color=if(status==null)MaterialTheme.colorScheme.onSurfaceVariant else StudentColors.colors(tone).second)}}
     }
    }}}
   }}
  }}
  if(!dark)item{StudentCard(tone=StudentTone.MINT){Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text(if(entries.isEmpty())"N/A"else "${present*100/entries.size}%",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold,color=StudentColors.colors(StudentTone.MINT).second);Text("Recorded sessions this month",style=MaterialTheme.typography.bodySmall);Text("$present attended · ${entries.count{it.status==AttendanceStatus.ABSENT}} absent · ${entries.count{it.status==AttendanceStatus.LEAVE}} on leave",style=MaterialTheme.typography.bodySmall)}}}
  item{StudentHeading("Recent attendance");Text("Read only. Calendar colors summarize saved sessions; tap a date for details.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  if(entries.isEmpty())item{Text("No recorded attendance for this month.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
  items(entries,key={"${it.date}_${it.session}"}){entry->Column{Row(Modifier.fillMaxWidth().padding(vertical=Spacing.sm8),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(entry.date.toString(),style=MaterialTheme.typography.bodyMedium);Text(entry.session,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(entry.status.label,style=MaterialTheme.typography.labelMedium,color=if(entry.status==AttendanceStatus.ABSENT)MaterialTheme.colorScheme.error else if(LocalStudentDark.current)StudentDarkPalette.mint else StudentColors.colors(StudentTone.MINT).second)};HorizontalDivider()}}
 }
}
