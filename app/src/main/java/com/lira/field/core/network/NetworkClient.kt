package com.lira.field.core.network

import com.lira.field.core.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    val moshi: Moshi = Moshi.Builder()
        .add(AssignmentStatusV13::class.java, EnumJsonAdapter.create(AssignmentStatusV13::class.java).withUnknownFallback(AssignmentStatusV13.UNKNOWN))
        .add(AssignmentKind::class.java, EnumJsonAdapter.create(AssignmentKind::class.java).withUnknownFallback(AssignmentKind.UNKNOWN))
        .add(WhyOrigin::class.java, EnumJsonAdapter.create(WhyOrigin::class.java).withUnknownFallback(WhyOrigin.UNKNOWN))
        .add(DriverStatusV13::class.java, EnumJsonAdapter.create(DriverStatusV13::class.java).withUnknownFallback(DriverStatusV13.UNKNOWN))
        .add(InterchangeStateV13::class.java, EnumJsonAdapter.create(InterchangeStateV13::class.java).withUnknownFallback(InterchangeStateV13.UNKNOWN))
        .add(CallStatusV13::class.java, EnumJsonAdapter.create(CallStatusV13::class.java).withUnknownFallback(CallStatusV13.UNKNOWN))
        .add(CallDirectionV13::class.java, EnumJsonAdapter.create(CallDirectionV13::class.java).withUnknownFallback(CallDirectionV13.UNKNOWN))
        .add(CallVerificationV13::class.java, EnumJsonAdapter.create(CallVerificationV13::class.java).withUnknownFallback(CallVerificationV13.UNKNOWN))
        .add(BayRowStatusV13::class.java, EnumJsonAdapter.create(BayRowStatusV13::class.java).withUnknownFallback(BayRowStatusV13.UNKNOWN))
        .add(PnrStatus::class.java, EnumJsonAdapter.create(PnrStatus::class.java).withUnknownFallback(PnrStatus.UNKNOWN))
        .add(DecisionOrigin::class.java, EnumJsonAdapter.create(DecisionOrigin::class.java).withUnknownFallback(DecisionOrigin.UNKNOWN))
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun createOkHttpClient(
        tokenProvider: () -> String?,
        apiKeyProvider: () -> String? = { com.lira.field.BuildConfig.SUPABASE_KEY }
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
            redactHeader("Authorization")
            redactHeader("apikey")
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider, apiKeyProvider))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    fun createRetrofit(baseUrl: String, okHttpClient: OkHttpClient): Retrofit {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun createApiService(
        baseUrl: String,
        tokenProvider: () -> String?,
        apiKeyProvider: () -> String? = { com.lira.field.BuildConfig.SUPABASE_KEY }
    ): ApiService {
        val client = createOkHttpClient(tokenProvider, apiKeyProvider)
        return createRetrofit(baseUrl, client).create(ApiService::class.java)
    }

    fun parseError(errorBody: String?): ApiErrorDetail {
        if (errorBody.isNullOrBlank()) {
            return ApiErrorDetail(code = "UNKNOWN_ERROR", message = "An unexpected error occurred.")
        }
        return try {
            val adapter = moshi.adapter(ApiErrorResponse::class.java)
            val response = adapter.fromJson(errorBody)
            response?.error ?: ApiErrorDetail(code = "PARSING_ERROR", message = errorBody)
        } catch (_: Exception) {
            ApiErrorDetail(code = "HTTP_ERROR", message = errorBody)
        }
    }
}

