package com.lira.field.core.model

import com.squareup.moshi.Json

data class DemoLoginRequest(
    @Json(name = "user_code") val userCode: String
)

data class DemoLoginResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "role") val role: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "truck_id") val truckId: String
)

data class SupabaseTokenRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

data class SupabaseUser(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String? = null,
    @Json(name = "user_metadata") val userMetadata: Map<String, Any?>? = null
)

data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String = "bearer",
    @Json(name = "expires_in") val expiresIn: Long = 3600,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "user") val user: SupabaseUser? = null
)

data class HealthResponse(
    @Json(name = "status") val status: String = "ok",
    @Json(name = "mode") val mode: String = "LIVE",
    @Json(name = "version") val version: String = "v13.0",
    @Json(name = "schema") val schema: String = "v13.0"
)

data class UserSession(
    val token: String,
    val userCode: String,
    val displayName: String,
    val role: String,
    val truckId: String,
    val expiresAt: Long = System.currentTimeMillis() + (3600 * 1000L),
    val isDemoSession: Boolean = false,
    val email: String? = null
) {
    val driverId: String get() = if (displayName.isNotBlank()) displayName else userCode
}
