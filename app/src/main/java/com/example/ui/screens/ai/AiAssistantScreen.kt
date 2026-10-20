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
import kotlinx.coroutines.delay
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
        text = "Hello ${currentUser.name}. I can offer general study help, guide you to ERP workflows, and summarize only records linked to your account. I will not make changes or invent school information.",
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
    delay(180)
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

  Scaffold(
    topBar = {
      Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = Elev.e1) {
        Row(
          modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(Spacing.s2),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
          Box(Modifier.size(38.dp).clip(CircleShape).background(ScholaSlateContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AutoAwesome, null, tint = ScholaSlateNavy)
          }
          Spacer(Modifier.width(Spacing.s3))
          Column(Modifier.weight(1f)) {
            Text("Revenex Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
              if (isDemo) "Guided assistant â€¢ demo records" else "Guided assistant â€¢ live AI unavailable",
              style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    },
    bottomBar = {
      Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = Elev.e3) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = Spacing.md12, vertical = Spacing.s2)) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s2), contentPadding = PaddingValues(bottom = Spacing.s2)) {
            items(suggestions) { suggestion ->
              AssistChip(onClick = { send(suggestion) }, label = { Text(suggestion, maxLines = 1, overflow = TextOverflow.Ellipsis) })
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
              value = inputText, onValueChange = { inputText = it }, enabled = !isTyping,
              placeholder = { Text("Ask for study help or ERP guidance") },
              modifier = Modifier.weight(1f).testTag("ai_assistant_input"), shape = RoundedCornerShape(Radius.input), singleLine = true
            )
            Spacer(Modifier.width(Spacing.s2))
            IconButton(
              onClick = { send(inputText) }, enabled = inputText.isNotBlank() && !isTyping,
              modifier = Modifier.size(48.dp).clip(CircleShape).background(RoleAccent.of(currentUser.role)).testTag("ai_assistant_send_btn")
            ) { Icon(Icons.Default.Send, "Send", tint = Color.White) }
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding).testTag("ai_messages_list"),
      contentPadding = PaddingValues(Spacing.lg16), verticalArrangement = Arrangement.spacedBy(Spacing.md12)
    ) {
      item { AssistantModeBanner(isDemo) }
      items(messages, key = { it.id }) { message ->
        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) { MessageBubble(message) }
      }
      item {
        AnimatedVisibility(visible = isTyping, enter = fadeIn(), exit = fadeOut()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(Spacing.s2))
            Text("Preparing a safe responseâ€¦", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}

@Composable
private fun AssistantModeBanner(isDemo: Boolean) {
  val title = if (isDemo) "Demo data mode" else "Live AI unavailable"
  val detail = if (isDemo) "Any personal summary uses sample data and is not a real school record."
    else "A secure model provider has not been configured. School facts will not be guessed."
  Card(colors = CardDefaults.cardColors(containerColor = if (isDemo) StatusInfoBg else StatusNeutralBg)) {
    Column(Modifier.padding(Spacing.md12)) {
      Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (isDemo) StatusInfoText else StatusNeutralText)
      Spacer(Modifier.height(Spacing.xs4))
      Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (message.isAssistant) Arrangement.Start else Arrangement.End,
    verticalAlignment = Alignment.Top
  ) {
    if (message.isAssistant) {
      Box(Modifier.size(30.dp).clip(CircleShape).background(ScholaSlateContainer), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp), tint = ScholaSlateNavy)
      }
      Spacer(Modifier.width(Spacing.s2))
    }
    Surface(
      color = if (message.isAssistant) MaterialTheme.colorScheme.surfaceVariant else ScholaSlateNavy,
      shape = RoundedCornerShape(Radius.lg), modifier = Modifier.widthIn(max = 340.dp)
    ) {
      Column(Modifier.padding(Spacing.md12)) {
        Text(message.text, style = MaterialTheme.typography.bodyMedium, color = if (message.isAssistant) MaterialTheme.colorScheme.onSurfaceVariant else Color.White)
        Spacer(Modifier.height(Spacing.xs4))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s2), verticalAlignment = Alignment.CenterVertically) {
          message.source?.let { Text(it.label(), style = MaterialTheme.typography.labelSmall, color = if (message.isAssistant) StatusNeutralText else Color.White.copy(alpha = .72f)) }
          Text(message.timestamp, style = MaterialTheme.typography.labelSmall, color = if (message.isAssistant) StatusNeutralText else Color.White.copy(alpha = .72f))
        }
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
