package com.example.data.datasource

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class ServerAssistantReply(val answer:String,val source:String)

/** No authorization claims, provider configuration or system prompt can enter this request. */
internal fun assistantRequestBody(message:String,history:List<Pair<String,String>>):JsonObject {
  require(message.isNotBlank())
  require(history.all { it.first in setOf("user","assistant") })
  return buildJsonObject {
    put("message",message.take(4000))
    put("history",buildJsonArray {
      history.takeLast(8).forEach { (role,content) ->
        add(buildJsonObject { put("role",role);put("content",content.take(2000)) })
      }
    })
  }
}

/** Android transports text and the Auth session only. */
object SupabaseAssistantChat {
  suspend fun reply(message:String,history:List<Pair<String,String>>):ServerAssistantReply {
    check(AppEnvironment.currentMode==DataMode.SUPABASE_DEMO)
    try {
      check(SupabaseProvider.client.auth.currentUserOrNull()!=null)
      val response=SupabaseProvider.client.functions.invoke("revenex-ai",
        body=assistantRequestBody(message,history),
        headers=Headers.build { append(HttpHeaders.ContentType,ContentType.Application.Json.toString()) })
      check(response.status.isSuccess())
      return SupabaseProvider.json.decodeFromString<ServerAssistantReply>(response.bodyAsText())
    } catch(e:Exception) {
      if(e is CancellationException)throw e
      return ServerAssistantReply("Live Revenex AI is unavailable. Check school sign-in, server deployment, model configuration or request limits. No school action was performed.","UNAVAILABLE")
    }
  }
}
