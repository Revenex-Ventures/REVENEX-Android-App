package com.example.ui.screens.students.portal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LibraryBook
import com.example.data.model.StudyMaterial
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SchoolDocumentButton
import com.example.ui.theme.*

/** Real catalogue records only; this presentation never invents personal loans. */
@Composable internal fun StudentDarkLibrary(books:List<LibraryBook>) {
 var query by rememberSaveable{mutableStateOf("")}
 var selected by rememberSaveable{mutableStateOf("")}
 var loans by rememberSaveable{mutableStateOf(false)}
 val book=books.firstOrNull{it.id==selected}
 BackHandler(book!=null || loans){selected="";loans=false}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)){
  item{val back=com.example.ui.components.LocalWorkspaceBack.current;CompositionLocalProvider(com.example.ui.components.LocalWorkspaceBack provides if(book!=null||loans)({selected="";loans=false})else back){StudentPageHeader(if(book==null)"Library"else "Book details")}}
  if(book!=null){
   item{Text("Your next good read.",style=MaterialTheme.typography.headlineLarge)}
   item{StudentDarkFeature(book.title,book.author,StudentTone.BLUE,StudentIcons.Book,eyebrow="Library")}
   item{StudentDarkFeature("${book.availableCopies} available","Copies currently listed by your school",StudentTone.MINT,StudentIcons.Book)}
   item{StudentCard{Column(Modifier.padding(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){Text("${book.category} · Shelf ${book.shelfLocation}",style=MaterialTheme.typography.titleSmall);Text("Borrowing is handled at your library desk.",style=MaterialTheme.typography.bodySmall,color=StudentDarkPalette.muted)}}}
   item{StudentAction("View library"){selected=""}}
  }else if(loans){
   item{EmptyStateView("Borrowing details unavailable","This school connection does not expose personal loans or return dates.")}
   item{StudentAction("Browse catalogue"){loans=false}}
  }else{
   item{OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Search a title, author or idea")},leadingIcon={Icon(Icons.Outlined.Search,null)},singleLine=true)}
   item{StudentDarkFeature("Your reading shelf","Browse books available at your school.",StudentTone.MINT,StudentIcons.Book,{loans=true},compact=true)}
   item{StudentHeading("Discover something new")}
   val visible=books.filter{it.title.contains(query,true)||it.author.contains(query,true)}
   if(visible.isEmpty())item{EmptyStateView("No books found","Try another title or author.")}
   items(visible.chunked(2)){row->Row(horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
    row.forEachIndexed{i,item->Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
     StudentCard(tone=if(i==0)StudentTone.BLUE else StudentTone.ORANGE,onClick={selected=item.id}){Column(Modifier.padding(StudentLayout.margin).heightIn(min=StudentDarkLayout.bookCover),verticalArrangement=Arrangement.spacedBy(16.dp)){Icon(StudentIcons.Book,null);Text(item.title.substringBefore(':').split(' ').take(2).joinToString(" ").uppercase(),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold,maxLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)}}
     Text(item.title,style=MaterialTheme.typography.titleSmall);Text(item.author,style=MaterialTheme.typography.bodySmall,color=StudentDarkPalette.muted)
    }}
    if(row.size==1)Spacer(Modifier.weight(1f))
   }}
  }
 }
}

@Composable internal fun StudentDarkResources(materials:List<StudyMaterial>) {
 var query by rememberSaveable{mutableStateOf("")};var subject by rememberSaveable{mutableStateOf("All")};var selected by rememberSaveable{mutableStateOf("")}
 val material=materials.firstOrNull{it.id==selected}
 BackHandler(material!=null){selected=""}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)){
  item{val back=com.example.ui.components.LocalWorkspaceBack.current;CompositionLocalProvider(com.example.ui.components.LocalWorkspaceBack provides if(material!=null)({selected=""})else back){StudentPageHeader(if(material==null)"Resources"else "Resource detail")}}
  if(material==null){
   item{OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search notes, topic or keyword")},leadingIcon={Icon(Icons.Outlined.Search,null)},singleLine=true)}
   item{androidx.compose.foundation.lazy.LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){
    items(listOf("All")+materials.map{it.subject}.distinct()){value->FilterChip(subject==value,{subject=value},border=null,colors=FilterChipDefaults.filterChipColors(containerColor=StudentDarkPalette.navigation,labelColor=StudentDarkPalette.muted,selectedContainerColor=StudentDarkPalette.orange,selectedLabelColor=StudentDarkPalette.ink),label={Text(value)})}
   }}
   val visible=materials.filter{(subject=="All"||subject==it.subject)&&(it.title.contains(query,true)||it.description.contains(query,true))}
   if(visible.isEmpty())item{EmptyStateView("No resources","Notes shared for your class will appear here.")}
   items(visible){item->StudentDarkFeature(item.title,"${item.subject} · ${item.fileType} · ${item.fileSize}",when{item.subject.contains("math",true)->StudentTone.MINT;item.subject.contains("english",true)->StudentTone.NEUTRAL;else->StudentTone.BLUE},StudentIcons.Book,{selected=item.id},compact=true)}
  }else{
   item{Text(material.title,style=MaterialTheme.typography.headlineLarge)}
   item{StudentDarkFeature(material.chapter.ifBlank{material.title},material.description,StudentTone.BLUE,StudentIcons.Book,eyebrow="${material.subject} · Class ${material.classGrade}")}
   item{StudentCard{Column(Modifier.padding(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){Text(material.attachmentName.ifBlank{material.title},style=MaterialTheme.typography.titleMedium);Text("${material.fileType} · ${material.fileSize}",style=MaterialTheme.typography.bodySmall,color=StudentDarkPalette.muted)}}}
   item{StudentRow("Available to your class","Shared by ${material.teacherName}",StudentIcons.People,StudentTone.BLUE,{selected=""})}
   item{if(material.attachmentUrl.isNotBlank())SchoolDocumentButton(material.attachmentUrl,"Open document")else Text("Download link unavailable",color=StudentDarkPalette.muted)}
   item{StudentAction("Back to resources"){selected=""}}
  }
 }
}
