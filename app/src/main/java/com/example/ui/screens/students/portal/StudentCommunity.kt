package com.example.ui.screens.students.portal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.screens.operations.WorkspaceDirectory
import com.example.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Presentation scope; the backend remains responsible for recipient authorization. */
internal fun studentInboxEntries(actor:UserProfile,entries:List<ErpNotification>)=entries.filter{actor.role==UserRole.STUDENT&&it.schoolId==actor.schoolId&&(it.targetRole==null||it.targetRole==UserRole.STUDENT)}

@Composable fun StudentInbox(repository:ErpDataRepository,navigate:(String)->Unit) {
 val actor by repository.currentUser.collectAsState();val all by repository.notifications.collectAsState();val student=ownStudent(repository)
 var tab by rememberSaveable{mutableIntStateOf(0)};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")};val scope=rememberCoroutineScope()
 val visible=studentInboxEntries(actor,all).filter{tab==0||!it.isRead}
 LazyColumn(Modifier.fillMaxSize().testTag("student_inbox"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("Inbox",actions={TextButton(enabled=!busy,colors=ButtonDefaults.textButtonColors(contentColor=StudentColors.actionText),onClick={if(!busy){busy=true;error="";scope.launch{try{repository.readAllNotificationsConfirmed()}catch(cancelled:CancellationException){throw cancelled}catch(_:Exception){error="Read status could not be saved. Please try again."}finally{busy=false}}}}){Text("Mark all read")}})}
  item{StudentTabs(listOf("All","Unread"),tab){tab=it}}
  item{Text("In-app updates. Device push is not configured.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  if(busy)item{LinearProgressIndicator(Modifier.fillMaxWidth())}
  if(error.isNotBlank())item{Text(error,color=MaterialTheme.colorScheme.error)}
  if(visible.isEmpty())item{EmptyStateView("No updates","Your school updates will appear here.")}
  items(visible,key={it.id}){notification->
   Surface(onClick={if(!busy){busy=true;error="";scope.launch{try{
    if(!notification.isRead)repository.readNotificationConfirmed(notification.id)
    val id=notification.actionableId
    val destination=when(notification.category){
     NotificationCategory.HOMEWORK->if(repository.assignments.value.any{it.id==id&&student!=null&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId&&it.classGrade==student.classGrade&&it.division==student.division})Screen.StudentAssignment.createRoute(id)else Screen.Homework.route
     NotificationCategory.FEES->if(repository.invoices.value.any{it.id==id&&student!=null&&it.studentId==student.id})Screen.StudentInvoice.createRoute(id)else Screen.Fees.route
     NotificationCategory.ATTENDANCE->Screen.Attendance.route
     NotificationCategory.EXAMS->Screen.ReportCard.route
     else->if(repository.notices.value.any{it.id==id&&it.schoolId==actor.schoolId})Screen.StudentNotice.createRoute(id)else Screen.Notices.route
    };navigate(destination)
   }catch(cancelled:CancellationException){throw cancelled}catch(_:Exception){error="Could not confirm read status. Tap the update to retry."}finally{busy=false}}}},enabled=!busy,color=MaterialTheme.colorScheme.background,modifier=Modifier.semantics{stateDescription=if(notification.isRead)"Read"else "Unread"}){
    Column{Row(Modifier.padding(vertical=Spacing.sm8),horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap),verticalAlignment=Alignment.Top){
     Text(if(notification.isRead)" "else "●",color=StudentColors.actionText,style=MaterialTheme.typography.labelSmall)
     StudentIcon(if(notification.category==NotificationCategory.FEES)StudentIcons.Wallet else StudentIcons.Document,StudentTone.ORANGE)
     Column(Modifier.weight(1f)){Row{Text(notification.title,Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)};Text(notification.message,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(notification.timestamp,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    };HorizontalDivider()}
   }
  }
 }
}

@Composable fun StudentNotices(repository:ErpDataRepository,navigate:(String)->Unit,id:String?=null) {
 val actor by repository.currentUser.collectAsState();val notices by repository.notices.collectAsState()
 val own=notices.filter{it.schoolId==actor.schoolId}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader(if(id==null)"Notices"else "Notice")}
  if(id==null){
   if(own.isEmpty())item{EmptyStateView("No notices","Published school notices appear here.")}
   items(own,key={it.id}){notice->StudentRow(notice.title,"${notice.category} · ${notice.publishedDate}",Icons.Outlined.Campaign,StudentTone.ORANGE,{navigate(Screen.StudentNotice.createRoute(notice.id))})}
  }else{
   val notice=own.firstOrNull{it.id==id}
   if(notice==null)item{EmptyStateView("Notice unavailable","This notice is not available to this account.")}
   else item{StudentCard{Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)){StudentBadge(notice.category.toString(),StudentTone.LAVENDER);Text(notice.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(notice.content);Text("Published ${notice.publishedDate} · ${notice.authorName}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);if(notice.attachmentName.isNotBlank())Text("${notice.attachmentName} · Download link not provided",style=MaterialTheme.typography.bodySmall)}}}
  }
 }
}

@Composable fun StudentLibrary(repository:ErpDataRepository) {
 val actor by repository.currentUser.collectAsState();val books by repository.libraryBooks.collectAsState();var tab by rememberSaveable{mutableIntStateOf(0)};var query by rememberSaveable{mutableStateOf("")}
 val own=books.filter{it.schoolId==actor.schoolId&&(it.title.contains(query,true)||it.author.contains(query,true))}
 LazyColumn(Modifier.fillMaxSize().testTag("student_library"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("Library")}
  item{StudentTabs(listOf("Borrowed","Catalog"),tab){tab=it}}
  if(tab==0)item{EmptyStateView("Borrowing details unavailable","Your school connection does not expose personal loans or return dates. Use Catalog to browse availability.")}
  else{
   item{OutlinedTextField(query,{query=it},label={Text("Search books")},leadingIcon={Icon(Icons.Outlined.Search,null)},modifier=Modifier.fillMaxWidth(),singleLine=true)}
   if(own.isEmpty())item{EmptyStateView("No books found","Try another title or author.")}
   items(own,key={it.id}){book->StudentCard{Row(Modifier.padding(StudentLayout.gap),horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap)){StudentIcon(StudentIcons.Book,StudentTone.MINT);Column{Text(book.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold);Text(book.author,style=MaterialTheme.typography.bodySmall);StudentBadge("${book.availableCopies} available",StudentTone.BLUE);Text("Shelf ${book.shelfLocation}",style=MaterialTheme.typography.bodySmall)}}}}
  }
 }
}

@Composable fun StudentIdentity(repository:ErpDataRepository) {
 val student=ownStudent(repository);val context=LocalContext.current;var correction by remember{mutableStateOf(false)}
 if(student==null){EmptyStateView("Student link unavailable","Ask your school to link your account.");return}
 if(correction)AlertDialog(onDismissRequest={correction=false},title={Text("Request a correction")},text={Text("Contact your school administrator to correct your student record. No request has been sent from this app.")},confirmButton={TextButton(colors=ButtonDefaults.textButtonColors(contentColor=StudentColors.actionText),onClick={correction=false}){Text("Close")}})
 LazyColumn(Modifier.fillMaxSize().testTag("student_profile"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("Profile")}
  item{StudentCard{Column(Modifier.fillMaxWidth().padding(StudentLayout.section),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Avatar(student.name,size=64.dp,background=StudentColors.colors(StudentTone.ORANGE).first,textColor=StudentColors.onOrange);Text(student.name,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("Class ${student.fullClass} · Roll ${student.rollNumber}",style=MaterialTheme.typography.bodyMedium)}}}
  item{StudentCard{Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){StudentField("Student ID",student.admissionNumber);HorizontalDivider();StudentField("Class",student.fullClass);HorizontalDivider();StudentField("School ID",student.schoolId)}}}
  item{StudentCard{Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text("Guardian contact",style=MaterialTheme.typography.labelLarge);Text(student.parentName);Text(student.parentPhone.takeLast(4).let{if(it.isBlank())"Not provided"else "•••• $it"},style=MaterialTheme.typography.bodySmall);if(student.parentPhone.isNotBlank())OutlinedButton(colors=ButtonDefaults.outlinedButtonColors(contentColor=StudentColors.actionText),shape=RoundedCornerShape(Radius.sm),onClick={runCatching{context.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL,android.net.Uri.fromParts("tel",student.parentPhone,null)))}}){Icon(Icons.Outlined.Call,null);Text("Open dialer")}}}}
  item{OutlinedButton(colors=ButtonDefaults.outlinedButtonColors(contentColor=StudentColors.actionText),shape=RoundedCornerShape(Radius.sm),onClick={correction=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Outlined.Edit,null);Text("Request correction")}}
 }
}
@Composable private fun StudentField(label:String,value:String){Row{Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,Modifier.weight(1f),style=MaterialTheme.typography.bodySmall)}}

