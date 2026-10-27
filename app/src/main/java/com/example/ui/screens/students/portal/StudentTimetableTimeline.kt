package com.example.ui.screens.students.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.data.model.TimetableSlot
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.WorkspacePageHeader
import com.example.ui.theme.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val indiaZone:ZoneId=ZoneId.of("Asia/Kolkata")
internal fun studentIndiaNow(clock:Clock):ZonedDateTime=clock.instant().atZone(indiaZone)
private val lessonTimeFormats=listOf("H:mm","H:mm:ss","h:mm a").map{
 DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(it).toFormatter(Locale.US).withResolverStyle(ResolverStyle.STRICT)
}
internal fun studentLessonTime(value:String):LocalTime? {
 val text=value.trim().replace(Regex("(?i)(\\d)(AM|PM)$"),"$1 $2").replace(Regex("\\s+")," ")
 return lessonTimeFormats.firstNotNullOfOrNull{runCatching{LocalTime.parse(text,it)}.getOrNull()}
}
internal enum class StudentLessonPhase(val label:String){UPCOMING("Upcoming"),LIVE("Now"),ENDED("Ended")}
internal data class StudentLessonProgress(val phase:StudentLessonPhase,val fraction:Float)
/** Display-only schedule position. This never writes attendance or lesson completion. */
internal fun studentLessonProgress(date:LocalDate,now:ZonedDateTime,start:String,end:String):StudentLessonProgress? {
 val from=studentLessonTime(start)?:return null
 val until=studentLessonTime(end)?:return null
 if(!until.isAfter(from))return null
 val schoolNow=now.withZoneSameInstant(indiaZone)
 val begins=date.atTime(from).atZone(indiaZone).toInstant()
 val ends=date.atTime(until).atZone(indiaZone).toInstant()
 val instant=schoolNow.toInstant()
 return when {
  instant<begins->StudentLessonProgress(StudentLessonPhase.UPCOMING,0f)
  instant>=ends->StudentLessonProgress(StudentLessonPhase.ENDED,1f)
  else->StudentLessonProgress(StudentLessonPhase.LIVE,(Duration.between(begins,instant).toMillis().toDouble()/Duration.between(begins,ends).toMillis()).toFloat().coerceIn(0f,1f))
 }
}

@Composable private fun rememberIndiaClock(clock:Clock):State<ZonedDateTime> {
 val owner=LocalLifecycleOwner.current
 return key(owner,clock){produceState(studentIndiaNow(clock),owner,clock){
  owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
   while(isActive){value=studentIndiaNow(clock);delay(1000L)}
  }
 }}
}

