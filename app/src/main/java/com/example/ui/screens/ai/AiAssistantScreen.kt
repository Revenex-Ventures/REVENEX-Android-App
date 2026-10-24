package com.example.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.datasource.DataMode
import com.example.data.repository.ErpDataRepository
import com.example.ui.theme.*
import com.example.ui.components.rememberReducedMotion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
  val id: String,
  val sender: String,
  val text: String,
  val isAssistant: Boolean,
  val timestamp: String,
  val source: AssistantReplySource? = null
)

@Composable
fun AiAssistantScreen(repository: ErpDataRepository, onBack: () -> Unit) {
  val currentUser by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()
  val feeRecords by repository.feeRecords.collectAsState()
  val dataMode by repository.dataMode.collectAsState()
  val isDemo = dataMode == DataMode.DEMO
  val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
  var inputText by remember(currentUser.id) { mutableStateOf("") }
  var isTyping by remember(currentUser.id) { mutableStateOf(false) }
  val messages = remember(currentUser.id, dataMode) {
    mutableStateListOf(
      ChatMessage(
        id = "welcome_${currentUser.id}", sender = "Revenex Guide",
        text = "Hello ${currentUser.name}. Ask about school tasks or study topics. Replies identify whether they use school records or general guidance.",
        isAssistant = true, timestamp = formatter.format(Date()), source = AssistantReplySource.GENERAL_GUIDANCE
      )
    )
  }
  val suggestions = when (currentUser.role.name) {
    "STUDENT", "PARENT" -> listOf("Explain fractions", "Show my attendance", "Check my fee summary", "How do I view notices?")
    else -> listOf("Explain photosynthesis", "How do I use attendance?", "Where are notices?", "How do I find a student?")
  }

  fun send(query: String) {
    val trimmed = query.trim()
    if (trimmed.isBlank() || isTyping) return
    messages += ChatMessage("user_${System.nanoTime()}", currentUser.name, trimmed, false, formatter.format(Date()))
    inputText = ""
    isTyping = true
  }

  LaunchedEffect(isTyping, messages.size, currentUser.id, dataMode) {
    if (!isTyping) return@LaunchedEffect
    val query = messages.lastOrNull { !it.isAssistant }?.text ?: return@LaunchedEffect
    val reply = if (dataMode==DataMode.SUPABASE_DEMO) {
      com.example.data.datasource.SupabaseAssistantChat.reply(query,messages.dropLast(1).map { (if(it.isAssistant) "assistant" else "user") to it.text }).let { reply ->
        AssistantReply(reply.answer,when(reply.source) {
          "DEMO_ERP" -> AssistantReplySource.SERVER_DEMO
          "LIVE_ERP" -> AssistantReplySource.SERVER_ERP
          "GENERAL_KNOWLEDGE" -> AssistantReplySource.SERVER_AI
          else -> AssistantReplySource.UNAVAILABLE
        })
      }
    } else AssistantReplyEngine.reply(query, AssistantContext(currentUser, students, feeRecords, isDemo))
    messages += ChatMessage("assistant_${System.nanoTime()}", "Revenex Guide", reply.text, true, formatter.format(Date()), reply.source)
    isTyping = false
  }

  val reducedMotion=rememberReducedMotion()
  val conversation=androidx.compose.foundation.lazy.rememberLazyListState()
  LaunchedEffect(messages.size,isTyping) {if(messages.size>1) if(reducedMotion) conversation.scrollToItem(messages.size) else conversation.animateScrollToItem(messages.size)}
  Scaffold(
    containerColor=MaterialTheme.colorScheme.background,
    topBar={Surface(color=MaterialTheme.colorScheme.surface) {Row(Modifier.fillMaxWidth().padding(horizontal=Spacing.sm8),verticalAlignment=Alignment.CenterVertically) {
      IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")}
      Column(Modifier.weight(1f)){Text("Revenex assistant",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(if(isDemo)"Local demo guidance" else "Secure server responses",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
      Icon(Icons.Default.AutoAwesome,null,Modifier.padding(Spacing.md12),tint=MaterialTheme.colorScheme.primary)
    }}},
    bottomBar={Surface(color=MaterialTheme.colorScheme.surface) {Column(Modifier.navigationBarsPadding().imePadding().padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
      if(messages.size==1) LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){items(suggestions){suggestion->AssistChip(onClick={send(suggestion)},label={Text(suggestion)})}}
      Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        OutlinedTextField(inputText,{inputText=it},Modifier.weight(1f).testTag("ai_assistant_input"),enabled=!isTyping,placeholder={Text("Ask a school or study question")},minLines=1,maxLines=4,shape=RoundedCornerShape(Radius.sm))
        FilledIconButton(onClick={send(inputText)},enabled=inputText.isNotBlank()&&!isTyping,modifier=Modifier.size(48.dp).testTag("ai_assistant_send_btn")){Icon(Icons.Default.Send,"Send")}
      }
    }}}
  ) {padding->
    LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("ai_messages_list"),state=conversation,contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      item {AssistantModeBanner(isDemo)}
      items(messages,key={it.id}){message->MessageBubble(message)}
      if(isTyping) item {Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){CircularProgressIndicator(Modifier.size(16.dp),strokeWidth=2.dp);Text("Preparing a response…",style=MaterialTheme.typography.bodySmall)}}
    }
  }
}

@Composable
private fun AssistantModeBanner(isDemo: Boolean) {
  Surface(shape=RoundedCornerShape(Radius.sm),color=MaterialTheme.colorScheme.secondaryContainer) {
    Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
      Text(if(isDemo)"Demo data mode" else "School account",style=MaterialTheme.typography.labelLarge,fontWeight=FontWeight.SemiBold)
      Text(if(isDemo)"School summaries use local sample data." else "Availability is checked when you send a message. School data is limited to your account.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSecondaryContainer)
    }
  }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
  Row(Modifier.fillMaxWidth(),horizontalArrangement=if(message.isAssistant)Arrangement.Start else Arrangement.End) {
    Surface(color=if(message.isAssistant)MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer,
      shape=RoundedCornerShape(topStart=Radius.md,topEnd=Radius.md,bottomStart=if(message.isAssistant)Radius.xs else Radius.md,bottomEnd=if(message.isAssistant)Radius.md else Radius.xs),
      border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),modifier=Modifier.widthIn(max=340.dp)) {
      Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        Text(message.text,style=MaterialTheme.typography.bodyMedium,color=if(message.isAssistant)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer)
        Text(listOfNotNull(message.source?.label(),message.timestamp).joinToString(" · "),style=MaterialTheme.typography.labelSmall,color=if(message.isAssistant)MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer)
      }
    }
  }
}

private fun AssistantReplySource.label(): String = when (this) {
  AssistantReplySource.GENERAL_GUIDANCE -> "Study guidance"
  AssistantReplySource.LOADED_RECORDS -> "Loaded records"
  AssistantReplySource.ERP_GUIDANCE -> "ERP guidance"
  AssistantReplySource.UNAVAILABLE -> "Unavailable"
  AssistantReplySource.SERVER_AI -> "AI · general knowledge"
  AssistantReplySource.SERVER_ERP -> "AI · ERP tools used"
  AssistantReplySource.SERVER_DEMO -> "AI · Supabase demo"
}
