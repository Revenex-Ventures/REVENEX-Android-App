package com.example.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.rememberReducedMotion
import java.util.concurrent.atomic.AtomicBoolean

/** Process lifetime only. Never an authentication or persisted-session flag. */
object LaunchLifetime {
  private val claimed = AtomicBoolean(false)
  fun claim(): Boolean = claimed.compareAndSet(false, true)
}

/** Compose the application immediately; only the brand presentation overlays it. */
@Composable
fun RevenexLaunchHost(
  showBrand: Boolean,
  nativeSplashExited: Boolean,
  reducedMotion: Boolean = rememberReducedMotion(),
  content: @Composable () -> Unit
) {
  var finished by remember { mutableStateOf(!showBrand) }
  val progress = remember { Animatable(0f) }
  LaunchedEffect(showBrand, nativeSplashExited, reducedMotion) {
    if (showBrand && nativeSplashExited && !finished) {
      if (!reducedMotion) progress.animateTo(1f, tween(1550, easing=FastOutSlowInEasing))
      // No timer gates app initialization, navigation or authentication.
      finished = true
    }
  }
  Box(Modifier.fillMaxSize()) {
    Box(if (!finished) Modifier.fillMaxSize().clearAndSetSemantics {} else Modifier.fillMaxSize()) { content() }
    if (!finished) {
      val p = progress.value
      val reveal = if (reducedMotion) 1f else ((p-.12f)/.53f).coerceIn(0f,1f)
      val opacity = if (p < .88f) 1f else ((1f-p)/.12f).coerceIn(0f,1f)
      Box(Modifier.fillMaxSize().alpha(opacity).background(MaterialTheme.colorScheme.background)
        .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } }
        .testTag("revenex_brand_intro"),contentAlignment=Alignment.Center) {
        // The native icon's 64dp book occupies the same centered position at p=0.
        Box(Modifier.fillMaxWidth().height(96.dp),contentAlignment=Alignment.Center) {
          Image(painterResource(R.drawable.revenex_launch_book),"Revenex",
            Modifier.offset(x=(-96f*reveal).dp).requiredSize(288.dp),alignment=Alignment.Center)
          Box(Modifier.offset(x=22.dp).width((174f*reveal).dp).height(48.dp).clipToBounds(),contentAlignment=Alignment.CenterStart) {
            Text("REVENEX",Modifier.requiredWidth(174.dp),fontSize=28.sp,letterSpacing=1.sp,
              fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onBackground,maxLines=1)
          }
        }
      }
    }
  }
}
