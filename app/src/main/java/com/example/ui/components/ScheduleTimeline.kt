package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.TimetableSlot
import com.example.ui.theme.ScholaBorder
import com.example.ui.theme.ScholaLinen
import com.example.ui.theme.ScholaMuted
import com.example.ui.theme.ScholaSurface
import com.example.ui.theme.ScholaTerracotta
import com.example.ui.theme.ScholaTextPrimary
import com.example.ui.theme.ScholaTextSecondary
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

// ============================================================================
// SCHEDULE TIMELINE — shared rail-track rows (live, real-time aware)
// Used by the Timetable workspace and the dashboard "Today's Schedule".
// Renders: time column | rail + status node | content (no cards, no boxing).
// ============================================================================

enum class ScheduleRowStatus { COMPLETED, CURRENT, UPCOMING }

fun currentDayOfWeekName(): String =
  Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: ""

fun currentMinutesOfDay(): Int {
  val cal = Calendar.getInstance()
  return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
}

private fun parseClockMinute(text: String): Int {
  val t = text.trim()
  val meridiem = when {
    t.endsWith("PM", ignoreCase = true) -> "PM"
    t.endsWith("AM", ignoreCase = true) -> "AM"
    else -> ""
  }
  val timePart = if (meridiem.isNotEmpty()) t.dropLast(2).trim() else t
  val parts = timePart.split(":")
  val mins = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
  var hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
  hour %= 12
  if (meridiem == "PM") hour += 12
  return hour * 60 + mins
}

private fun stripMeridiem(text: String): String =
  Regex(" ?[AP]M$", RegexOption.IGNORE_CASE).replace(text.trim(), "")

private fun formatMeridiem(minutes: Int): String {
  val h = (minutes / 60) % 24
  val m = minutes % 60
  val hh = if (h % 12 == 0) 12 else h % 12
  return String.format("%d:%02d %s", hh, m, if (h < 12) "AM" else "PM")
}

fun scheduleStatus(isToday: Boolean, nowMinutes: Int, slot: TimetableSlot): ScheduleRowStatus {
  if (!isToday) return ScheduleRowStatus.UPCOMING
  val start = parseClockMinute(slot.startTime)
  val end = parseClockMinute(slot.endTime)
  return when {
    nowMinutes < start -> ScheduleRowStatus.UPCOMING
    nowMinutes < max(end, start + 1) -> ScheduleRowStatus.CURRENT
    else -> ScheduleRowStatus.COMPLETED
  }
}

// ── LIVE CLOCK (pulsing accent dot + current time) ─────────────────────────
@Composable
fun ScheduleLiveClock(minutes: Int, modifier: Modifier = Modifier) {
  val pulse = rememberInfiniteTransition(label = "liveClock")
  val dotAlpha by pulse.animateFloat(
    initialValue = 1f,
    targetValue = 0.15f,
    animationSpec = infiniteRepeatable(tween(950), RepeatMode.Reverse),
    label = "dotAlpha"
  )
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(7.dp)
        .clip(CircleShape)
        .background(ScholaTerracotta.copy(alpha = dotAlpha))
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = formatMeridiem(minutes),
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = ScholaTerracotta
    )
  }
}

// ── SINGLE TIMELINE ROW ────────────────────────────────────────────────────
@Composable
fun ScheduleTimelineRow(
  slot: TimetableSlot,
  status: ScheduleRowStatus,
  modifier: Modifier = Modifier,
  backgroundFill: Color = ScholaSurface,
  showDivider: Boolean = false,
  dividerStartX: Dp = 114.dp
) {
  val subjectColor = Color(slot.subjectColorHex)
  val isCurrent = status == ScheduleRowStatus.CURRENT
  val isDone = status == ScheduleRowStatus.COMPLETED
  val accent = if (isCurrent) ScholaTerracotta else subjectColor

  Row(
    modifier = modifier.drawBehind {
      val trackColor = if (isDone || isCurrent) accent else ScholaBorder
      val trackX = 79.dp.toPx()
      drawLine(
        color = trackColor,
        start = Offset(trackX, 0f),
        end = Offset(trackX, size.height),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Square
      )
    },
    verticalAlignment = Alignment.CenterVertically
  ) {
    // ── TIME COLUMN ─────────────────────────────────────
    Column(
      modifier = Modifier
        .width(58.dp)
        .padding(start = 14.dp),
      horizontalAlignment = Alignment.End
    ) {
      Text(
        text = stripMeridiem(slot.startTime),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = when (status) {
          ScheduleRowStatus.CURRENT -> accent
          ScheduleRowStatus.COMPLETED -> ScholaMuted
          ScheduleRowStatus.UPCOMING -> ScholaTextPrimary
        }
      )
      Text(
        text = stripMeridiem(slot.endTime),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = if (isDone) ScholaMuted.copy(alpha = 0.6f) else ScholaMuted
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // ── NODE (track line is drawn behind the whole row) ──
    Box(
      modifier = Modifier.width(26.dp),
      contentAlignment = Alignment.Center
    ) {
      TimelineNode(
        status = status,
        subjectColor = subjectColor,
        backgroundFill = backgroundFill
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // ── CONTENT ─────────────────────────────────────────
    Column(
      modifier = Modifier
        .weight(1f)
        .padding(top = 12.dp, bottom = 12.dp, end = 16.dp)
        .alpha(if (isDone) 0.5f else 1f)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = slot.subject,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = if (isCurrent) accent else ScholaTextPrimary,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        when (status) {
          ScheduleRowStatus.CURRENT -> LiveBadge(color = subjectColor)
          ScheduleRowStatus.COMPLETED -> Text(
            text = "P${slot.periodNumber}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = accent.copy(alpha = 0.5f)
          )
          ScheduleRowStatus.UPCOMING -> Text(
            text = "P${slot.periodNumber}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = ScholaMuted
          )
        }
      }
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = slot.teacherName,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = ScholaTextSecondary
      )
      Text(
        text = slot.roomNumber,
        style = MaterialTheme.typography.labelSmall,
        color = ScholaMuted
      )
    }
  }

  if (showDivider) {
    HorizontalDivider(
      modifier = Modifier.padding(start = dividerStartX, end = 16.dp),
      color = ScholaBorder,
      thickness = 1.dp
    )
  }
}

// ── TIMELINE NODE (status dot on the rail) ──────────────────────────────────
@Composable
private fun TimelineNode(
  status: ScheduleRowStatus,
  subjectColor: Color,
  backgroundFill: Color = ScholaLinen,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.size(14.dp), contentAlignment = Alignment.Center) {
    when (status) {
      ScheduleRowStatus.COMPLETED -> Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(subjectColor.copy(alpha = 0.45f))
      )

      ScheduleRowStatus.UPCOMING -> Box(
        modifier = Modifier
          .size(12.dp)
          .clip(CircleShape)
          .background(backgroundFill)
          .border(1.5.dp, ScholaBorder, CircleShape)
      )

      ScheduleRowStatus.CURRENT -> {
        val pulse = rememberInfiniteTransition(label = "currentNode")
        val ringAlpha by pulse.animateFloat(0.5f, 0f, infiniteRepeatable(tween(1300)), label = "ringAlpha")
        val ringScale by pulse.animateFloat(1f, 2.3f, infiniteRepeatable(tween(1300)), label = "ringScale")
        Box(
          modifier = Modifier
            .size(12.dp)
            .graphicsLayer {
              scaleX = ringScale
              scaleY = ringScale
              alpha = ringAlpha
            }
            .clip(CircleShape)
            .background(subjectColor)
        )
        Box(
          modifier = Modifier
            .size(14.dp)
            .border(2.dp, Color.White, CircleShape)
            .clip(CircleShape)
            .background(subjectColor)
        )
      }
    }
  }
}

