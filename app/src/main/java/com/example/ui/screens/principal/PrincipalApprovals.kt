package com.example.ui.screens.principal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.*

@Composable fun PrincipalApprovals(repository:ErpDataRepository,open:(String)->Unit) {
 val principalDark=LocalPrincipalDark.current
 val requests by repository.leaveRequests.collectAsState();val actor by repository.currentUser.collectAsState()
 var tab by rememberSaveable(actor.id){mutableIntStateOf(0)}
 val pending=requests.filter{it.status==LeaveStatus.PENDING};val reviewed=requests.filter{it.status!=LeaveStatus.PENDING}
 val rows=if(tab==0)pending else reviewed
 LazyColumn(Modifier.fillMaxSize().testTag("principal_approvals"),contentPadding=PaddingValues(horizontal=if(principalDark)Spacing.xl20 else Spacing.screenPadding)) {
  item {PrincipalPageHeader("Approvals");PrincipalTabs(listOf("Pending (${pending.size})","Reviewed (${reviewed.size})"),tab,{tab=it})}
  item {Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){Surface(color=PrincipalColors.blue,contentColor=PrincipalColors.blueInk,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.md)){Text("Leave requests",Modifier.padding(Spacing.sm8),style=MaterialTheme.typography.labelMedium)}}}
  if(rows.isEmpty())item {EmptyStateView("No requests","No leave requests in this view. Admission and fee-concession approvals are not configured.")}
  items(rows,key={it.id}){request->PrincipalRecordRow(request.applicantName,"${request.leaveType} · ${request.startDate}",{open(request.id)},Modifier.testTag("principal_approval_${request.id}")){
   if(tab==0)Text("Review",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)
   else StatusBadge(request.status.label)
  }}
 }
}

@Composable fun PrincipalApprovalDetail(repository:ErpDataRepository,id:String) {
 val principalDark=LocalPrincipalDark.current
 val requests by repository.leaveRequests.collectAsState();val actor by repository.currentUser.collectAsState()
 val request=requests.firstOrNull{it.id==id&&it.schoolId==actor.schoolId}
 var comment by rememberSaveable(actor.id,id){mutableStateOf("")}
 var confirming by remember(actor.id,id){mutableStateOf<LeaveStatus?>(null)}
 var busy by remember(actor.id,id){mutableStateOf(false)};var message by remember(actor.id,id){mutableStateOf<String?>(null)}
 val scope=rememberCoroutineScope()
 val allowed=actor.role==UserRole.PRINCIPAL&&request?.status==LeaveStatus.PENDING
 if(request==null){Column(Modifier.padding(Spacing.screenPadding)){PrincipalPageHeader("Request");EmptyStateView("Request unavailable","Reload the school account or return to approvals.")};return}
 Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={if(allowed)Row(Modifier.fillMaxWidth().padding(Spacing.screenPadding),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
  Button(onClick={if(!busy)confirming=LeaveStatus.APPROVED},enabled=!busy&&comment.length<=1000,modifier=Modifier.weight(1f).testTag("principal_approve"),shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.xs)){Text(if(busy)"Saving…"else"Approve")}
  OutlinedButton(onClick={if(!busy)confirming=LeaveStatus.REJECTED},enabled=!busy&&comment.length<=1000,modifier=Modifier.weight(1f),shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.xs)){Text("Decline")}
 }}) {padding->
 LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("principal_approval_detail"),contentPadding=PaddingValues(if(principalDark)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  item {PrincipalPageHeader("Leave request"){StatusBadge(request.status.label)}}
  if(principalDark)item {PrincipalSummary("Applicant",request.applicantName,request.classOrDept,Icons.Outlined.Person,PrincipalColors.mint,PrincipalColors.mintInk)}
  if(!principalDark)item {Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){Avatar(request.applicantName,size=PrincipalLayout.listAvatar,background=PrincipalColors.blue,textColor=PrincipalColors.blueInk);Column{Text(request.applicantName,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(request.classOrDept,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
  item {SchoolDetailRow("Dates","${request.startDate} – ${request.endDate}");SchoolDetailRow("Duration","${request.daysCount} days");SchoolDetailRow("Type",request.leaveType);SchoolDetailRow("Reason",request.reason);SchoolDetailRow("Applied",request.appliedDate)}
  if(request.approverRemark.isNotBlank())item {SchoolDetailRow("Decision comment",request.approverRemark)}
  if(allowed)item {HorizontalDivider();OutlinedTextField(comment,{comment=it},Modifier.fillMaxWidth().testTag("principal_approval_comment"),enabled=!busy,label={Text("Comment (optional)")},minLines=2,isError=comment.length>1000,supportingText={Text("${comment.length}/1000 · Applicant receives an in-app update after confirmation.")})}
  message?.let{item{Text(it,color=if(allowed)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.testTag("principal_decision_feedback"))}}
 }
 }
 confirming?.let {status->AlertDialog(onDismissRequest={if(!busy)confirming=null},title={Text(if(status==LeaveStatus.APPROVED)"Approve leave?"else"Decline leave?")},text={Text("Confirm the decision for ${request.applicantName}. A confirmed decision cannot be overwritten here.")},confirmButton={TextButton(enabled=!busy,onClick={
  if(!busy){confirming=null;busy=true;message=null
   val actorId=actor.id;val remark=comment.trim()
   scope.launch{try{
    check(repository.currentUser.value.id==actorId)
    check(repository.leaveRequests.value.firstOrNull{it.id==id}?.status==LeaveStatus.PENDING)
    repository.decideLeaveConfirmed(id,status,remark)
    message="Decision confirmed"
   }catch(e:CancellationException){throw e}catch(e:Exception){message="Decision not confirmed. Reload the request and check your school access before retrying."}finally{busy=false}}
  }
 }){Text("Confirm")}},dismissButton={TextButton(onClick={confirming=null}){Text("Cancel")}})}
}
