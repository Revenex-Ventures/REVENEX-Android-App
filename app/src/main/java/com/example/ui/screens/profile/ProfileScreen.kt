package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  repository: ErpDataRepository,
  onBack: () -> Unit,
  onNavigateTo: (String) -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val isAppLockEnabled by repository.isAppLockEnabled.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "User Profile & Security",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("profile_screen_list"),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {
      // 1. HERO USER CARD
      item {
        AppCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.screenPadding),
          containerColor = RevenexInk,
          elevation = Elev.e2
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(ScholaOnyx, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = currentUser.avatarInitials.ifBlank { "U" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ScholaTextPrimary
              )
            }
            Spacer(modifier = Modifier.width(Spacing.s4))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = currentUser.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "${currentUser.role.displayName} • ${currentUser.email}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
              )
              Text(
                text = "Phone: ${currentUser.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
              )
            }
          }
        }
      }

      // 2. SECURITY SETTINGS
      item {
        SectionHeader(title = "App Security & Biometrics")
        AppCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(RevenexPrimaryContainer, RoundedCornerShape(Radius.sm)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Fingerprint,
                  contentDescription = null,
                  tint = RevenexPrimary,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(Spacing.s3))
              Column {
                Text(
                  text = "Biometric & App Lock",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Require fingerprint / PIN when resuming the app",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Switch(
              checked = isAppLockEnabled,
              onCheckedChange = { repository.setAppLockEnabled(it) }
            )
          }
        }
      }

      // 3. INSTITUTIONAL INFO
      item {
        Spacer(modifier = Modifier.height(Spacing.s4))
        SectionHeader(title = "Institutional Affiliation")
        AppCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(Spacing.s2)) {
            Text(
              text = "Institution: Revenex Public School",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Affiliation: CBSE Board New Delhi (Affil # 1130492)",
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = "Academic Session: 2026-2027 (Active Term 1)",
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = "ERP Version: 2.4.0 (Enterprise Native)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 4. LOGOUT ACTION
      item {
        Spacer(modifier = Modifier.height(Spacing.s5))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding)
        ) {
          OutlinedButton(
            onClick = { repository.logout() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.md),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
          ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.s2))
            Text("Sign Out of ERP", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
