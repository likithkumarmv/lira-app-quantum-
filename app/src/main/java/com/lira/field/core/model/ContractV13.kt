package com.lira.field.core.model

import com.squareup.moshi.Json

// ---------------------------------------------------------------------------
// CONTRACT v13 ENUMS WITH UNKNOWN FALLBACK
// ---------------------------------------------------------------------------

enum class AssignmentStatusV13 {
    @Json(name = "APPROVED_ACTIVE") APPROVED_ACTIVE,
    @Json(name = "SUPERSEDED") SUPERSEDED,
    @Json(name = "COMPLETED") COMPLETED,
    @Json(name = "CANCELLED") CANCELLED,
    @Json(name = "PROPOSED") PROPOSED,
    UNKNOWN
}

enum class AssignmentKind {
    @Json(name = "ACTIVE") ACTIVE,
    @Json(name = "RESERVE_STANDBY") RESERVE_STANDBY,
    UNKNOWN
}

enum class PnrStatus {
    @Json(name = "ON_TRACK") ON_TRACK,
    @Json(name = "PNR_IMMINENT") PNR_IMMINENT,
    @Json(name = "PAST_SAFE_PNR") PAST_SAFE_PNR,
    @Json(name = "ALREADY_LOST") ALREADY_LOST,
    UNKNOWN
}

enum class WhyOrigin {
    @Json(name = "LIVE_CALL") LIVE_CALL,
    @Json(name = "SIMULATED_CALL") SIMULATED_CALL,
    @Json(name = "COPILOT") COPILOT,
    @Json(name = "STRESS_PROMOTED") STRESS_PROMOTED,
    @Json(name = "DISPATCHER") DISPATCHER,
    @Json(name = "STATION_BRANCH") STATION_BRANCH,
    @Json(name = "STRESS_SANDBOX") STRESS_SANDBOX,
    UNKNOWN
}

enum class DecisionOrigin {
    @Json(name = "SIMULATED_CALL") SIMULATED_CALL,
    @Json(name = "LIVE_CALL") LIVE_CALL,
    @Json(name = "OPTIMIZER") OPTIMIZER,
    @Json(name = "MANUAL") MANUAL,
    UNKNOWN
}

enum class DriverStatusV13 {
    @Json(name = "HOLD_DO_NOT_PROCEED") HOLD_DO_NOT_PROCEED,
    @Json(name = "COMMERCIAL_HOLD") COMMERCIAL_HOLD,
    @Json(name = "ADVISORY_MONITORING") ADVISORY_MONITORING,
    @Json(name = "RESOURCE_ALLOCATED") RESOURCE_ALLOCATED,
    @Json(name = "ACTION_REQUIRED") ACTION_REQUIRED,
    @Json(name = "EMPTY_RETURN_PENDING") EMPTY_RETURN_PENDING,
    UNKNOWN
}

enum class InterchangeStateV13 {
    @Json(name = "NOT_STARTED") NOT_STARTED,
    @Json(name = "TRUCK_OBSERVED") TRUCK_OBSERVED,
    @Json(name = "EQUIPMENT_OBSERVED") EQUIPMENT_OBSERVED,
    @Json(name = "MATCH_PREVIEW") MATCH_PREVIEW,
    @Json(name = "HOLD") HOLD,
    @Json(name = "AWAITING_CUSTODIAN") AWAITING_CUSTODIAN,
    @Json(name = "COMMITTED") COMMITTED,
    @Json(name = "REJECTED") REJECTED,
    @Json(name = "EXPIRED") EXPIRED,
    UNKNOWN
}

enum class CallStatusV13 {
    @Json(name = "PENDING_AUTHORIZATION") PENDING_AUTHORIZATION,
    @Json(name = "QUEUED") QUEUED,
    @Json(name = "RINGING") RINGING,
    @Json(name = "IN_PROGRESS") IN_PROGRESS,
    @Json(name = "COMPLETED") COMPLETED,
    @Json(name = "NO_ANSWER") NO_ANSWER,
    @Json(name = "BUSY") BUSY,
    @Json(name = "FAILED") FAILED,
    UNKNOWN
}

enum class CallDirectionV13 {
    @Json(name = "OUTBOUND") OUTBOUND,
    @Json(name = "INBOUND") INBOUND,
    UNKNOWN
}

enum class CallVerificationV13 {
    @Json(name = "T4_UNVERIFIED") T4_UNVERIFIED,
    @Json(name = "CORROBORATED") CORROBORATED,
    @Json(name = "CONTRADICTED") CONTRADICTED,
    UNKNOWN
}

