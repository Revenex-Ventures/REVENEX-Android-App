package com.example.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.model.MobileRoleAccess
import com.example.ui.components.RevenexBrand
import com.example.ui.theme.*

@Composable
fun UnsupportedMobileAccountScreen(onReturnToSignIn: () -> Unit) {
  Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
    Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(),contentAlignment=Alignment.Center) {
      Column(Modifier.widthIn(max=EntryTokens.formWidth).fillMaxWidth()
        .verticalScroll(rememberScrollState()).padding(Spacing.xl24)
        .testTag("unsupported_mobile_account"),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
        RevenexBrand()
        Text("This account is not supported on Android",style=MaterialTheme.typography.headlineSmall)
        Text(MobileRoleAccess.unsupportedMessage,style=MaterialTheme.typography.bodyLarge,
          color=MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick=onReturnToSignIn,modifier=Modifier.fillMaxWidth().heightIn(min=Spacing.xxxl48)
          .testTag("unsupported_mobile_sign_out")) { Text("Return to sign-in") }
      }
    }
  }
}
