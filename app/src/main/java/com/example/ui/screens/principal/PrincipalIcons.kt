package com.example.ui.screens.principal

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp

/** Consistent line glyphs following the six approved School shortcuts. */
internal object PrincipalIcons {
 private fun icon(name:String,draw:PathBuilder.()->Unit)=ImageVector.Builder(name,24.dp,24.dp,24f,24f).apply {
  path(fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.8f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round,pathBuilder=draw)
 }.build()
 private fun PathBuilder.circle(x:Float,y:Float,r:Float) {
  val k=r*.5523f;moveTo(x+r,y);curveTo(x+r,y+k,x+k,y+r,x,y+r);curveTo(x-k,y+r,x-r,y+k,x-r,y);curveTo(x-r,y-k,x-k,y-r,x,y-r);curveTo(x+k,y-r,x+r,y-k,x+r,y);close()
 }
 val Students=icon("PrincipalStudents") {circle(9f,7f,3f);circle(18f,8f,2.4f);moveTo(2f,21f);lineTo(2f,18f);curveTo(2f,12f,16f,12f,16f,18f);lineTo(16f,21f);close();moveTo(17f,14f);curveTo(21f,14f,23f,16f,23f,19f);lineTo(23f,21f);lineTo(19f,21f)}
 val Staff=icon("PrincipalStaff") {moveTo(3f,8f);lineTo(21f,8f);lineTo(21f,21f);lineTo(3f,21f);close();moveTo(8f,8f);lineTo(8f,4f);lineTo(16f,4f);lineTo(16f,8f);moveTo(3f,13f);lineTo(21f,13f);moveTo(12f,12f);lineTo(12f,16f)}
 val Attendance=icon("PrincipalAttendance") {moveTo(4f,3f);lineTo(20f,3f);lineTo(21f,4f);lineTo(21f,20f);lineTo(20f,21f);lineTo(4f,21f);lineTo(3f,20f);lineTo(3f,4f);close();moveTo(7f,12f);lineTo(11f,16f);lineTo(18f,8f)}
 val Academics=icon("PrincipalAcademics") {moveTo(12f,5f);curveTo(8f,3f,5f,3f,1f,5f);lineTo(1f,21f);curveTo(5f,19f,8f,19f,12f,21f);curveTo(16f,19f,19f,19f,23f,21f);lineTo(23f,5f);curveTo(19f,3f,16f,3f,12f,5f);lineTo(12f,21f)}
 val Fees=icon("PrincipalFees") {moveTo(3f,7f);lineTo(19f,2f);lineTo(19f,7f);moveTo(3f,7f);lineTo(21f,7f);lineTo(22f,8f);lineTo(22f,21f);lineTo(3f,21f);lineTo(2f,20f);lineTo(2f,8f);close();moveTo(22f,12f);lineTo(16f,12f);lineTo(16f,17f);lineTo(22f,17f);circle(18.5f,14.5f,.3f)}
 val Reports=icon("PrincipalReports") {moveTo(2f,14f);lineTo(6f,14f);lineTo(6f,22f);lineTo(2f,22f);close();moveTo(10f,8f);lineTo(14f,8f);lineTo(14f,22f);lineTo(10f,22f);close();moveTo(18f,2f);lineTo(22f,2f);lineTo(22f,22f);lineTo(18f,22f);close()}
}
