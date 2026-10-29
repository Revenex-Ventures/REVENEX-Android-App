package com.example.ui.screens.students.portal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.Student
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Display values supplied by the existing authorized Home projection. */
internal data class StudentLightTask(val id:String,val title:String,val subtitle:String,val date:LocalDate?)

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun StudentLightHome(
 student:Student,rate:String,tasks:List<StudentLightTask>,nextTitle:String?,nextContext:String?,
 noticeTitle:String?,noticeDate:String?,noticeId:String?,navigate:(String)->Unit
){
 val today=studentToday()
 LazyColumn(Modifier.fillMaxSize().testTag("student_dashboard_list"),contentPadding=PaddingValues(StudentLightLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLightLayout.section)){
  item{
   Surface(color=StudentLightPalette.information,shape=RoundedCornerShape(Radius.hero),border=BorderStroke(1.dp,StudentLightPalette.powder)){
    Row(Modifier.fillMaxWidth().padding(start=Spacing.xs4).background(StudentLightPalette.navy).padding(start=Spacing.xs4).background(StudentLightPalette.information).padding(Spacing.lg16),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
      Text("STUDENT PORTAL",style=MaterialTheme.typography.labelSmall)
      Text("${studentGreeting()},\n${student.name.substringBefore(' ')}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
      FlowRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
       StudentIdentityPill("Class ${student.fullClass}");StudentIdentityPill("Roll ${student.rollNumber}")
      }
     }
     if(LocalDensity.current.fontScale<=1.2f)StudentDateBlock(today,large=true)
    }
   }
  }
  item{
   Surface(onClick={navigate(Screen.Timetable.route)},color=StudentLightPalette.navy,contentColor=Color.White,shape=RoundedCornerShape(Radius.hero)){
    Row(Modifier.fillMaxWidth().padding(Spacing.lg16),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
      Text("NEXT CLASS",style=MaterialTheme.typography.labelMedium,color=StudentLightPalette.powder)
      Text(nextTitle?:"No upcoming lesson",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
      Text(nextContext?:"No remaining lesson scheduled today",style=MaterialTheme.typography.bodyMedium)
      Surface(color=StudentLightPalette.orange,contentColor=StudentLightPalette.onAction,shape=CircleShape){Row(Modifier.padding(horizontal=Spacing.md12,vertical=Spacing.sm8),verticalAlignment=Alignment.CenterVertically){Text("View timetable",style=MaterialTheme.typography.labelLarge);Icon(Icons.Outlined.ChevronRight,null)}}
     }
     if(LocalDensity.current.fontScale<=1.2f)Surface(color=StudentLightPalette.powder,shape=CircleShape){Icon(StudentIcons.Book,null,Modifier.padding(Spacing.lg16).size(32.dp),tint=StudentLightPalette.navy)}
    }
   }
  }
  item{Row(horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){
   StudentLightMetric(rate,"Attendance",StudentIcons.Results,Modifier.weight(1f)){navigate(Screen.Attendance.route)}
   StudentLightMetric(tasks.size.toString(),"Tasks due",StudentIcons.Document,Modifier.weight(1f),true){navigate(Screen.Homework.route)}
  }}
  item{StudentHeading("Learning","View all"){navigate(Screen.StudentLearn.route)};Spacer(Modifier.height(Spacing.md12));StudentLightShortcutGrid(listOf(
   StudentLightModule("Assignments",StudentIcons.Clipboard,Screen.Homework.route),StudentLightModule("Results",StudentIcons.Results,Screen.ReportCard.route),StudentLightModule("Timetable",StudentIcons.Calendar,Screen.Timetable.route),StudentLightModule("Library",StudentIcons.Book,Screen.Library.route)
  ),4,navigate)}
  item{StudentHeading("School essentials");Spacer(Modifier.height(Spacing.md12));StudentLightShortcutGrid(listOf(
   StudentLightModule("Attendance",StudentIcons.Calendar,Screen.Attendance.route),StudentLightModule("Fees",StudentIcons.Wallet,Screen.Fees.route),StudentLightModule("Resources",Icons.Outlined.FolderOpen,Screen.StudyMaterial.route)
  ),3,navigate)}
  item{StudentHeading("Due soon","View all"){navigate(Screen.Homework.route)};Spacer(Modifier.height(Spacing.sm8));StudentCard{
   if(tasks.isEmpty())Text("No pending assignments.",Modifier.padding(Spacing.lg16),color=StudentLightPalette.muted)
   tasks.take(2).forEachIndexed{index,task->
    Row(Modifier.fillMaxWidth().clickable{navigate(Screen.StudentAssignment.createRoute(task.id))}.padding(Spacing.md12).heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){
     if(task.date!=null)StudentDateBlock(task.date,orange=index==0)
     else StudentIcon(StudentIcons.Document,StudentTone.BLUE)
     Column(Modifier.weight(1f)){Text(task.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold);Text(task.subtitle,style=MaterialTheme.typography.bodySmall,color=StudentLightPalette.muted)}
     Icon(Icons.Outlined.ChevronRight,null)
    };if(index<minOf(tasks.size,2)-1)HorizontalDivider(Modifier.padding(horizontal=Spacing.lg16))
   }
  }}
  item{if(noticeTitle!=null&&noticeId!=null)StudentRow(noticeTitle,noticeDate.orEmpty(),Icons.Outlined.Campaign,StudentTone.BLUE,{navigate(Screen.StudentNotice.createRoute(noticeId))})else Text("No school notice available.",color=StudentLightPalette.muted)}
 }
}

