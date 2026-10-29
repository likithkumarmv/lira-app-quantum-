package com.lira.field.core.network

import com.lira.field.core.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Development & Test Mode Authentication
    @POST("auth/demo-login")
    suspend fun demoLogin(
        @Body request: DemoLoginRequest
    ): Response<DemoLoginResponse>

    // Supabase Production Auth Flow
    @POST("token?grant_type=password")
    suspend fun supabasePasswordLogin(
        @Body request: SupabaseTokenRequest
    ): Response<SupabaseAuthResponse>

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>

    @GET("driver/health")
    suspend fun checkDriverHealth(): Response<HealthResponse>

    @GET("driver/assignments")
    suspend fun getAssignments(): Response<AssignmentsResponse>

    @GET("driver/assignments")
    suspend fun getAssignmentsV13(): Response<AssignmentsResponseV13>

    @POST("driver/assignments/{assignment_id}/ack")
    suspend fun acknowledgeAssignment(
        @Path("assignment_id") assignmentId: String,
        @Header("Idempotency-Key") idempotencyKey: String? = null,
        @Body request: AckRequest
    ): Response<AckResponse>

    @POST("driver/eta")
    suspend fun reportEta(
        @Header("Idempotency-Key") idempotencyKey: String? = null,
        @Body request: EtaReportRequest
    ): Response<EtaReportResponse>

    @POST("driver/assignments/{assignment_id}/eta")
    suspend fun reportAssignmentEta(
        @Path("assignment_id") assignmentId: String,
        @Header("Idempotency-Key") idempotencyKey: String? = null,
        @Body request: EtaReportRequest
    ): Response<EtaReportResponse>

    @Multipart
    @POST("driver/assignments/{assignment_id}/voice-report")
    suspend fun uploadVoiceReport(
        @Path("assignment_id") assignmentId: String,
        @Part audio: MultipartBody.Part,
        @Part("idempotency_key") idempotencyKey: RequestBody,
        @Part("plan_version") planVersion: RequestBody,
        @Part("client_recorded_at") clientRecordedAt: RequestBody
    ): Response<VoiceReport>

    @POST("driver/voice-reports/{report_id}/confirm")
    suspend fun confirmVoiceReport(
        @Path("report_id") reportId: String,
        @Body request: VoiceReportConfirmRequest
    ): Response<VoiceReport>

    @GET("driver/calls")
    suspend fun getCalls(): Response<List<CallV13>>

    @GET("driver/bay-board")
    suspend fun getBayBoard(
        @Query("site_id") siteId: String = "JNPT-BAY-04"
    ): Response<BayBoard>

    @GET("driver/interchanges")
    suspend fun getInterchanges(): Response<List<Interchange>>

    @GET("driver/assignments/{assignment_id}/why")
    suspend fun getWhySheet(
        @Path("assignment_id") assignmentId: String
    ): Response<WhySheet>

    @PUT("driver/profile")
    suspend fun updateProfile(
        @Body request: ProfileUpdateRequest
    ): Response<Unit>

    @POST("driver/events/receipt")
    suspend fun postReceipt(
        @Body receipt: Map<String, Any>
    ): Response<Unit>

    @GET("driver/disruptions")
    suspend fun getDisruptions(): Response<DisruptionsResponse>

    @GET("driver/events")
    suspend fun getEvents(
        @Query("after") after: Long = 0
    ): Response<DriverEventsResponse>

    @GET("driver/checkpoint-status")
    suspend fun getCheckpointStatus(
        @Query("assignment_id") assignmentId: String
    ): Response<CheckpointStatus>

    @POST("driver/verify-qr-custody")
    suspend fun verifyQrCustody(
        @Body request: VerifyQrCustodyRequest
    ): Response<VerifyQrCustodyResponse>
}
