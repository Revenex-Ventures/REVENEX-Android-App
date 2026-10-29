package com.example.ui.screens.students.portal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.Student
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable internal fun StudentLightIdentity(student:Student){
 var correction by rememberSaveable(student.id){mutableStateOf(false)}
 var field by rememberSaveable(student.id){mutableStateOf("Guardian phone")}
 var value by rememberSaveable(student.id){mutableStateOf("")}
 var reason by rememberSaveable(student.id){mutableStateOf("")}
 val context=LocalContext.current
 BackHandler(correction){correction=false}
 LazyColumn(Modifier.fillMaxSize().testTag("student_profile"),contentPadding=PaddingValues(StudentLightLayout.margin),verticalArrangement=Arrangement.spacedBy(Spacing.lg16)){
  item{val back=LocalWorkspaceBack.current;CompositionLocalProvider(LocalWorkspaceBack provides if(correction)({correction=false})else back){StudentPageHeader(if(correction)"Request correction"else "Student profile")}}
  if(correction){
   item{StudentCard(tone=StudentTone.BLUE){Text("Corrections require school review. This connection does not yet provide a protected correction-request service. No request will be sent.",Modifier.padding(Spacing.lg16),style=MaterialTheme.typography.bodyMedium)}}
   item{OutlinedTextField(field,{field=it},Modifier.fillMaxWidth(),label={Text("Field to correct")},singleLine=true)}
   item{OutlinedTextField(value,{value=it},Modifier.fillMaxWidth(),label={Text("New value")},singleLine=true)}
   item{OutlinedTextField(reason,{reason=it},Modifier.fillMaxWidth(),label={Text("Reason")},minLines=3,isError=reason.length>200,supportingText={Text("${reason.length}/200")})}
   item{StudentAction("Submission unavailable",enabled=false){};Text("Contact the school office to request this correction.",style=MaterialTheme.typography.bodySmall,color=StudentLightPalette.muted)}
  }else{
   item{StudentCard(tone=StudentTone.BLUE){Row(Modifier.padding(Spacing.lg16),horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){Avatar(student.name,size=StudentLightLayout.accountAvatar,background=StudentLightPalette.navy,textColor=StudentLightPalette.canvas);Column(Modifier.weight(1f)){Text(student.name,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("Class ${student.fullClass} · Roll ${student.rollNumber}",style=MaterialTheme.typography.bodySmall);Text(student.admissionNumber,style=MaterialTheme.typography.bodySmall)}}}}
   item{StudentHeading("Academic identity");StudentLightField("Student ID",student.admissionNumber);HorizontalDivider();StudentLightField("Class",student.fullClass);HorizontalDivider();StudentLightField("School ID",student.schoolId)}
   item{StudentHeading("Guardian contact");StudentCard{Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text(student.parentName.ifBlank{"Not provided"},style=MaterialTheme.typography.titleSmall);Text(if(student.parentPhone.isBlank())"Not provided"else "•••• ${student.parentPhone.takeLast(4)}",style=MaterialTheme.typography.bodyMedium);if(student.parentPhone.isNotBlank())OutlinedButton(onClick={runCatching{context.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL,android.net.Uri.fromParts("tel",student.parentPhone,null)))}},modifier=Modifier.heightIn(min=StudentLightLayout.accountAvatar)){Icon(Icons.Outlined.Call,null);Text("Open dialer")}}}}
   item{StudentRow("Request correction","School review required",Icons.Outlined.Edit,StudentTone.BLUE,{correction=true})}
  }
 }
}