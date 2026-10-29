package com.lira.field.core.model

import com.squareup.moshi.Json

/**
 * LIRA Field PRD v9.0 Checkpoint & ResourceLock Model
 * Authoritative Hardware Reality:
 * - ESP32 DevKit V1 + ILI9341 320x240 TFT (DISPLAY ONLY) + RC522 RFID
 * - ZERO physical buttons, ZERO touchscreen input, NO touch pressure/biometrics, NO ultrasonic/buzzer
 * - Handover approval is performed by the human custodian on the WEB INTERFACE
 * - Driver application displays read-only confirmed ResourceLock status from FastAPI/PostgreSQL
 */
data class CheckpointStatus(
    @Json(name = "assignment_id") val assignmentId: String,
    @Json(name = "device_id") val deviceId: String = "LIRA-MARSHAL-01",
    @Json(name = "state") val state: String = CheckpointStateV9.TAG_MATCHED,
    @Json(name = "rfid_state") val rfidState: String = Tier3ARfidState.TAG_MATCHED,
    @Json(name = "last_event_at") val lastEventAt: String? = null,

    // Real-Time Edge Telemetry
    @Json(name = "edge_online") val edgeOnline: Boolean = true,
    @Json(name = "hardware_heartbeat") val hardwareHeartbeat: String? = "1s ago",
    @Json(name = "last_sync") val lastSync: String? = "Just now",
    @Json(name = "event_sequence") val eventSequence: Long = 101L,
    @Json(name = "scanned_tag_id") val scannedTagId: String? = "RFID-T07-9941",
    @Json(name = "expected_tag_id") val expectedTagId: String? = "RFID-T07-9941",
    @Json(name = "recovery_wave_triggered") val recoveryWaveTriggered: Boolean = false,
    @Json(name = "mismatch_details") val mismatchDetails: String? = null,

    // Strict 3-Row Epistemic Evidence Matrix (PRD v9.0)
    // Tier 3: Passive sensor/tag read (proves tag presented only)
    @Json(name = "tier3a_rfid_observation") val tier3aRfidObservation: String = Tier3ARfidState.TAG_MATCHED,
    // Tier 4: Custodian's authenticated web attestation (separate from ESP32 read, conducted on web dashboard)
    @Json(name = "tier4_custodian_web_attestation") val tier4CustodianWebAttestation: String = Tier4CustodianWebState.CONFIRMED_ON_WEB,
    // Tier 1: Statutory port / ULIP / PCS 1x customs gate proof
    @Json(name = "tier1_port_proof") val tier1PortProof: String = Tier1PortProofState.ACTION_PENDING_EXTERNAL_PROOF,
    @Json(name = "marshal_exception_reason") val marshalExceptionReason: String? = null,

    // Authoritative v9 ResourceLock Read-Only Telemetry
    @Json(name = "resource_lock") val resourceLock: ResourceLockStatus = ResourceLockStatus(),

    // Legacy/historical fixture fields (labeled obsolete, preserved for model compatibility)
    @Deprecated("Obsolete v7 fixture only. ESP32 is display-only with zero touch.")
    @Json(name = "tier3b_marshal_attestation") val tier3bMarshalAttestation: String = Tier3BMarshalState.OPERATOR_CONFIRMED,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "eeprom_sector4_status") val eepromSector4Status: String = EepromSector4Status.BURNED_VERIFIED,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "burned_slot_id") val burnedSlotId: String = "S1700",
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "burned_truck_id") val burnedTruckId: String = "T09",
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "rolling_ratchet_counter") val rollingRatchetCounter: Long = 42L,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "eeprom_hex_dump") val eepromHexDump: String = "4C 49 52 41 20 53 31 37 30 30 20 54 30 39 00 2A",
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "signature_z_pressure_hash") val signatureZPressureHash: String = "SHA256:8f4c2e1b8c0491ab54e7d93412aa03fe88c7",
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "stroke_points_count") val strokePointsCount: Int = 184,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "mean_z_pressure") val meanZPressure: Float = 0.74f,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "biometric_attestation_verified") val biometricAttestationVerified: Boolean = true,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "edge_cached_qaoa_bitstring") val edgeCachedQaoaBitstring: String = "|1010⟩ -> T09/S1730",
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "edge_eval_latency_us") val edgeEvalLatencyUs: Long = 1420L,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "offline_edge_contingency_triggered") val offlineEdgeContingencyTriggered: Boolean = false,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "edge_contingency_plan_id") val edgeContingencyPlanId: String = "PLAN-R3-EDGE-HOT",
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "tft_custody_qr_payload") val tftCustodyQrPayload: String = "LIRA-QR:ASN-102-R2:0042:B78E49A1",
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "custody_ratchet") val custodyRatchet: Long = 42L,
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "custody_hmac8") val custodyHmac8: String = "B78E49A1",
    @Deprecated("Obsolete v7 fixture only.")
    @Json(name = "custody_chain_status") val custodyChainStatus: String = "CUSTODY_ATTESTED_HMAC8_MATCH",
    @Json(name = "bay_present") val bayPresent: Boolean = true,
    @Json(name = "container_truck_match") val containerTruckMatch: Boolean = true
)

