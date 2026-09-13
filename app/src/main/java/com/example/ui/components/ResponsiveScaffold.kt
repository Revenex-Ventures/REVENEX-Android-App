package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.navigation.NavConfig
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.util.Locale

// Device Form Factor Breakpoints
enum class WindowSizeClass {
  COMPACT_MOBILE,   // < 600dp
  MEDIUM_TABLET,    // 600dp .. 1024dp
  EXPANDED_DESKTOP  // > 1024dp
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
  val configuration = LocalConfiguration.current
  val screenWidthDp = configuration.screenWidthDp
  return when {
    screenWidthDp < 600 -> WindowSizeClass.COMPACT_MOBILE
    screenWidthDp <= 1024 -> WindowSizeClass.MEDIUM_TABLET
    else -> WindowSizeClass.EXPANDED_DESKTOP
  }
}

/**
 * Responsive ScholaOS Shell:
 * - Mobile: Top Institutional Bar + Content + Bottom Floating Dock (64dp, 10dp elevation)
 * - Tablet: Left Collapsed Navigation Rail (80dp) + Top Bar + Content
 * - Desktop: Left Fixed Navigation Drawer (260dp) with branding & profile + Wide Content
 */
@Composable
fun ScholaResponsiveScaffold(
  currentRoute: String?,
  currentUser: UserProfile,
  unreadNotificationsCount: Int,
  onNavigateToRoute: (String) -> Unit,
  onOpenCommandPalette: () -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenProfile: () -> Unit,
  onLogout: () -> Unit,
  content: @Composable (PaddingValues) -> Unit
) {
  val windowSizeClass = rememberWindowSizeClass()
  val navItems = remember(currentUser.role) {
    NavConfig.getBottomNavItems(currentUser.role).map {
      ScholaDockItem(
        route = it.route,
        label = it.label,
        selectedIcon = it.selectedIcon,
        unselectedIcon = it.unselectedIcon
      )
    }
  }

  val isTopLevelRoute = currentRoute in navItems.map { it.route }

  when (windowSizeClass) {
    WindowSizeClass.COMPACT_MOBILE -> {
      // 1. MOBILE LAYOUT (< 600dp)
      Scaffold(
        containerColor = ScholaLinen,
        topBar = {
          if (currentRoute != Screen.Auth.route && currentRoute != Screen.GlobalSearch.route) {
            ScholaTopAppBar(
              title = "ScholaOS",
              subtitle = "Schola Academy",
              currentRole = currentUser.role,
              currentUserName = currentUser.name,
              unreadCount = unreadNotificationsCount,
              onSearchClick = onOpenCommandPalette,
              onNotificationClick = onOpenNotifications,
              onProfileClick = onOpenProfile,
              onLogoutClick = onLogout
            )
          }
        },
        bottomBar = {
          if (isTopLevelRoute && currentRoute != Screen.Auth.route) {
            ScholaFloatingDock(
              items = navItems,
              currentRoute = currentRoute,
              onItemSelected = onNavigateToRoute
            )
          }
        }
      ) { paddingValues ->
        content(paddingValues)
      }
    }

    WindowSizeClass.MEDIUM_TABLET -> {
      // 2. TABLET LAYOUT (600dp .. 1024dp)
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScholaLinen
      ) {
        Row(modifier = Modifier.fillMaxSize()) {
          // Left Collapsed Navigation Rail (80dp)
          if (currentRoute != Screen.Auth.route) {
            TabletNavigationRail(
              items = navItems,
              currentRoute = currentRoute,
              onItemSelected = onNavigateToRoute,
              onOpenCommandPalette = onOpenCommandPalette,
              currentUser = currentUser
            )
          }

          // Right Content Area
          Scaffold(
            containerColor = ScholaLinen,
            topBar = {
              if (currentRoute != Screen.Auth.route && currentRoute != Screen.GlobalSearch.route) {
                ScholaTopAppBar(
                  title = "ScholaOS Tablet Hub",
                  subtitle = "Schola Academy • Multitask Suite",
                  currentRole = currentUser.role,
                  currentUserName = currentUser.name,
                  unreadCount = unreadNotificationsCount,
                  onSearchClick = onOpenCommandPalette,
                  onNotificationClick = onOpenNotifications,
                  onProfileClick = onOpenProfile,
                  onLogoutClick = onLogout
                )
              }
            }
          ) { paddingValues ->
            content(paddingValues)
          }
        }
      }
    }

    WindowSizeClass.EXPANDED_DESKTOP -> {
      // 3. DESKTOP LAYOUT (> 1024dp)
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScholaLinen
      ) {
        Row(modifier = Modifier.fillMaxSize()) {
          // Left Fixed Navigation Drawer (260dp)
          if (currentRoute != Screen.Auth.route) {
            DesktopNavigationDrawer(
              items = navItems,
              currentRoute = currentRoute,
              currentUser = currentUser,
              unreadCount = unreadNotificationsCount,
              onItemSelected = onNavigateToRoute,
              onOpenCommandPalette = onOpenCommandPalette,
              onOpenNotifications = onOpenNotifications,
              onOpenProfile = onOpenProfile,
              onLogout = onLogout
            )
          }

          // Main Expanded Content Canvas
          Scaffold(
            containerColor = ScholaLinen,
            topBar = {
              if (currentRoute != Screen.Auth.route && currentRoute != Screen.GlobalSearch.route) {
                ScholaTopAppBar(
                  title = "ScholaOS Executive Operating System",
                  subtitle = "Schola Academy • Academic Cycle 2026-2027",
                  currentRole = currentUser.role,
                  currentUserName = currentUser.name,
                  unreadCount = unreadNotificationsCount,
                  onSearchClick = onOpenCommandPalette,
                  onNotificationClick = onOpenNotifications,
                  onProfileClick = onOpenProfile,
                  onLogoutClick = onLogout
                )
              }
            }
          ) { paddingValues ->
            content(paddingValues)
          }
        }
      }
    }
  }
}

