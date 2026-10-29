package com.example.ui.screens.principal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable fun PrincipalStudents(repository:ErpDataRepository,open:(String)->Unit,add:()->Unit) {
 val principalDark=LocalPrincipalDark.current
 val students by repository.students.collectAsState();val actor by repository.currentUser.collectAsState()
 var query by rememberSaveable(actor.id){mutableStateOf("")};var search by rememberSaveable(actor.id) {mutableStateOf(false)}
 var cohort by rememberSaveable(actor.id){mutableStateOf("All classes")};var status by rememberSaveable(actor.id){mutableIntStateOf(0)};var filters by remember {mutableStateOf(false)}
 val columns=if(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp>=700)2 else 1
 val scoped=students.filter {(cohort=="All classes"||it.fullClass==cohort)&&(query.isBlank()||it.name.contains(query,true)||it.admissionNumber.contains(query,true))}
 val active=scoped.filter {it.status=="ACTIVE"};val archived=scoped.filter {it.status=="ARCHIVED"};val other=scoped.filter {it.status !in setOf("ACTIVE","ARCHIVED")}
 val hasOther=students.any{it.status !in setOf("ACTIVE","ARCHIVED")}
 val groups=listOf(active,archived)+if(hasOther)listOf(other)else emptyList()
 val rows=groups.getOrElse(status){emptyList()}
 LazyColumn(Modifier.fillMaxSize().testTag("principal_students"),contentPadding=PaddingValues(horizontal=if(principalDark)Spacing.xl20 else Spacing.screenPadding)) {
  item {PrincipalPageHeader("Students") {IconButton(onClick={search=!search}){Icon(Icons.Outlined.Search,"Search students")};IconButton(onClick={filters=true}){Icon(Icons.Outlined.FilterAlt,"Filter students")};Button(onClick=add,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.xs),modifier=Modifier.testTag("fab_admit_student")){Icon(Icons.Outlined.Add,null);Text("Add")}}}
  if(principalDark)item {PrincipalSummary("Students",students.size.toString(),"Authorized directory",PrincipalIcons.Students,PrincipalColors.blue,PrincipalColors.blueInk)}
  if(search||principalDark)item {OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("Search students")},singleLine=true)}
  item {PrincipalSelect(cohort,listOf("All classes")+students.map {it.fullClass}.distinct().sorted(),{cohort=it})}
  item {PrincipalTabs(listOf("Active (${active.size})","Archived (${archived.size})")+if(hasOther)listOf("Other (${other.size})")else emptyList(),status.coerceAtMost(groups.lastIndex),{status=it})}
  if(rows.isEmpty())item {EmptyStateView("No students","No accessible records match these filters.")}
  items(rows.chunked(columns),key={it.first().id}) {group->Row(horizontalArrangement=Arrangement.spacedBy(Spacing.xl24)){group.forEach{s->Box(Modifier.weight(1f)){PrincipalRecordRow(s.name,"Class ${s.fullClass} · Roll ${s.rollNumber}${if(s.status !in setOf("ACTIVE","ARCHIVED"))" · ${s.status}" else ""}",{open(s.id)},Modifier.testTag("student_row_${s.id}"))}};if(group.size<columns)Spacer(Modifier.weight(1f))}}
 }
 if(filters)AlertDialog(onDismissRequest={filters=false},title={Text("Filters")},text={PrincipalSelect(cohort,listOf("All classes")+students.map{it.fullClass}.distinct().sorted(),{cohort=it})},confirmButton={TextButton(onClick={filters=false}){Text("Done")}})
}
@Composable fun PrincipalStaff(repository:ErpDataRepository,open:(String)->Unit,add:()->Unit,allocations:()->Unit={}) {
 val principalDark=LocalPrincipalDark.current
 val teachers by repository.teachers.collectAsState();val actor by repository.currentUser.collectAsState()
 var department by rememberSaveable(actor.id){mutableStateOf("All departments")};var query by rememberSaveable(actor.id){mutableStateOf("")};var search by rememberSaveable(actor.id) {mutableStateOf(false)}
 val faculty=teachers;val rows=faculty.filter {(department=="All departments"||it.department==department)&&(query.isBlank()||it.name.contains(query,true)||it.subjects.any {subject->subject.contains(query,true)})}
 Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={Surface(color=if(principalDark)PrincipalDarkPalette.surface else PrincipalColors.blue,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.sm),modifier=Modifier.padding(Spacing.screenPadding)){SchoolRow("Allocations","View subject and class allocations",Icons.Outlined.People,allocations)}}) {padding->
 LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("principal_staff"),contentPadding=PaddingValues(horizontal=if(principalDark)Spacing.xl20 else Spacing.screenPadding)) {
  item {PrincipalPageHeader("Staff"){IconButton(onClick={search=!search}){Icon(Icons.Outlined.Search,"Search staff")};IconButton(onClick=add,modifier=Modifier.testTag("fab_add_faculty")){Icon(Icons.Outlined.PersonAdd,"Add staff")}}}
  if(principalDark)item {PrincipalSummary("Teaching staff",faculty.size.toString(),"Authorized directory",Icons.Outlined.People,PrincipalColors.mint,PrincipalColors.mintInk)}
  if(search||principalDark)item {OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("Search staff")},singleLine=true)}
  item {PrincipalTabs(listOf("Teaching (${faculty.size})"),0,{})}
  item {PrincipalSelect(department,listOf("All departments")+faculty.map {it.department}.distinct().sorted(),{department=it})}
  if(rows.isEmpty())item {EmptyStateView("No staff","No accessible teaching records match these filters.")}
  items(rows,key={it.id}){teacher->PrincipalRecordRow(teacher.name,"${teacher.department} · ${teacher.assignedClasses.joinToString()}${if(teacher.status!="ACTIVE")" · ${teacher.status}"else""}",{open(teacher.id)})}

 }
 }
}

@Composable fun PrincipalAllocations(repository:ErpDataRepository,open:(String)->Unit) {
 val principalDark=LocalPrincipalDark.current
 val staff by repository.teachers.collectAsState()
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(if(principalDark)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  item {PrincipalPageHeader("Allocations")}
  if(staff.isEmpty())item {EmptyStateView("No allocations","No accessible teaching allocations are available.")}
  items(staff,key={it.id}){teacher->SchoolGroup {Column(Modifier.padding(Spacing.md12)) {
   PrincipalRecordRow(teacher.name,teacher.department,{open(teacher.id)})
   SchoolDetailRow("Subjects",teacher.subjects.joinToString())
   SchoolDetailRow("Classes",teacher.assignedClasses.joinToString())
   SchoolDetailRow("Weekly periods",teacher.weeklyPeriods.toString())
  }}}
 }
}
