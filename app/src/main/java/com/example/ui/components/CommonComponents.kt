package com.example.ui.components

import kotlinx.coroutines.launch

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import java.util.Locale

val LocalWorkspaceBack = staticCompositionLocalOf<(() -> Unit)?> { null }

@Composable
fun WorkspacePageHeader(title: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
  val back = LocalWorkspaceBack.current
  Row(modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
    if (back != null) IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Back") }
    Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    actions()
  }
}

/** Compact presentation primitives shared by the routed school workspaces. */
@Composable
fun SchoolSection(title: String, action: String? = null, onAction: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      if (action != null) TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = Spacing.sm8)) {
        Text(action, style = MaterialTheme.typography.labelMedium)
      }
    }
    content()
  }
}

@Composable
fun SchoolGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.md),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
    Column(content = content)
  }
}

@Composable
fun SchoolRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit,
  trailing: String? = null, modifier: Modifier = Modifier) {
  Row(modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 64.dp).padding(Spacing.md12),
    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
    val (well,ink)=when(icon.name.substringAfterLast('.')) {
      "Assignment","Assessment","Grade","Description","MenuBook","EditNote" -> SemanticColors.lavenderContainer to SemanticColors.onLavender
      "Payments","Payment","CurrencyRupee","ReceiptLong","AccountBalanceWallet" -> SemanticColors.butterContainer to SemanticColors.onButter
      "CalendarMonth","CheckCircle","HowToReg","EventAvailable" -> SemanticColors.successContainer to SemanticColors.onSuccess
      "Campaign","Notifications","Event" -> SemanticColors.peachContainer to SemanticColors.onPeach
      else -> SemanticColors.blueContainer to SemanticColors.onBlue
    }
    Surface(shape=RoundedCornerShape(Radius.sm),color=well) {
      Icon(icon,null,Modifier.padding(Spacing.sm8).size(20.dp),tint=ink)
    }
    Column(Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
      if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (trailing != null) Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
fun SchoolMetric(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier,
  tone: SchoolMetricTone = SchoolMetricTone.NEUTRAL, onClick: (() -> Unit)? = null) {
  val (well, ink) = when (tone) {
    SchoolMetricTone.POSITIVE -> SemanticColors.successContainer to SemanticColors.onSuccess
    SchoolMetricTone.DANGER -> SemanticColors.dangerContainer to SemanticColors.onDanger
    SchoolMetricTone.FINANCE -> SemanticColors.butterContainer to SemanticColors.onButter
    SchoolMetricTone.NEUTRAL -> SemanticColors.blueContainer to SemanticColors.onBlue
  }
  Surface(if (onClick == null) modifier else modifier.clickable(onClick = onClick), shape = RoundedCornerShape(Radius.md),
    color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
    Column(Modifier.padding(Spacing.md12), verticalArrangement = Arrangement.spacedBy(Spacing.xs4)) {
      Surface(color = well, shape = RoundedCornerShape(Radius.xs)) {
        Icon(icon, null, Modifier.padding(Spacing.xs4).size(20.dp), tint = ink)
      }
      Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

enum class SchoolMetricTone { NEUTRAL, POSITIVE, DANGER, FINANCE }

// ============================================================================
// 1. SCHOLA TOP APP BAR (Institutional crest, ⌘K search trigger, unread badge)
// ============================================================================
@Composable
fun ScholaTopAppBar(
  title: String = "REVENEX",
  subtitle: String? = "School ERP demonstration",
  currentRole: UserRole = UserRole.PRINCIPAL,
  currentUserName: String = "Dr. Arthur Pendelton",
  unreadCount: Int = 3,
  onSearchClick: () -> Unit = {},
  onNotificationClick: () -> Unit = {},
  onProfileClick: () -> Unit = {},
  onLogoutClick: () -> Unit = {},
  navigationIcon: @Composable (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
    Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.sm8, vertical = Spacing.xs4), verticalAlignment = Alignment.CenterVertically) {
      navigationIcon?.invoke()
      Row(Modifier.weight(1f).padding(start = Spacing.sm8), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        RevenexBrand()
      }
      IconButton(onClick = onSearchClick, modifier = Modifier.testTag("command_palette_trigger_pill")) {
        Icon(Icons.Default.Search, "Search school records")
      }
      IconButton(onClick = onNotificationClick, modifier = Modifier.testTag("topbar_notifications_button")) {
        BadgedBox(badge = { if (unreadCount > 0) androidx.compose.material3.Badge { Text(if (unreadCount > 99) "99+" else "$unreadCount") } }) {
          Icon(Icons.Outlined.Notifications, "Notifications, $unreadCount unread")
        }
      }
      var menu by remember { mutableStateOf(false) }
      Box {
        IconButton(onClick = { menu = true }, modifier = Modifier.testTag("topbar_profile_avatar")) { Icon(Icons.Outlined.AccountCircle, "Account and settings") }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
          DropdownMenuItem(text = { Text("Settings & profile") }, onClick = { menu = false; onProfileClick() })
          DropdownMenuItem(text = { Text("Sign out") }, onClick = { menu = false; onLogoutClick() })
        }
      }
    }
  }
}

// Backward compatibility alias for RevenexTopBar
@Composable
fun RevenexTopBar(
  title: String,
  subtitle: String? = null,
  currentRole: UserRole,
  currentUserName: String = "",
  unreadCount: Int = 0,
  onNotificationClick: () -> Unit,
  onSearchClick: () -> Unit,
  onProfileClick: () -> Unit,
  onLogoutClick: () -> Unit,
  navigationIcon: @Composable (() -> Unit)? = null
) {
  ScholaTopAppBar(
    title = title,
    subtitle = subtitle,
    currentRole = currentRole,
    currentUserName = currentUserName,
    unreadCount = unreadCount,
    onSearchClick = onSearchClick,
    onNotificationClick = onNotificationClick,
    onProfileClick = onProfileClick,
    onLogoutClick = onLogoutClick,
    navigationIcon = navigationIcon
  )
}

// ============================================================================
// 2. WORKFLOW QUICK ACTION PILL
// ============================================================================
@Composable
fun WorkflowQuickPill(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isHighlighted: Boolean = false
) {
  Box(
    modifier = modifier
.glassEffect(cornerRadius = Radius.pill)
      .clickable(onClick = onClick)
      .heightIn(min = 48.dp)
      .testTag("workflow_pill_${title.lowercase().replace(" ", "_")}")
      .padding(horizontal = Spacing.s4, vertical = 10.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isHighlighted) MaterialTheme.colorScheme.primary else ScholaSlateNavy,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(Spacing.s2))
      Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

// ============================================================================
// 3. SECTION HEADER
// ============================================================================
@Composable
fun WorkspaceHeading(title: String, description: String, action: (@Composable () -> Unit)? = null) {
  Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    action?.invoke()
  }
}

@Composable
fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null,
  horizontalPadding: Dp = Spacing.s4
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = horizontalPadding, vertical = Spacing.s2),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )
    if (actionText != null && onActionClick != null) {
      TextButton(
        onClick = onActionClick,
        contentPadding = PaddingValues(horizontal = Spacing.s2, vertical = Spacing.s1)
      ) {
        Text(
          text = actionText,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

// ============================================================================
// 4. STATUS BADGE (Delegating to ScholaPillBadge)
// ============================================================================
@Composable
fun StatusBadge(
  status: String,
  type: String = "general",
  modifier: Modifier = Modifier
) {
  ScholaPillBadge(status = status, modifier = modifier)
}

// ============================================================================
// 5. QUICK ACTION BUTTON & STAT CARD WRAPPERS
// ============================================================================
@Composable
fun QuickActionButton(
  title: String,
  icon: ImageVector,
  iconTint: Color = MaterialTheme.colorScheme.primary,
  backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  WorkflowQuickPill(
    title = title,
    icon = icon,
    onClick = onClick,
    modifier = modifier
  )
}

@Composable
fun StatCard(
  title: String,
  value: String,
  sublabel: String,
  icon: ImageVector,
  iconTint: Color = MaterialTheme.colorScheme.primary,
  iconBackground: Color = MaterialTheme.colorScheme.primaryContainer,
  modifier: Modifier = Modifier,
  trendText: String? = null,
  isPositiveTrend: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  CompactStatTile(
    categoryLabel = title,
    statCounter = value,
    icon = icon,
    iconTint = iconTint,
    iconContainerColor = iconBackground,
    deltaText = trendText,
    isPositiveDelta = isPositiveTrend,
    modifier = modifier,
    onClick = onClick
  )
}

// ============================================================================
// 6. ANIMATED BAR CHART & DONUT CHART
// ============================================================================
@Composable
fun SimpleBarChart(
  title: String,
  data: List<Pair<String, Float>>,
  modifier: Modifier = Modifier,
  barColor: Color = MaterialTheme.colorScheme.primary,
  onBarClick: ((String) -> Unit)? = null
) {
  AppCard(modifier = modifier.fillMaxWidth()) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(Spacing.md12))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs4), verticalAlignment = Alignment.Bottom) {
      data.forEach { (label, value) ->
        Column(Modifier.weight(1f).then(onBarClick?.let { action -> Modifier.clickable { action(label) } } ?: Modifier),
          horizontalAlignment = Alignment.CenterHorizontally) {
          Text("${value.toInt()}%", style = MaterialTheme.typography.labelMedium)
          Box(Modifier.height(80.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Box(Modifier.width(18.dp).height((80f * (value / 100f).coerceIn(0f,1f)).dp)
              .clip(RoundedCornerShape(topStart = Radius.xs, topEnd = Radius.xs)).background(barColor))
          }
          Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
      }
    }
  }
}

@Composable
fun FeeCollectionDonutChart(
  collectedAmount: Long,
  totalAmount: Long,
  modifier: Modifier = Modifier
) {
  val fraction = if (totalAmount > 0) (collectedAmount.toFloat() / totalAmount.toFloat()).coerceIn(0f, 1f) else 0f
  val animFraction = remember { Animatable(0f) }
  LaunchedEffect(fraction) {
    animFraction.animateTo(fraction, tween(900, easing = FastOutSlowInEasing))
  }

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "COLLECTION REALIZATION",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = TypeTokens.trackingMicroLabel
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${(fraction * 100).toInt()}% Realized",
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "₹${collectedAmount / 100000L}k of ₹${totalAmount / 100000L}k target collected",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
      )
    }

    Box(contentAlignment = Alignment.Center) {
      CircularProgressIndicator(
        progress = { animFraction.value },
        modifier = Modifier.size(64.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        strokeWidth = 6.dp
      )
      Text(
        text = "${(fraction * 100).toInt()}%",
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

// ============================================================================
// 7. EMPTY STATE & SUCCESS CHECKMARK
// ============================================================================
@Composable
fun EmptyStateView(title: String,message: String="",description: String=message,icon: ImageVector=Icons.Outlined.Inbox,
  actionLabel: String?=null,onActionClick:(()->Unit)?=null,modifier:Modifier=Modifier) {
  Surface(modifier.fillMaxWidth(),color=MaterialTheme.colorScheme.surfaceVariant,shape=RoundedCornerShape(Radius.md)) {
    Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      Row(verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
        Icon(icon,null,Modifier.size(24.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
          Text(title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurface)
          if(description.isNotBlank()) Text(description,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      if(actionLabel!=null&&onActionClick!=null) TextButton(onClick=onActionClick){Text(actionLabel)}
    }
  }
}

@Composable
fun SuccessCheckmarkView(
  message: String,
  modifier: Modifier = Modifier
) {
  val scale = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    scale.animateTo(1f, animationSpec = Motion.springSmooth())
  }

  Column(
    modifier = modifier.padding(Spacing.cardPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .scale(scale.value)
        .background(SemanticColors.successContainer, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = SemanticColors.onSuccess,
        modifier = Modifier.size(32.dp)
      )
    }
    Spacer(modifier = Modifier.height(Spacing.s3))
    Text(
      text = message,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )
  }
}

// PDF Export Helper Stub
object PdfExportHelper {
  fun exportFeeReceiptPdf(
    context: android.content.Context,
    student: com.example.data.model.Student?,
    record: com.example.data.model.FeeRecord,
    transaction: com.example.data.model.FeePaymentTransaction?
  ) {
    android.widget.Toast.makeText(context, "Receipt exported for ${record.studentName} (ID: ${record.id})", android.widget.Toast.LENGTH_SHORT).show()
  }
}

/** Resolves a private document only on request; errors never fall back to expired bearer URLs. */
@Composable
fun SchoolDocumentButton(reference: String, label: String = "Open document") {
  val context=androidx.compose.ui.platform.LocalContext.current
  val scope=rememberCoroutineScope()
  var opening by remember(reference) { mutableStateOf(false) }
  var error by remember(reference) { mutableStateOf(false) }
  Column {
    TextButton(enabled=!opening,onClick={
      opening=true;error=false
      scope.launch {
        try {
          val url=com.example.data.datasource.SupabaseFileStore.resolveUrl(reference)
          context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse(url)))
        } catch(e:Exception) {
          if(e is kotlinx.coroutines.CancellationException) throw e
          error=true
        } finally { opening=false }
      }
    }) { Text(if(opening) "Opening…" else label) }
    if(error) Text("Document could not be opened. Check access and connection, then retry.",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
  }
}

/** Selected filters share the same orange identity as navigation and actions. */
@Composable
fun SchoolFilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit,
  modifier: Modifier = Modifier, enabled: Boolean = true) {
  FilterChip(selected = selected, onClick = onClick, label = label, modifier = modifier, enabled = enabled,
    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
      selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
}


@Composable
fun SchoolCompactMetric(value: String,label: String,icon: ImageVector,modifier: Modifier=Modifier,
 tone: SchoolMetricTone=SchoolMetricTone.NEUTRAL,onClick: ()->Unit) {
 val (well,ink)=when(tone){SchoolMetricTone.POSITIVE->SemanticColors.successContainer to SemanticColors.onSuccess;SchoolMetricTone.DANGER->SemanticColors.dangerContainer to SemanticColors.onDanger;SchoolMetricTone.FINANCE->SemanticColors.butterContainer to SemanticColors.onButter;else->SemanticColors.blueContainer to SemanticColors.onBlue}
 Surface(modifier.clickable(onClick=onClick),shape=RoundedCornerShape(Radius.md),color=MaterialTheme.colorScheme.surface,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
  Row(Modifier.padding(Spacing.sm8).heightIn(min=Spacing.xxxl48),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   Surface(color=well,shape=RoundedCornerShape(Radius.sm)){Icon(icon,null,Modifier.padding(Spacing.xs4).size(Spacing.xl20),tint=ink)}
   Column(Modifier.weight(1f)){Text(value,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  }
 }
}
