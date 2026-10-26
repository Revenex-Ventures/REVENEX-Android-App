package com.example.data.datasource

/** Shared document metadata and picker validation; storage is supplied by Supabase. */
object DocumentFiles {
  data class UploadResult(val name: String, val mimeType: String, val url: String, val sizeBytes: Long)
  fun isAllowedDocumentType(mimeType: String): Boolean = mimeType.lowercase() in setOf(
    "application/pdf", "image/jpeg", "image/png",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  )
}