/** Only the Student timetable uses this presentation; all data still comes from its existing repository. */
@Composable fun StudentTimetable(repository:ErpDataRepository,clock:Clock=Clock.systemUTC()) {
 val student=ownStudent(repository)
 val slots by repository.timetable.collectAsState()
 val now by rememberIndiaClock(clock)
 // -1 follows the current Indian school day, including midnight rollover.
 var chosenDay by rememberSaveable(student?.id){mutableIntStateOf(-1)}
 val today=now.toLocalDate()
 val day=if(chosenDay<0)today.dayOfWeek.value-1 else chosenDay
 val selectedDate=today.minusDays((today.dayOfWeek.value-1).toLong()).plusDays(day.toLong())
 val days=DayOfWeek.entries
 val dayScroll=rememberLazyListState()
 LaunchedEffect(day){dayScroll.scrollToItem((day-2).coerceAtLeast(0))}
 val visible=slots.filter{student!=null&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId&&it.classGrade==student.classGrade&&it.division==student.division&&it.dayOfWeek.equals(days[day].name,true)}
  .sortedWith(compareBy<TimetableSlot>{studentLessonTime(it.startTime)?:LocalTime.MAX}.thenBy{it.periodNumber})
 LazyColumn(Modifier.fillMaxSize().testTag("student_timetable"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{StudentPageHeader("Timetable")}
  item{LazyRow(state=dayScroll,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){
   items(days.indices.toList()){index->
    if(LocalStudentDark.current)FilterChip(day==index,{chosenDay=if(index==today.dayOfWeek.value-1)-1 else index},modifier=Modifier.heightIn(min=48.dp),border=null,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.sm),colors=FilterChipDefaults.filterChipColors(containerColor=StudentDarkPalette.navigation,labelColor=StudentDarkPalette.muted,selectedContainerColor=StudentDarkPalette.orange,selectedLabelColor=StudentDarkPalette.ink),label={Column(horizontalAlignment=Alignment.CenterHorizontally){Text(days[index].getDisplayName(java.time.format.TextStyle.SHORT,Locale.US),style=MaterialTheme.typography.labelSmall);Text(today.minusDays((today.dayOfWeek.value-1).toLong()).plusDays(index.toLong()).dayOfMonth.toString(),style=MaterialTheme.typography.titleSmall)}})
    else FilterChip(day==index,{chosenDay=if(index==today.dayOfWeek.value-1)-1 else index},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=StudentColors.action,selectedLabelColor=StudentColors.onAction),label={Text(days[index].getDisplayName(java.time.format.TextStyle.FULL,Locale.US))})
   }
  }}
  item{Column(verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
    Text(selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM uuuu",Locale.US)),Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
    if(chosenDay>=0)TextButton(onClick={chosenDay=-1}){Text("Today")}
   }
   Text("India time · ${now.format(DateTimeFormatter.ofPattern("HH:mm:ss",Locale.US))} IST",Modifier.testTag("student_ist_clock"),style=MaterialTheme.typography.bodySmall,color=StudentColors.actionText)
   if(student!=null)Text("Class ${student.fullClass}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }}
  if(student==null)item{EmptyStateView("Student link unavailable","Your timetable requires your verified student record.")}
  else if(visible.isEmpty())item{EmptyStateView("No lessons scheduled","No timetable has been provided for ${days[day].getDisplayName(java.time.format.TextStyle.FULL,Locale.US)}.")}
  else item{if(LocalStudentDark.current)StudentDarkSchedule(visible,selectedDate,now)else StudentScheduleTimeline(visible,selectedDate,now)}
  item{Text("Time-based lesson status. Attendance is recorded separately.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
}

@Composable internal fun StudentScheduleTimeline(slots:List<TimetableSlot>,date:LocalDate,now:ZonedDateTime) {
 StudentCard(tag="student_schedule_rail"){
  Row(Modifier.fillMaxWidth().padding(StudentLayout.gap),horizontalArrangement=Arrangement.SpaceBetween){
   Text("SCHEDULE",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Text("${slots.size} LESSONS",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
  HorizontalDivider()
  slots.forEachIndexed{index,slot->
   val progress=studentLessonProgress(date,now,slot.startTime,slot.endTime)
   StudentRailRow(slot.startTime,slot.endTime,progress,false,"student_period_${slot.id}"){
    Row(verticalAlignment=Alignment.Top){Text(slot.subject,Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold);Spacer(Modifier.width(Spacing.sm8));Text("P${slot.periodNumber}",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    if(slot.teacherName.isNotBlank())Text(slot.teacherName,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    if(slot.roomNumber.isNotBlank())Text(slot.roomNumber,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    Text(progress?.phase?.label?:"Time unavailable",style=MaterialTheme.typography.labelSmall,color=when(progress?.phase){StudentLessonPhase.LIVE->StudentColors.onOrange;StudentLessonPhase.ENDED->StudentColors.colors(StudentTone.MINT).second;else->MaterialTheme.colorScheme.onSurfaceVariant})
   }
   val next=slots.getOrNull(index+1)
   val end=studentLessonTime(slot.endTime)
   val nextStart=next?.let{studentLessonTime(it.startTime)}
   if(end!=null&&nextStart!=null&&nextStart.isAfter(end)){
    StudentRailRow(slot.endTime,next!!.startTime,studentLessonProgress(date,now,slot.endTime,next.startTime),true,"student_gap_${slot.id}"){
     Text("No lesson scheduled",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
     Text("${Duration.between(end,nextStart).toMinutes()} min",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
   }
  }
 }
}

@Composable private fun StudentRailRow(start:String,end:String,progress:StudentLessonProgress?,gap:Boolean,tag:String,content:@Composable ColumnScope.()->Unit) {
 val dark=LocalStudentDark.current
 val timeWidth=if(LocalDensity.current.fontScale>1.2f)StudentTimelineLayout.largeTimeColumn else StudentTimelineLayout.timeColumn
 val railColor=MaterialTheme.colorScheme.outlineVariant
 val endedColor=if(dark)StudentDarkPalette.mint else StudentColors.colors(StudentTone.MINT).second
 val liveColor=StudentColors.selection
 val live=progress?.phase==StudentLessonPhase.LIVE
 val background=if(dark)StudentDarkPalette.canvas else if(live)StudentColors.colors(StudentTone.ORANGE).first else MaterialTheme.colorScheme.surface
 val clockFormat=DateTimeFormatter.ofPattern("HH:mm",Locale.US)
 Row(Modifier.fillMaxWidth().heightIn(min=if(gap)StudentTimelineLayout.gapHeight else StudentTimelineLayout.rowHeight).testTag(tag)
  .semantics(mergeDescendants=true){stateDescription=progress?.phase?.label?:"Time unavailable"}
  .background(background).drawBehind{
   val x=(timeWidth+StudentTimelineLayout.rail/2).toPx()
   val stroke=StudentTimelineLayout.stroke.toPx()
   val radius=StudentTimelineLayout.nodeRadius.toPx()
   drawLine(railColor,Offset(x,0f),Offset(x,size.height),stroke)
   if(progress!=null&&progress.fraction>0f)drawLine(if(live)liveColor else endedColor,Offset(x,0f),Offset(x,size.height*progress.fraction),stroke)
   val y=if(live)(size.height*progress!!.fraction).coerceIn(radius,size.height-radius)else size.height/2
   drawCircle(background,radius+stroke,Offset(x,y))
   drawCircle(when(progress?.phase){StudentLessonPhase.LIVE->liveColor;StudentLessonPhase.ENDED->endedColor;else->railColor},radius,Offset(x,y))
   if(live)drawLine(liveColor,Offset(x,y),Offset(x+StudentTimelineLayout.cursorTick.toPx(),y),stroke)
   else if(progress?.phase==StudentLessonPhase.ENDED){
    drawLine(background,Offset(x-radius*.6f,y),Offset(x-radius*.1f,y+radius*.4f),stroke/1.5f)
    drawLine(background,Offset(x-radius*.1f,y+radius*.4f),Offset(x+radius*.65f,y-radius*.5f),stroke/1.5f)
   }else drawCircle(background,radius-stroke,Offset(x,y))
   if(!dark)drawLine(railColor,Offset((timeWidth+StudentTimelineLayout.rail+Spacing.sm8).toPx(),size.height),Offset(size.width,size.height),1.dp.toPx())
  },verticalAlignment=Alignment.CenterVertically){
  Column(Modifier.width(timeWidth).padding(horizontal=Spacing.sm8),horizontalAlignment=Alignment.End){
   Text(studentLessonTime(start)?.format(clockFormat)?:start,style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold,color=if(dark&&live)StudentDarkPalette.orange else if(live)StudentColors.onOrange else MaterialTheme.colorScheme.onSurface)
   Text(studentLessonTime(end)?.format(clockFormat)?:end,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
  Spacer(Modifier.width(StudentTimelineLayout.rail))
  Column(Modifier.weight(1f).padding(start=Spacing.sm8,end=StudentLayout.gap,top=StudentLayout.gap,bottom=StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(Spacing.xs4),content=content)
 }
}

@Composable private fun StudentDarkSchedule(slots:List<TimetableSlot>,date:LocalDate,now:ZonedDateTime) {
 Column(Modifier.testTag("student_schedule_rail")){
  slots.forEachIndexed{index,slot->
   val progress=studentLessonProgress(date,now,slot.startTime,slot.endTime)
   var expanded by rememberSaveable(slot.id){mutableStateOf(false)}
   StudentRailRow(slot.startTime,slot.endTime,progress,false,"student_period_${slot.id}"){
    StudentCard(Modifier.fillMaxWidth(),studentSubjectTone(slot.subject),{expanded=!expanded}){
     Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
      Text(slot.subject,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
      Text(slot.roomNumber.ifBlank{"Room not provided"},style=MaterialTheme.typography.bodySmall)
      if(expanded&&slot.teacherName.isNotBlank())Text(slot.teacherName,style=MaterialTheme.typography.bodySmall)
      Text(if(progress?.phase==StudentLessonPhase.LIVE)"Now · India time"else progress?.phase?.label?:"Time unavailable",style=MaterialTheme.typography.labelSmall)
      if(progress?.phase==StudentLessonPhase.LIVE)LinearProgressIndicator(progress={progress.fraction},modifier=Modifier.fillMaxWidth().testTag("student_live_time_stroke"),color=StudentDarkPalette.ink,trackColor=StudentDarkPalette.ink.copy(alpha=.15f))
     }
    }
   }
   val next=slots.getOrNull(index+1)
   val end=studentLessonTime(slot.endTime)
   val nextStart=next?.let{studentLessonTime(it.startTime)}
   if(end!=null&&nextStart!=null&&nextStart.isAfter(end)){
    StudentRailRow(slot.endTime,next!!.startTime,studentLessonProgress(date,now,slot.endTime,next.startTime),true,"student_gap_${slot.id}"){
     Text("Break · ${Duration.between(end,nextStart).toMinutes()} min",style=MaterialTheme.typography.bodySmall,color=StudentDarkPalette.muted)
    }
   }
  }
 }
}
