package com.example

import com.example.data.datasource.SupabaseConfiguration
import org.junit.Assert.*
import org.junit.Test

class SupabaseConfigurationTest {
  @Test fun acceptsOnlyPublicHttpsConfiguration() {
    SupabaseConfiguration("https://example.supabase.co", "sb_publishable_12345678901234567890").validate()
  }
  @Test fun rejectsPrivateKeysAndUnsafeUrls() {
    listOf("", "sb_secret_private", "service_role", "eyJhbGciOiJIUzI1NiJ9").forEach {
      assertTrue(runCatching { SupabaseConfiguration("https://example.supabase.co", it).validate() }.isFailure)
    }
    listOf("", "http://example.com", "https://user:pass@example.com", "https://example.com?key=x").forEach {
      assertTrue(runCatching { SupabaseConfiguration(it, "sb_publishable_12345678901234567890").validate() }.isFailure)
    }
  }
  @Test fun configuredSerializerAcceptsRelationalColumnsOutsideUiPayload() {
    val serializer=io.github.jan.supabase.serializer.KotlinXSerializer(com.example.data.datasource.SupabaseProvider.json)
    val row=serializer.decode<com.example.data.datasource.SupabaseRow>(
      kotlin.reflect.typeOf<com.example.data.datasource.SupabaseRow>(),
      """{"id":"demo","owner_id":"ignored-relational-column","payload":{"title":"From database"}}"""
    )
    assertEquals("demo",row.id)
    assertEquals("From database",row.payload["title"].toString().trim('"'))
  }
}
