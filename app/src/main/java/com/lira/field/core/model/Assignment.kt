package com.lira.field.core.model

import com.squareup.moshi.Json

/**
 * LIRA Field PRD v9.0 Assignment DTO
 * Authoritative fields aligned with OpenAPI and shared integration contract:
 * - assignment_id, container_id, truck_id, slot_id, plan_version
 * - work_order_reference, resource_handover_status, last_sync
 * - next_task, task_owner, deadline
 * - actual solver_run summary, solver availability status
 * - strictly labeled financial quantities (MODELED ESTIMATE vs ACTUAL INVOICE vs NOT VERIFIED)
 */
data class Assignment(
    @Json(name = "assignment_id") val assignmentId: String,
    @Json(name = "plan_id") val planId: String,
    @Json(name = "container_id") val containerId: String,
    @Json(name = "truck_id") val truckId: String,
    @Json(name = "slot_id") val slotId: String,
    @Json(name = "slot_start") val slotStart: String,
    @Json(name = "slot_end") val slotEnd: String,
    @Json(name = "checkpoint_id") val checkpointId: String,
    @Json(name = "status") val status: String,
    @Json(name = "driver_ack_state") val driverAckState: String,
    @Json(name = "physical_state") val physicalState: String,
    @Json(name = "reason_code") val reasonCode: String? = null,
    @Json(name = "reason_text") val reasonText: String? = null,
    @Json(name = "customs_release") val customsRelease: String? = null,
    @Json(name = "deadline") val deadline: String,
    @Json(name = "projected_avoidable_cost_inr") val projectedAvoidableCostInr: Long,
    @Json(name = "evidence_ids") val evidenceIds: List<String> = emptyList(),
    @Json(name = "updated_at") val updatedAt: String,

    // Extended fields per LIRA Master PRD v9.0
    @Json(name = "assignment_version") val assignmentVersion: String = "1.0",
    @Json(name = "recovery_wave") val recoveryWave: Int = 1,
    @Json(name = "port_terminal") val portTerminal: String = "JNPT / Nhava Sheva Terminal-2",
    @Json(name = "destination") val destination: String = "Bhiwandi CFS Logistics Hub, Bay 4",
    @Json(name = "required_equipment") val requiredEquipment: String = "40ft Skeletal Chassis, 3-Axle",
    @Json(name = "driver_obligations") val driverObligations: String = "Verify EIR slip, inspect container seal #SL-8849",
    @Json(name = "release_prerequisites") val releasePrerequisites: String = "Customs e-Sanchit released, Demurrage waiver filed",
    @Json(name = "operational_condition") val operationalCondition: String = OperationalCondition.CONFIRMED,
    @Json(name = "previous_plan_summary") val previousPlanSummary: String? = null,
    @Json(name = "urgency") val urgency: String = UrgencyLevel.ELEVATED,
    @Json(name = "is_superseded") val isSuperseded: Boolean = false,

    // PRD v9.0 Work Order & Task Ownership
    @Json(name = "work_order_reference") val workOrderReference: String = "WO-JNPT-2026-9941",
    @Json(name = "resource_handover_status") val resourceHandoverStatus: String = "CHECKED_OUT",
    @Json(name = "last_sync") val lastSync: String = "—",
    @Json(name = "next_task") val nextTask: String = "—",
    @Json(name = "task_owner") val taskOwner: String = "Driver T09",
    @Json(name = "solver_run_summary") val solverRunSummary: String = "—",
    @Json(name = "is_solver_available") val isSolverAvailable: Boolean = true,
    @Json(name = "no_feasible_plan") val noFeasiblePlan: Boolean = false,

    // Financial Modeling & Integrity (Labeled Estimates vs Actuals)
    @Json(name = "invoiced_actual_inr") val invoicedActualInr: Long = 0L,
    @Json(name = "exposure_financial_label") val exposureFinancialLabel: String = "MODELED ESTIMATE",
    @Json(name = "dpd_evacuation_cliff") val dpdEvacuationCliff: String = "2026-10-29T16:00:00+05:30",
    @Json(name = "e_do_bank_utr_cutoff") val eDoBankUtrCutoff: String = "2026-10-29T14:30:00+05:30",
    @Json(name = "solver_provenance") val solverProvenance: String = "QpiAI XY-QAOA + Warm-Start Gurobi 12.0.1 (0.84% Duality Gap)",
    @Json(name = "invalidation_trigger") val invalidationTrigger: String? = null,
    @Json(name = "plan_version") val planVersion: Int = 1,

    // Quantum Slot-Swap Arbitrage & Demurrage Immunity Dossier (Honest v9 Telemetry)
    @Json(name = "slot_swap_arbitrage") val slotSwapArbitrage: SlotSwapArbitrage? = SlotSwapArbitrage(),
    @Json(name = "demurrage_immunity") val demurrageImmunity: DemurrageImmunityDossier? = DemurrageImmunityDossier(),

    // RAG Explainability, QpiAI Investigation Director & Robust Recovery
    @Json(name = "rag_disclosure") val ragDisclosure: RagExplainabilityDisclosure? = RagExplainabilityDisclosure(),
    @Json(name = "active_investigation_task") val activeInvestigationTask: QpiAiInvestigationTask? = null,
    @Json(name = "robust_recovery_summary") val robustRecoverySummary: RobustRecoverySummary? = null,

    // LIRA-Q V11 Six Freight Cases & Bolna AI Voice Telephony Verification
    @Json(name = "freight_case") val freightCase: FreightExceptionCase? = null,
    @Json(name = "bolna_verification") val bolnaVerification: BolnaCallVerification? = null,
    @Json(name = "pnr") val pnr: String? = null,
    @Json(name = "act_by") val actBy: String? = null,
    @Json(name = "pnr_status") val pnrStatus: String? = null,
    @Json(name = "decision") val decision: String? = null,
    @Json(name = "why_sheet") val whySheet: String? = null
) {
    val modeledCounterfactualExposureInr: Long
        get() = projectedAvoidableCostInr
}