enum class BayRowStatusV13 {
    @Json(name = "NEXT") NEXT,
    @Json(name = "WAIT") WAIT,
    @Json(name = "AT_BAY") AT_BAY,
    @Json(name = "DONE") DONE,
    @Json(name = "HOLD") HOLD,
    UNKNOWN
}

// ---------------------------------------------------------------------------
// CONTRACT v13 DATA CLASSES
// ---------------------------------------------------------------------------

data class ContainerV13(
    @Json(name = "container_no") val containerNo: String,
    @Json(name = "bl_no") val blNo: String? = null,
    @Json(name = "size_type") val sizeType: String? = null,
    @Json(name = "gross_kg") val grossKg: Long? = null
)

data class FreightCaseV13(
    @Json(name = "case_code") val caseCode: String,
    @Json(name = "blocker_type") val blockerType: String,
    @Json(name = "driver_status") val driverStatus: DriverStatusV13 = DriverStatusV13.ACTION_REQUIRED,
    @Json(name = "owner_role") val ownerRole: String? = null,
    @Json(name = "action_due") val actionDue: String? = null,
    @Json(name = "hard_deadline") val hardDeadline: String? = null
)

data class RouteV13(
    @Json(name = "from_label") val fromLabel: String? = null,
    @Json(name = "to_label") val toLabel: String? = null,
    @Json(name = "eta_minutes") val etaMinutes: Int? = null,
    @Json(name = "eta_source") val etaSource: String? = null,
    @Json(name = "eta_observed_at") val etaObservedAt: String? = null,
    @Json(name = "label") val label: String = "PREDICTED"
)

data class BayV13(
    @Json(name = "site_id") val siteId: String,
    @Json(name = "queue_position") val queuePosition: Int? = null,
    @Json(name = "window_start") val windowStart: String? = null,
    @Json(name = "window_end") val windowEnd: String? = null,
    @Json(name = "chassis_id") val chassisId: String? = null,
    @Json(name = "station_device_id") val stationDeviceId: String? = null,
    @Json(name = "station_online") val stationOnline: Boolean = true
)

data class InstructionV13(
    @Json(name = "lang") val lang: String = "en",
    @Json(name = "text") val text: String,
    @Json(name = "text_en") val textEn: String? = null,
    @Json(name = "source") val source: String? = null
)

data class InterchangeSummaryV13(
    @Json(name = "session_id") val sessionId: String? = null,
    @Json(name = "state") val state: InterchangeStateV13 = InterchangeStateV13.NOT_STARTED,
    @Json(name = "txn_id") val txnId: String? = null,
    @Json(name = "eir_no") val eirNo: String? = null
)

data class CostExposureV13(
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "baseline") val baseline: Long = 0,
    @Json(name = "plan") val plan: Long = 0,
    @Json(name = "avoidable") val avoidable: Long = 0,
    @Json(name = "label") val label: String = "MODELED",
    @Json(name = "tariff_source_ids") val tariffSourceIds: List<String> = emptyList()
)

data class SolverRunRef(
    @Json(name = "run_id") val runId: String,
    @Json(name = "mode") val mode: String? = null,
    @Json(name = "engine") val engine: String? = null,
    @Json(name = "execution_mode") val executionMode: String? = null,
    @Json(name = "job_id") val jobId: String? = null,
    @Json(name = "validator") val validator: String? = null
)

data class ProvenanceV13(
    @Json(name = "solver_runs") val solverRuns: List<SolverRunRef> = emptyList(),
    @Json(name = "evidence_count") val evidenceCount: Int = 0
)

data class ActiveBranch(
    @Json(name = "branch_id") val branchId: String,
    @Json(name = "activated_at") val activatedAt: String? = null,
    @Json(name = "activated_by") val activatedBy: String? = null,
    @Json(name = "trigger") val trigger: String? = null,
    @Json(name = "pre_approved_by") val preApprovedBy: String? = null,
    @Json(name = "pre_approved_at") val preApprovedAt: String? = null,
    @Json(name = "provisional") val provisional: Boolean = false
)

