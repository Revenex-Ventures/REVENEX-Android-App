package com.example.ui.screens.students

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(repository: ErpDataRepository, onNavigateToStudentDetail: (String) -> Unit, onShowAddStudent: () -> Unit, onOpenFaculty: (() -> Unit)? = null) {
  val students by repository.students.collectAsState()
  val actor by repository.currentUser.collectAsState()
  var query by remember(actor.id) {mutableStateOf("")}
  var cohort by remember(actor.id) {mutableStateOf("")}
  var selected by remember(actor.id) {mutableStateOf<String?>(null)}
  val classes=students.map {it.fullClass}.distinct().sorted()
  val visible=students.filter {(cohort.isBlank()||it.fullClass==cohort)&&(query.isBlank()||it.name.contains(query,true)||it.admissionNumber.contains(query,true)||it.parentName.contains(query,true))}
  val wide=rememberWindowSizeClass()!=WindowSizeClass.COMPACT_MOBILE
  val detail=visible.firstOrNull {it.id==selected} ?: visible.firstOrNull()
  Column(Modifier.fillMaxSize().padding(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    WorkspacePageHeader("Students") {
      if(actor.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)) Button(onClick=onShowAddStudent,shape=RoundedCornerShape(Radius.sm),modifier=Modifier.testTag("fab_admit_student")){Icon(Icons.Default.Add,null);Text("Add")}
    }
    if(onOpenFaculty!=null && actor.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)) SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
      listOf("Students","Faculty").forEachIndexed {index,label ->
        SegmentedButton(selected=index==0,onClick={if(index!=0)onOpenFaculty.invoke()},shape=SegmentedButtonDefaults.itemShape(index,2)){Text(label)}
      }
    }
    OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search students")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,shape=RoundedCornerShape(Radius.sm),colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=MaterialTheme.colorScheme.outlineVariant))
    LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
      item {SchoolFilterChip(cohort.isBlank(),{cohort=""},{Text("All classes")})}
      items(classes){value->SchoolFilterChip(cohort==value,{cohort=value},{Text(value)})}
    }
    Text("${visible.size} students",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(Spacing.lg16)) {
      Surface(Modifier.weight(1f).fillMaxHeight(),shape=RoundedCornerShape(Radius.md),color=MaterialTheme.colorScheme.surface,
        border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=Spacing.md12)) {
          if(visible.isEmpty()) item {EmptyStateView(title="No matching students",message="Try another name, admission ID or class.")}
          items(visible,key={it.id}) {student ->
            Row(Modifier.fillMaxWidth().background(if(wide&&detail?.id==student.id)MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent)
              .clickable {if(wide)selected=student.id else onNavigateToStudentDetail(student.id)}.testTag("student_row_${student.id}").padding(Spacing.md12),
              verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
              Avatar(student.name,size=Spacing.xxl32,background=SemanticColors.lavenderContainer,textColor=SemanticColors.onLavender)
              Column(Modifier.weight(1f)) {
                Text(student.name,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
                Text("${student.fullClass} · Roll ${student.rollNumber}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Icon(Icons.Default.ChevronRight,null,Modifier.size(Spacing.lg16))
            }
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
          }
        }
      }
      if(wide) Box(Modifier.weight(1.3f).fillMaxHeight()) {
        if(detail!=null) Student360DetailPane(detail,repository,onPayFee={_,_->onNavigateToStudentDetail(detail.id)})
        else EmptyStateView(title="Select a student",message="Choose a student to view academic, contact and fee information.")
      }
    }
  }
}