/**
 * Concise RAG Explainability Disclosure (PRD v9.0 Addendum)
 * Answers "WHY THIS ACTION?" using backend source citations without a generic chatbot.
 */
data class RagExplainabilityDisclosure(
    @Json(name = "reason_for_action") val reasonForAction: String = "Slot swap avoids ₹45,000 detention cliff",
    @Json(name = "blocker_description") val blockerDescription: String = "Kalamboli gate congestion causes 42m delay",
    @Json(name = "appointment_deadline") val appointmentDeadline: String = "2026-10-29T16:00:00+05:30",
    @Json(name = "evidence_source_title") val evidenceSourceTitle: String = "NHAI Traffic Authority Advisory #2026-44",
    @Json(name = "evidence_timestamp") val evidenceTimestamp: String = "2026-10-29T13:40:00+05:30",
    @Json(name = "exact_quote") val exactQuote: String = "Corridor NH-348 lane closure at Km 14+200 due to overturned heavy vehicle.",
    @Json(name = "span_reference_id") val spanReferenceId: String = "RAG-DOC-NHAI-44:P3:L14-22",
    @Json(name = "rag_status") val ragStatus: String = "GROUNDED_EVIDENCE",
    @Json(name = "driver_visibility_level") val driverVisibilityLevel: String = "AUTHORIZED_DRIVER_REDACTED"
)

/**
 * QpiAI Investigation Director — Driver-Facing Task
 * Driver sees only tasks addressed to them (e.g. VERIFY_DRIVER_AVAILABILITY / CONFIRM_ETA)
 */
data class QpiAiInvestigationTask(
    @Json(name = "task_id") val taskId: String = "—",
    @Json(name = "task_type") val taskType: String = InvestigationTaskType.CONFIRM_ETA,
    @Json(name = "target_truck_id") val targetTruckId: String = "—",
    @Json(name = "headline") val headline: String = "—",
    @Json(name = "question_text") val questionText: String = "—",
    @Json(name = "deadline") val deadline: String = "—",
    @Json(name = "solver_run_id") val solverRunId: String? = null,
    @Json(name = "solver_status") val solverStatus: String = "VALIDATED_FEASIBLE",
    @Json(name = "provenance_label") val provenanceLabel: String = "—",
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "response_state") val responseState: String = "PENDING_DRIVER_RESPONSE"
)

