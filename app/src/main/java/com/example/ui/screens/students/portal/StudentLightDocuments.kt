package com.example.ui.screens.students.portal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LibraryBook
import com.example.data.model.StudyMaterial
import com.example.ui.components.*
import com.example.ui.theme.*

/** Source records supply every title, availability and file reference. No synthetic cover art. */
@Composable internal fun StudentLightLibrary(books:List<LibraryBook>){
 var query by rememberSaveable{mutableStateOf("")};var selected by rememberSaveable{mutableStateOf("")};var shelf by rememberSaveable{mutableStateOf(false)}
 val book=books.firstOrNull{it.id==selected}
 BackHandler(book!=null||shelf){selected="";shelf=false}
 LazyColumn(Modifier.fillMaxSize().testTag("student_library"),contentPadding=PaddingValues(StudentLightLayout.margin),verticalArrangement=Arrangement.spacedBy(Spacing.lg16)){
  item{val back=LocalWorkspaceBack.current;CompositionLocalProvider(LocalWorkspaceBack provides if(book!=null||shelf)({selected="";shelf=false})else back){StudentPageHeader(if(book!=null)"Book details"else if(shelf)"My books"else "Library")}}
  if(book!=null){
   item{Row(horizontalArrangement=Arrangement.spacedBy(Spacing.lg16)){
    StudentBookSpine(book.title,book.author)
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text(book.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(book.author,style=MaterialTheme.typography.bodyMedium,color=StudentLightPalette.muted);StudentBadge("${book.availableCopies} copies available",StudentTone.BLUE);StudentLightField("Category",book.category);StudentLightField("Shelf",book.shelfLocation)}
   }}
   item{HorizontalDivider();Spacer(Modifier.height(Spacing.lg16));StudentHeading("Available at");StudentCard(tone=StudentTone.BLUE){Row(Modifier.padding(Spacing.lg16),horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){Icon(Icons.Outlined.AccountBalance,null);Column{Text("School library",style=MaterialTheme.typography.titleSmall);Text("Borrow at your library desk",style=MaterialTheme.typography.bodySmall)}}}}
   item{StudentAction("View in library"){selected=""}}
  }else if(shelf){
   item{StudentTabs(listOf("Borrowed","History"),0){}}
   item{EmptyStateView("Borrowing details unavailable","This school connection does not expose personal loans or return dates. Ask the library desk for your borrowing record.")}
   item{StudentAction("Browse catalogue"){shelf=false}}
  }else{
   item{OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search books, authors or subjects")},leadingIcon={Icon(Icons.Outlined.Search,null)},singleLine=true)}
   item{StudentLightShortcutGrid(listOf(StudentLightModule("Catalog",StudentIcons.Book,"catalog"),StudentLightModule("My books",Icons.Outlined.LocalLibrary,"shelf")),2){if(it=="shelf")shelf=true else query=""}}
   item{StudentHeading("School catalogue")}
   val visible=books.filter{it.title.contains(query,true)||it.author.contains(query,true)||it.category.contains(query,true)}
   if(visible.isEmpty())item{EmptyStateView("No books found","Try another title or author.")}
   items(visible,key={it.id}){item->Surface(onClick={selected=item.id},color=StudentLightPalette.quiet,shape=RoundedCornerShape(Radius.sm)){
    Row(Modifier.fillMaxWidth().padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){
     Icon(StudentIcons.Book,null,tint=StudentLightPalette.navy)
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Spacing.xs4)){Text(item.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold);Text(item.author,style=MaterialTheme.typography.bodySmall,color=StudentLightPalette.muted);StudentBadge("${item.availableCopies} available",StudentTone.BLUE)}
     Icon(Icons.Outlined.ChevronRight,null)
    }
   }}
  }
 }
}

