package com.example.ui.screens.students.portal

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun studentDate(value:String):LocalDate? = listOf(DateTimeFormatter.ISO_LOCAL_DATE,DateTimeFormatter.ofPattern("dd MMM uuuu",Locale.US),DateTimeFormatter.ofPattern("dd-MM-uuuu",Locale.US)).firstNotNullOfOrNull{runCatching{LocalDate.parse(if(value.contains('T'))value.take(10)else value,it)}.getOrNull()}
internal fun studentToday():LocalDate=LocalDate.now(ZoneId.of("Asia/Kolkata"))
internal fun studentSubjectTone(subject:String):StudentTone=when{
 subject.contains("math",true)->StudentTone.ORANGE
 subject.contains("english",true)||subject.contains("hindi",true)||subject.contains("language",true)->StudentTone.LAVENDER
 subject.contains("history",true)||subject.contains("geography",true)||subject.contains("social",true)->StudentTone.MINT
 else->StudentTone.BLUE
}
internal fun studentSubjectIcon(subject:String):ImageVector=when(studentSubjectTone(subject)){
 StudentTone.ORANGE->StudentIcons.Document
 StudentTone.LAVENDER->StudentIcons.Book
 StudentTone.MINT->Icons.Outlined.AccountBalance
 else->Icons.Outlined.Science
}
/** Never falls back to another loaded profile or a UI-selected child. */
@Composable internal fun ownStudent(repository:ErpDataRepository):Student? {
 val actor by repository.currentUser.collectAsState();val students by repository.students.collectAsState()
 return students.firstOrNull{actor.role==UserRole.STUDENT&&it.id==actor.associatedStudentId&&it.schoolId==actor.schoolId}
}
@Composable internal fun StudentCard(modifier:Modifier=Modifier,tone:StudentTone?=null,onClick:(()->Unit)?=null,tag:String="",content:@Composable ColumnScope.()->Unit) {
 val interaction=remember{MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState();val reduced=rememberReducedMotion()
 val scale by animateFloatAsState(if(pressed&&!reduced).98f else 1f,tween(if(reduced)0 else StudentLayout.pressMillis),label="Student press")
 val color=if(tone==null)MaterialTheme.colorScheme.surface else StudentColors.colors(tone).first
 val style=modifier.fillMaxWidth().testTag(tag).graphicsLayer{scaleX=scale;scaleY=scale}
 val border=BorderStroke(1.dp,if(tone==null)MaterialTheme.colorScheme.outlineVariant else color)
 if(onClick==null)Surface(style,color=color,contentColor=MaterialTheme.colorScheme.onSurface,shape=RoundedCornerShape(StudentLayout.radius),border=border){Column(content=content)}
 else Surface(onClick=onClick,modifier=style,color=color,contentColor=MaterialTheme.colorScheme.onSurface,shape=RoundedCornerShape(StudentLayout.radius),border=border,interactionSource=interaction){Column(content=content)}
}
@Composable internal fun StudentIcon(icon:ImageVector,tone:StudentTone,size:androidx.compose.ui.unit.Dp=StudentLayout.iconWell) {
 val colors=StudentColors.colors(tone)
 Surface(shape=RoundedCornerShape(Radius.sm),color=colors.first){Box(Modifier.size(size),contentAlignment=Alignment.Center){Icon(icon,null,Modifier.size(if(size>40.dp)28.dp else 22.dp),tint=colors.second)}}
}
@Composable internal fun StudentHeading(title:String,action:String?=null,onClick:()->Unit={}) {
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(action!=null)TextButton(onClick=onClick,colors=ButtonDefaults.textButtonColors(contentColor=StudentColors.actionText)){Text(action);Icon(Icons.Outlined.ChevronRight,null)}}
}
@Composable internal fun StudentRow(title:String,subtitle:String,icon:ImageVector,tone:StudentTone,onClick:()->Unit,trailing:String?=null,statusTone:StudentTone?=null) {
 StudentCard(onClick=onClick){Row(Modifier.padding(StudentLayout.gap).heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap)) {
  StudentIcon(icon,tone);Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold);if(subtitle.isNotBlank())Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  if(trailing!=null){if(statusTone==null)Text(trailing,style=MaterialTheme.typography.labelSmall,color=StudentColors.actionText)else StudentBadge(trailing,statusTone)}
  Icon(Icons.Outlined.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)
 }}
}
@Composable internal fun StudentTabs(labels:List<String>,selected:Int,change:(Int)->Unit) {
 val reduced=rememberReducedMotion()
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){labels.forEachIndexed{i,label->
  val color by animateColorAsState(if(i==selected)StudentColors.selection else MaterialTheme.colorScheme.surfaceVariant,tween(if(reduced)0 else StudentLayout.selectionMillis),label="Student selection")
  Surface(onClick={change(i)},modifier=Modifier.weight(1f).heightIn(min=48.dp).semantics{this.selected=i==selected;role=Role.Tab},color=color,shape=RoundedCornerShape(Radius.sm)){Box(Modifier.padding(Spacing.sm8),contentAlignment=Alignment.Center){Text(label,style=MaterialTheme.typography.labelMedium,color=if(i==selected)StudentColors.onSelection else MaterialTheme.colorScheme.onSurfaceVariant)}}
 }}
}
@Composable internal fun StudentAction(label:String,enabled:Boolean=true,onClick:()->Unit) {
 val interaction=remember{MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState();val reduced=rememberReducedMotion()
 val scale by animateFloatAsState(if(pressed&&!reduced).98f else 1f,tween(if(reduced)0 else StudentLayout.pressMillis),label="Action press")
 Button(onClick=onClick,enabled=enabled,interactionSource=interaction,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).graphicsLayer{scaleX=scale;scaleY=scale},shape=RoundedCornerShape(Radius.sm),colors=ButtonDefaults.buttonColors(containerColor=StudentColors.action,contentColor=StudentColors.onAction)){Text(label)}
}
@Composable internal fun StudentBadge(label:String,tone:StudentTone) {
 val colors=StudentColors.colors(tone)
 Surface(color=colors.first,shape=RoundedCornerShape(Radius.sm)){Text(label,Modifier.padding(horizontal=Spacing.sm8,vertical=Spacing.xs4),style=MaterialTheme.typography.labelSmall,color=if(tone==StudentTone.ORANGE)StudentColors.onOrange else colors.second)}
}
@Composable fun StudentHeader(user:UserProfile,unread:Int,inbox:()->Unit,profile:()->Unit) {
 Surface(color=MaterialTheme.colorScheme.background){Row(Modifier.fillMaxWidth().padding(horizontal=StudentLayout.margin),verticalAlignment=Alignment.CenterVertically){Box(Modifier.weight(1f)){RevenexBrand(nameWidth=EntryTokens.compactName)};IconButton(onClick=inbox){BadgedBox(badge={if(unread>0)Badge{Text(unread.toString())}}){Icon(Icons.Outlined.Notifications,"Inbox, $unread unread")}};VerticalDivider(Modifier.height(24.dp));IconButton(onClick=profile,modifier=Modifier.semantics{contentDescription="Your profile"}){Avatar(user.name,size=32.dp,background=StudentColors.colors(StudentTone.ORANGE).first,textColor=StudentColors.onOrange)}}}
}
@Composable fun StudentNavigation(route:String?,navigate:(String)->Unit) {
 val items=NavConfig.getBottomNavItems(UserRole.STUDENT)
 Surface(color=MaterialTheme.colorScheme.surface){Column{HorizontalDivider();BoxWithConstraints{
  val rows=if(maxWidth<360.dp&&androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f)items.chunked(3)else listOf(items)
  Column{rows.forEach{row->Row(Modifier.fillMaxWidth()){row.forEach{item->
   val selected=item.route==route
   val tint by animateColorAsState(if(selected)StudentColors.selection else MaterialTheme.colorScheme.onSurfaceVariant,tween(if(rememberReducedMotion())0 else StudentLayout.selectionMillis),label="Navigation selection")
   Surface(onClick={navigate(item.route)},modifier=Modifier.weight(1f).heightIn(min=64.dp).semantics{this.selected=selected;role=Role.Tab},color=Color.Transparent){Column(Modifier.padding(vertical=Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){Icon(if(selected)item.selectedIcon else item.unselectedIcon,null,tint=tint);Text(item.label,style=MaterialTheme.typography.labelSmall,color=if(selected)StudentColors.actionText else MaterialTheme.colorScheme.onSurfaceVariant)}}
  }}}}
 }}}
}
