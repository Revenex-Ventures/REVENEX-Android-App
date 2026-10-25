package com.example.ui.screens.principal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable fun PrincipalStudents(repository:ErpDataRepository,open:(String)->Unit,add:()->Unit) {
 val students by repository.students.collectAsState();val actor by repository.currentUser.collectAsState()
 var query by remember(actor.id){mutableStateOf("")};var search by remember {mutableStateOf(false)}
 var cohort by remember(actor.id){mutableStateOf("All classes")};var archived by remember(actor.id){mutableStateOf(false)};var filters by remember {mutableStateOf(false)}
 val active=students.filter {it.status=="ACTIVE"};val other=students.filter {it.status=="ARCHIVED"}
 val rows=(if(archived)other else active).filter {(cohort=="All classes"||it.fullClass==cohort)&&(query.isBlank()||it.name.contains(query,true)||it.admissionNumber.contains(query,true))}
 LazyColumn(Modifier.fillMaxSize().testTag("principal_students"),contentPadding=PaddingValues(horizontal=Spacing.screenPadding)) {
  item {PrincipalPageHeader("Students") {IconButton(onClick={search=!search}){Icon(Icons.Outlined.Search,"Search students")};IconButton(onClick={filters=true}){Icon(Icons.Outlined.FilterAlt,"Filter students")};Button(onClick=add,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.xs),modifier=Modifier.testTag("fab_admit_student")){Icon(Icons.Outlined.Add,null);Text("Add")}}}
  if(search)item {OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("Search students")},singleLine=true)}
  item {PrincipalSelect(cohort,listOf("All classes")+students.map {it.fullClass}.distinct().sorted(),{cohort=it})}
  item {PrincipalTabs(listOf("Active (${active.size})","Archived (${other.size})"),if(archived)1 else 0,{archived=it==1})}
  if(rows.isEmpty())item {EmptyStateView("No students","No accessible records match these filters.")}
  items(rows,key={it.id}) {s->PrincipalRecordRow(s.name,"Class ${s.fullClass} · Roll ${s.rollNumber}",{open(s.id)},Modifier.testTag("student_row_${s.id}"))}
 }
 if(filters)AlertDialog(onDismissRequest={filters=false},title={Text("Filters")},text={PrincipalSelect(cohort,listOf("All classes")+students.map{it.fullClass}.distinct().sorted(),{cohort=it})},confirmButton={TextButton(onClick={filters=false}){Text("Done")}})
}
@Composable fun PrincipalStaff(repository:ErpDataRepository,open:(String)->Unit,add:()->Unit,allocations:()->Unit={}) {
 val teachers by repository.teachers.collectAsState();val actor by repository.currentUser.collectAsState()
 var department by remember(actor.id){mutableStateOf("All departments")};var query by remember(actor.id){mutableStateOf("")};var search by remember {mutableStateOf(false)}
 val active=teachers.filter {it.status=="ACTIVE"};val rows=active.filter {(department=="All departments"||it.department==department)&&(query.isBlank()||it.name.contains(query,true)||it.subjects.any {subject->subject.contains(query,true)})}
 Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={Surface(color=PrincipalColors.blue,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.sm),modifier=Modifier.padding(Spacing.screenPadding)){SchoolRow("Allocations","View subject and class allocations",Icons.Outlined.People,allocations)}}) {padding->
 LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("principal_staff"),contentPadding=PaddingValues(horizontal=Spacing.screenPadding)) {
  item {PrincipalPageHeader("Staff"){IconButton(onClick={search=!search}){Icon(Icons.Outlined.Search,"Search staff")};IconButton(onClick=add,modifier=Modifier.testTag("fab_add_faculty")){Icon(Icons.Outlined.PersonAdd,"Add staff")}}}
  if(search)item {OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("Search staff")},singleLine=true)}
  item {PrincipalTabs(listOf("Teaching (${active.size})"),0,{})}
  item {PrincipalSelect(department,listOf("All departments")+active.map {it.department}.distinct().sorted(),{department=it})}
  if(rows.isEmpty())item {EmptyStateView("No staff","No accessible teaching records match these filters.")}
  items(rows,key={it.id}){teacher->PrincipalRecordRow(teacher.name,"${teacher.department} · ${teacher.assignedClasses.joinToString()}",{open(teacher.id)})}

 }
 }
}

@Composable fun PrincipalAllocations(repository:ErpDataRepository,open:(String)->Unit) {
 val staff by repository.teachers.collectAsState()
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
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