data class AssignmentV13(
    @Json(name = "assignment_id") val assignmentId: String,
    @Json(name = "plan_id") val planId: String,
    @Json(name = "plan_version") val planVersion: Int = 1,
    @Json(name = "status") val status: AssignmentStatusV13 = AssignmentStatusV13.APPROVED_ACTIVE,
    @Json(name = "assignment_kind") val assignmentKind: AssignmentKind = AssignmentKind.ACTIVE,
    @Json(name = "origin") val origin: WhyOrigin? = null,
    @Json(name = "pnr") val pnr: String? = null,
    @Json(name = "act_by") val actBy: String? = null,
    @Json(name = "pnr_status") val pnrStatus: PnrStatus? = PnrStatus.ON_TRACK,
    @Json(name = "superseded_by") val supersededBy: String? = null,
    @Json(name = "active_branch") val activeBranch: ActiveBranch? = null,
    @Json(name = "driver_id") val driverId: String,
    @Json(name = "truck_id") val truckId: String,
    @Json(name = "container") val container: ContainerV13,
    @Json(name = "freight_case") val freightCase: FreightCaseV13,
    @Json(name = "route") val route: RouteV13? = null,
    @Json(name = "bay") val bay: BayV13? = null,
    @Json(name = "instruction") val instruction: InstructionV13? = null,
    @Json(name = "interchange") val interchange: InterchangeSummaryV13? = null,
    @Json(name = "cost_exposure") val costExposure: CostExposureV13? = null,
    @Json(name = "provenance") val provenance: ProvenanceV13? = null,
    @Json(name = "issued_at") val issuedAt: String? = null
)

data class VoiceInterpretation(
    @Json(name = "reason_code") val reasonCode: String? = null,
    @Json(name = "eta_minutes") val etaMinutes: Int? = null,
    @Json(name = "confidence") val confidence: Double? = null
)

data class VoiceReport(
    @Json(name = "report_id") val reportId: String,
    @Json(name = "status") val status: String = "TRANSCRIBED",
    @Json(name = "language_detected") val languageDetected: String? = null,
    @Json(name = "transcript_original") val transcriptOriginal: String? = null,
    @Json(name = "transcript_en") val transcriptEn: String? = null,
    @Json(name = "interpretation") val interpretation: VoiceInterpretation? = null,
    @Json(name = "evidence_tier") val evidenceTier: String = "T4_DRIVER_STATEMENT",
    @Json(name = "provider") val provider: String = "SARVAM",
    @Json(name = "provider_request_id") val providerRequestId: String? = null
)

data class CallExtracted(
    @Json(name = "answer") val answer: String? = null,
    @Json(name = "eta_minutes") val etaMinutes: Int? = null,
    @Json(name = "reason_code") val reasonCode: String? = null,
    @Json(name = "confidence") val confidence: Double? = null
)

data class CallV13(
    @Json(name = "call_id") val callId: String,
    @Json(name = "provider") val provider: String = "BOLNA",
    @Json(name = "execution_id") val executionId: String? = null,
    @Json(name = "agent_id_masked") val agentIdMasked: String? = null,
    @Json(name = "direction") val direction: CallDirectionV13 = CallDirectionV13.OUTBOUND,
    @Json(name = "language") val language: String? = null,
    @Json(name = "purpose") val purpose: String? = null,
    @Json(name = "question") val question: String? = null,
    @Json(name = "status") val status: CallStatusV13 = CallStatusV13.QUEUED,
    @Json(name = "from_number_masked") val fromNumberMasked: String? = null,
    @Json(name = "started_at") val startedAt: String? = null,
    @Json(name = "ended_at") val endedAt: String? = null,
    @Json(name = "transcript_excerpt_original") val transcriptExcerptOriginal: String? = null,
    @Json(name = "transcript_excerpt_en") val transcriptExcerptEn: String? = null,
    @Json(name = "extracted") val extracted: CallExtracted? = null,
    @Json(name = "verification") val verification: CallVerificationV13 = CallVerificationV13.T4_UNVERIFIED
)

data class BayBoardRow(
    @Json(name = "seq") val seq: Int,
    @Json(name = "truck_id") val truckId: String,
    @Json(name = "container_no") val containerNo: String,
    @Json(name = "chassis_id") val chassisId: String,
    @Json(name = "window_start") val windowStart: String,
    @Json(name = "window_end") val windowEnd: String,
    @Json(name = "status") val status: BayRowStatusV13 = BayRowStatusV13.WAIT
)

data class NextCliff(
    @Json(name = "container_no") val containerNo: String,
    @Json(name = "kind") val kind: String,
    @Json(name = "at") val at: String
)

data class BayBoard(
    @Json(name = "site_id") val siteId: String,
    @Json(name = "generated_at") val generatedAt: String? = null,
    @Json(name = "plan_version") val planVersion: Int? = null,
    @Json(name = "solver_run_id") val solverRunId: String? = null,
    @Json(name = "validator") val validator: String? = null,
    @Json(name = "qaoa_shadow") val qaoaShadow: String? = null,
    @Json(name = "rows") val rows: List<BayBoardRow> = emptyList(),
    @Json(name = "next_cliff") val nextCliff: NextCliff? = null
)

