package com.example.ui.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentAttendance
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
  repository: ErpDataRepository,
  initialClassGrade: String? = null,
  initialDivision: String? = null,
  onBack: () -> Unit
) {
  val students by repository.students.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val teacher = currentUser.role==UserRole.TEACHER
  val largeText=androidx.compose.ui.platform.LocalDensity.current.fontScale>1.3f
  val allocation = com.example.ui.screens.teachers.teacherAllocation(repository)
  val activeStudents=students.filter{it.status!="ARCHIVED"}
  val permittedStudents=if(teacher)activeStudents.filter{it.fullClass in allocation?.assignedClasses.orEmpty()}else activeStudents
  val canEdit = currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER)
  val classAttendanceMap by repository.classAttendance.collectAsState()
  val dataMode by repository.dataMode.collectAsState()
  val capabilities by repository.backendCapabilities.collectAsState()
  val backendReady=dataMode!=com.example.data.datasource.DataMode.SUPABASE_DEMO || (capabilities.loaded&&capabilities.attendance)

  val cohorts = permittedStudents.map { "Class ${it.classGrade}-${it.division}" }.distinct().sorted()
  var selectedCohort by remember(currentUser.id, initialClassGrade, initialDivision) {
    mutableStateOf(if (initialClassGrade != null && initialDivision != null) "Class $initialClassGrade-$initialDivision" else "")
  }
  LaunchedEffect(cohorts) { if (selectedCohort !in cohorts) selectedCohort = cohorts.firstOrNull().orEmpty() }
  var saving by remember(currentUser.id) { mutableStateOf(false) }

  var selectedSessionSlot by remember { mutableStateOf("Morning Roll Call") }
  val sessionSlots = listOf("Morning Roll Call", "STEM Lab", "Senior Practicum")

  var showSuccessNotification by remember { mutableStateOf(false) }
  var saveError by remember { mutableStateOf<String?>(null) }

  val classStudents = remember(permittedStudents, selectedCohort) {
    permittedStudents.filter { "Class ${it.classGrade}-${it.division}" == selectedCohort }
  }
  val today = com.example.data.model.schoolDate()
  var selectedDate by remember(currentUser.id) { mutableStateOf(today) }
  var showDatePicker by remember { mutableStateOf(false) }
  val editingToday = selectedDate == today
  if (showDatePicker) {
    val picker = rememberDatePickerState()
    DatePickerDialog(onDismissRequest = { showDatePicker = false },
      confirmButton = { TextButton(onClick = {
        picker.selectedDateMillis?.let { millis ->
          selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
          }.format(Date(millis))
          showSuccessNotification = false; saveError = null
        }
        showDatePicker = false
      }, enabled = picker.selectedDateMillis != null) { Text("View date") } },
      dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
    ) { DatePicker(state = picker) }
  }
  val savedRecord = classAttendanceMap.values.lastOrNull { record ->
    attendanceDateMatches(record.date, selectedDate) && record.sessionSlot == selectedSessionSlot &&
      classStudents.firstOrNull()?.let { it.classGrade == record.classGrade && it.division == record.division } == true
  }
  var edited by remember(currentUser.id,selectedCohort,selectedSessionSlot,selectedDate) {mutableStateOf(false)}
  var baseRecord by remember(currentUser.id,selectedCohort,selectedSessionSlot,selectedDate) {mutableStateOf(savedRecord)}
  var baseRoster by remember(currentUser.id,selectedCohort,selectedSessionSlot,selectedDate) {mutableStateOf(classStudents.map{it.id}.toSet())}
  val rollCallMap = remember(currentUser.id, selectedCohort, selectedSessionSlot, selectedDate, canEdit) {
    mutableStateMapOf<String, AttendanceStatus>().apply {
      classStudents.filter { (editingToday && canEdit) || savedRecord != null }.forEach { st ->
        put(st.id, savedRecord?.studentList?.firstOrNull { it.studentId == st.id }?.status ?: AttendanceStatus.PRESENT)
      }
    }
  }
  fun loadSavedRegister() {
    rollCallMap.clear()
    classStudents.filter{(editingToday&&canEdit)||savedRecord!=null}.forEach { st ->
      rollCallMap[st.id]=savedRecord?.studentList?.firstOrNull{it.studentId==st.id}?.status?:AttendanceStatus.PRESENT
    }
    baseRecord=savedRecord;baseRoster=classStudents.map{it.id}.toSet();edited=false
  }
  LaunchedEffect(savedRecord,classStudents.map{it.id},edited) {if(!edited)loadSavedRegister()}
  val registerChanged=edited&&(savedRecord!=baseRecord||baseRoster!=classStudents.map{it.id}.toSet())

  var showRegisterSearch by remember {mutableStateOf(false)}
  var studentQuery by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id, selectedCohort) { mutableStateOf("") }
  val visibleStudents = classStudents.filter { student ->
    studentQuery.isBlank() || student.name.contains(studentQuery.trim(), true) || student.rollNumber.toString() == studentQuery.trim() || student.admissionNumber.contains(studentQuery.trim(), true)
  }.sortedBy { it.rollNumber }
  val totalScholars = classStudents.size
  val presentCount = rollCallMap.values.count { it == AttendanceStatus.PRESENT }
  val lateCount = rollCallMap.values.count { it == AttendanceStatus.LATE }
  val absentCount = rollCallMap.values.count { it == AttendanceStatus.ABSENT }
  val excusedCount = rollCallMap.values.count { it == AttendanceStatus.EXCUSED }
  val dirty=savedRecord==null||classStudents.any{rollCallMap[it.id]!=savedRecord.studentList.firstOrNull{entry->entry.studentId==it.id}?.status}
  var extraStudent by remember{mutableStateOf<com.example.data.model.Student?>(null)}
  extraStudent?.let {pupil->AlertDialog(onDismissRequest={extraStudent=null},title={Text(pupil.name)},text={Text("Use Excused for an authorized absence. L means Late, not Leave.")},confirmButton={TextButton(enabled=editingToday&&!saving&&canEdit,onClick={rollCallMap[pupil.id]=AttendanceStatus.EXCUSED;edited=true;showSuccessNotification=false;extraStudent=null}){Text("Mark excused")}},dismissButton={TextButton(onClick={extraStudent=null}){Text("Cancel")}})}

  val livePresenceRate = if (totalScholars > 0) {
    ((presentCount + lateCount + excusedCount).toDouble() / totalScholars.toDouble()) * 100.0
  } else 0.0

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg))
          .navigationBarsPadding()
      ) {
        Column(modifier = Modifier.padding(Spacing.s4)) {
          saveError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
          if (showSuccessNotification) {
            Surface(
              shape = RoundedCornerShape(Radius.md),
              color = SemanticColors.successContainer,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.s2)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticColors.onSuccess)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Attendance saved · $selectedCohort",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = SemanticColors.onSuccess
                )
              }
            }
          }

          if(!backendReady)Text("School attendance update required. Your edits remain on this screen; no save was sent.",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
          if(registerChanged) {
            Text("The saved register or class roster changed. Your edits have been retained.",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
            TextButton(enabled=!saving,onClick={loadSavedRegister();saveError=null;showSuccessNotification=false}){Text("Load saved register (discard edits)")}
          }
          Text("${if(!dirty) "Saved" else "Not saved"} · $presentCount present · $absentCount absent · Whole class",
            style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(bottom=Spacing.sm8))
          if (canEdit) AppButton(
            text = if (saving) "Saving…" else "Save attendance",
            onClick = {
              showSuccessNotification = false
              saveError = null
              val list = classStudents.map { st ->
                StudentAttendance(
                  studentId = st.id,
                  studentName = st.name,
                  rollNumber = st.rollNumber,
                  status = rollCallMap[st.id] ?: AttendanceStatus.PRESENT,
                  remark = baseRecord?.studentList?.firstOrNull{it.studentId==st.id}?.remark.orEmpty(),
                  revision = baseRecord?.studentList?.firstOrNull{it.studentId==st.id}?.revision?:0
                )
              }
              val cohort = classStudents.firstOrNull() ?: return@AppButton
              val grade = cohort.classGrade
              val div = cohort.division
              saving = true
              repository.recordRollCallSession(
                classGrade = grade,
                division = div,
                sessionSlot = selectedSessionSlot,
                studentList = list,
                onResult = { saved -> saving = false; if(saved)edited=false; showSuccessNotification = saved; saveError = if (saved) null else "Attendance was not confirmed. Check your connection and refresh the saved register before retrying." }
              )
            },
            enabled = backendReady && !registerChanged && editingToday && !saving && classStudents.isNotEmpty() && currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER),
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("submit_save_attendance")
          ) else Text("Only saved school attendance appears here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
        .testTag("attendance_matrix_view"),
      verticalArrangement = Arrangement.spacedBy(if(teacher)Spacing.xs4 else Spacing.s3),
      contentPadding = PaddingValues(bottom = 20.dp)
    ) {
      item {
        if(teacher) {
          Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")}
            Text("Attendance",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
            if(!largeText)TextButton(onClick={showDatePicker=true},enabled=!saving){Text(selectedDate,style=MaterialTheme.typography.labelSmall)}
            IconButton(onClick={if(showRegisterSearch)studentQuery="";showRegisterSearch=!showRegisterSearch}){Icon(Icons.Default.Search,"Search register")}
          }
          if(largeText)TextButton(onClick={showDatePicker=true},enabled=!saving){Text(selectedDate)}
          ResponsiveFormFields(
            first={field->com.example.ui.screens.teachers.TeacherSelect("Class",selectedCohort.removePrefix("Class "),cohorts,{if(!saving){selectedCohort=it;showSuccessNotification=false}},field)},
            second={field->com.example.ui.screens.teachers.TeacherSelect("Session",selectedSessionSlot,sessionSlots,{if(!saving){selectedSessionSlot=it;showSuccessNotification=false}},field)}
          )
          if(!editingToday)Text("History is view-only.",style=MaterialTheme.typography.bodySmall)
        } else {

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
          Text(if(teacher)"Attendance"else "Class attendance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
          if(canEdit) TextButton(enabled=editingToday && !saving && classStudents.isNotEmpty(),onClick={
            classStudents.forEach {rollCallMap[it.id]=AttendanceStatus.PRESENT};showSuccessNotification=false
          },modifier=Modifier.testTag("mark_all_present")){Text("All present")}
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          OutlinedButton(onClick = { showDatePicker = true }, enabled = !saving, modifier = Modifier.weight(1f), shape = RoundedCornerShape(Radius.sm), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.xs4)); Text(selectedDate)
          }
          var cohortMenu by remember { mutableStateOf(false) }
          Box(Modifier.weight(1f)) {
            OutlinedButton(onClick = { cohortMenu = true }, enabled = !saving, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.sm), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
              Text(selectedCohort.ifBlank { "Select class" }, Modifier.weight(1f))
              Icon(Icons.Default.ExpandMore,null,Modifier.size(18.dp))
            }
            DropdownMenu(expanded = cohortMenu, onDismissRequest = { cohortMenu = false }) {
              cohorts.forEach { cohort -> DropdownMenuItem(text = { Text(cohort) }, onClick = {
                selectedCohort = cohort; cohortMenu = false; showSuccessNotification = false
              }) }
            }
          }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          sessionSlots.forEach { slot ->
            SchoolFilterChip(selected = selectedSessionSlot == slot, onClick = {
              if (!saving) { selectedSessionSlot = slot; showSuccessNotification = false }
            }, label = { Text(when(slot) { "Morning Roll Call" -> "Morning"; "Senior Practicum" -> "Practicum"; else -> slot }) }, enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 48.dp))
          }
        }
        if (!editingToday) Text("History is view-only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
      }

      item {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          listOf(Triple(presentCount,"Present",SchoolMetricTone.POSITIVE),Triple(absentCount,"Absent",SchoolMetricTone.DANGER),
            Triple(lateCount,"Late",SchoolMetricTone.FINANCE),Triple(excusedCount,"Excused",SchoolMetricTone.NEUTRAL)).filter{!teacher||it.second in listOf("Present","Absent")}.forEach {(count,label,tone)->
            val colors=when(tone){SchoolMetricTone.POSITIVE->SemanticColors.successContainer to SemanticColors.onSuccess;SchoolMetricTone.DANGER->SemanticColors.dangerContainer to SemanticColors.onDanger;SchoolMetricTone.FINANCE->SemanticColors.butterContainer to SemanticColors.onButter;else->SemanticColors.blueContainer to SemanticColors.onBlue}
            Surface(Modifier.weight(1f),shape=RoundedCornerShape(Radius.sm),color=colors.first) {
              Column(Modifier.padding(Spacing.sm8)) {
                Text(count.toString(),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,color=colors.second)
                Text(label,style=MaterialTheme.typography.bodySmall,color=colors.second)
              }
            }
          }
        }
      }

      if(!teacher||showRegisterSearch)item {
        if(teacher)TextButton(enabled=editingToday&&!saving,onClick={classStudents.forEach{rollCallMap[it.id]=AttendanceStatus.PRESENT};edited=true;showSuccessNotification=false},modifier=Modifier.testTag("mark_all_present")){Text("All present")}
        OutlinedTextField(studentQuery, { studentQuery = it }, Modifier.fillMaxWidth().testTag("register_search"),
          placeholder = { Text("Search students") }, singleLine = true,
          leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(Radius.sm),
          colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant))
        Text(if (canEdit) "${visibleStudents.size} of ${classStudents.size} students" else "Saved records for the selected date and session",
          Modifier.padding(top = Spacing.sm8), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      // 3. STUDENT ROLL CALL CARDS
      if (savedRecord == null && (!editingToday || !canEdit)) item {
        EmptyStateView(title = "No register loaded", message = "No saved attendance is available for this class, date and session.")
      }
      items(if ((editingToday && canEdit) || savedRecord != null) visibleStudents else emptyList(), key = { it.id }) { student ->
        val currentStatus = rollCallMap[student.id] ?: AttendanceStatus.PRESENT
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(if(teacher)0.dp else Radius.md), color = MaterialTheme.colorScheme.surface,
          border = if(teacher)null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
          BoxWithConstraints(Modifier.padding(Spacing.sm8)) {
            val stacked = maxWidth < (if(teacher)280.dp else 300.dp) || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
            val identity: @Composable () -> Unit = {
              Row(Modifier.heightIn(min=48.dp).then(if(teacher)Modifier.clickable{extraStudent=student}else Modifier),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
                Avatar(student.name,size=if(teacher)Spacing.xxl32 else Spacing.xl24,background=if(teacher)TeacherColors.blue else SemanticColors.lavenderContainer,textColor=if(teacher)TeacherColors.blueInk else SemanticColors.onLavender)
                Column {
                Text(student.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Roll ${student.rollNumber}${if(currentStatus==AttendanceStatus.EXCUSED)" · Excused"else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
            val controls: @Composable () -> Unit = {
              Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs4)) {
                listOf(AttendanceStatus.PRESENT to "P", AttendanceStatus.ABSENT to "A", AttendanceStatus.LATE to "L", AttendanceStatus.EXCUSED to "E").filter{!teacher||it.first!=AttendanceStatus.EXCUSED}.forEach { (status,label) ->
                  val selected = currentStatus == status
                  val container = when (status) {
                    AttendanceStatus.PRESENT -> if(teacher)TeacherColors.green else SemanticColors.onSuccess
                    AttendanceStatus.ABSENT -> if(teacher)TeacherColors.red else SemanticColors.onDanger
                    else -> SemanticColors.onWarning
                  }
                  Surface(onClick = { rollCallMap[student.id] = status; edited=true; showSuccessNotification = false },
                    enabled = editingToday && !saving && canEdit,
                    color = if (selected) container else MaterialTheme.colorScheme.surfaceVariant,
                    shape = if(teacher)CircleShape else RoundedCornerShape(Radius.xs),
                    modifier = Modifier.size(48.dp).semantics {
                      contentDescription = "${status.name.lowercase()} for ${student.name}"
                      this.selected = selected
                    }
                      .testTag("btn_${status.name.lowercase()}_${student.rollNumber}")) {
                    Box(contentAlignment = Alignment.Center) { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                      color = if (selected) when (status) { AttendanceStatus.PRESENT -> SemanticColors.successContainer; AttendanceStatus.ABSENT -> SemanticColors.dangerContainer; else -> SemanticColors.warningContainer } else MaterialTheme.colorScheme.onSurfaceVariant) }
                  }
                }
              }
            }
            if (stacked) Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) { identity(); controls() }
            else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
              Box(Modifier.weight(1f)) { identity() }; controls()
            }
          }
        }
      }
      item { Text(if(teacher)"P Present · A Absent · L Late. Tap a student for Excused. $lateCount late · $excusedCount excused."else "P Present · A Absent · L Late · E Excused", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
  }
}
