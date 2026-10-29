package com.example.ui.screens.students.portal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.ReportCard
import com.example.data.model.Student
import com.example.ui.components.*
import com.example.ui.theme.*

/** One existing Results route, with a real nested report and the original PDF exporter. */
@Composable internal fun StudentLightResults(student:Student?,published:List<ReportCard>,initialId:String?){
 var selected by rememberSaveable(student?.id){mutableStateOf(initialId.orEmpty())}
 var detail by rememberSaveable(student?.id){mutableStateOf(false)}
 var feedback by remember{mutableStateOf("")}
 val report=published.firstOrNull{it.id==selected}?:published.firstOrNull()
 val context=LocalContext.current
 BackHandler(detail){detail=false}
 LazyColumn(Modifier.fillMaxSize().testTag("student_results"),contentPadding=PaddingValues(StudentLightLayout.margin),verticalArrangement=Arrangement.spacedBy(Spacing.lg16)){
  item{val back=LocalWorkspaceBack.current;CompositionLocalProvider(LocalWorkspaceBack provides if(detail)({detail=false})else back){StudentPageHeader(if(detail)"Academic report"else "Results")}}
  if(student!=null)item{Text(student.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text("Class ${student.fullClass} · Roll ${student.rollNumber}",style=MaterialTheme.typography.bodySmall,color=StudentLightPalette.muted)}
  if(report==null)item{EmptyStateView("No published results","Draft marks remain private until your school publishes them.")}
  else{
   if(!detail)item{LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){items(published,key={it.id}){value->FilterChip(value.id==report.id,{selected=value.id},modifier=Modifier.heightIn(min=StudentLightLayout.accountAvatar),label={Text("${value.term} · ${value.academicYear}")})}}}
   item{StudentCard(tone=StudentTone.BLUE){Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.md12)){
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){Icon(StudentIcons.Document,null);Text("${report.term} ${if(detail)"report"else "results"}",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);StudentBadge("Published",StudentTone.BLUE)}
    if(detail)Text("Academic session ${report.academicYear}",style=MaterialTheme.typography.bodySmall)
    else Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){Text("${"%.0f".format(report.percentage)}%",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);Text("Overall",style=MaterialTheme.typography.bodyMedium)}
    Surface(color=StudentLightPalette.canvas,shape=MaterialTheme.shapes.small){Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.md12)){
     if(detail){StudentReportLine("Subject","Marks",true);HorizontalDivider()}
     report.scores.forEachIndexed{index,score->StudentReportLine(score.subjectName,"${score.obtainedMarks} / ${score.maxMarks}");if(index<report.scores.lastIndex)HorizontalDivider()}
     if(detail){HorizontalDivider();StudentReportLine("Total","${report.totalObtained} / ${report.totalMax} (${"%.0f".format(report.percentage)}%)",true)}
    }}
    if(!detail)Text("${report.totalObtained} / ${report.totalMax} marks",style=MaterialTheme.typography.bodySmall)
   }}}
   if(report.principalRemark.isNotBlank())item{Text(report.principalRemark,style=MaterialTheme.typography.bodyMedium)}
   item{StudentAction(if(detail)"Open report PDF"else "View report"){
    if(!detail)detail=true else{
     val uri=student?.let{com.example.ui.screens.academics.exportPublishedStudentReport(context,it,report)}
     if(uri==null)feedback="Report could not be saved. Please try again." else runCatching{context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW).setDataAndType(uri,"application/pdf").addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION))}.onFailure{feedback="Report saved. Install a PDF viewer to open it."}
    }
   };if(feedback.isNotBlank())Text(feedback,color=StudentLightPalette.actionText,style=MaterialTheme.typography.bodySmall)}
  }
 }
}
@Composable private fun StudentReportLine(label:String,value:String,strong:Boolean=false){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium,fontWeight=if(strong)FontWeight.Bold else FontWeight.Normal);Text(value,style=MaterialTheme.typography.bodyMedium,fontWeight=if(strong)FontWeight.Bold else FontWeight.Medium)}}