object InvestigationTaskType {
    const val VERIFY_DRIVER_AVAILABILITY = "VERIFY_DRIVER_AVAILABILITY"
    const val CONFIRM_ETA = "CONFIRM_ETA"
    const val EQUIPMENT_INSPECTION_CHECK = "EQUIPMENT_INSPECTION_CHECK"
}

/**
 * Optional Robust Recovery Insight (PRD v9.0 Addendum)
 * Server-verified stress-test summary labeled MODELED.
 */
data class RobustRecoverySummary(
    @Json(name = "scenario_name") val scenarioName: String = "Corridor Buffer Stress Test (+20m Traffic)",
    @Json(name = "plan_risk_assessment") val planRiskAssessment: String = "Backup slot S1645 required if road transit exceeds 35 minutes.",
    @Json(name = "modeled_risk_label") val modeledRiskLabel: String = "MODELED STRESS SCENARIO",
    @Json(name = "solver_run_id") val solverRunId: String = "QPIAI-OPT-STRESS-992",
    @Json(name = "is_verified_by_server") val isVerifiedByServer: Boolean = true
)

data class SlotSwapArbitrage(
    @Json(name = "urgent_container_id") val urgentContainerId: String = "C102",
    @Json(name = "urgent_container_type") val urgentContainerType: String = "AEO-T2 Reefer (Cold-Chain Active)",
    @Json(name = "urgent_cliff_inr") val urgentCliffInr: Long = 48000L,
    @Json(name = "urgent_cliff_window") val urgentCliffWindow: String = "45m remaining",
    @Json(name = "donor_container_id") val donorContainerId: String = "C208",
    @Json(name = "donor_container_type") val donorContainerType: String = "Dry Cargo (Standard General Purpose)",
    @Json(name = "donor_free_time_remaining") val donorFreeTimeRemaining: String = "72h free time remaining (₹0 demurrage today)",
    @Json(name = "slot_yield_bounty_inr") val slotYieldBountyInr: Long = 3000L,
    @Json(name = "net_demurrage_saved_inr") val netDemurrageSavedInr: Long = 45000L,
    @Json(name = "swap_status") val swapStatus: String = "PROPOSED_SLOT_SWAP",
    @Json(name = "swap_status_label") val swapStatusLabel: String = "PROPOSED BY QpiAI HUBO (AWAITING TERMINAL CONFIRMATION)",
    @Json(name = "barter_algorithm") val barterAlgorithm: String = "QpiAI HUBO Slot Barter Matrix (Backend Server)"
)

data class DemurrageImmunityDossier(
    @Json(name = "driver_staging_timestamp") val driverStagingTimestamp: String = "2026-10-28T15:48:22+05:30",
    @Json(name = "slot_cutoff_timestamp") val slotCutoffTimestamp: String = "2026-10-28T16:00:00+05:30",
    @Json(name = "is_ready_on_time") val isReadyOnTime: Boolean = true,
    @Json(name = "hmac_proof") val hmacProof: String = "HMAC-SHA256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    @Json(name = "terminal_fault_code") val terminalFaultCode: String = "YARD_STACK_DEPTH_DELAY",
    @Json(name = "terminal_fault_reason") val terminalFaultReason: String = "Tier 4+ RTG Dig-Out Delay (100% Terminal Fault)",
    @Json(name = "statutory_waiver_active") val statutoryWaiverActive: Boolean = true,
    @Json(name = "statutory_waiver_status") val statutoryWaiverStatus: String = "CLAIM_FILED_PENDING_REVIEW",
    @Json(name = "legal_statute_citation") val legalStatuteCitation: String = "GATE DISPUTE BACKUP RECORD (AUDIT PENDING)",
    @Json(name = "immunity_badge_text") val immunityBadgeText: String = "46 CFR § 541.6 [LAB_FIXTURE] GATE AUDIT RECORD LOGGED (HMAC READY — PENDING TERMINAL CONCURRENCE)"
)

data class AssignmentsResponse(
    @Json(name = "items") val items: List<Assignment>,
    @Json(name = "updated_at") val updatedAt: String
)

data class AckRequest(
    @Json(name = "action") val action: String,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "assignment_version") val assignmentVersion: String? = "1.0",
    @Json(name = "plan_version") val planVersion: Int? = null,
    @Json(name = "driver_id") val driverId: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "client_timestamp") val clientTimestamp: String? = null,
    @Json(name = "idempotency_key") val idempotencyKey: String? = null
)

