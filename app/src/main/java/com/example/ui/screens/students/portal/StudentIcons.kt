package com.example.ui.screens.students.portal

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp
import com.example.ui.screens.principal.PrincipalIcons

/** Approved silhouettes, one rounded 1.8-unit line family; color is semantic at call sites. */
internal object StudentIcons {
 private fun glyph(name:String,draw:PathBuilder.()->Unit)=ImageVector.Builder(name,24.dp,24.dp,24f,24f).apply {
  path(fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.8f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round,pathBuilder=draw)
 }.build()
 val Book=PrincipalIcons.Academics
 val Wallet=PrincipalIcons.Fees
 val Results=PrincipalIcons.Reports
 val People=PrincipalIcons.Students
 val Clipboard=glyph("StudentClipboard") {
  moveTo(8f,4f);lineTo(5f,4f);lineTo(4f,5f);lineTo(4f,21f);lineTo(5f,22f);lineTo(19f,22f);lineTo(20f,21f);lineTo(20f,5f);lineTo(19f,4f);lineTo(16f,4f)
  moveTo(9f,2f);lineTo(15f,2f);lineTo(16f,6f);lineTo(8f,6f);close();moveTo(8f,11f);lineTo(16f,11f);moveTo(8f,16f);lineTo(14f,16f)
 }
 val Calendar=glyph("StudentCalendar") {
  moveTo(3f,5f);lineTo(21f,5f);lineTo(22f,6f);lineTo(22f,21f);lineTo(21f,22f);lineTo(3f,22f);lineTo(2f,21f);lineTo(2f,6f);close();moveTo(7f,2f);lineTo(7f,8f);moveTo(17f,2f);lineTo(17f,8f);moveTo(2f,11f);lineTo(22f,11f)
 }
 val Clock=glyph("StudentClock") {
  moveTo(22f,12f);curveTo(22f,25.3f,2f,25.3f,2f,12f);curveTo(2f,-1.3f,22f,-1.3f,22f,12f);close();moveTo(12f,5f);lineTo(12f,12f);lineTo(17f,15f)
 }
 val Document=glyph("StudentDocument") {
  moveTo(5f,2f);lineTo(14f,2f);lineTo(20f,8f);lineTo(20f,22f);lineTo(4f,22f);lineTo(4f,3f);close();moveTo(14f,2f);lineTo(14f,8f);lineTo(20f,8f);moveTo(8f,12f);lineTo(16f,12f);moveTo(8f,17f);lineTo(14f,17f)
 }
}
