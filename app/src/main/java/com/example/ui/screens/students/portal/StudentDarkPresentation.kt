package com.example.ui.screens.students.portal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WorkspacePageHeader
import com.example.ui.navigation.BottomNavItem
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

/** Editorial hierarchy only for the resolved Student dark appearance. */
@Composable internal fun StudentPageHeader(title:String,actions:@Composable RowScope.()->Unit={}) {
 if(!LocalStudentDark.current){WorkspacePageHeader(title,actions=actions);return}
 Column(verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)) {
  Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically) {
   val back=com.example.ui.components.LocalWorkspaceBack.current
   if(back!=null)IconButton(onClick=back){Icon(Icons.Outlined.ArrowBack,"Back")}
   Text(title.uppercase(),Modifier.weight(1f),style=MaterialTheme.typography.labelSmall.copy(letterSpacing=1.5.sp),color=StudentDarkPalette.muted)
   actions()
  }
  val headline=when(title){
   "Learn"->"Your next chapter."
   "Assignments"->"A little focus. A big\nleap."
   "Attendance"->"Showing up is a superpower."
   "Results"->"Look how far you’ve come."
   "Timetable"->"Your day, in rhythm."
   "Fees"->"A clear view of your fees."
   "Invoice"->"Every detail, in one place."
   "Library"->"Stay curious."
   "Inbox"->"Good to know."
   "Profile"->"Uniquely you."
   "Settings"->"Uniquely you."
   "Resources"->"Good things to learn from."
   else->null
  }
  if(headline!=null)Text(headline,style=MaterialTheme.typography.headlineLarge)
 }
}

@Composable internal fun StudentDarkDock(items:List<BottomNavItem>,route:String?,navigate:(String)->Unit) {
 Surface(color=StudentDarkPalette.canvas){
  Surface(Modifier.padding(horizontal=StudentLayout.margin,vertical=Spacing.sm8).fillMaxWidth(),color=StudentDarkPalette.navigation,shape=RoundedCornerShape(StudentDarkPalette.radius)){
   val large=androidx.compose.ui.platform.LocalDensity.current.fontScale>1.3f
   val rows=if(large && androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp<400)items.chunked(3)else listOf(items)
   Column{rows.forEach{row->Row(Modifier.padding(Spacing.xs4)) {row.forEach{item->
    val selected=item.route==route
    val capsule by androidx.compose.animation.animateColorAsState(
     if(selected)StudentDarkPalette.orange else Color.Transparent,
     androidx.compose.animation.core.tween(if(com.example.ui.components.rememberReducedMotion())0 else StudentLayout.selectionMillis),label="Student dark dock")
    val you=item.route==Screen.OperationsHub.route
    Surface(onClick={navigate(if(you)Screen.Profile.route else item.route)},modifier=Modifier.weight(1f).heightIn(min=StudentDarkLayout.dockTarget).semantics{this.selected=selected;role=Role.Tab},shape=RoundedCornerShape(StudentDarkPalette.radius),color=capsule,contentColor=if(selected)StudentDarkPalette.ink else StudentDarkPalette.muted){
     Column(Modifier.padding(vertical=Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){
      Icon(if(you)Icons.Outlined.Person else item.unselectedIcon,null,Modifier.size(StudentDarkLayout.dockIcon))
      Text(if(you)"You"else item.label,style=MaterialTheme.typography.labelSmall)
     }
    }
   }}}}
  }
 }
}

@Composable internal fun StudentDarkFeature(title:String,subtitle:String,tone:StudentTone,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:(()->Unit)?=null,eyebrow:String?=null,compact:Boolean=false) {
 if(compact){StudentCard(tone=tone,onClick=onClick){Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(title,Modifier.weight(1f),style=MaterialTheme.typography.headlineMedium);Icon(icon,null,Modifier.size(StudentDarkLayout.featureIcon))}
  if(subtitle.isNotBlank())Text(subtitle,style=MaterialTheme.typography.bodySmall)
 }};return}
 StudentCard(tone=tone,onClick=onClick){Column(Modifier.padding(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){if(eyebrow!=null)Text(eyebrow.uppercase(),Modifier.weight(1f),style=MaterialTheme.typography.labelSmall.copy(letterSpacing=1.4.sp));Icon(icon,null,Modifier.size(StudentDarkLayout.featureIcon))}
  Text(title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold)
  if(subtitle.isNotBlank())Text(subtitle,style=MaterialTheme.typography.bodySmall)
 }}
}

@Composable internal fun StudentDarkAccountSummary(name:String,context:String){
 StudentCard(tone=StudentTone.BLUE){Column(Modifier.fillMaxWidth().padding(StudentLayout.margin),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  com.example.ui.components.Avatar(name,size=48.dp,background=StudentDarkPalette.ink,textColor=StudentDarkPalette.blue)
  Text(name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
  Text(context,style=MaterialTheme.typography.bodySmall)
 }}
}
