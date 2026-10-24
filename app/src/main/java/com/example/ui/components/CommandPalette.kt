package com.example.ui.components

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserRole
import com.example.ui.screens.operations.WorkspaceDirectory
import com.example.data.model.Student
import com.example.data.repository.ErpDataRepository
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.util.Locale

data class PaletteAction(
  val title: String,
  val category: String,
  val icon: ImageVector,
  val iconTint: Color = ScholaTerracotta,
  val route: String? = null,
  val onAction: (() -> Unit)? = null
)

@Composable
fun GlobalCommandPaletteModal(
  repository: ErpDataRepository,
  onDismiss: () -> Unit,
  onNavigateTo: (String) -> Unit,
  onTriggerAction: (String) -> Unit
) {
  val students by repository.students.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  var query by remember { mutableStateOf("") }

  val palette = MaterialTheme.colorScheme
  val defaultActions = buildList {
    add(PaletteAction("Home", "WORKSPACE", Icons.Default.Home, palette.primary, Screen.Dashboard.route))
    WorkspaceDirectory.visible(currentUser.role).forEach { destination ->
      add(PaletteAction(destination.title, destination.area.label, Icons.Default.Apps, palette.primary, destination.route))
    }
    add(PaletteAction("Modules", "WORKSPACE", Icons.Default.Apps, palette.primary, Screen.OperationsHub.route))
    if (currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN)) {
      add(PaletteAction("Add student", "ACTION", Icons.Default.PersonAdd, palette.primary, onAction = { onTriggerAction("admit_student") }))
      add(PaletteAction("Publish notice", "ACTION", Icons.Default.Campaign, palette.primary, onAction = { onTriggerAction("create_notice") }))
    }
    if (currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER)) {
      add(PaletteAction("New assignment", "ACTION", Icons.Default.Assignment, palette.primary, onAction = { onTriggerAction("create_assignment") }))
    }
  }

  val filteredActions = remember(query, defaultActions) {
    if (query.isBlank()) defaultActions
    else defaultActions.filter { it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
  }

  val matchedStudents = remember(query, students) {
    if (query.isBlank()) emptyList()
    else students.filter {
      it.name.contains(query, ignoreCase = true) ||
      it.admissionNumber.contains(query, ignoreCase = true) ||
      it.classGrade.contains(query, ignoreCase = true)
    }.take(4)
  }

  Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
    Surface(Modifier.widthIn(max=600.dp).fillMaxWidth(.94f)
      .heightIn(max=androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp-Spacing.xxl32)
      .safeDrawingPadding().imePadding(),shape=RoundedCornerShape(Radius.lg),color=MaterialTheme.colorScheme.surface) {
      Column {
        Row(Modifier.fillMaxWidth().padding(horizontal=Spacing.md12),verticalAlignment=Alignment.CenterVertically) {
          Text("Search school",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
          IconButton(onClick=onDismiss){Icon(Icons.Default.Close,"Close search")}
        }
        TextField(query,{query=it},Modifier.fillMaxWidth().padding(horizontal=Spacing.md12).testTag("command_palette_search_field"),
          placeholder={Text("Student, workspace or action")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,
          trailingIcon={if(query.isNotEmpty())IconButton(onClick={query=""}){Icon(Icons.Default.Close,"Clear")}},shape=RoundedCornerShape(Radius.input))
        LazyColumn(Modifier.fillMaxWidth().weight(1f,fill=false),contentPadding=PaddingValues(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          if(query.isNotBlank()&&matchedStudents.isEmpty()&&filteredActions.isEmpty())item{EmptyStateView("No matches found","Try a student name, admission ID, or workspace name.")}
          if(matchedStudents.isNotEmpty()) {
            item{Text("Students",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            items(matchedStudents,key={it.id}){student->SchoolRow(student.name,"Class ${student.fullClass} · Roll ${student.rollNumber} · ${student.admissionNumber}",Icons.Default.PersonOutline,{onDismiss();onNavigateTo(Screen.StudentDetail.createRoute(student.id))})}
          }
          filteredActions.groupBy{it.category}.forEach{(category,actions)->
            item{Text(category.lowercase().replaceFirstChar{it.titlecase()},Modifier.padding(top=Spacing.sm8),style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            items(actions){action->SchoolRow(action.title,if(action.route!=null)"Open workspace" else "School action",action.icon,{
              onDismiss();if(action.route!=null)onNavigateTo(action.route) else action.onAction?.invoke()
            })}
          }
        }
        HorizontalDivider()
        Text("${currentUser.role.displayName} · Your permitted school workspace",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}