/**
 * Authoritative ResourceLock Status (PRD v9.0)
 * Tracks physical equipment reservation, check-out, and web custodian authorization.
 */
data class ResourceLockStatus(
    @Json(name = "resource_id") val resourceId: String = "CHASSIS-40SK-09",
    @Json(name = "resource_type") val resourceType: String = "40ft Skeletal Chassis (3-Axle)",
    @Json(name = "resource_state") val resourceState: String = ResourceState.CHECKOUT_ACCEPTED,
    @Json(name = "work_order_id") val workOrderId: String = "WO-JNPT-2026-9941",
    @Json(name = "operator_web_approval_state") val operatorWebApprovalState: String = CustodianWebApprovalState.APPROVED_ON_WEB,
    @Json(name = "operator_web_approval_timestamp") val operatorWebApprovalTimestamp: String? = "2026-10-29T15:42:10+05:30",
    @Json(name = "operator_web_user_id") val operatorWebUserId: String? = "CUSTODIAN_DESK_04",
    @Json(name = "server_tx_id") val serverTxId: String? = "TX-AUTH-9941-R2",
    @Json(name = "rejection_code") val rejectionCode: String? = null,
    @Json(name = "plan_version") val planVersion: Int = 1,
    @Json(name = "solver_provenance") val solverProvenance: String = "QpiAI Opt Validated (Backend Solver)",
    @Json(name = "terminal_availability") val terminalAvailability: String = "CONFIRMED_AVAILABLE",
    @Json(name = "recovery_review_status") val recoveryReviewStatus: String? = null
)

object ResourceState {
    const val SCAN_REQUESTED = "SCAN_REQUESTED"
    const val TAG_OBSERVED = "TAG_OBSERVED"
    const val AWAITING_CUSTODIAN_CONFIRMATION = "AWAITING_CUSTODIAN_CONFIRMATION"
    const val CHECKOUT_ACCEPTED = "CHECKOUT_ACCEPTED"
    const val REJECTED = "REJECTED"
    const val QUARANTINED = "QUARANTINED"
    const val OFFLINE_UNCONFIRMED = "OFFLINE_UNCONFIRMED"

    // Backward-compatibility aliases
    const val RESERVED = "AWAITING_CUSTODIAN_CONFIRMATION"
    const val CHECKED_OUT = "CHECKOUT_ACCEPTED"
    const val RETURN_PENDING = "SCAN_REQUESTED"
    const val EXCEPTION_HOLD = "REJECTED"
}

object CustodianWebApprovalState {
    const val APPROVED_ON_WEB = "APPROVED_ON_WEB"
    const val PENDING_OPERATOR_WEB_ACTION = "PENDING_OPERATOR_WEB_ACTION"
    const val REJECTED_ON_WEB = "REJECTED_ON_WEB"
}

data class VerifyQrCustodyRequest(
    @Json(name = "assignment_id") val assignmentId: String,
    @Json(name = "qr_payload") val qrPayload: String,
    @Json(name = "client_timestamp") val clientTimestamp: String = System.currentTimeMillis().toString()
)

data class VerifyQrCustodyResponse(
    @Json(name = "verified") val verified: Boolean,
    @Json(name = "token_valid") val tokenValid: Boolean,
    @Json(name = "assignment_id") val assignmentId: String,
    @Json(name = "message") val message: String,
    @Json(name = "verified_at") val verifiedAt: String,
    @Json(name = "chain_of_custody_status") val chainOfCustodyStatus: String = "CUSTODY_ATTESTED_HMAC8_MATCH",
    @Json(name = "ratchet") val ratchet: Long = 42L,
    @Json(name = "hmac8") val hmac8: String = "B78E49A1"
)

