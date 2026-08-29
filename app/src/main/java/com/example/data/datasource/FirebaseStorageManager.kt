package com.example.data.datasource

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Handles real binary file uploads to Firebase Storage (honest persistence, never faked).
 * Files are stored under schools/{schoolId}/{folder}/ with a timestamped name so that
 * re-uploads of the same logical file create distinct objects rather than silently
 * replacing prior uploads.
 *
 * Upload requires Google Firebase configuration at runtime. When unavailable (e.g. a
 * build without google-services.json) the call throws; callers surface the error to the
 * user instead of pretending the upload succeeded.
 */
object FirebaseStorageManager {

  private const val TAG = "FirebaseStorageManager"

  data class UploadResult(
    val name: String,
    val mimeType: String,
    val url: String,
    val sizeBytes: Long
  )

  fun isConfigured(): Boolean =
    try {
      FirebaseStorage.getInstance()
      true
    } catch (e: Exception) {
      false
    }

  @Throws(Exception::class)
  suspend fun uploadFile(
    context: Context,
    schoolId: String,
    folder: String,
    sourceUri: Uri,
    displayName: String? = null,
    mimeType: String = "application/pdf"
  ): UploadResult {
    return withContext(Dispatchers.IO) {
      val resolver = context.contentResolver
      val name = buildStoredName(displayName, mimeType)
      val storageRef: StorageReference = FirebaseStorage.getInstance()
        .getReference("schools/$schoolId/$folder/$name")

      // Resolve a stable local temp file from the content URI.
      val tempFile = File.createTempFile("upload_", extensionFor(mimeType), context.cacheDir)
      resolver.openInputStream(sourceUri)?.use { input ->
        FileOutputStream(tempFile).use { output ->
          input.copyTo(output)
        }
      } ?: throw IllegalStateException("Could not open the selected file.")

      val bytes = tempFile.length()
      storageRef.putFile(Uri.fromFile(tempFile)).await()
      val url = storageRef.downloadUrl.await().toString()
      tempFile.delete()
      UploadResult(name = name, mimeType = mimeType, url = url, sizeBytes = bytes)
    }
  }

  private fun buildStoredName(displayName: String?, mimeType: String): String {
    val base = displayName
      ?.trim()
      ?.replace(Regex("[^A-Za-z0-9._-]"), "_")
      ?.take(80)
      ?: "file"
    val stamp = System.currentTimeMillis()
    return "${stamp}_$base.${extensionFor(mimeType)}"
  }

  private fun extensionFor(mimeType: String): String = when (mimeType.lowercase()) {
    "application/pdf" -> "pdf"
    "application/msword" -> "doc"
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
    "application/vnd.ms-excel" -> "xls"
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
    "image/png" -> "png"
    "image/jpeg" -> "jpg"
    "video/mp4" -> "mp4"
    "video/x-msvideo" -> "avi"
    else -> "bin"
  }

  /** Returns true when the MIME type is an allowed document/video type. */
  fun isAllowedDocumentType(mimeType: String): Boolean = when (mimeType.lowercase()) {
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> true
    else -> false
  }

  fun isAllowedVideoType(mimeType: String): Boolean = when (mimeType.lowercase()) {
    "video/mp4", "video/x-msvideo", "video/quicktime" -> true
    else -> false
  }
}
