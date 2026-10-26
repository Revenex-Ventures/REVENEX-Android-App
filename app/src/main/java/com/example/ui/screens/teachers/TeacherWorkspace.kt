package com.example.ui.screens.teachers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/** Presentation filter only. Supabase RLS remains the authority for loaded records/writes. */
@Composable fun teacherAllocation(repository:ErpDataRepository):Teacher? {
 val actor by repository.currentUser.collectAsState();val teachers by repository.teachers.collectAsState()
 return teachers.firstOrNull{it.id==actor.id||it.email.equals(actor.email,true)&&actor.email.isNotBlank()||it.name.equals(actor.name,true)}
}
@Composable fun TeacherCard(modifier:Modifier=Modifier,color:Color=MaterialTheme.colorScheme.surface,content:@Composable ColumnScope.()->Unit) {
 Surface(modifier.fillMaxWidth(),color=color,contentColor=MaterialTheme.colorScheme.onSurface,shape=RoundedCornerShape(TeacherLayout.radius),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){Column(content=content)}
}
@Composable fun TeacherIcon(icon:ImageVector,tone:Int=0,size:androidx.compose.ui.unit.Dp=TeacherLayout.iconWell) {
 val colors=when(tone){1->TeacherColors.mint to TeacherColors.green;2->TeacherColors.blue to TeacherColors.blueInk;3->TeacherColors.peach to MaterialTheme.colorScheme.primary;else->TeacherColors.lavender to TeacherColors.purple}
 Surface(shape=CircleShape,color=colors.first){Box(Modifier.size(size),contentAlignment=Alignment.Center){Icon(icon,null,Modifier.size(if(size<TeacherLayout.iconWell)20.dp else 24.dp),tint=colors.second)}}
}
@Composable fun TeacherTitle(title:String,action:String?=null,onClick:()->Unit={}) {
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(action!=null)TextButton(onClick=onClick){Text(action)}}
}
@Composable fun TeacherRow(title:String,subtitle:String,icon:ImageVector,tone:Int=0,onClick:()->Unit) {
 Row(Modifier.fillMaxWidth().clickable(onClick=onClick).heightIn(min=64.dp).padding(TeacherLayout.gap),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(TeacherLayout.gap)) {
  TeacherIcon(icon,tone);Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold);if(subtitle.isNotBlank())Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Icon(Icons.Outlined.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)
 }
}
@Composable fun TeacherSelect(label:String,value:String,options:List<String>,onSelect:(String)->Unit,modifier:Modifier=Modifier) {
 var open by remember{mutableStateOf(false)}
 Column(modifier){if(label.isNotBlank())Text(label,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
 Box{OutlinedButton(onClick={open=true},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),shape=RoundedCornerShape(Radius.sm),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=ButtonDefaults.outlinedButtonColors(contentColor=MaterialTheme.colorScheme.onSurface)){Text(value,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Icon(Icons.Outlined.ExpandMore,null)}
 DropdownMenu(open,{open=false}){options.forEach{option->DropdownMenuItem(text={Text(option)},onClick={onSelect(option);open=false})}}}
 }
}
@Composable fun TeacherTabs(labels:List<String>,index:Int,select:(Int)->Unit) {
  TabRow(index,containerColor=MaterialTheme.colorScheme.background){labels.forEachIndexed{i,label->Tab(index==i,{select(i)},text={Text(label)},unselectedContentColor=MaterialTheme.colorScheme.onSurfaceVariant,selectedContentColor=if(MaterialTheme.colorScheme.background==TeacherColors.canvas)TeacherColors.pressed else MaterialTheme.colorScheme.primary)}}
}
@Composable fun TeacherNavigation(route:String?,navigate:(String)->Unit) {
 val items=NavConfig.getBottomNavItems(UserRole.TEACHER)
 Surface(color=MaterialTheme.colorScheme.surface){Column{HorizontalDivider();BoxWithConstraints {
  val rows=if(maxWidth<360.dp&&androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f)items.chunked(3)else listOf(items)
  Column{rows.forEach{row->Row(Modifier.fillMaxWidth()){row.forEach{item->val active=item.route==route
   Surface(onClick={navigate(item.route)},modifier=Modifier.weight(1f).heightIn(min=64.dp),color=Color.Transparent){Column(Modifier.padding(vertical=Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){Icon(if(active)item.selectedIcon else item.unselectedIcon,item.label,tint=if(active)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant);Text(item.label,style=MaterialTheme.typography.labelSmall,color=if(active&&MaterialTheme.colorScheme.background==TeacherColors.canvas)TeacherColors.pressed else if(active)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)}}
  }}}}
 }}}
}

@Composable fun TeacherClasses(repository:ErpDataRepository,navigate:(String)->Unit) {
 val teacher=teacherAllocation(repository);val students by repository.students.collectAsState()
 val timetable by repository.timetable.collectAsState()
 var subject by rememberSaveable{mutableStateOf("")}
  val columns=if(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp>=700)2 else 1
  val subjects=teacher?.subjects.orEmpty();val classes=teacher?.assignedClasses.orEmpty().filter{cohort->subject.isBlank()||timetable.any{it.subject==subject&&"${it.classGrade}-${it.division}"==cohort&&it.teacherName.equals(teacher?.name,true)}}
 LazyColumn(Modifier.fillMaxSize().testTag("teacher_classes"),contentPadding=PaddingValues(horizontal=TeacherLayout.margin,vertical=Spacing.sm8),verticalArrangement=Arrangement.spacedBy(TeacherLayout.gap)) {
  item{WorkspacePageHeader("My classes")}
  item{TeacherSelect("Subject",subject.ifBlank{"All subjects"},listOf("All subjects")+subjects,{subject=if(it=="All subjects")""else it})}
  if(classes.isEmpty())item{EmptyStateView("No assigned classes","Your verified school allocation will appear here.")}
  items(classes.chunked(columns),key={it.joinToString()}) { row ->
   Row(horizontalArrangement=Arrangement.spacedBy(TeacherLayout.gap)) {
    row.forEach {cohort->Box(Modifier.weight(1f)){TeacherCard{TeacherRow("Class $cohort","${students.count{it.fullClass==cohort}} students",Icons.Outlined.Groups,2){navigate(Screen.TeacherClass.createRoute(cohort))}}}}
    if(row.size<columns)Spacer(Modifier.weight(1f))
   }
  }
 }
}
@Composable fun TeacherClassDetail(repository:ErpDataRepository,cohort:String,navigate:(String)->Unit) {
 val teacher=teacherAllocation(repository);val students by repository.students.collectAsState()
 var query by rememberSaveable(cohort){mutableStateOf("")};var tab by rememberSaveable(cohort){mutableIntStateOf(0)}
 if(cohort !in teacher?.assignedClasses.orEmpty()){EmptyStateView("Class unavailable","This class is not in your loaded teaching allocation.");return}
 val pupils=students.filter{it.fullClass==cohort}.sortedBy{it.rollNumber}
 Column(Modifier.fillMaxSize().testTag("teacher_class_detail")) {
  Column(Modifier.padding(horizontal=TeacherLayout.margin)){WorkspacePageHeader("Class $cohort");Text(teacher?.subjects.orEmpty().joinToString(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  TeacherTabs(listOf("Students","Work"),tab,{tab=it})
  LazyColumn(contentPadding=PaddingValues(horizontal=TeacherLayout.margin,vertical=Spacing.sm8),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   item {ResponsiveFormFields(
    first={field->OutlinedButton(onClick={navigate(Screen.Attendance.createRoute(cohort.substringBeforeLast('-'),cohort.substringAfterLast('-')))},modifier=field){Text("Attendance")}},
    second={field->OutlinedButton(onClick={navigate(Screen.Homework.route)},modifier=field){Text("Assignments")}}
   )}
   if(tab==0){
    item{OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search students")},singleLine=true,leadingIcon={Icon(Icons.Outlined.Search,null)})}
    if(pupils.isEmpty())item{Text("No accessible students.")}
    items(pupils.filter{it.name.contains(query,true)||it.rollNumber.toString()==query},key={it.id}){pupil->Column{Row(Modifier.fillMaxWidth().clickable{navigate(Screen.StudentDetail.createRoute(pupil.id))}.heightIn(min=56.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text(pupil.rollNumber.toString(),style=MaterialTheme.typography.bodySmall);Avatar(pupil.name,size=32.dp,background=TeacherColors.blue,textColor=TeacherColors.blueInk);Text(pupil.name,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Icon(Icons.Outlined.ChevronRight,null)};HorizontalDivider()}}
   }else{item{TeacherCard{TeacherRow("Marks","Drafts and saved academic records",Icons.Outlined.BarChart,2){navigate(Screen.ReportCard.route)};TeacherRow("Resources","Class documents",Icons.Outlined.Folder,0){navigate(Screen.StudyMaterial.route)}}}}
  }
 }
}

@Composable fun TeacherAssignments(repository:ErpDataRepository,create:()->Unit) {
 val actor by repository.currentUser.collectAsState();val all by repository.assignments.collectAsState();val teacher=teacherAllocation(repository)
 var cohort by rememberSaveable(actor.id){mutableStateOf("All my classes")};var subject by rememberSaveable(actor.id){mutableStateOf("All subjects")};var tab by rememberSaveable(actor.id){mutableIntStateOf(0)};var selected by rememberSaveable(actor.id){mutableStateOf<String?>(null)}
 val own=all.filter{"${it.classGrade}-${it.division}" in teacher?.assignedClasses.orEmpty() && it.subject in teacher?.subjects.orEmpty()}
 val filtered=own.filter{(cohort=="All my classes"||"${it.classGrade}-${it.division}"==cohort)&&(subject=="All subjects"||it.subject==subject)}
 val work=filtered.filter{it.publicationState==if(tab==1)"DRAFT"else "PUBLISHED"}
 val focused=own.firstOrNull{it.id==selected}
 if(focused!=null){TeacherAssignmentDetail(repository,focused){selected=null};return}
 Column(Modifier.fillMaxSize().testTag("teacher_assignments")) {
  Column(Modifier.padding(horizontal=TeacherLayout.margin)){WorkspacePageHeader("Assignments",actions={Button(onClick=create,shape=RoundedCornerShape(Radius.sm)){Icon(Icons.Outlined.Add,null);Text("Add")}});ResponsiveFormFields(
    first={field->TeacherSelect("Class",cohort,listOf("All my classes")+teacher?.assignedClasses.orEmpty(),{cohort=it},field)},
    second={field->TeacherSelect("Subject",subject,listOf("All subjects")+teacher?.subjects.orEmpty(),{subject=it},field)}
   )}
  Box(Modifier.padding(horizontal=TeacherLayout.margin)){TeacherTabs(listOf("Published","Drafts","Resources"),tab,{tab=it})}
  if(tab==2)com.example.ui.screens.academics.StudyMaterialScreen(repository) else LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=TeacherLayout.margin,vertical=Spacing.sm8),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   if(work.isEmpty())item{EmptyStateView("No assignments",if(tab==1)"Private drafts for your selected class and subject appear here."else "Published work for your selected class and subject appears here.")}
   items(work,key={it.id}){assignment->TeacherCard(Modifier.testTag("assignment_details_${assignment.id}")){TeacherRow(assignment.title,"Class ${assignment.classGrade}-${assignment.division} · Due ${assignment.dueDate}",Icons.Outlined.Description,2){selected=assignment.id}}}
  }
 }
}
@Composable private fun TeacherAssignmentDetail(repository:ErpDataRepository,assignment:HomeworkAssignment,back:()->Unit) {
 androidx.activity.compose.BackHandler(onBack=back)
 var tab by remember{mutableIntStateOf(0)}
 var editing by remember{mutableStateOf(false)}
 var busy by remember{mutableStateOf(false)}
 var feedback by remember{mutableStateOf("")}
 val actor by repository.currentUser.collectAsState()
 val scope=rememberCoroutineScope()
 if(editing)CreateAssignmentDialog(actor.name,repository,{editing=false},{},onSubmitAssignment={repository.createAssignmentConfirmed(it)},initialAssignment=assignment)
 Column(Modifier.fillMaxSize().testTag("teacher_assignment_detail")) {
  Row(Modifier.padding(horizontal=TeacherLayout.margin),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=back){Icon(Icons.Outlined.ArrowBack,"Back to assignments")};Text(assignment.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
  Text("Class ${assignment.classGrade}-${assignment.division} · ${assignment.subject}",Modifier.padding(horizontal=TeacherLayout.margin),style=MaterialTheme.typography.bodySmall)
  Row(Modifier.padding(horizontal=TeacherLayout.margin),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){
   OutlinedButton(enabled=!busy,onClick={editing=true}){Text("Edit")}
   if(assignment.publicationState=="DRAFT")Button(enabled=!busy,onClick={busy=true;scope.launch{
    try{repository.createAssignmentConfirmed(assignment.copy(publicationState="PUBLISHED"));feedback="Publication confirmed."}
    catch(e:kotlinx.coroutines.CancellationException){throw e}catch(_:Exception){feedback="Publication not confirmed. Refresh before retrying; the draft is retained."}finally{busy=false}
   }}){Text(if(busy)"Publishing…"else "Publish draft")}
  }
  if(feedback.isNotBlank())Text(feedback,Modifier.padding(horizontal=TeacherLayout.margin),style=MaterialTheme.typography.bodySmall)
  TeacherTabs(listOf("Details","Submissions"),tab,{tab=it})
  if(tab==1)Box(Modifier.weight(1f)){TeacherSubmissionInbox(repository,assignment)}else LazyColumn(contentPadding=PaddingValues(horizontal=TeacherLayout.margin,vertical=Spacing.sm8),verticalArrangement=Arrangement.spacedBy(TeacherLayout.gap)) {
   if(tab==0){item{TeacherCard{Column(Modifier.padding(TeacherLayout.gap),verticalArrangement=Arrangement.spacedBy(TeacherLayout.gap)){Text("Due ${assignment.dueDate}",fontWeight=FontWeight.SemiBold);Text(assignment.instructions);Text("${assignment.maxPoints} points · Assigned ${assignment.assignedDate}",style=MaterialTheme.typography.bodySmall);if(assignment.attachmentUrl.isNotBlank())SchoolDocumentButton(assignment.attachmentUrl,assignment.attachmentName.ifBlank{"Open attachment"})}}}}
  }
 }
}

@Composable fun TeacherTimetable(repository:ErpDataRepository,navigate:(String)->Unit) {
 val actor by repository.currentUser.collectAsState();val timetable by repository.timetable.collectAsState();val teacher=teacherAllocation(repository)
 var day by rememberSaveable(actor.id){mutableStateOf(java.time.LocalDate.now().dayOfWeek.name)};var cohort by rememberSaveable(actor.id){mutableStateOf("All my classes")}
 val slots=timetable.filter{it.teacherName.equals(actor.name,true)&&"${it.classGrade}-${it.division}" in teacher?.assignedClasses.orEmpty()&&it.dayOfWeek.equals(day,true)&&(cohort=="All my classes"||"${it.classGrade}-${it.division}"==cohort)}.sortedBy{it.periodNumber}
 LazyColumn(Modifier.fillMaxSize().testTag("teacher_timetable"),contentPadding=PaddingValues(horizontal=TeacherLayout.margin,vertical=Spacing.sm8),verticalArrangement=Arrangement.spacedBy(TeacherLayout.gap)) {
  item{WorkspacePageHeader("Timetable")}
  item{LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){items(java.time.DayOfWeek.values()){value->FilterChip(day==value.name,{day=value.name},label={Text(value.name.take(3).lowercase().replaceFirstChar{it.uppercase()})})}}}
  item{TeacherSelect("",cohort,listOf("All my classes")+teacher?.assignedClasses.orEmpty(),{cohort=it});Text(day.lowercase().replaceFirstChar{it.uppercase()},style=MaterialTheme.typography.titleSmall)}
  if(slots.isEmpty())item{EmptyStateView("No lessons scheduled","No accessible periods for this day and class.")}
  items(slots,key={it.id}){slot->TeacherCard{Row(Modifier.padding(TeacherLayout.gap),horizontalArrangement=Arrangement.spacedBy(TeacherLayout.gap),verticalAlignment=Alignment.CenterVertically){Column(Modifier.width(56.dp)){Text(slot.startTime,style=MaterialTheme.typography.labelMedium);Text(slot.endTime,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Box(Modifier.weight(1f)){TeacherRow("Class ${slot.classGrade}-${slot.division}","${slot.subject} · ${slot.roomNumber}",Icons.Outlined.Groups,2){navigate(Screen.TeacherClass.createRoute("${slot.classGrade}-${slot.division}"))}}}}
 }
}

}

@Composable fun TeacherLeave(repository:ErpDataRepository,apply:()->Unit) {
 val actor by repository.currentUser.collectAsState();val all by repository.leaveRequests.collectAsState();val own=all.filter{it.applicantId==actor.id}
 var detail by remember{mutableStateOf<LeaveRequest?>(null)}
 detail?.let{request->AlertDialog(onDismissRequest={detail=null},title={Text(request.leaveType)},text={Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text("${request.startDate} – ${request.endDate}");Text(request.status.label);Text(request.reason);if(request.approverRemark.isNotBlank())Text(request.approverRemark)}},confirmButton={TextButton(onClick={detail=null}){Text("Close")}})}
 Column(Modifier.fillMaxSize().testTag("teacher_leave")){Box(Modifier.padding(horizontal=TeacherLayout.margin)){WorkspacePageHeader("My leave requests")}
  LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(horizontal=TeacherLayout.margin,vertical=Spacing.sm8),verticalArrangement=Arrangement.spacedBy(TeacherLayout.gap)){
   if(own.isEmpty())item{EmptyStateView("No leave requests","Your own requests and confirmed decisions appear here.")}
   items(own,key={it.id}){request->TeacherCard{TeacherRow("${request.startDate} – ${request.endDate}","${request.leaveType} · ${request.status.label}",if(request.status==LeaveStatus.APPROVED)Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,if(request.status==LeaveStatus.APPROVED)1 else 3){detail=request}}}
  };Button(onClick=apply,Modifier.fillMaxWidth().padding(TeacherLayout.margin).heightIn(min=48.dp).testTag("fab_apply_leave"),shape=RoundedCornerShape(Radius.sm)){Icon(Icons.Outlined.Add,null);Text("New request")}
 }
}