object EepromSector4Status {
    const val BURNED_VERIFIED = "BURNED_VERIFIED"
    const val STALE_SLOT = "STALE_SLOT"
    const val UNINITIALIZED = "UNINITIALIZED"
}

object CheckpointStateV9 {
    const val ASSIGNMENT_DISPATCHED = "ASSIGNMENT_DISPATCHED"
    const val AWAITING_TAG = "AWAITING_TAG"
    const val TAG_OBSERVED = "TAG_OBSERVED"
    const val TAG_MATCHED = "TAG_MATCHED"
    const val TAG_MISMATCH = "TAG_MISMATCH"
    const val UNKNOWN_TAG = "UNKNOWN_TAG"
    const val CUSTODIAN_CONFIRMED_ON_WEB = "CUSTODIAN_CONFIRMED_ON_WEB"
    const val OPERATOR_CONFIRMED = "CUSTODIAN_CONFIRMED_ON_WEB"
    const val EXCEPTION_REPORTED_ON_WEB = "EXCEPTION_REPORTED_ON_WEB"
    const val EXCEPTION_REPORTED = "EXCEPTION_REPORTED_ON_WEB"
    const val BACKEND_VALIDATED = "BACKEND_VALIDATED"
    const val ACTION_PENDING_EXTERNAL_PROOF = "ACTION_PENDING_EXTERNAL_PROOF"
    const val RECOVERY_REQUIRED = "RECOVERY_REQUIRED"
}

typealias CheckpointStateV7 = CheckpointStateV9

object Tier3ARfidState {
    const val AWAITING_TAG = "AWAITING_TAG"
    const val TAG_OBSERVED = "TAG_OBSERVED"
    const val TAG_MATCHED = "TAG_MATCHED"
    const val TAG_MISMATCH = "TAG_MISMATCH"
    const val UNKNOWN_TAG = "UNKNOWN_TAG"
}

object Tier4CustodianWebState {
    const val AWAITING_WEB_APPROVAL = "AWAITING_WEB_APPROVAL"
    const val CONFIRMED_ON_WEB = "CONFIRMED_ON_WEB"
    const val EXCEPTION_REPORTED_ON_WEB = "EXCEPTION_REPORTED_ON_WEB"
}

object Tier3BMarshalState {
    const val AWAITING_OPERATOR = "AWAITING_OPERATOR"
    const val OPERATOR_CONFIRMED = "OPERATOR_CONFIRMED"
    const val EXCEPTION_REPORTED = "EXCEPTION_REPORTED"
}

object Tier1PortProofState {
    const val ACTION_PENDING_EXTERNAL_PROOF = "ACTION_PENDING_EXTERNAL_PROOF"
    const val EXTERNALLY_VERIFIED = "EXTERNALLY_VERIFIED"
}

object MarshalExceptionReason {
    const val WRONG_TAG = "WRONG_TAG"
    const val NO_TAG = "NO_TAG"
    const val DRIVER_NO_SHOW = "DRIVER_NO_SHOW"
    const val VEHICLE_UNAVAILABLE = "VEHICLE_UNAVAILABLE"
    const val CHASSIS_DAMAGED = "CHASSIS_DAMAGED"
}

object CheckpointRfidState {
    const val NOT_SCANNED = "AWAITING_TAG"
    const val AWAITING_TAG = "AWAITING_TAG"
    const val SCANNED_MATCH = "TAG_MATCHED"
    const val TAG_MATCHED = "TAG_MATCHED"
    const val MATCHED = "TAG_MATCHED"
    const val SCANNED_MISMATCH = "TAG_MISMATCH"
    const val TAG_MISMATCH = "TAG_MISMATCH"
    const val RFID_FAIL = "UNKNOWN_TAG"
}

object HardwareSpec {
    const val MICROCONTROLLER = "ESP32 DevKit V1 (Tensilica 240MHz, Wi-Fi)"
    const val DISPLAY = "ILI9341 320x240 SPI TFT (DISPLAY ONLY)"
    const val RFID_READER = "RC522 13.56MHz SPI (Passive Tag Sensor)"
    const val OPERATOR_INTERFACE = "Web Dashboard Custodian Terminal (Zero On-Device Buttons/Touch)"
}
