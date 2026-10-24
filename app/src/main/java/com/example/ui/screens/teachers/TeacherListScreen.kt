package com.example.ui.screens.teachers

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
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherListScreen(repository: ErpDataRepository, onNavigateToTeacherDetail: (String) -> Unit, onShowAddTeacher: () -> Unit, onOpenStudents: (() -> Unit)? = null) {
  val teachers by repository.teachers.collectAsState()
  val actor by repository.currentUser.collectAsState()
  var query by remember(actor.id) {mutableStateOf("")}
  var department by remember(actor.id) {mutableStateOf("")}
  val departments=teachers.filter {it.status=="ACTIVE"}.map {it.department}.distinct().sorted()
  val visible=teachers.filter {it.status=="ACTIVE"&&(department.isBlank()||it.department==department)&&(query.isBlank()||it.name.contains(query,true)||it.department.contains(query,true)||it.subjects.any {subject->subject.contains(query,true)})}
  Column(Modifier.fillMaxSize().padding(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    WorkspacePageHeader("Faculty") {
      if(actor.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)) Button(onClick=onShowAddTeacher,shape=RoundedCornerShape(Radius.sm),modifier=Modifier.testTag("fab_add_faculty")){Icon(Icons.Default.Add,null);Text("Add")}
    }
    if(onOpenStudents!=null && actor.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)) SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
      listOf("Students","Faculty").forEachIndexed {index,label ->
        SegmentedButton(selected=index==1,onClick={if(index!=1)onOpenStudents.invoke()},shape=SegmentedButtonDefaults.itemShape(index,2)){Text(label)}
      }
    }
    OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search faculty")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,shape=RoundedCornerShape(Radius.sm),colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=MaterialTheme.colorScheme.outlineVariant))
    LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
      item {SchoolFilterChip(department.isBlank(),{department=""},{Text("All")})}
      items(departments){value->SchoolFilterChip(department==value,{department=value},{Text(value)})}
    }
    Text("${visible.size} active faculty",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(columns=androidx.compose.foundation.lazy.grid.GridCells.Adaptive(300.dp),verticalArrangement=Arrangement.spacedBy(Spacing.sm8),horizontalArrangement=Arrangement.spacedBy(Spacing.md12),contentPadding=PaddingValues(bottom=Spacing.xl24)) {
      items(visible.size,key={visible[it].id}) {index -> val teacher=visible[index]
        SchoolGroup {Row(Modifier.fillMaxWidth().clickable {onNavigateToTeacherDetail(teacher.id)}.padding(Spacing.md12),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
          Avatar(teacher.name,size=Spacing.xxl32,background=SemanticColors.blueContainer,textColor=SemanticColors.onBlue)
          Column(Modifier.weight(1f)) {Text(teacher.name,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold);Text(teacher.department,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(teacher.subjects.joinToString(", "),style=MaterialTheme.typography.bodySmall)}
          Icon(Icons.Default.ChevronRight,null,Modifier.size(Spacing.lg16))
        }}
      }
      if(visible.isEmpty()) item {EmptyStateView(title="No matching faculty",message="Try a different department or search.")}
    }
  }
}
