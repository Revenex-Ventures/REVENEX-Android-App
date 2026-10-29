package com.example.ui.screens.students.portal

import android.content.Context
import android.media.*
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.R
import com.example.ui.theme.StudentExperience

/** Optional local confirmation only, after an awaited successful action. No page/tap audio. */
internal fun studentConfirmedFeedback(context:Context,view:View,preference:StudentExperience?) {
 runCatching {
 if(preference?.haptics==true)view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
 if(preference?.sounds!=true)return
 val manager=context.getSystemService(AudioManager::class.java)?:return
 if(manager.ringerMode!=AudioManager.RINGER_MODE_NORMAL||manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)==0)return
 val attributes=AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
 var releasePlayback:()->Unit={}
 val listener=AudioManager.OnAudioFocusChangeListener{if(it<0)releasePlayback()}
 val focus=if(android.os.Build.VERSION.SDK_INT>=26)AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(attributes).setOnAudioFocusChangeListener(listener).build()else null
 val granted=if(android.os.Build.VERSION.SDK_INT>=26)manager.requestAudioFocus(requireNotNull(focus))else manager.requestAudioFocus(listener,AudioManager.STREAM_NOTIFICATION,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
 if(granted!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED)return
 fun abandon(){if(android.os.Build.VERSION.SDK_INT>=26)manager.abandonAudioFocusRequest(requireNotNull(focus))else manager.abandonAudioFocus(listener)}
 runCatching{
  val player=MediaPlayer();var released=false
  fun release(){if(!released){released=true;player.release();abandon()}}
  releasePlayback={release()}
  try{
  player.setAudioAttributes(attributes)
  context.resources.openRawResourceFd(R.raw.student_confirm).use{player.setDataSource(it.fileDescriptor,it.startOffset,it.length)}
  player.setOnCompletionListener{release()};player.setOnErrorListener{_,_,_->release();true}
  player.prepare();player.setVolume(.25f,.25f);player.start()}catch(_:Exception){release()}
 }.onFailure{abandon()}
 }
}
