package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme

/** Public presentation preferences only; no account, credentials or school data. */
class StudentExperience(context:Context) {
 private val preferences=context.applicationContext.getSharedPreferences("revenex_student_experience",Context.MODE_PRIVATE)
 var reduced by mutableStateOf(preferences.getBoolean("reduced",false));private set
 var sounds by mutableStateOf(preferences.getBoolean("sounds",false));private set
 var haptics by mutableStateOf(preferences.getBoolean("haptics",false));private set
 fun reduce(value:Boolean){reduced=value;preferences.edit().putBoolean("reduced",value).apply()}
 fun sound(value:Boolean){sounds=value;preferences.edit().putBoolean("sounds",value).apply()}
 fun haptic(value:Boolean){haptics=value;preferences.edit().putBoolean("haptics",value).apply()}
}
val LocalStudentExperience=staticCompositionLocalOf<StudentExperience?>{null}
@Composable fun rememberStudentExperience():StudentExperience {
 val context=LocalContext.current.applicationContext
 return remember(context){StudentExperience(context)}
}
object StudentLayout {
 val margin:androidx.compose.ui.unit.Dp @Composable get()=if(LocalStudentLight.current)StudentLightLayout.margin else 20.dp
 val section:androidx.compose.ui.unit.Dp @Composable get()=if(LocalStudentLight.current)StudentLightLayout.section else 24.dp
 val gap=12.dp;val radius=12.dp
 val iconWell=40.dp;val moduleHeight=88.dp
 const val pressMillis=100;const val selectionMillis=160;const val navigationMillis=200
}
enum class StudentTone { ORANGE, MINT, BLUE, LAVENDER, NEUTRAL, ERROR }
object StudentColors {
 private val dark:Boolean @Composable get()=LocalStudentDark.current
 val action:Color @Composable get()=if(dark)StudentDarkPalette.orange else StudentLightPalette.orange
 /** Orange text shades meet AA on the cool canvas and peach respectively; icon/fill hues stay unchanged. */
 val actionText:Color @Composable get()=if(dark)StudentDarkPalette.orange else StudentLightPalette.actionText
 val onOrange:Color @Composable get()=if(dark)StudentDarkPalette.ink else StudentLightPalette.actionText
 val selection:Color @Composable get()=if(dark)StudentDarkPalette.orange else StudentLightPalette.navy
 val onSelection:Color @Composable get()=if(dark)StudentDarkPalette.ink else Color.White
 val onAction:Color @Composable get()=if(dark)StudentDarkPalette.ink else StudentLightPalette.onAction
 @Composable fun colors(tone:StudentTone):Pair<Color,Color> = when(tone) {
  StudentTone.ORANGE->if(dark)StudentDarkPalette.orange to StudentDarkPalette.ink else StudentLightPalette.highlight to StudentLightPalette.actionText
  StudentTone.MINT->if(dark)StudentDarkPalette.mint to StudentDarkPalette.ink else StudentLightPalette.information to StudentLightPalette.navy
  StudentTone.BLUE->if(dark)StudentDarkPalette.blue to StudentDarkPalette.ink else StudentLightPalette.information to StudentLightPalette.navy
  StudentTone.LAVENDER->if(dark)StudentDarkPalette.blue to StudentDarkPalette.ink else StudentLightPalette.quiet to StudentLightPalette.navy
  StudentTone.ERROR->if(dark)StudentDarkPalette.orange to StudentDarkPalette.ink else StudentLightPalette.highlight to StudentLightPalette.actionText
  else->MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
 }
}

/** Resolved Student-only appearance; never inferred from a nested colored card. */
val LocalStudentDark = staticCompositionLocalOf { false }
val LocalStudentLight = staticCompositionLocalOf { false }
object StudentDarkPalette {
 val canvas=Color(0xFF0B1220)
 val surface=Color(0xFF1C2B43)
 val navigation=Color(0xFF162237)
 val blue=Color(0xFF92BAF2)
 val mint=Color(0xFFA6D9BF)
 val orange=Color(0xFFFF743D)
 val ink=Color(0xFF102034)
 val text=Color(0xFFF4F6FB)
 val muted=Color(0xFFB5C5DB)
 val border=Color(0xFF2D405A)
 val radius=22.dp
}

object StudentDarkLayout {
 val bookCover=132.dp
 val dockTarget=56.dp
 val viewportMax=720.dp
 val dockIcon=21.dp
 val featureIcon=28.dp
}
