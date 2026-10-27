package com.example.ui.screens.principal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable internal fun PrincipalDarkModules(items:List<PrincipalModule>,navigate:(String)->Unit) {
 Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
  items.chunked(3).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){
   row.forEach{item->Surface(onClick={navigate(item.route)},modifier=Modifier.weight(1f).testTag("principal_module_${item.title.lowercase()}"),color=PrincipalDarkPalette.surface,shape=RoundedCornerShape(Radius.hero)){
    Column(Modifier.fillMaxWidth().heightIn(min=PrincipalLayout.compactModuleHeight).padding(Spacing.md12),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
     Icon(item.icon,null,Modifier.size(PrincipalLayout.moduleIcon),tint=PrincipalDarkPalette.muted)
     Text(item.title,style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center)
    }
   }};repeat(3-row.size){Spacer(Modifier.weight(1f))}
  }}
 }
}

@Composable internal fun PrincipalDarkNavigation(route:String?,navigate:(String)->Unit) {
 val destinations=listOf(Triple("Home",Screen.Dashboard.route,Icons.Outlined.Home),Triple("People",Screen.Students.route,Icons.Outlined.People),Triple("Academics",Screen.Academics.route,Icons.Outlined.MenuBook),Triple("Reports",Screen.Reports.route,Icons.Outlined.BarChart),Triple("You",Screen.Profile.route,Icons.Outlined.Person))
 val selected=when(route){Screen.Students.route,Screen.Teachers.route,Screen.PrincipalAllocations.route->1;Screen.Academics.route,Screen.ReportCard.route,Screen.StudyMaterial.route,Screen.Homework.route,Screen.Timetable.route,Screen.Exams.route,Screen.PrincipalSubjects.route,Screen.PrincipalCalendar.route->2;Screen.Reports.route,Screen.Fees.route->3;Screen.Profile.route,Screen.Leaves.route,Screen.OperationsHub.route,Screen.Audit.route->4;else->0}
 Surface(color=PrincipalDarkPalette.navigation,shape=RoundedCornerShape(Radius.hero)){
  Row(Modifier.fillMaxWidth().padding(Spacing.sm8),horizontalArrangement=Arrangement.spacedBy(Spacing.xs4)){
   destinations.forEachIndexed{i,(label,target,icon)->Surface(onClick={navigate(target)},modifier=Modifier.weight(1f).testTag("principal_dark_nav_$label"),shape=RoundedCornerShape(Radius.pill),color=if(selected==i)PrincipalDarkPalette.orange else PrincipalDarkPalette.navigation,contentColor=if(selected==i)PrincipalDarkPalette.ink else PrincipalDarkPalette.muted){
    Column(Modifier.heightIn(min=Spacing.xxxl48).padding(vertical=Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){Icon(icon,label);Text(label,style=MaterialTheme.typography.labelSmall,textAlign=TextAlign.Center)}
   }}
  }
 }
}
