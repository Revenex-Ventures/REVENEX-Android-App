package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ErpDataRepository
import com.example.ui.theme.*

data class ChatMessage(
  val id: String,
  val sender: String,
  val text: String,
  val isAi: Boolean,
  val timestamp: String
)

@Composable
fun AiAssistantScreen(
  repository: ErpDataRepository,
  onBack: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val feeRecords by repository.feeRecords.collectAsState()
  val notices by repository.notices.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var isTyping by remember { mutableStateOf(false) }

  val messages = remember {
    mutableStateListOf(
      ChatMessage(
        id = "1",
        sender = "Revenex AI",
        text = "Hello ${currentUser.name}! I am your Revenex School ERP Intelligent Assistant. You can ask me to analyze attendance trends, check fee dues, summarize class academic performance, or locate staff and bus records.",
        isAi = true,
        timestamp = "Just now"
      )
    )
  }

  val promptChips = listOf(
    "Summarize Class 10-A attendance",
    "List students with pending fees",
    "Who are top rankers in Class 10?",
    "Upcoming school events & holidays",
    "Show physics lab & IT inventory"
  )

  fun handleSend(query: String) {
    if (query.isBlank()) return
    val userMsg = ChatMessage(
      id = System.currentTimeMillis().toString(),
      sender = currentUser.name,
      text = query.trim(),
      isAi = false,
      timestamp = "Just now"
    )
    messages.add(userMsg)
    inputText = ""
    isTyping = true

    // Generate accurate contextual ERP response
    val aiResponseText = when {
      query.contains("attendance", ignoreCase = true) -> {
        "📊 **Attendance Analysis for Today:**\n• Overall School Attendance: **96.4%**\n• Class 10-A: 5 Present, 1 Absent (Vikram Reddy on approved medical leave).\n• Class 8-A has the highest attendance at **97.4%** today.\n• 100% of teaching faculty are present on duty."
      }
      query.contains("fee", ignoreCase = true) || query.contains("pending", ignoreCase = true) -> {
        val totalPending = feeRecords.sumOf { it.pendingAmount }
        val overdueCount = feeRecords.count { it.status == com.example.data.model.FeeStatus.OVERDUE }
        "💳 **Fee Accounts Summary:**\n• Total Outstanding Dues: **₹${(totalPending / 1000).toInt()},000**\n• Overdue Accounts: $overdueCount student(s).\n• Vikram Reddy (10-A) has ₹28,000 overdue.\n• Aarav Patel (10-A) has ₹14,500 Term 2 dues payable by 15th Sep."
      }
      query.contains("rank", ignoreCase = true) || query.contains("top", ignoreCase = true) -> {
        "🏆 **Top Academic Performers (Class 10-A):**\n1. **Ananya Sharma** - GPA 9.8 (Rank #1)\n2. **Priya Verma** - GPA 9.6 (Rank #2)\n3. **Aarav Patel** - GPA 9.4 (Rank #3)\n4. **Sneha Nair** - GPA 9.5 (Class 10-B Rank #1)"
      }
      query.contains("event", ignoreCase = true) || query.contains("holiday", ignoreCase = true) || query.contains("circular", ignoreCase = true) -> {
        "📅 **Upcoming School Calendar & Circulars:**\n• **1st Sep 2026:** Ganesh Chaturthi (School Holiday)\n• **5th Sep 2026:** Term 1 Parent-Teacher Meeting (PTM) & Teachers' Day\n• **18th Sep 2026:** Annual Science & Robotics Expo 2026\n• **21st Sep 2026:** Pre-Board Examinations commence for Grades 10 & 12."
      }
      query.contains("inventory", ignoreCase = true) || query.contains("lab", ignoreCase = true) -> {
        "🔬 **School Infrastructure & Assets:**\n• 24 BenQ 75\" 4K Interactive Smart Panels (Operational)\n• 60 Lenovo Core-i7 Computer Lab Workstations (Lab 1)\n• 35 Olympus Binocular Lab Microscopes in Bio Wing\n• All assets verified in August 2026 audit."
      }
      else -> {
        "I have processed your query against the Revenex Central Database. There are currently ${students.size} active students, ${teachers.size} faculty members, and 3 GPS bus routes operating normally. Would you like a detailed export or specific student profile?"
      }
    }

    isTyping = false
    messages.add(
      ChatMessage(
        id = (System.currentTimeMillis() + 1).toString(),
        sender = "Revenex AI",
        text = aiResponseText,
        isAi = true,
        timestamp = "Just now"
      )
    )
  }

  Scaffold(
    topBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(RevenexPrimaryContainer, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RevenexBlue, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Revenex AI ERP Assistant",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Instant School Intelligence & Insights",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    },
    bottomBar = {
      Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          // Smart Prompt Chips
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
          ) {
            items(promptChips) { chip ->
              SuggestionChip(
                onClick = { handleSend(chip) },
                label = { Text(chip, fontSize = 11.sp) }
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = inputText,
              onValueChange = { inputText = it },
              placeholder = { Text("Ask anything about students, fees, attendance...") },
              modifier = Modifier
                .weight(1f)
                .testTag("ai_assistant_input"),
              shape = RoundedCornerShape(24.dp),
              singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
              onClick = { handleSend(inputText) },
              modifier = Modifier
                .size(46.dp)
                .background(RevenexBlue, CircleShape)
                .testTag("ai_assistant_send_btn")
            ) {
              Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("ai_messages_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (msg.isAi) Arrangement.Start else Arrangement.End
        ) {
          if (msg.isAi) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(RevenexPrimaryContainer, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RevenexBlue, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          Surface(
            shape = RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (msg.isAi) 4.dp else 16.dp,
              bottomEnd = if (msg.isAi) 16.dp else 4.dp
            ),
            color = if (msg.isAi) MaterialTheme.colorScheme.surfaceVariant else RevenexBlue,
            modifier = Modifier.widthIn(max = 300.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (msg.isAi) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                lineHeight = 20.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = msg.timestamp,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = if (msg.isAi) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
              )
            }
          }
        }
      }

      if (isTyping) {
        item {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Revenex AI is querying school records...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}
