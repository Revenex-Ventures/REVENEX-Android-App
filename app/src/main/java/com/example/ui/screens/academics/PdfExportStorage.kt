package com.example.ui.screens.academics

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.io.OutputStream

/** Saves a completed export without requesting broad storage access. */
internal fun saveSchoolPdf(context: Context, fileName: String, writePdf: (OutputStream) -> Unit): Uri? {
  val safeBase = fileName.removeSuffix(".pdf")
    .replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_").trim().take(100).ifBlank { "school-document" }
  val safeName = "$safeBase.pdf"
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, safeName)
      put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
      put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Revenex")
      put(MediaStore.MediaColumns.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
    try {
      resolver.openOutputStream(uri)?.use(writePdf) ?: throw IOException("Cannot open PDF destination")
      val published = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
      if (resolver.update(uri, published, null, null) == 0) throw IOException("Cannot publish PDF")
      return uri
    } catch (error: Exception) {
      try { resolver.delete(uri, null, null) } catch (cleanup: Exception) { error.addSuppressed(cleanup) }
      throw error
    }
  } else {
    val root = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
    val directory = File(root, "Revenex")
    if (!directory.mkdirs() && !directory.isDirectory) throw IOException("Export folder is unavailable")
    val target = File.createTempFile("${safeBase}_".padEnd(3, '_'), ".pdf", directory)
    try {
      target.outputStream().use(writePdf)
      return Uri.fromFile(target)
    } catch (error: Exception) {
      target.delete()
      throw error
    }
  }
}

internal fun pdfSaveLocationLabel(): String =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "Downloads/Revenex" else "the app's Downloads/Revenex folder"