@Composable fun StudentSettings(repository:ErpDataRepository,navigate:(String)->Unit,logout:()->Unit) {
 val appearance=LocalAppearance.current?:rememberAppearancePreference();val experience=LocalStudentExperience.current?:rememberStudentExperience();val actor by repository.currentUser.collectAsState();var signOut by remember{mutableStateOf(false)}
 val student=ownStudent(repository);val locked by repository.isAppLockEnabled.collectAsState();var help by remember{mutableStateOf(false)};var account by remember{mutableStateOf(false)}
 if(help)AlertDialog(onDismissRequest={help=false},title={Text("School support")},text={Text("Contact your school administrator for account access, password recovery or corrections. Your verified account determines access. Use Inbox for in-app updates; device push is not configured.")},confirmButton={TextButton(colors=ButtonDefaults.textButtonColors(contentColor=StudentColors.actionText),onClick={help=false}){Text("Close")}})
 if(account)AlertDialog(onDismissRequest={account=false},title={Text("School account")},text={Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text(actor.name);Text(actor.email);Text("School: ${actor.schoolId}");Text("Session: ${student?.sessionId?:"Not loaded"}")}},confirmButton={TextButton(colors=ButtonDefaults.textButtonColors(contentColor=StudentColors.actionText),onClick={account=false}){Text("Close")}})
 if(signOut)AlertDialog(onDismissRequest={signOut=false},title={Text("Sign out?")},text={Text("Your current school session will be closed.")},confirmButton={TextButton(colors=ButtonDefaults.textButtonColors(contentColor=StudentColors.actionText),onClick={signOut=false;repository.logout();logout()}){Text("Sign out")}},dismissButton={TextButton(colors=ButtonDefaults.textButtonColors(contentColor=StudentColors.actionText),onClick={signOut=false}){Text("Cancel")}})
 LazyColumn(Modifier.fillMaxSize().testTag("student_settings"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)){
  item{WorkspacePageHeader("Settings")}
  item{StudentCard{Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){Text("Appearance",style=MaterialTheme.typography.titleSmall);StudentTabs(Appearance.entries.map{it.label},Appearance.entries.indexOf(appearance.selection)){appearance.select(Appearance.entries[it])}}}}
  item{StudentCard{Column(Modifier.padding(StudentLayout.gap)){
   StudentToggle("Reduce motion",experience.reduced,experience::reduce)
   Text("Also respects Android animation settings.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   HorizontalDivider();StudentToggle("Sounds",experience.sounds,experience::sound)
   Text("Quiet confirmation after a saved submission status. Off by default.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   HorizontalDivider();StudentToggle("Haptics",experience.haptics,experience::haptic)
  }}}
  item{StudentHeading("Account");StudentRow(actor.name,"Student",Icons.Outlined.Person,StudentTone.NEUTRAL,{navigate(Screen.StudentIdentity.route)})}
  item{StudentCard{Column(Modifier.padding(StudentLayout.gap)){StudentToggle("App lock",locked,repository::setAppLockEnabled);Text("Require fingerprint or PIN when resuming",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
  item{StudentRow("Notifications","Your school inbox",Icons.Outlined.Notifications,StudentTone.ORANGE,{navigate(Screen.Notifications.route)})}
  item{StudentRow("Documents","Notes and shared resources",StudentIcons.Document,StudentTone.BLUE,{navigate(Screen.StudyMaterial.route)})}
  item{StudentRow("School account","Verified account and school context",StudentIcons.Book,StudentTone.MINT,{account=true})}
  item{StudentRow("Help","School access and support",Icons.Outlined.HelpOutline,StudentTone.NEUTRAL,{help=true})}
  item{OutlinedButton(shape=RoundedCornerShape(Radius.sm),onClick={signOut=true},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.outlinedButtonColors(contentColor=MaterialTheme.colorScheme.error)){Icon(Icons.Outlined.Logout,null);Text("Sign out")}}
 }
}

/** Student search never builds results from another loaded student's identity or ledger. */
@Composable fun StudentSearch(repository:ErpDataRepository,navigate:(String)->Unit) {
 val student=ownStudent(repository);val assignments by repository.assignments.collectAsState();val invoices by repository.invoices.collectAsState();val notices by repository.notices.collectAsState();var query by rememberSaveable{mutableStateOf("")}
 val work=assignments.filter{student!=null&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId&&it.classGrade==student.classGrade&&it.division==student.division&&(it.title.contains(query,true)||it.subject.contains(query,true))}
 val ownInvoices=invoices.filter{student!=null&&it.studentId==student.id&&(it.title.contains(query,true)||it.invoiceNumber.contains(query,true))}
 val ownNotices=notices.filter{student!=null&&it.schoolId==student.schoolId&&(it.title.contains(query,true)||it.content.contains(query,true))}
 LazyColumn(Modifier.fillMaxSize().testTag("student_search"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("Search")}
  item{OutlinedTextField(query,{query=it},label={Text("Search your school work")},singleLine=true,leadingIcon={Icon(Icons.Outlined.Search,null)},modifier=Modifier.fillMaxWidth())}
  if(query.isBlank())item{Text("Search your assignments, invoices and school notices.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  else{
   if(student!=null&&student.name.contains(query,true))item{StudentRow(student.name,"Your student profile",Icons.Outlined.Person,StudentTone.ORANGE,{navigate(Screen.StudentIdentity.route)})}
   items(work,key={"work_${it.id}"}){item->StudentRow(item.title,"${item.subject} · ${item.dueDate}",StudentIcons.Document,StudentTone.BLUE,{navigate(Screen.StudentAssignment.createRoute(item.id))})}
   items(ownInvoices,key={"fee_${it.id}"}){item->StudentRow(item.title,studentMoney(item.pendingAmount),StudentIcons.Wallet,StudentTone.ORANGE,{navigate(Screen.StudentInvoice.createRoute(item.id))})}
   items(ownNotices,key={"notice_${it.id}"}){item->StudentRow(item.title,item.publishedDate,Icons.Outlined.Campaign,StudentTone.ORANGE,{navigate(Screen.StudentNotice.createRoute(item.id))})}
   if(work.isEmpty()&&ownInvoices.isEmpty()&&ownNotices.isEmpty()&&(student==null||!student.name.contains(query,true)))item{EmptyStateView("No matching records","Try a subject, invoice number or notice title.")}
  }
 }
}
@Composable private fun StudentToggle(label:String,value:Boolean,change:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f));Switch(value,change,modifier=Modifier.semantics{contentDescription=label},colors=SwitchDefaults.colors(checkedTrackColor=StudentColors.action,checkedThumbColor=StudentColors.onAction,uncheckedTrackColor=MaterialTheme.colorScheme.surfaceVariant,uncheckedThumbColor=MaterialTheme.colorScheme.onSurfaceVariant,uncheckedBorderColor=MaterialTheme.colorScheme.outlineVariant))}}

@Composable fun StudentMore(navigate:(String)->Unit) {
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("More")}
  item{StudentRow("Profile","Your student record",Icons.Outlined.Person,StudentTone.ORANGE,{navigate(Screen.StudentIdentity.route)})}
  items(WorkspaceDirectory.visible(UserRole.STUDENT).filter{it.route !in setOf(Screen.Timetable.route,Screen.Homework.route,Screen.ReportCard.route,Screen.Library.route,Screen.Notifications.route)},key={it.route}){destination->StudentRow(destination.title,destination.description,when(destination.route){Screen.Fees.route->StudentIcons.Wallet;Screen.Attendance.route->StudentIcons.Calendar;else->StudentIcons.Document},when(destination.route){Screen.Fees.route->StudentTone.ORANGE;Screen.Attendance.route->StudentTone.MINT;else->StudentTone.BLUE},{navigate(destination.route)})}
 }
}
