package com.lira.field.core.model

import com.squareup.moshi.Json

/**
 * Bolna AI Calling Agent Field State
 * Backend-orchestrated voice verification workflow.
 * STRICT SECURITY: Zero API keys or agent secrets in Android source code.
 */
data class BolnaCallVerification(
    @Json(name = "call_execution_id") val callExecutionId: String = "",
    @Json(name = "caller_mask") val callerMask: String = "",
    @Json(name = "intent_headline") val intentHeadline: String = "—",
    @Json(name = "verification_target") val verificationTarget: String = "—",
    @Json(name = "deadline") val deadline: String = "—",
    @Json(name = "status") val status: String = BolnaCallStatus.SCHEDULED,
    @Json(name = "scheduled_time") val scheduledTime: String = "—",
    @Json(name = "transcript_snippet") val transcriptSnippet: String? = null,
    @Json(name = "driver_consent_given") val driverConsentGiven: Boolean = true,
    @Json(name = "is_awaiting_verification") val isAwaitingVerification: Boolean = true,
    @Json(name = "provenance_tag") val provenanceTag: String = "—"
)

object BolnaCallStatus {
    const val PENDING_AUTHORIZATION = "PENDING_AUTHORIZATION"
    const val SCHEDULED = "SCHEDULED"
    const val DIALING = "DIALING"
    const val RINGING = "RINGING"
    const val COMPLETED = "COMPLETED"
    const val NO_ANSWER = "NO_ANSWER"
    const val FAILED = "FAILED"
    const val DECLINED = "DECLINED"
    const val NEEDS_VERIFICATION = "NEEDS_VERIFICATION"
    const val CALLING_AGENT_NOT_CONFIGURED = "CALLING_AGENT_NOT_CONFIGURED"

    fun formatStatus(status: String): String = when (status) {
        PENDING_AUTHORIZATION -> "Pending Control Tower Authorization"
        SCHEDULED -> "Scheduled (Incoming Call Ready)"
        DIALING -> "Dialing Driver Handset..."
        RINGING -> "Incoming Call Ringing..."
        COMPLETED -> "Call Completed (Voice Verified)"
        NO_ANSWER -> "Driver Unreachable (No Answer)"
        FAILED -> "Call Failed / Network Error"
        DECLINED -> "Call Declined by Operator"
        NEEDS_VERIFICATION -> "Pending Secondary Attestation"
        CALLING_AGENT_NOT_CONFIGURED -> "Calling Agent Not Configured on Server"
        else -> status
    }
}

