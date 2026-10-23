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
  val cloud = repository.dataMode.collectAsState().value == com.example.data.datasource.DataMode.SUPABASE_DEMO
  val sessionLabel = if (cloud && currentUser.id.isNotBlank()) runCatching { repository.supabaseDataSource.sessionId() }.getOrDefault("Not loaded") else "2026–2027 · local demo"

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Settings & profile",
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
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          elevation = Elev.e2
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = currentUser.avatarInitials.ifBlank { "U" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.width(Spacing.s4))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = currentUser.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${currentUser.role.displayName} • ${currentUser.email}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
              )
              Text(
                text = "Phone: ${currentUser.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
              )
            }
          }
        }
      }

      item {
        SectionHeader(title = "Settings · Appearance")
        val appearance = LocalAppearance.current
        Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.screenPadding)) {
          Text("Choose one appearance for the whole app.", style = MaterialTheme.typography.bodyMedium)
          SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = Spacing.sm8)) {
            Appearance.entries.forEachIndexed { index, option ->
              SegmentedButton(
                colors = SegmentedButtonDefaults.colors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                selected = appearance?.selection == option,
                onClick = { appearance?.select(option) },
                shape = SegmentedButtonDefaults.itemShape(index, Appearance.entries.size)
              ) { Text(option.label) }
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
                  .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(Radius.sm)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Fingerprint,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
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
              text = if (cloud) "School: ${currentUser.schoolId}" else "Institution: Revenex Public School (sample)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = if (cloud) "School membership is assigned by your school operator." else "Sample school affiliation · CBSE",
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = "Academic session: $sessionLabel",
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = "Revenex School ERP · demonstration build",
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
            onClick = { repository.logout(); onNavigateTo(com.example.ui.navigation.Screen.Auth.route) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.md),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
