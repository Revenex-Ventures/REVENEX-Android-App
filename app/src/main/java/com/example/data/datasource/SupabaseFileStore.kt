package com.example.data.datasource

import android.content.Context
import android.net.Uri
import com.example.data.model.UserProfile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.auth
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

object SupabaseFileStore {
  private val allowed = setOf("application/pdf", "image/jpeg", "image/png", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
  suspend fun upload(context:Context,uri:Uri,displayName:String?,mimeType:String,user:UserProfile,session:String):DocumentFiles.UploadResult = withContext(Dispatchers.IO) {
    check(SupabaseProvider.client.auth.currentUserOrNull()?.id==user.id){"School account changed. Choose the file again after signing in."}
    require(mimeType in allowed) { "Choose a PDF, PNG, JPEG or DOCX file." }
    val bytes=context.contentResolver.openInputStream(uri)?.use { input ->
      val out=java.io.ByteArrayOutputStream()
      val buffer=ByteArray(8192)
      while(true) {
        val count=input.read(buffer);if(count<0)break
        require(out.size()+count<=10*1024*1024) { "Files must be 10 MB or smaller." }
        out.write(buffer,0,count)
      }
      out.toByteArray()
    } ?: error("Cannot open the selected file.")
    require(bytes.isNotEmpty()) { "The selected file is empty." }
    val id=UUID.randomUUID().toString()
    val path="${user.schoolId}/$session/${user.id}/$id"
    val name=displayName?.replace(Regex("[^A-Za-z0-9._ -]"),"_")?.take(100)?.ifBlank { "document" } ?: "document"
    val client=SupabaseProvider.client
    // Create the access-controlled manifest first; Storage INSERT checks this exact row.
    client.from("file_metadata").insert(buildJsonObject {
      put("id",id);put("school_id",user.schoolId);put("session_id",session);put("owner_id",user.id)
      put("object_path",path);put("name",name);put("mime_type",mimeType);put("size_bytes",bytes.size)
    })
    try {
      client.storage.from("erp-documents").upload(path,bytes) { contentType=ContentType.parse(mimeType);upsert=false }
    } catch(e:Exception) {
      // Unknown outcomes are safe to retry with a fresh file; cleanup is RLS-limited to unlinked objects.
      kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable){runCatching{
        kotlinx.coroutines.withTimeout(5000){discardStaged("supabase://erp-documents/$path")}
      }}
      throw e
    }
    // Persist a stable reference; sign only when the recipient requests a download.
    DocumentFiles.UploadResult(name,mimeType,"supabase://erp-documents/$path",bytes.size.toLong())
  }
  suspend fun discardStaged(reference:String)=withContext(Dispatchers.IO) {
    val path=requireNotNull(documentPath(reference)){"Invalid school document reference."}
    val client=SupabaseProvider.client
    client.storage.from("erp-documents").delete(path)
    client.from("file_metadata").delete { filter { eq("object_path",path) } }
  }

  internal fun documentPath(reference: String): String? {
    if (!reference.startsWith("supabase://erp-documents/")) return null
    val path=reference.removePrefix("supabase://erp-documents/")
    val segments=path.split('/')
    require(segments.size==4 && segments.take(2).all { it.matches(Regex("[A-Za-z0-9_-]+")) } &&
      segments.drop(2).all { runCatching { UUID.fromString(it) }.isSuccess }) { "Invalid school document reference." }
    return path
  }

  suspend fun resolveUrl(reference: String): String = withContext(Dispatchers.IO) {
    if(reference.isBlank()) return@withContext ""
    val path=documentPath(reference)
    if(path!=null) return@withContext SupabaseProvider.client.storage.from("erp-documents").createSignedUrl(path,5.minutes)
    val uri=runCatching { java.net.URI(reference) }.getOrNull()
    require(uri?.scheme=="https" && !uri.host.isNullOrBlank() && uri.userInfo==null) { "This document link is unavailable." }
    // External HTTPS references remain external. Never sign arbitrary foreign URLs.
    reference
  }
}