private fun studentGreeting():String=when(java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).hour){in 5..11->"Good morning";in 12..16->"Good afternoon";else->"Good evening"}

@Composable private fun StudentIdentityPill(text:String){Surface(color=Color.White,shape=RoundedCornerShape(Radius.sm),border=BorderStroke(1.dp,StudentLightPalette.border)){Text(text,Modifier.padding(horizontal=Spacing.sm8,vertical=Spacing.xs4),style=MaterialTheme.typography.labelLarge)}}

@Composable internal fun StudentDateBlock(date:LocalDate,orange:Boolean=false,large:Boolean=false){
 Surface(color=if(large)StudentLightPalette.navy else if(orange)StudentLightPalette.orange else StudentLightPalette.information,contentColor=if(large)Color.White else if(orange)StudentLightPalette.onAction else StudentLightPalette.navy,shape=RoundedCornerShape(Radius.md)){
  Column(Modifier.width(if(large)StudentLightLayout.dateWidth else StudentLightLayout.shortcut).padding(vertical=Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally){
   if(large)Text(date.format(DateTimeFormatter.ofPattern("EEE",Locale.US)).uppercase(),style=MaterialTheme.typography.labelMedium)
   Text(date.dayOfMonth.toString(),style=if(large)MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
   Text(date.format(DateTimeFormatter.ofPattern("MMM",Locale.US)).uppercase(),style=MaterialTheme.typography.labelSmall)
  }
 }
}

@Composable internal fun StudentLightMetric(value:String,label:String,icon:ImageVector,modifier:Modifier=Modifier,highlight:Boolean=false,onClick:()->Unit){
 StudentCard(modifier,if(highlight)StudentTone.ORANGE else StudentTone.BLUE,onClick){
  if(LocalDensity.current.fontScale>1.2f)Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Icon(icon,null);Text(value,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.bodySmall)}
  else Row(Modifier.padding(Spacing.lg16),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){Icon(icon,null,Modifier.size(28.dp),tint=if(highlight)StudentLightPalette.actionText else StudentLightPalette.navy);Column(Modifier.weight(1f)){Text(value,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.bodySmall)}}
 }
}

internal data class StudentLightModule(val title:String,val icon:ImageVector,val route:String)
@Composable internal fun StudentLightShortcutGrid(modules:List<StudentLightModule>,preferredColumns:Int,navigate:(String)->Unit){
 BoxWithConstraints{
  val columns=if(LocalDensity.current.fontScale>1.2f||maxWidth<300.dp)2 else preferredColumns
  Column(verticalArrangement=Arrangement.spacedBy(Spacing.lg16)){modules.chunked(columns).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){
   row.forEach{module->Column(Modifier.weight(1f).testTag(if(module.title=="Fees")"family_fee_accounts"else "student_module_${module.title.lowercase()}").clickable(onClickLabel="Open ${module.title}"){navigate(module.route)}.semantics{role=Role.Button}.padding(vertical=Spacing.xs4),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
    Surface(color=StudentLightPalette.navy,shape=CircleShape){Box(Modifier.size(StudentLightLayout.shortcut),contentAlignment=Alignment.Center){Icon(module.icon,null,Modifier.size(StudentLightLayout.shortcutIcon),tint=Color.White)}}
    Text(module.title,Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.labelMedium)
   }}
   repeat(columns-row.size){Spacer(Modifier.weight(1f))}
  }}}
 }
}

/** Navy selected pill is used on every compact Student light destination. */
@Composable internal fun StudentLightNavigation(route:String?,navigate:(String)->Unit){
 val items=listOf(StudentLightModule("Home",Icons.Outlined.Home,Screen.Dashboard.route),StudentLightModule("Learn",StudentIcons.Book,Screen.StudentLearn.route),StudentLightModule("Schedule",StudentIcons.Calendar,Screen.Timetable.route),StudentLightModule("Inbox",Icons.Outlined.MailOutline,Screen.Notifications.route),StudentLightModule("You",Icons.Outlined.Person,Screen.OperationsHub.route))
 Surface(Modifier.fillMaxWidth().padding(horizontal=Spacing.md12,vertical=Spacing.sm8),shape=RoundedCornerShape(Radius.hero),color=Color.White,border=BorderStroke(1.dp,StudentLightPalette.border)){
  BoxWithConstraints{
   val columns=if(maxWidth<360.dp&&LocalDensity.current.fontScale>1.2f)3 else 5
   Column(Modifier.padding(Spacing.xs4),verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){items.chunked(columns).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(Spacing.xs4)){
    row.forEach{item->val selected=route==item.route
     Surface(onClick={navigate(if(item.title=="You")Screen.Profile.route else item.route)},modifier=Modifier.weight(1f).heightIn(min=PortalDockLayout.target).testTag("student_light_nav_${item.title.lowercase()}").semantics{this.selected=selected;role=Role.Tab},color=if(selected)StudentLightPalette.navy else Color.Transparent,contentColor=if(selected)Color.White else StudentLightPalette.muted,shape=RoundedCornerShape(Radius.hero)){
      Column(Modifier.padding(vertical=Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){Icon(item.icon,null);Text(item.title,Modifier.fillMaxWidth(),textAlign=TextAlign.Center,style=MaterialTheme.typography.labelSmall)}
     }
    };repeat(columns-row.size){Spacer(Modifier.weight(1f))}
   }}}
  }
 }
}
