package com.example.ui.screens.teachers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Row editor over existing confirmed, per-student draft writes. No synthetic bulk transaction. */
@Composable fun TeacherMarks(repository:ErpDataRepository,onIndividualRecords:()->Unit) {
 val actor by repository.currentUser.collectAsState();val students by repository.students.collectAsState();val reports by repository.reportCards.collectAsState();val teacher=teacherAllocation(repository)
 var cohort by rememberSaveable(actor.id){mutableStateOf(teacher?.assignedClasses?.firstOrNull().orEmpty())}
 var subject by rememberSaveable(actor.id){mutableStateOf(teacher?.subjects?.firstOrNull().orEmpty())}
 var term by rememberSaveable(actor.id){mutableStateOf("")};var maximum by rememberSaveable(actor.id){mutableStateOf("100")}
 val entries=remember(actor.id,cohort,subject,term){mutableStateMapOf<String,String>()}
 val pupils=students.filter{it.fullClass==cohort&&cohort in teacher?.assignedClasses.orEmpty()}.sortedBy{it.rollNumber}
 var busy by remember{mutableStateOf(false)};var feedback by remember{mutableStateOf<String?>(null)};var failed by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope();val max=maximum.toIntOrNull()
 val valid=term.trim().length in 1..80&&subject in teacher?.subjects.orEmpty()&&max!=null&&max in 1..10000&&entries.isNotEmpty()&&entries.values.all{it.toIntOrNull()?.let{n->n in 0..max}==true}
 Column(Modifier.fillMaxSize().imePadding().testTag("teacher_marks")) {
  LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(horizontal=TeacherLayout.margin,vertical=Spacing.sm8),verticalArrangement=Arrangement.spacedBy(TeacherLayout.gap)) {
   item{WorkspacePageHeader("Marks")}
   item {
    ResponsiveFormFields(
     first={field->TeacherSelect("Class",cohort,teacher?.assignedClasses.orEmpty(),{if(!busy)cohort=it},field)},
     second={field->TeacherSelect("Subject",subject,teacher?.subjects.orEmpty(),{if(!busy)subject=it},field)}
    )
   }
   item{TeacherMarkFields(first={OutlinedTextField(term,{term=it},it.testTag("marks_term"),label={Text("Assessment")},singleLine=true,enabled=!busy)},second={OutlinedTextField(maximum,{maximum=it},it.testTag("marks_maximum"),label={Text("Max marks")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,enabled=!busy,isError=max==null||max !in 1..10000)})}
   if(pupils.isEmpty())item{EmptyStateView("No accessible students","Select a class from your verified teaching allocation.")}
   items(pupils,key={it.id}){pupil->
    val report=reports.firstOrNull{it.studentId==pupil.id&&it.term==term.trim()};val saved=report?.scores?.firstOrNull{it.subjectName==subject};val value=entries[pupil.id]?:saved?.obtainedMarks?.toString().orEmpty()
    Row(Modifier.fillMaxWidth().heightIn(min=64.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
     Text(pupil.rollNumber.toString(),style=MaterialTheme.typography.bodySmall);Avatar(pupil.name,size=32.dp,background=TeacherColors.blue,textColor=TeacherColors.blueInk)
     Column(Modifier.weight(1f)){Text(pupil.name,style=MaterialTheme.typography.bodyMedium,fontWeight=FontWeight.Medium);if(report?.published==true)Text("Published · locked",style=MaterialTheme.typography.labelSmall)}
     OutlinedTextField(value,{entries[pupil.id]=it;feedback=null},Modifier.width(76.dp).testTag("teacher_mark_${pupil.id}"),singleLine=true,enabled=!busy&&report?.published!=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),isError=value.isNotBlank()&&(value.toIntOrNull()==null||max==null||value.toIntOrNull() !in 0..max),textStyle=MaterialTheme.typography.bodyMedium)
    };HorizontalDivider()
   }
   item{Text("Private draft · Only changed rows are saved. Published terms stay locked.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   item{TextButton(onClick=onIndividualRecords,enabled=!busy){Text("Individual records & report cards")}}
  }
  Surface(color=MaterialTheme.colorScheme.surface){Column(Modifier.padding(TeacherLayout.margin),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
   feedback?.let{Text(it,color=if(failed)MaterialTheme.colorScheme.error else TeacherColors.green,style=MaterialTheme.typography.bodySmall)}
   Button(onClick={
    busy=true;feedback=null;failed=false
    val changes=entries.toMap();val studentIds=pupils.map{it.id}.toSet();val saveTerm=term;val saveSubject=subject;val saveMax=max!!
    scope.launch{var confirmed=0
     try{changes.forEach{(id,value)->check(repository.currentUser.value.id==actor.id);check(id in studentIds);repository.saveMarksConfirmed(id,saveTerm,saveSubject,value.toInt(),saveMax);confirmed++};entries.clear();feedback="$confirmed draft row(s) confirmed."}
     catch(e:Exception){if(e is CancellationException)throw e;failed=true;feedback="$confirmed row(s) confirmed. Remaining saves were not confirmed; reload records before retrying."}
     finally{busy=false}
    }
   },enabled=!busy&&valid,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("save_teacher_drafts")){Text(if(busy)"Saving…"else "Save draft")}
  }}
 }
}

@Composable private fun TeacherMarkFields(first:@Composable (Modifier)->Unit,second:@Composable (Modifier)->Unit) {
 BoxWithConstraints(Modifier.fillMaxWidth()) {
  if(maxWidth<300.dp||androidx.compose.ui.platform.LocalDensity.current.fontScale>1.3f)Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){first(Modifier.fillMaxWidth());second(Modifier.fillMaxWidth())}
  else Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){first(Modifier.weight(2f));second(Modifier.weight(1f))}
 }
}