data class Interchange(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "site_id") val siteId: String,
    @Json(name = "truck_id") val truckId: String,
    @Json(name = "truck_tag_ref_masked") val truckTagRefMasked: String? = null,
    @Json(name = "equipment_id") val equipmentId: String? = null,
    @Json(name = "state") val state: InterchangeStateV13 = InterchangeStateV13.NOT_STARTED,
    @Json(name = "local_decision") val localDecision: String? = null,
    @Json(name = "reason_code") val reasonCode: String? = null,
    @Json(name = "challenge_code") val challengeCode: String? = null,
    @Json(name = "txn_id") val txnId: String? = null,
    @Json(name = "eir_no") val eirNo: String? = null,
    @Json(name = "eir_document_id") val eirDocumentId: String? = null,
    @Json(name = "observed_at") val observedAt: String? = null,
    @Json(name = "committed_at") val committedAt: String? = null,
    @Json(name = "branch_id_applied") val branchIdApplied: String? = null
)

data class WhyEvidence(
    @Json(name = "document_id") val documentId: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "source_type") val sourceType: String,
    @Json(name = "authority_tier") val authorityTier: String,
    @Json(name = "quote") val quote: String? = null,
    @Json(name = "quote_start") val quoteStart: Int? = null,
    @Json(name = "quote_end") val quoteEnd: Int? = null,
    @Json(name = "observed_at") val observedAt: String? = null
)

data class CostComponent(
    @Json(name = "component") val component: String,
    @Json(name = "amount") val amount: Long,
    @Json(name = "label") val label: String = "MODELED",
    @Json(name = "source_id") val sourceId: String? = null
)

data class ForecastProvenance(
    @Json(name = "model") val model: String = "quantum scenario forecast",
    @Json(name = "qcloud_job_id") val qcloudJobId: String? = null,
    @Json(name = "generated_at") val generatedAt: String? = null
)

data class DecisionV13(
    @Json(name = "engine") val engine: String? = null,
    @Json(name = "run_id") val runId: String? = null,
    @Json(name = "execution_mode") val executionMode: String? = null,
    @Json(name = "job_id") val jobId: String? = null,
    @Json(name = "validator") val validator: String? = null,
    @Json(name = "reason_en") val reasonEn: String? = null,
    @Json(name = "reason_local") val reasonLocal: String? = null,
    @Json(name = "alternatives_considered") val alternativesConsidered: Int? = null,
    @Json(name = "greedy_would_have") val greedyWouldHave: String? = null,
    @Json(name = "origin") val origin: DecisionOrigin = DecisionOrigin.UNKNOWN
)

data class CallStatementV13(
    @Json(name = "role") val role: String? = null,
    @Json(name = "execution_id_masked") val executionIdMasked: String? = null,
    @Json(name = "answer") val answer: String? = null,
    @Json(name = "quote_original") val quoteOriginal: String? = null,
    @Json(name = "quote_en") val quoteEn: String? = null
)

object QpiAiDecisionHelper {
    fun formatWhyYou(decision: DecisionV13?): String {
        val text = decision?.reasonLocal?.takeIf { it.isNotBlank() }
            ?: decision?.reasonEn?.takeIf { it.isNotBlank() }
        return text ?: "—"
    }

    fun formatProvenanceChip(decision: DecisionV13?): String {
        if (decision == null) return "—"
        val runId = decision.runId?.takeIf { it.isNotBlank() }
        val validator = decision.validator?.takeIf { it.isNotBlank() }
        if (runId == null && validator == null) return "—"
        val runIdPrefix = runId?.take(8) ?: "—"
        val validatorText = validator ?: "—"
        return "QpiAI run $runIdPrefix · validator $validatorText"
    }

    fun formatChipText(decision: DecisionV13?): String = formatProvenanceChip(decision)

    fun isSimulated(decision: DecisionV13?): Boolean {
        return decision?.origin == DecisionOrigin.SIMULATED_CALL
    }
}

