package com.example.data.datasource

import kotlinx.serialization.Serializable

/** Availability metadata only. These flags never authorize a role or replace RLS/RPC checks. */
@Serializable
data class SchoolBackendCapabilities(
 val loaded:Boolean=false,
 val schema_version:Int=0,
 val assignments:Boolean=false,
 val submissions:Boolean=false,
 val marks:Boolean=false,
 val targeted_notices:Boolean=false,
 val attendance:Boolean=false,
 val leaves:Boolean=false,
 val attendance_summaries:Boolean=false,
 val result_batches:Boolean=false
)