// ── LIVE BADGE (current period) ────────────────────────────────────────────
@Composable
private fun LiveBadge(color: Color, modifier: Modifier = Modifier) {
  val pulse = rememberInfiniteTransition(label = "liveBadge")
  val dotAlpha by pulse.animateFloat(
    initialValue = 1f,
    targetValue = 0.2f,
    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
    label = "badgeDot"
  )
  Surface(shape = RoundedCornerShape(50), color = color) {
    Row(
      modifier = modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = dotAlpha))
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "LIVE",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }
  }
}

// ── HOME SCHEDULE BLOCK (rail + full-width rectangular period card) ────────
@Composable
fun ScheduleBlockRow(
  slot: TimetableSlot,
  status: ScheduleRowStatus,
  modifier: Modifier = Modifier
) {
  val subjectColor = Color(slot.subjectColorHex)
  val isCurrent = status == ScheduleRowStatus.CURRENT
  val isDone = status == ScheduleRowStatus.COMPLETED
  val accent = if (isCurrent) ScholaTerracotta else subjectColor
  val blockShape = RoundedCornerShape(18.dp)

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // ── TIME COLUMN ─────────────────────────────────────
    Column(
      modifier = Modifier
        .width(58.dp)
        .padding(start = 14.dp),
      horizontalAlignment = Alignment.End
    ) {
      Text(
        text = stripMeridiem(slot.startTime),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = when (status) {
          ScheduleRowStatus.CURRENT -> accent
          ScheduleRowStatus.COMPLETED -> ScholaMuted
          ScheduleRowStatus.UPCOMING -> ScholaTextPrimary
        }
      )
      Text(
        text = stripMeridiem(slot.endTime),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = if (isDone) ScholaMuted.copy(alpha = 0.6f) else ScholaMuted
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // ── NODE on the rail ─────────────────────────────────
    Box(
      modifier = Modifier.width(26.dp),
      contentAlignment = Alignment.Center
    ) {
      TimelineNode(
        status = status,
        subjectColor = subjectColor,
        backgroundFill = ScholaLinen
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // ── RECTANGULAR PERIOD BLOCK ─────────────────────────
    Column(
      modifier = Modifier
        .weight(1f)
        .alpha(if (isDone) 0.55f else 1f)
        .clip(blockShape)
        .background(ScholaSurface, blockShape)
        .border(1.dp, if (isCurrent) accent.copy(alpha = 0.6f) else ScholaBorder, blockShape)
        .drawBehind {
          drawRect(
            color = if (isCurrent) accent else subjectColor.copy(alpha = 0.4f),
            topLeft = Offset.Zero,
            size = Size(3.dp.toPx(), size.height)
          )
        }
        .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = slot.subject,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = if (isCurrent) accent else ScholaTextPrimary,
          modifier = Modifier.weight(1f)
        )
        if (isCurrent) {
          LiveBadge(color = accent)
        } else {
          Text(
            text = "P${slot.periodNumber}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = ScholaMuted
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = slot.teacherName,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = ScholaTextSecondary
      )
      Spacer(modifier = Modifier.height(7.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.Place,
          contentDescription = "Room",
          tint = if (isCurrent) accent else ScholaMuted,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = slot.roomNumber,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = if (isCurrent) accent else ScholaTextSecondary
        )
      }
    }
  }
}

