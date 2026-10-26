package com.example.ui.screens.students.portal

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.datasource.SupabaseFileStore
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.SchoolDocumentButton
import com.example.ui.theme.StudentLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Recoverable input; only a confirmed server response changes submission state. */
@Composable internal fun StudentCloudSubmission(repository:ErpDataRepository,assignmentId:String) {
 val actor by repository.currentUser.collectAsState()
 val capabilities by repository.backendCapabilities.collectAsState()
 val ready=capabilities.loaded&&capabilities.submissions
 val submissions by repository.assignmentSubmissions.collectAsState()
 val existing=submissions.firstOrNull{it.assignmentId==assignmentId && it.studentId==actor.associatedStudentId && it.schoolId==actor.schoolId}
 var note by rememberSaveable(actor.id,assignmentId){mutableStateOf(existing?.note.orEmpty())}
 var baseVersion by rememberSaveable(actor.id,assignmentId){mutableIntStateOf(existing?.version?:0)}
 var dirty by rememberSaveable(actor.id,assignmentId){mutableStateOf(false)}
 var picked by rememberSaveable(actor.id,assignmentId){mutableStateOf("")}
 var pickedName by rememberSaveable(actor.id,assignmentId){mutableStateOf("")}
 var uploaded by rememberSaveable(actor.id,assignmentId){mutableStateOf("")}
 var removeExisting by rememberSaveable(actor.id,assignmentId){mutableStateOf(false)}
 var busy by remember{mutableStateOf(false)}
 var message by remember(actor.id,assignmentId){mutableStateOf("")}
 var failed by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope();val context=LocalContext.current
 val locked=existing?.reviewStatus=="REVIEWED"
 LaunchedEffect(existing?.version){if(!dirty && !busy){note=existing?.note.orEmpty();baseVersion=existing?.version?:0}}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null){
   runCatching{context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}
   val old=uploaded
   if(old.isNotBlank())scope.launch{runCatching{SupabaseFileStore.discardStaged(old)}}
   picked=uri.toString();uploaded="";dirty=true;removeExisting=false;message=""
   pickedName=runCatching{context.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{cursor->if(cursor.moveToFirst())cursor.getString(0)else null}}.getOrNull().orEmpty()
  }
 }
 Column(verticalArrangement=Arrangement.spacedBy(StudentLayout.gap),modifier=Modifier.testTag("cloud_submission_form")){
  if(!ready)Text("The school submission service could not be confirmed. Your input stays here. Refresh after reconnect or ask the school to finish setup.",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
  Text(if(existing==null)"Add a file or write your solution. Files stay private to your school permissions." else "${existing.reviewStatus.lowercase().replaceFirstChar{it.uppercase()}} · Version ${existing.version}",style=MaterialTheme.typography.bodySmall)
  if(existing?.attachmentRef?.isNotBlank()==true)SchoolDocumentButton(existing.attachmentRef,existing.attachmentName.ifBlank{"Submitted file"})
  if(existing?.feedback?.isNotBlank()==true){Text("Teacher feedback",style=MaterialTheme.typography.labelLarge);Text(existing.feedback)}
  if(locked)Text("Reviewed work is locked. Contact your teacher if a correction is needed.",style=MaterialTheme.typography.bodySmall)
  OutlinedTextField(note,{note=it;dirty=true;message=""},Modifier.fillMaxWidth().testTag("submission_note"),label={Text("Your solution / note")},enabled=!busy&&!locked,minLines=3,isError=note.length>8000,supportingText={Text("${note.length}/8000")})
  OutlinedButton(onClick={picker.launch(arrayOf("application/pdf","image/jpeg","image/png","application/vnd.openxmlformats-officedocument.wordprocessingml.document"))},enabled=!busy&&!locked,modifier=Modifier.fillMaxWidth()){
   Text(if(picked.isBlank())"Add file · PDF, image or DOCX · up to 10 MB"else pickedName.ifBlank{"Selected document"})
  }
  if(picked.isNotBlank() || existing?.attachmentRef?.isNotBlank()==true && !removeExisting)TextButton(enabled=!busy&&!locked,onClick={
   val old=uploaded;if(old.isNotBlank())scope.launch{runCatching{SupabaseFileStore.discardStaged(old)}}
   picked="";pickedName="";uploaded="";removeExisting=true;dirty=true
  }){Text("Remove attachment from next submission")}
  if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
  if(message.isNotBlank())Text(message,color=if(failed)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodySmall)
  if(failed||!ready)TextButton(enabled=!busy,onClick={repository.reconcileOnResume()}){Text("Refresh school status")}
  if(dirty && existing!=null && existing.version!=baseVersion)TextButton(enabled=!busy,onClick={note=existing.note;baseVersion=existing.version;dirty=picked.isNotBlank()||removeExisting}){Text("Load saved solution · Version ${existing.version}")}
  StudentAction(if(busy)"Submitting…"else if(existing==null)"Submit"else "Resubmit",ready&&!busy&&!locked&&note.length<=8000&&(note.isNotBlank()||picked.isNotBlank()||existing?.attachmentRef?.isNotBlank()==true&&!removeExisting)){
   if(!busy){busy=true;message="";scope.launch{
    try{
     val owner=actor.id
     if(picked.isNotBlank() && uploaded.isBlank()){
      val uri=Uri.parse(picked)
      uploaded=repository.uploadAttachment(context,"submissions",uri,pickedName,context.contentResolver.getType(uri).orEmpty()).url
     }
     check(repository.currentUser.value.id==owner){"Account changed"}
     val ref=uploaded.ifBlank{if(removeExisting)""else existing?.attachmentRef.orEmpty()}
     val saved=repository.submitAssignmentContentConfirmed(assignmentId,ref,note,baseVersion)
     baseVersion=saved.version;note=saved.note;dirty=false;picked="";pickedName="";uploaded="";removeExisting=false
     failed=false;message="Submission confirmed by your school."
    }catch(e:CancellationException){throw e}catch(_:Exception){
     failed=true;message="Not confirmed. Your input is retained. Check connection and school setup; reload to check for a newer version, review lock or closed deadline before retrying."
    }finally{busy=false}
   }}
  }
 }
}