@Composable private fun StudentBookSpine(title:String,author:String){
 Surface(Modifier.width(StudentLightLayout.coverWidth).heightIn(min=StudentLightLayout.coverHeight),color=StudentLightPalette.navy,contentColor=StudentLightPalette.canvas,shape=RoundedCornerShape(Radius.xs)){
  Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.md12)){Icon(StudentIcons.Book,null);Text(title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold);Text(author,style=MaterialTheme.typography.labelSmall)}
 }
}

@Composable internal fun StudentLightResources(materials:List<StudyMaterial>){
 var query by rememberSaveable{mutableStateOf("")};var subject by rememberSaveable{mutableStateOf("All")};var selected by rememberSaveable{mutableStateOf("")}
 val resource=materials.firstOrNull{it.id==selected}
 BackHandler(resource!=null){selected=""}
 LazyColumn(Modifier.fillMaxSize().testTag("student_resources"),contentPadding=PaddingValues(StudentLightLayout.margin),verticalArrangement=Arrangement.spacedBy(Spacing.lg16)){
  item{val back=LocalWorkspaceBack.current;CompositionLocalProvider(LocalWorkspaceBack provides if(resource!=null)({selected=""})else back){StudentPageHeader(if(resource==null)"Resources"else "Resource details")}}
  if(resource==null){
   item{StudentCard(tone=StudentTone.BLUE){Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.md12)){Text("Learning resources",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search resources…")},leadingIcon={Icon(Icons.Outlined.Search,null)},singleLine=true)}}}
   item{LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){items(listOf("All")+materials.map{it.subject}.distinct()){value->FilterChip(subject==value,{subject=value},modifier=Modifier.heightIn(min=48.dp),colors=FilterChipDefaults.filterChipColors(containerColor=StudentLightPalette.information,selectedContainerColor=StudentLightPalette.navy,selectedLabelColor=StudentLightPalette.canvas),label={Text(value)})}}}
   val visible=materials.filter{(subject=="All"||subject==it.subject)&&(it.title.contains(query,true)||it.description.contains(query,true))}
   if(visible.isEmpty())item{EmptyStateView("No resources","Notes shared for your class will appear here.")}
   items(visible,key={it.id}){item->StudentRow(item.title,"${item.subject} · Class ${item.classGrade}\n${item.fileType} · ${item.fileSize}",StudentIcons.Document,StudentTone.BLUE,{selected=item.id})}
  }else{
   item{StudentCard(tone=StudentTone.BLUE){Row(Modifier.padding(Spacing.lg16),horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){StudentIcon(StudentIcons.Document,StudentTone.BLUE);Column(Modifier.weight(1f)){Text(resource.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("${resource.subject} · Class ${resource.classGrade}",style=MaterialTheme.typography.bodySmall);Text("${resource.fileType} · ${resource.fileSize}",style=MaterialTheme.typography.bodySmall)}}}}
   item{StudentHeading("About this resource");Text(resource.description.ifBlank{"No additional description."},style=MaterialTheme.typography.bodyMedium)}
   item{if(resource.attachmentUrl.isNotBlank())SchoolDocumentButton(resource.attachmentUrl,resource.attachmentName.ifBlank{"Open document"})else EmptyStateView("Document unavailable","The school has not provided a download link for this resource.")}
   item{HorizontalDivider();StudentLightField("File type",resource.fileType);StudentLightField("File size",resource.fileSize);StudentLightField("Subject",resource.subject);StudentLightField("Shared by",resource.teacherName)}
   item{OutlinedButton(onClick={selected=""},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Icon(Icons.Outlined.ArrowBack,null);Text("Back to resources")}}
  }
 }
}

@Composable internal fun StudentLightField(label:String,value:String){
 Row(Modifier.fillMaxWidth().padding(vertical=Spacing.sm8),horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){
  Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,color=StudentLightPalette.muted)
  Text(value.ifBlank{"Not provided"},Modifier.weight(1.4f),style=MaterialTheme.typography.bodySmall,fontWeight=FontWeight.Medium)
 }
}