data class AckResponse(
    @Json(name = "accepted") val accepted: Boolean,
    @Json(name = "assignment_id") val assignmentId: String,
    @Json(name = "driver_ack_state") val driverAckState: String,
    @Json(name = "event_id") val eventId: String? = null,
    @Json(name = "recovery_wave_triggered") val recoveryWaveTriggered: Boolean = false
)

data class EtaReportRequest(
    @Json(name = "truck_id") val truckId: String,
    @Json(name = "eta") val eta: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "source") val source: String = "DRIVER_SELF_REPORT",
    @Json(name = "comment") val comment: String? = null
)

data class EtaReportResponse(
    @Json(name = "accepted") val accepted: Boolean,
    @Json(name = "event_id") val eventId: String? = null,
    @Json(name = "recovery_status") val recoveryStatus: String? = null,
    @Json(name = "plan_valid") val planValid: Boolean = true
)

object AssignmentStatus {
    const val APPROVED = "APPROVED"
    const val ACKNOWLEDGED = "ACKNOWLEDGED"
    const val REASSIGNED = "REASSIGNED"
    const val STAGING_EVIDENCED = "STAGING_EVIDENCED"
    const val PHYSICAL_NO_SHOW = "PHYSICAL_NO_SHOW"
    const val SUPERSEDED = "SUPERSEDED"
    const val CANCELLED = "CANCELLED"
}

object DriverAckState {
    const val PENDING = "PENDING"
    const val ACKNOWLEDGED = "ACKNOWLEDGED"
    const val CANNOT_COMPLY = "CANNOT_COMPLY"
}

object PhysicalState {
    const val EXPECTED = "EXPECTED"
    const val RFID_MISMATCH = "RFID_MISMATCH"
    const val PHYSICAL_MISMATCH = "PHYSICAL_MISMATCH"
    const val BAY_ABSENT_WARNING = "BAY_ABSENT_WARNING"
    const val PHYSICAL_NO_SHOW = "PHYSICAL_NO_SHOW"
    const val STAGING_EVIDENCED = "STAGING_EVIDENCED"
}

object CannotComplyReasons {
    const val TRUCK_BREAKDOWN = "TRUCK_BREAKDOWN"
    const val DRIVER_UNAVAILABLE = "DRIVER_UNAVAILABLE"
    const val TRAFFIC_DELAY = "TRAFFIC_DELAY"
    const val EQUIPMENT_MISMATCH = "EQUIPMENT_MISMATCH"
    const val CHASSIS_UNAVAILABLE = "CHASSIS_UNAVAILABLE"
    const val ROUTE_BLOCKED = "ROUTE_BLOCKED"
    const val TERMINAL_ACCESS_ISSUE = "TERMINAL_ACCESS_ISSUE"
    const val SAFETY_CONCERN = "SAFETY_CONCERN"
    const val OTHER = "OTHER"

    val ALL = listOf(
        TRUCK_BREAKDOWN to "Mechanical Breakdown / Tire Failure",
        DRIVER_UNAVAILABLE to "Driver Medical / Rest Hours Limit",
        TRAFFIC_DELAY to "Severe Highway Bottleneck / Gridlock",
        EQUIPMENT_MISMATCH to "Container Chassis Size / Fitting Mismatch",
        CHASSIS_UNAVAILABLE to "Yard Chassis Unavailable",
        ROUTE_BLOCKED to "Highway Bridge / Corridor Closure",
        TERMINAL_ACCESS_ISSUE to "Gate Pass / Terminal System Failure",
        SAFETY_CONCERN to "Hazardous Cargo / Adverse Weather Alert",
        OTHER to "Other Operational Blocker"
    )
}

object OperationalCondition {
    const val CONFIRMED = "CONFIRMED"
    const val BLOCKED = "BLOCKED"
    const val UNKNOWN = "UNKNOWN"
    const val STALE = "STALE"
    const val REQUIRES_VERIFICATION = "REQUIRES_VERIFICATION"
}

object UrgencyLevel {
    const val CRITICAL = "CRITICAL"
    const val ELEVATED = "ELEVATED"
    const val STANDARD = "STANDARD"
}