fun Assignment.toV13(): AssignmentV13 {
    val statusEnum = try {
        AssignmentStatusV13.valueOf(this.status)
    } catch (_: Exception) {
        AssignmentStatusV13.APPROVED_ACTIVE
    }
    val pnrEnum = try {
        PnrStatus.valueOf(this.pnrStatus ?: "UNKNOWN")
    } catch (_: Exception) {
        PnrStatus.UNKNOWN
    }
    return AssignmentV13(
        assignmentId = this.assignmentId,
        planId = this.planId,
        planVersion = this.planVersion,
        status = statusEnum,
        driverId = this.truckId,
        truckId = this.truckId,
        container = ContainerV13(containerNo = this.containerId),
        freightCase = FreightCaseV13(caseCode = "C5", blockerType = "TRUCK_BREAKDOWN", hardDeadline = this.deadline),
        bay = BayV13(siteId = this.checkpointId, windowStart = this.slotStart, windowEnd = this.slotEnd),
        instruction = InstructionV13(lang = "en", text = this.reasonText ?: "—", textEn = this.reasonText ?: "—"),
        costExposure = CostExposureV13(avoidable = this.projectedAvoidableCostInr),
        pnr = this.pnr,
        actBy = this.actBy,
        pnrStatus = pnrEnum,
        issuedAt = this.updatedAt
    )
}

data class WhySheet(
    @Json(name = "assignment_id") val assignmentId: String,
    @Json(name = "origin") val origin: WhyOrigin = WhyOrigin.DISPATCHER,
    @Json(name = "pnr_explanation") val pnrExplanation: String? = null,
    @Json(name = "decision") val decision: DecisionV13? = null,
    @Json(name = "calls") val calls: List<CallStatementV13> = emptyList(),
    @Json(name = "forecast_provenance") val forecastProvenance: ForecastProvenance? = null,
    @Json(name = "evidence") val evidence: List<WhyEvidence> = emptyList(),
    @Json(name = "solver_runs") val solverRuns: List<SolverRunRef> = emptyList(),
    @Json(name = "cost_breakdown") val costBreakdown: List<CostComponent> = emptyList(),
    @Json(name = "insufficient_evidence") val insufficientEvidence: Boolean = false
)

data class AssignmentsResponseV13(
    @Json(name = "server_time") val serverTime: String? = null,
    @Json(name = "assignments") val assignments: List<AssignmentV13> = emptyList()
)

data class VoiceReportConfirmRequest(
    @Json(name = "idempotency_key") val idempotencyKey: String,
    @Json(name = "accepted") val accepted: Boolean,
    @Json(name = "corrected_reason_code") val correctedReasonCode: String? = null,
    @Json(name = "corrected_eta_minutes") val correctedEtaMinutes: Int? = null
)

data class ProfileUpdateRequest(
    @Json(name = "language") val language: String
)

data class SseStreamEvent(
    @Json(name = "type") val type: String,
    @Json(name = "incident_id") val incidentId: String? = null,
    @Json(name = "assignment_id") val assignmentId: String? = null,
    @Json(name = "plan_version") val planVersion: Int? = null,
    @Json(name = "at") val at: String? = null
)

// ---------------------------------------------------------------------------
// ROOM / PERSISTENCE ENTITIES FOR DATABASE
// ---------------------------------------------------------------------------

data class AssignmentEntity(
    val assignmentId: String,
    val planId: String,
    val planVersion: Int = 1,
    val containerId: String,
    val truckId: String,
    val slotId: String,
    val slotStart: String,
    val slotEnd: String,
    val checkpointId: String,
    val status: String,
    val driverAckState: String,
    val physicalState: String,
    val reasonCode: String?,
    val reasonText: String?,
    val customsRelease: String?,
    val deadline: String,
    val projectedAvoidableCostInr: Long,
    val evidenceIds: String?,
    val activeBranchJson: String? = null,
    val bayJson: String? = null,
    val instructionJson: String? = null,
    val interchangeJson: String? = null,
    val updatedAt: String
)

data class CallEntity(
    val callId: String,
    val executionId: String?,
    val agentIdMasked: String?,
    val direction: String,
    val language: String?,
    val purpose: String?,
    val question: String?,
    val status: String,
    val fromNumberMasked: String?,
    val startedAt: String?,
    val endedAt: String?,
    val transcriptExcerptOriginal: String?,
    val transcriptExcerptEn: String?,
    val extractedJson: String?,
    val verification: String
)

data class InterchangeEntity(
    val sessionId: String,
    val siteId: String,
    val truckId: String,
    val truckTagRefMasked: String?,
    val equipmentId: String?,
    val state: String,
    val localDecision: String?,
    val reasonCode: String?,
    val challengeCode: String?,
    val txnId: String?,
    val eirNo: String?,
    val eirDocumentId: String?,
    val observedAt: String?,
    val committedAt: String?,
    val branchIdApplied: String?
)

data class VoiceReportEntity(
    val reportId: String,
    val assignmentId: String,
    val status: String,
    val languageDetected: String?,
    val transcriptOriginal: String?,
    val transcriptEn: String?,
    val interpretationJson: String?,
    val evidenceTier: String,
    val provider: String,
    val providerRequestId: String?,
    val createdAt: String
)

