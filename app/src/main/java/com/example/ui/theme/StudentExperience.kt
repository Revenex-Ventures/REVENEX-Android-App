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
 private val dark:Boolean @Composable get()=MaterialTheme.colorScheme.background.luminance()<.5f
 val action:Color @Composable get()=if(dark)Color(0xFFFFB581)else TeacherColors.pressed
 /** Orange text shades meet AA on the cool canvas and peach respectively; icon/fill hues stay unchanged. */
 val actionText:Color @Composable get()=if(dark)Color(0xFFFFB581)else Color(0xFFC24807)
 val onOrange:Color @Composable get()=if(dark)Color(0xFFFFB581)else Color(0xFFA33B07)
 val selection:Color @Composable get()=if(dark)Color(0xFFFFB581)else TeacherColors.orange
 val onSelection:Color @Composable get()=if(dark)Color(0xFF3E1900)else Color(0xFF241000)
 val onAction:Color @Composable get()=if(dark)Color(0xFF3E1900)else Color.White
 @Composable fun colors(tone:StudentTone):Pair<Color,Color> = when(tone) {
  StudentTone.ORANGE->TeacherColors.peach to if(dark)Color(0xFFFFB581)else TeacherColors.orange
  StudentTone.MINT->TeacherColors.mint to if(dark)Color(0xFF8EDACB)else Color(0xFF006C65)
  StudentTone.BLUE->TeacherColors.blue to if(dark)Color(0xFFA9C9FF)else Color(0xFF146AC6)
  StudentTone.LAVENDER->TeacherColors.lavender to if(dark)Color(0xFFCDBBFF)else Color(0xFF7653BD)
  StudentTone.ERROR->MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
  else->MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
 }
}
