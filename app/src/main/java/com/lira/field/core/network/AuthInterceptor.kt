package com.lira.field.core.network

import com.lira.field.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * LIRA v7.0 Network Auth Interceptor
 * Prevents 3-laptop network split-brain:
 * - Injects 'apikey' ONLY when communicating with Supabase Cloud (*.supabase.co).
 * - Connects to authoritative FastAPI Control Tower LAN server (http://<LAN_IP>:8000/api/v1/)
 *   with clean 'Authorization: Bearer <token>' and passes 'Idempotency-Key' without header rejections or CORS preflight failures.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val apiKeyProvider: () -> String? = { BuildConfig.SUPABASE_KEY }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", "LIRA-Field-Android/7.0")

        val host = original.url.host

        // 1. Only inject Supabase apikey header when communicating with Supabase cloud endpoints
        if (host.contains(".supabase.co")) {
            val apiKey = apiKeyProvider()
            if (!apiKey.isNullOrBlank()) {
                builder.header("apikey", apiKey)
            }
        }

        // 2. Inject Authorization: Bearer <token> for authenticated endpoints
        val token = tokenProvider()
        if (!token.isNullOrBlank()) {
            if (original.header("Authorization") == null) {
                builder.header("Authorization", "Bearer $token")
            }
        } else if (host.contains(".supabase.co")) {
            val apiKey = apiKeyProvider()
            if (!apiKey.isNullOrBlank() && original.header("Authorization") == null) {
                if (apiKey.startsWith("ey") && apiKey.count { it == '.' } == 2) {
                    builder.header("Authorization", "Bearer $apiKey")
                }
            }
        }

        val response = chain.proceed(builder.build())
        if (response.code == 401) {
            try {
                com.lira.field.LiraApp.instance.sessionRepository.clearSession()
            } catch (_: Exception) {}
        }
        return response
    }
}
