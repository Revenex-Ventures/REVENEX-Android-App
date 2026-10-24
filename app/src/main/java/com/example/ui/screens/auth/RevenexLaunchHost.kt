package com.example.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.core.view.WindowCompat
import com.example.R
import com.example.ui.components.rememberReducedMotion
import com.example.ui.theme.EntryTokens
import java.util.concurrent.atomic.AtomicBoolean

object LaunchLifetime {
  private val claimed=AtomicBoolean(false)
  fun claim(): Boolean=claimed.compareAndSet(false,true)
}

/** Opaque brand stage, then content fade. Outgoing letters never blend over login. */
@Composable
fun RevenexLaunchHost(showBrand: Boolean,nativeSplashExited: Boolean,
  reducedMotion: Boolean=rememberReducedMotion(),content: @Composable ()->Unit) {
  var finished by remember {mutableStateOf(!showBrand)}
  val progress=remember {Animatable(0f)}
  val contentAlpha=remember {Animatable(if(showBrand)0f else 1f)}
  val view=LocalView.current
  val lightBars=finished && MaterialTheme.colorScheme.background.luminance()>.5f
  SideEffect {(view.context as? android.app.Activity)?.let {activity->
    WindowCompat.getInsetsController(activity.window,view).apply {
      isAppearanceLightStatusBars=lightBars;isAppearanceLightNavigationBars=lightBars
    }
  }}
  LaunchedEffect(showBrand,nativeSplashExited,reducedMotion) {
    if(showBrand && nativeSplashExited && !finished) {
      if(!reducedMotion)progress.animateTo(1f,tween(EntryTokens.brandDuration,easing=FastOutSlowInEasing))
      finished=true
      if(reducedMotion)contentAlpha.snapTo(1f) else contentAlpha.animateTo(1f,tween(EntryTokens.contentFade))
    }
  }
  Box(Modifier.fillMaxSize().background(EntryTokens.launchBackground)) {
    Box(Modifier.fillMaxSize().alpha(contentAlpha.value).then(if(!finished)Modifier.clearAndSetSemantics{} else Modifier)) {content()}
    if(!finished) {
      val p=progress.value
      val reveal=if(reducedMotion)1f else ((p-.12f)/.56f).coerceIn(0f,1f)
      val logoOpacity=if(p<.88f)1f else ((1f-p)/.12f).coerceIn(0f,1f)
      Box(Modifier.fillMaxSize().background(EntryTokens.launchBackground)
        .pointerInput(Unit){awaitPointerEventScope {while(true)awaitPointerEvent().changes.forEach {it.consume()}}}
        .testTag("revenex_brand_intro"),contentAlignment=Alignment.Center) {
        Box(Modifier.fillMaxWidth().height(EntryTokens.brandStageHeight).alpha(logoOpacity),contentAlignment=Alignment.Center) {
          Image(painterResource(R.drawable.revenex_monogram),"Revenex RV logo",
            Modifier.width(EntryTokens.markWidth).aspectRatio(184f/135f).offset(y=-EntryTokens.markTravel*reveal))
          Box(Modifier.fillMaxWidth(EntryTokens.wordmarkFraction).widthIn(max=EntryTokens.wordmarkMaxWidth)
            .offset(y=EntryTokens.wordmarkOffset).aspectRatio(490f/54f).clipToBounds()) {
            androidx.compose.material3.Icon(painterResource(R.drawable.revenex_wordmark),"REVENEX",
              Modifier.fillMaxSize().alpha(reveal).testTag("brand_wordmark"),tint=EntryTokens.brandWhite)
          }
        }
      }
    }
  }
}
