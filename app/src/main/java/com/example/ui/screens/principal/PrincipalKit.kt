package com.example.ui.screens.principal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.navigation.*
import com.example.data.model.UserRole

@Composable internal fun PrincipalHeading(title:String,action:String?=null,onAction:()->Unit={},content:@Composable ColumnScope.()->Unit) {
 Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
   Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge.copy(fontSize=PrincipalLayout.sectionSize),fontWeight=FontWeight.Bold)
   if(action!=null)Row(Modifier.clickable(onClick=onAction).heightIn(min=Spacing.xxl32),verticalAlignment=Alignment.CenterVertically){Text(action,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary);Icon(Icons.Default.ChevronRight,null,Modifier.size(Spacing.lg16),tint=MaterialTheme.colorScheme.primary)}
  };content()
 }
}
@Composable internal fun PrincipalMetric(value:String,label:String,icon:ImageVector,background:Color,ink:Color,modifier:Modifier=Modifier,onClick:()->Unit={}) {
 Surface(onClick=onClick,modifier=modifier.heightIn(min=PrincipalLayout.metricHeight),shape=RoundedCornerShape(Radius.md),color=background) {
  Column(Modifier.padding(Spacing.md12),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
   Icon(icon,null,Modifier.size(PrincipalLayout.moduleIcon),tint=ink)
   Text(value,style=MaterialTheme.typography.headlineSmall.copy(fontSize=PrincipalLayout.metricSize),fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
   Text(label,style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
}
internal data class PrincipalModule(val title:String,val icon:ImageVector,val route:String,val tone:Int)
@Composable internal fun PrincipalModules(items:List<PrincipalModule>,navigate:(String)->Unit,compact:Boolean=false) {
 if(LocalPrincipalDark.current){PrincipalDarkModules(items,navigate);return}
 Column(verticalArrangement=Arrangement.spacedBy(Spacing.lg16)) {
  items.chunked(3).forEach {row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   row.forEach {item->
    val bg=when(item.tone){0->PrincipalColors.blue;1->PrincipalColors.mint;2->PrincipalColors.lavender;else->PrincipalColors.peach}
    val ink=when(item.tone){0->PrincipalColors.blueInk;1->PrincipalColors.mintInk;2->PrincipalColors.lavenderInk;else->PrincipalColors.peachInk}
    Column(Modifier.weight(1f).clickable {navigate(item.route)}.testTag("principal_module_${item.title.lowercase()}"),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
     Surface(shape=RoundedCornerShape(if(compact)Radius.sm else Radius.lg),color=bg,modifier=if(compact)Modifier.fillMaxWidth().heightIn(min=PrincipalLayout.compactModuleHeight)else Modifier.size(PrincipalLayout.moduleWell)) {
      if(compact)Column(Modifier.padding(Spacing.md12),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {Icon(item.icon,null,Modifier.size(PrincipalLayout.moduleIcon),tint=ink);Text(item.title,style=MaterialTheme.typography.bodySmall,textAlign=TextAlign.Center)}
      else Box(contentAlignment=Alignment.Center){Icon(item.icon,null,Modifier.size(PrincipalLayout.moduleIcon),tint=ink)}
     }
     if(!compact)Text(item.title,style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center)
    }
   };repeat(3-row.size){Spacer(Modifier.weight(1f))}
  }}
 }
}
@Composable internal fun PrincipalRecordRow(name:String,detail:String,onClick:()->Unit,modifier:Modifier=Modifier,trailing:@Composable ()->Unit={}) {
 val dark=LocalPrincipalDark.current
 Row(modifier.fillMaxWidth().then(if(dark)Modifier.background(PrincipalDarkPalette.surface,RoundedCornerShape(Radius.md)).padding(horizontal=Spacing.md12)else Modifier).clickable(onClick=onClick).heightIn(min=PrincipalLayout.rowHeight).padding(vertical=Spacing.sm8),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  Avatar(name.replace(Regex("^(Dr\\.?|Prof\\.?|Mr\\.?|Mrs\\.?|Ms\\.?)\\s+"),""),size=PrincipalLayout.listAvatar,background=PrincipalColors.blue,textColor=PrincipalColors.blueInk)
  Column(Modifier.weight(1f)){Text(name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold);Text(detail,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  trailing();Icon(Icons.Default.ChevronRight,null,Modifier.size(Spacing.lg16),tint=MaterialTheme.colorScheme.onSurfaceVariant)
 }
 if(!dark)HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f))
 else Spacer(Modifier.height(Spacing.sm8))
}
@Composable internal fun PrincipalSelect(value:String,options:List<String>,onSelect:(String)->Unit,modifier:Modifier=Modifier) {
 var open by remember {mutableStateOf(false)}
 Box(modifier){OutlinedButton(onClick={open=true},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(Radius.xs),colors=ButtonDefaults.outlinedButtonColors(contentColor=MaterialTheme.colorScheme.onSurface),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
  Text(value,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Icon(Icons.Default.ExpandMore,null)
 };DropdownMenu(open,{open=false}){options.forEach {v->DropdownMenuItem(text={Text(v)},onClick={onSelect(v);open=false})}}}
}
@Composable internal fun PrincipalTabs(labels:List<String>,selected:Int,onSelect:(Int)->Unit) {
 if(LocalPrincipalDark.current){
  Row(Modifier.fillMaxWidth().padding(vertical=Spacing.sm8),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){labels.forEachIndexed{i,label->Surface(onClick={onSelect(i)},modifier=Modifier.weight(1f),color=if(i==selected)PrincipalDarkPalette.orange else PrincipalDarkPalette.navigation,contentColor=if(i==selected)PrincipalDarkPalette.ink else PrincipalDarkPalette.muted,shape=RoundedCornerShape(Radius.pill)){Box(Modifier.heightIn(min=Spacing.xxxl48).padding(Spacing.sm8),contentAlignment=Alignment.Center){Text(label,style=MaterialTheme.typography.labelMedium,textAlign=TextAlign.Center)}}}
  };return
 }
 TabRow(selectedTabIndex=selected,containerColor=MaterialTheme.colorScheme.background){labels.forEachIndexed {i,label->Tab(selected==i,onClick={onSelect(i)},selectedContentColor=MaterialTheme.colorScheme.primary,unselectedContentColor=MaterialTheme.colorScheme.onSurfaceVariant,text={Text(label,style=MaterialTheme.typography.labelLarge)})}}
}
@Composable fun PrincipalBottomNavigation(route:String?,navigate:(String)->Unit) {
 if(LocalPrincipalDark.current){PrincipalDarkNavigation(route,navigate);return}
 val items=NavConfig.getBottomNavItems(UserRole.PRINCIPAL)
 Surface(color=MaterialTheme.colorScheme.surface) {
  BoxWithConstraints {
   val largeText=androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f
   val rows=if(maxWidth<PrincipalLayout.navigationWrapWidth&&largeText)items.chunked(3) else listOf(items)
   Column {
    HorizontalDivider()
    rows.forEach {row->
     Row(Modifier.fillMaxWidth().padding(horizontal=Spacing.sm8),horizontalArrangement=Arrangement.SpaceEvenly) {
      row.forEach {item->
       val selected=item.route==route
       Surface(onClick={navigate(item.route)},modifier=Modifier.weight(1f).heightIn(min=PrincipalLayout.moduleWell),shape=RoundedCornerShape(Radius.md),color=if(selected&&item.route==Screen.Dashboard.route)PrincipalColors.peach else Color.Transparent) {
        Column(Modifier.padding(vertical=Spacing.sm8),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
         Icon(if(selected)item.selectedIcon else item.unselectedIcon,item.label,tint=if(selected)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
         Text(item.label,style=MaterialTheme.typography.labelSmall,color=if(selected)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
       }
      }
     }
    }
   }
  }
 }
}

@Composable internal fun PrincipalPageHeader(title:String,modifier:Modifier=Modifier,actions:@Composable RowScope.()->Unit={}) {
 val back=LocalWorkspaceBack.current
 BoxWithConstraints(modifier.fillMaxWidth()) {
  val large=androidx.compose.ui.platform.LocalDensity.current.fontScale>1.2f&&maxWidth<360.dp
  Column {
   Row(Modifier.fillMaxWidth().heightIn(min=Spacing.xxxl48),verticalAlignment=Alignment.CenterVertically) {
    if(back!=null)IconButton(onClick=back){Icon(Icons.Default.ArrowBack,"Back")}
    Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge.copy(fontSize=PrincipalLayout.sectionSize),fontWeight=FontWeight.Bold)
    if(!large)actions()
   }
   if(large)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End,verticalAlignment=Alignment.CenterVertically,content=actions)
  }
 }
}

@Composable internal fun PrincipalSummary(label:String,value:String,caption:String,icon:ImageVector,bg:Color,ink:Color,modifier:Modifier=Modifier) {
 Surface(modifier=modifier,shape=RoundedCornerShape(Radius.sm),color=bg,contentColor=if(LocalPrincipalDark.current)PrincipalDarkPalette.ink else MaterialTheme.colorScheme.onSurface) {
  Row(Modifier.heightIn(min=PrincipalLayout.summaryHeight).padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   Icon(icon,null,Modifier.size(PrincipalLayout.moduleIcon),tint=ink)
   Column(Modifier.weight(1f)) {
    Text(label,style=MaterialTheme.typography.bodySmall)
    Text(value,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,color=ink)
    if(caption.isNotBlank())Text(caption,style=MaterialTheme.typography.bodySmall,color=if(LocalPrincipalDark.current)ink else MaterialTheme.colorScheme.onSurfaceVariant)
   }
  }
 }
}