// Tablet Navigation Rail (80dp width, dark onyx / warm linen hybrid)
@Composable
private fun TabletNavigationRail(
  items: List<ScholaDockItem>,
  currentRoute: String?,
  onItemSelected: (String) -> Unit,
  onOpenCommandPalette: () -> Unit,
  currentUser: UserProfile
) {
  Surface(
    modifier = Modifier
      .width(80.dp)
      .fillMaxHeight(),
    color = ScholaOnyx,
    tonalElevation = Elev.e2
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(vertical = Spacing.s4),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Crest Emblem
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(ScholaTerracotta),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = "ScholaOS",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.height(Spacing.s4))

        // ⌘K Search trigger icon
        IconButton(
          onClick = onOpenCommandPalette,
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(ScholaOnyxSurface)
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Command Palette",
            tint = ScholaGoldLight,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.height(Spacing.s4))

        // Nav Item Icons
        items.forEach { item ->
          val isSelected = currentRoute == item.route
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(Radius.md))
              .background(if (isSelected) ScholaTerracotta else Color.Transparent)
              .clickable { onItemSelected(item.route) }
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = if (isSelected) Color.White else ScholaOnyxMuted,
                modifier = Modifier.size(22.dp)
              )
              Text(
                text = item.label.take(5),
                color = if (isSelected) Color.White else ScholaOnyxMuted,
                fontSize = 8.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
          Spacer(modifier = Modifier.height(Spacing.s2))
        }
      }

      // User Avatar Footer
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(RoleAccent.of(currentUser.role)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = currentUser.name.take(2).uppercase(Locale.getDefault()),
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }
    }
  }
}

// Desktop Fixed Navigation Drawer (260dp width)
@Composable
private fun DesktopNavigationDrawer(
  items: List<ScholaDockItem>,
  currentRoute: String?,
  currentUser: UserProfile,
  unreadCount: Int,
  onItemSelected: (String) -> Unit,
  onOpenCommandPalette: () -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenProfile: () -> Unit,
  onLogout: () -> Unit
) {
  Surface(
    modifier = Modifier
      .width(260.dp)
      .fillMaxHeight(),
    color = ScholaOnyx,
    tonalElevation = Elev.e3
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(Spacing.s4),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top section: Crest Branding + ⌘K trigger pill + nav links
      Column {
        // Institutional Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = Spacing.s4)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(Radius.md))
              .background(ScholaTerracotta),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AccountBalance,
              contentDescription = "ScholaOS Crest",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(Spacing.s3))
          Column {
            Text(
              text = "ScholaOS",
              color = ScholaOnyxText,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Enterprise Educational OS",
              color = ScholaOnyxMuted,
              style = MaterialTheme.typography.bodySmall,
              fontSize = 10.sp
            )
          }
        }

        // ⌘K Search Pill Trigger
        Surface(
          shape = RoundedCornerShape(Radius.lg),
          color = ScholaOnyxSurface,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .border(1.dp, ScholaOnyxBorder, RoundedCornerShape(Radius.lg))
            .clickable(onClick = onOpenCommandPalette)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = Spacing.s3, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Search, contentDescription = null, tint = ScholaOnyxMuted, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(Spacing.s2))
              Text("Omni Command...", color = ScholaOnyxMuted, fontSize = 12.sp)
            }
            Surface(
              shape = RoundedCornerShape(Radius.sm),
              color = ScholaTerracotta
            ) {
              Text("⌘K", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(Spacing.s5))

        Text(
          text = "CORE WORKSPACES",
          color = ScholaOnyxMuted,
          style = MaterialTheme.typography.labelSmall,
          letterSpacing = TypeTokens.trackingMicroLabel,
          fontSize = 9.sp,
          modifier = Modifier.padding(horizontal = Spacing.s2, vertical = Spacing.s1)
        )

        // Navigation Items List
        items.forEach { item ->
          val isSelected = currentRoute == item.route
          Surface(
            shape = RoundedCornerShape(Radius.md),
            color = if (isSelected) ScholaTerracotta else Color.Transparent,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(Radius.md))
              .clickable { onItemSelected(item.route) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = Spacing.s3, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = if (isSelected) Color.White else ScholaOnyxMuted,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(Spacing.s3))
              Text(
                text = item.label,
                color = if (isSelected) Color.White else ScholaOnyxText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
        }
      }

      // User Profile Footer
      Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = ScholaOnyxSurface,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.lg))
          .border(1.dp, ScholaOnyxBorder, RoundedCornerShape(Radius.lg))
      ) {
        Row(
          modifier = Modifier.padding(Spacing.s3),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(RoleAccent.of(currentUser.role)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = currentUser.name.take(2).uppercase(Locale.getDefault()),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
            Spacer(modifier = Modifier.width(Spacing.s2))
            Column {
              Text(
                text = currentUser.name,
                color = ScholaOnyxText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = currentUser.role.displayName,
                color = ScholaOnyxMuted,
                fontSize = 9.sp,
                maxLines = 1
              )
            }
          }
          IconButton(onClick = onLogout, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = StatusDangerText, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}
