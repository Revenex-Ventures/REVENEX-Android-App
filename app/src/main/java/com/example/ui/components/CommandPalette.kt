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
    add(PaletteAction("All school workspaces", "WORKSPACE", Icons.Default.Apps, palette.primary, Screen.OperationsHub.route))
    if (currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN)) {
      add(PaletteAction("Enrol student", "ACTION", Icons.Default.PersonAdd, palette.primary, onAction = { onTriggerAction("admit_student") }))
      add(PaletteAction("Publish notice", "ACTION", Icons.Default.Campaign, palette.primary, onAction = { onTriggerAction("create_notice") }))
    }
    if (currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER)) {
      add(PaletteAction("Create assignment", "ACTION", Icons.Default.Assignment, palette.primary, onAction = { onTriggerAction("create_assignment") }))
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

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
        .imePadding()
        .background(Color.Black.copy(alpha = 0.55f))
        .padding(Spacing.s4),
      contentAlignment = Alignment.TopCenter
    ) {
      Surface(
        modifier = Modifier
          .widthIn(max = 640.dp)
          .fillMaxWidth()
          .padding(top = 40.dp)
          .clip(RoundedCornerShape(Radius.hero))
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.hero)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.hero),
        tonalElevation = Elev.e3
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Search Input Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(Spacing.s4),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.s3))
            TextField(
              value = query,
              onValueChange = { query = it },
              placeholder = { Text("Type a command, workspace, or scholar name...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp) },
              modifier = Modifier
                .weight(1f)
                .testTag("command_palette_search_field"),
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
              ),
              singleLine = true
            )
            if (query.isNotEmpty()) {
              IconButton(onClick = { query = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
              }
            }
            Surface(
              shape = RoundedCornerShape(Radius.sm),
              color = MaterialTheme.colorScheme.secondaryContainer
            ) {
              Text("ESC", color = ScholaSlateNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(MaterialTheme.colorScheme.outlineVariant)
          )

          // Results / Shortcuts List
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 420.dp),
            contentPadding = PaddingValues(vertical = Spacing.s2)
          ) {
            // Matched Scholars
            if (query.isNotBlank() && matchedStudents.isEmpty() && filteredActions.isEmpty()) {
              item {
                Column(Modifier.fillMaxWidth().padding(Spacing.lg16)) {
                  Text("No matches found", style = MaterialTheme.typography.titleMedium)
                  Spacer(Modifier.height(Spacing.xs4))
                  Text("Try a student name, admission ID, or workspace name.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
            if (matchedStudents.isNotEmpty()) {
              item {
                Text(
                  text = "SCHOLARS DIRECTORY",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  letterSpacing = TypeTokens.trackingMicroLabel,
                  modifier = Modifier.padding(horizontal = Spacing.s4, vertical = Spacing.s1)
                )
              }
              items(matchedStudents, key = { it.id }) { st ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      onDismiss()
                      onNavigateTo(Screen.StudentDetail.createRoute(st.id))
                    }
                    .padding(horizontal = Spacing.s4, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Avatar(name = st.name, size = 32.dp)
                  Spacer(modifier = Modifier.width(Spacing.s3))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(st.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Class ${st.fullClass} • Roll #${st.rollNumber} • Adm: ${st.admissionNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  ScholaPillBadge(status = st.feeStatus.label)
                }
              }
            }

            // Commands & Workspaces
            item {
              Text(
                text = if (query.isBlank()) "QUICK WORKSPACE COMMANDS" else "MATCHED COMMANDS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = TypeTokens.trackingMicroLabel,
                modifier = Modifier.padding(horizontal = Spacing.s4, vertical = Spacing.s2)
              )
            }

            items(filteredActions, key = { it.title }) { action ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onDismiss()
                    if (action.route != null) {
                      onNavigateTo(action.route)
                    } else {
                      action.onAction?.invoke()
                    }
                  }
                  .padding(horizontal = Spacing.s4, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(Spacing.s3))
                Column(modifier = Modifier.weight(1f)) {
                  Text(action.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                  Text(action.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }
}
