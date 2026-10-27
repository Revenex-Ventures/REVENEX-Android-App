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
 val margin=20.dp;val section=24.dp;val gap=12.dp;val radius=12.dp
 val iconWell=40.dp;val moduleHeight=88.dp
 const val pressMillis=100;const val selectionMillis=160;const val navigationMillis=200
}
enum class StudentTone { ORANGE, MINT, BLUE, LAVENDER, NEUTRAL, ERROR }
object StudentColors {
 private val dark:Boolean @Composable get()=LocalStudentDark.current
 val action:Color @Composable get()=if(dark)StudentDarkPalette.orange else TeacherColors.pressed
 /** Orange text shades meet AA on the cool canvas and peach respectively; icon/fill hues stay unchanged. */
 val actionText:Color @Composable get()=if(dark)StudentDarkPalette.orange else Color(0xFFC24807)
 val onOrange:Color @Composable get()=if(dark)StudentDarkPalette.ink else Color(0xFFA33B07)
 val selection:Color @Composable get()=if(dark)StudentDarkPalette.orange else TeacherColors.orange
 val onSelection:Color @Composable get()=if(dark)StudentDarkPalette.ink else Color(0xFF241000)
 val onAction:Color @Composable get()=if(dark)StudentDarkPalette.ink else Color.White
 @Composable fun colors(tone:StudentTone):Pair<Color,Color> = when(tone) {
  StudentTone.ORANGE->if(dark)StudentDarkPalette.orange to StudentDarkPalette.ink else TeacherColors.peach to TeacherColors.orange
  StudentTone.MINT->if(dark)StudentDarkPalette.mint to StudentDarkPalette.ink else TeacherColors.mint to Color(0xFF006C65)
  StudentTone.BLUE->if(dark)StudentDarkPalette.blue to StudentDarkPalette.ink else TeacherColors.blue to Color(0xFF146AC6)
  StudentTone.LAVENDER->if(dark)StudentDarkPalette.blue to StudentDarkPalette.ink else TeacherColors.lavender to Color(0xFF7653BD)
  StudentTone.ERROR->if(dark)StudentDarkPalette.orange to StudentDarkPalette.ink else MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
  else->MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
 }
}

/** Resolved Student-only appearance; never inferred from a nested colored card. */
val LocalStudentDark = staticCompositionLocalOf { false }
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
