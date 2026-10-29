package com.lira.field.data

import com.lira.field.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class MockDataEngine {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    private fun currentTime(): String = isoFormat.format(Date())

    // Demo Stages:
    // 0: Initial Plan R2 (Assigned to T07, Pending Ack)
    // 1: Driver T07 Acknowledged
    // 2: Checkpoint No-Show / Mismatch reported
    // 3: Recovery wave 2 (Plan R3 Approved)
    private val _demoStage = MutableStateFlow(0)
    val demoStage: StateFlow<Int> = _demoStage.asStateFlow()

    private var driverAckState: String = DriverAckState.PENDING
    private var eventsList = mutableListOf<DriverEvent>()
    private var sequenceCounter = 120L

    private var currentInvestigationTask = QpiAiInvestigationTask(
        taskId = "INV-TASK-4401",
        taskType = InvestigationTaskType.CONFIRM_ETA,
        targetTruckId = "T07",
        headline = "Verification Requested by LIRA",
        questionText = "Can you maintain arrival at JNPT Bay 4 by 16:15 IST?",
        deadline = "2026-10-29T15:45:00+05:30",
        solverRunId = "QOPT-8f2c",
        solverStatus = "VALIDATED_FEASIBLE",
        provenanceLabel = "QpiAI Opt Investigation Director (Backend)",
        isActive = true,
        responseState = "PENDING_DRIVER_RESPONSE"
    )

    fun answerInvestigationTask(taskId: String, responseState: String) {
        if (currentInvestigationTask.taskId == taskId) {
            currentInvestigationTask = currentInvestigationTask.copy(responseState = responseState)
        }
    }

    init {
        resetToInitial()
    }

    fun resetToInitial() {
        _demoStage.value = 0
        driverAckState = DriverAckState.PENDING
        sequenceCounter = 120L
        eventsList.clear()

        currentInvestigationTask = QpiAiInvestigationTask(
            taskId = "INV-TASK-4401",
            taskType = InvestigationTaskType.CONFIRM_ETA,
            targetTruckId = "T07",
            headline = "Verification Requested by LIRA",
            questionText = "Can you maintain arrival at JNPT Bay 4 by 16:15 IST?",
            deadline = "2026-10-29T15:45:00+05:30",
            solverRunId = "QOPT-8f2c",
            solverStatus = "VALIDATED_FEASIBLE",
            provenanceLabel = "QpiAI Opt Investigation Director (Backend)",
            isActive = true,
            responseState = "PENDING_DRIVER_RESPONSE"
        )

        eventsList.add(
            DriverEvent(
                eventId = "EVT-${sequenceCounter++}",
                sequence = sequenceCounter,
                type = EventType.PLAN_APPROVED,
                assignmentId = "ASN-102-R2",
                createdAt = "2026-10-28T15:30:00+05:30",
                payload = mapOf(
                    "plan_id" to "PLAN-R2",
                    "container_id" to "C102",
                    "truck_id" to "T07",
                    "reason" to "T04 delayed 45 min"
                )
            )
        )
    }

    fun setStage(stage: Int) {
        _demoStage.value = stage
        when (stage) {
            0 -> {
                driverAckState = DriverAckState.PENDING
            }
            1 -> {
                driverAckState = DriverAckState.ACKNOWLEDGED
                eventsList.add(
                    DriverEvent(
                        eventId = "EVT-${sequenceCounter++}",
                        sequence = sequenceCounter,
                        type = EventType.DRIVER_ACKNOWLEDGED,
                        assignmentId = "ASN-102-R2",
                        createdAt = currentTime(),
                        payload = mapOf("action" to "ACKNOWLEDGE", "truck_id" to "T07")
                    )
                )
            }
            2 -> {
                eventsList.add(
                    DriverEvent(
                        eventId = "EVT-${sequenceCounter++}",
                        sequence = sequenceCounter,
                        type = EventType.PHYSICAL_MISMATCH,
                        assignmentId = "ASN-102-R2",
                        createdAt = currentTime(),
                        payload = mapOf("event" to "NO_SHOW", "bay_present" to false)
                    )
                )
                eventsList.add(
                    DriverEvent(
                        eventId = "EVT-${sequenceCounter++}",
                        sequence = sequenceCounter,
                        type = EventType.RECOVERY_STARTED,
                        assignmentId = "ASN-102-R2",
                        createdAt = currentTime(),
                        payload = mapOf("reason" to "PHYSICAL_NO_SHOW", "solver" to "QpiAI_Opt")
                    )
                )
            }
            3 -> {
                eventsList.add(
                    DriverEvent(
                        eventId = "EVT-${sequenceCounter++}",
                        sequence = sequenceCounter,
                        type = EventType.PLAN_APPROVED,
                        assignmentId = "ASN-102-R3",
                        createdAt = currentTime(),
                        payload = mapOf(
                            "plan_id" to "PLAN-R3",
                            "container_id" to "C102",
                            "truck_id" to "T07",
                            "slot_id" to "S1645",
                            "reason" to "Physical mismatch resolved via QpiAI Opt"
                        )
                    )
                )
            }
        }
    }

    fun getAssignmentsForDriver(truckId: String): List<Assignment> {
        if (truckId == "T99_UNAUTHORIZED") return emptyList()

        if (truckId == "T04") {
            return listOf(
                Assignment(
                    assignmentId = "ASN-099-R1",
                    planId = "PLAN-R1",
                    containerId = "C099",
                    truckId = "T04",
                    slotId = "S1730",
                    slotStart = "2026-10-28T17:30:00+05:30",
                    slotEnd = "2026-10-28T18:00:00+05:30",
                    checkpointId = "LIRA-EDGE-02",
                    status = AssignmentStatus.APPROVED,
                    driverAckState = DriverAckState.PENDING,
                    physicalState = PhysicalState.EXPECTED,
                    reasonCode = "RESCHEDULED",
                    reasonText = "Delayed arrival adjusted slot",
                    customsRelease = "CONFIRMED",
                    deadline = "2026-10-28T20:00:00+05:30",
                    projectedAvoidableCostInr = 6500,
                    evidenceIds = listOf("EV-DOC-099"),
                    updatedAt = currentTime(),
                    requiredEquipment = "40ft Skeletal Chassis, 3-Axle",
                    urgency = UrgencyLevel.ELEVATED,
                    driverObligations = "Verify EIR slip, inspect container seal #SL-8849"
                )
            )
        }

        if (truckId != "T07") return emptyList()

        return when (_demoStage.value) {
            0 -> listOf(
                Assignment(
                    assignmentId = "ASN-102-R2",
                    planId = "PLAN-R2",
                    containerId = "C102",
                    truckId = "T07",
                    slotId = "S1600",
                    slotStart = "2026-10-28T16:00:00+05:30",
                    slotEnd = "2026-10-28T16:30:00+05:30",
                    checkpointId = "LIRA-EDGE-01",
                    status = AssignmentStatus.APPROVED,
                    driverAckState = driverAckState,
                    physicalState = PhysicalState.EXPECTED,
                    reasonCode = "T04_ETA_CONFLICT",
                    reasonText = "Original driver cannot reach the confirmed slot",
                    customsRelease = "CONFIRMED",
                    deadline = "2026-10-28T18:30:00+05:30",
                    projectedAvoidableCostInr = 12000,
                    evidenceIds = listOf("EV-DOC-102", "EV-CALL-04"),
                    updatedAt = "2026-10-28T15:30:00+05:30",
                    recoveryWave = 1,
                    isSuperseded = false,
                    requiredEquipment = "40ft Skeletal Chassis, 3-Axle",
                    urgency = UrgencyLevel.ELEVATED,
                    driverObligations = "Verify EIR slip, inspect container seal #SL-8849",
                    activeInvestigationTask = currentInvestigationTask
                )
            )
            1 -> listOf(
                Assignment(
                    assignmentId = "ASN-102-R2",
                    planId = "PLAN-R2",
                    containerId = "C102",
                    truckId = "T07",
                    slotId = "S1600",
                    slotStart = "2026-10-28T16:00:00+05:30",
                    slotEnd = "2026-10-28T16:30:00+05:30",
                    checkpointId = "LIRA-EDGE-01",
                    status = AssignmentStatus.ACKNOWLEDGED,
                    driverAckState = DriverAckState.ACKNOWLEDGED,
                    physicalState = PhysicalState.EXPECTED,
                    reasonCode = "T04_ETA_CONFLICT",
                    reasonText = "Original driver cannot reach the confirmed slot",
                    customsRelease = "CONFIRMED",
                    deadline = "2026-10-28T18:30:00+05:30",
                    projectedAvoidableCostInr = 12000,
                    evidenceIds = listOf("EV-DOC-102", "EV-CALL-04"),
                    updatedAt = currentTime(),
                    recoveryWave = 1,
                    isSuperseded = false,
                    requiredEquipment = "40ft Skeletal Chassis, 3-Axle",
                    urgency = UrgencyLevel.ELEVATED,
                    driverObligations = "Verify EIR slip, inspect container seal #SL-8849",
                    activeInvestigationTask = currentInvestigationTask
                )
            )
            2 -> listOf(
                Assignment(
                    assignmentId = "ASN-102-R2",
                    planId = "PLAN-R2",
                    containerId = "C102",
                    truckId = "T07",
                    slotId = "S1600",
                    slotStart = "2026-10-28T16:00:00+05:30",
                    slotEnd = "2026-10-28T16:30:00+05:30",
                    checkpointId = "LIRA-EDGE-01",
                    status = AssignmentStatus.PHYSICAL_NO_SHOW,
                    driverAckState = DriverAckState.ACKNOWLEDGED,
                    physicalState = PhysicalState.RFID_MISMATCH,
                    reasonCode = "PHYSICAL_NO_SHOW",
                    reasonText = "Marshal reported mismatch / driver no-show. Previous Plan v1 is superseded. Central QpiAI Wave 2 queued.",
                    customsRelease = "CONFIRMED",
                    deadline = "2026-10-28T18:30:00+05:30",
                    projectedAvoidableCostInr = 12000,
                    evidenceIds = listOf("EV-DOC-102", "EV-CALL-04", "EV-PHYSICAL-FAIL"),
                    updatedAt = currentTime(),
                    recoveryWave = 1,
                    isSuperseded = true,
                    requiredEquipment = "40ft Skeletal Chassis, 3-Axle",
                    urgency = UrgencyLevel.ELEVATED,
                    driverObligations = "Verify EIR slip, inspect container seal #SL-8849",
                    activeInvestigationTask = currentInvestigationTask
                )
            )
            3 -> listOf(
                Assignment(
                    assignmentId = "ASN-102-R3",
                    planId = "PLAN-R3",
                    containerId = "C102",
                    truckId = "T07",
                    slotId = "S1645",
                    slotStart = "2026-10-28T16:45:00+05:30",
                    slotEnd = "2026-10-28T17:15:00+05:30",
                    checkpointId = "LIRA-EDGE-01",
                    status = AssignmentStatus.APPROVED,
                    driverAckState = DriverAckState.PENDING,
                    physicalState = PhysicalState.EXPECTED,
                    reasonCode = "RECOVERY_WAVE_2",
                    reasonText = "Physical no-show resolved via QpiAI Opt. Container rescheduled to Slot S1645.",
                    customsRelease = "CONFIRMED",
                    deadline = "2026-10-28T19:30:00+05:30",
                    projectedAvoidableCostInr = 15000,
                    evidenceIds = listOf("EV-DOC-102", "EV-OPT-R3"),
                    updatedAt = currentTime(),
                    recoveryWave = 2,
                    planVersion = 2,
                    isSuperseded = false,
                    requiredEquipment = "40ft Skeletal Chassis, 3-Axle",
                    urgency = UrgencyLevel.ELEVATED,
                    driverObligations = "Verify EIR slip, inspect container seal #SL-8849",
                    activeInvestigationTask = currentInvestigationTask
                )
            )
            else -> emptyList()
        }
    }

    fun acknowledge(assignmentId: String, action: String, reason: String?): AckResponse {
        val newState = if (action == "ACKNOWLEDGE") DriverAckState.ACKNOWLEDGED else DriverAckState.CANNOT_COMPLY
        driverAckState = newState

        val eventId = "EVT-${sequenceCounter++}"
        eventsList.add(
            DriverEvent(
                eventId = eventId,
                sequence = sequenceCounter,
                type = if (action == "ACKNOWLEDGE") EventType.DRIVER_ACKNOWLEDGED else EventType.DRIVER_CANNOT_COMPLY,
                assignmentId = assignmentId,
                createdAt = currentTime(),
                payload = mapOf(
                    "action" to action,
                    "reason" to (reason ?: ""),
                    "truck_id" to "T07"
                )
            )
        )

        return AckResponse(
            accepted = true,
            assignmentId = assignmentId,
            driverAckState = newState,
            eventId = eventId,
            recoveryWaveTriggered = (action == "CANNOT_COMPLY")
        )
    }

    fun reportEta(truckId: String, etaIso: String, reason: String): EtaReportResponse {
        val eventId = "EVT-${sequenceCounter++}"
        eventsList.add(
            DriverEvent(
                eventId = eventId,
                sequence = sequenceCounter,
                type = EventType.ETA_UPDATED,
                assignmentId = "ASN-102-R2",
                createdAt = currentTime(),
                payload = mapOf(
                    "truck_id" to truckId,
                    "reported_eta" to etaIso,
                    "reason" to reason,
                    "source" to "DRIVER_SELF_REPORT"
                )
            )
        )
        return EtaReportResponse(
            accepted = true,
            eventId = eventId,
            recoveryStatus = null,
            planValid = true
        )
    }

    fun getDisruptions(): List<Disruption> {
        return listOf(
            Disruption(
                eventId = "EXT-01",
                type = "HEAVY_RAIN",
                trustState = TrustState.FORECAST_RISK,
                severity = Severity.HIGH,
                title = "Heavy rain may delay the approach road",
                sourceLabel = "Weather feed / DEMO",
                affectedTrucks = listOf("T07"),
                startsAt = "2026-10-28T15:00:00+05:30",
                endsAt = "2026-10-28T19:00:00+05:30",
                updatedAt = "2026-10-28T14:45:00+05:30"
            ),
            Disruption(
                eventId = "EXT-02",
                type = "PORT_MAINTENANCE",
                trustState = TrustState.CONFIRMED_DISRUPTION,
                severity = Severity.MEDIUM,
                title = "Terminal Gate 4 express bay slow due to scheduled maintenance",
                sourceLabel = "Port Operations Feed",
                affectedTrucks = listOf("T07", "T04"),
                startsAt = "2026-10-28T14:00:00+05:30",
                endsAt = "2026-10-28T18:00:00+05:30",
                updatedAt = "2026-10-28T14:30:00+05:30"
            ),
            Disruption(
                eventId = "EXT-03",
                type = DisruptionType.TRAFFIC_CONGESTION,
                trustState = TrustState.FORECAST_RISK,
                severity = Severity.HIGH,
                title = "Heavy port corridor traffic delay",
                sourceLabel = "Traffic Feed via FastAPI Control Tower",
                affectedTrucks = listOf("T07"),
                startsAt = "2026-10-28T14:00:00+05:30",
                endsAt = "2026-10-28T16:00:00+05:30",
                updatedAt = "2026-10-28T14:30:00+05:30",
                riskLevelLabel = "PREDICTED TRAFFIC DELAY (NON-AUTHORITATIVE)",
                dataSourceMode = DataSourceMode.LIVE_EXTERNAL,
                ttlMinutes = 30,
                predictedDelayMinutes = 20,
                revisedEta = "2026-10-28T15:20:00+05:30",
                bookedSlotCutoff = "2026-10-28T15:30:00+05:30",
                provider = "via FastAPI Control Tower"
            ),
            Disruption(
                eventId = "EXT-04",
                type = DisruptionType.CONFIRMED_PORT_CLOSURE,
                trustState = TrustState.CONFIRMED_DISRUPTION,
                severity = Severity.CRITICAL,
                title = "Confirmed Port Closure",
                sourceLabel = "Port Gate System",
                affectedTrucks = listOf("T07", "T09"),
                startsAt = "2026-10-28T14:00:00+05:30",
                endsAt = "2026-10-28T18:00:00+05:30",
                updatedAt = "2026-10-28T14:30:00+05:30",
                riskLevelLabel = "CONFIRMED PORT CLOSURE (AUTHORITATIVE)"
            )
        )
    }

    fun getEvents(after: Long): DriverEventsResponse {
        val filtered = eventsList.filter { it.sequence > after }
        return DriverEventsResponse(
            items = filtered,
            nextSequence = eventsList.lastOrNull()?.sequence ?: after
        )
    }

    fun getCheckpointStatus(assignmentId: String): CheckpointStatus {
        return when (_demoStage.value) {
            0 -> CheckpointStatus(
                assignmentId = assignmentId,
                deviceId = "LIRA-EDGE-01",
                state = CheckpointStateV9.ASSIGNMENT_DISPATCHED,
                rfidState = CheckpointRfidState.NOT_SCANNED,
                bayPresent = false,
                lastEventAt = null,
                resourceLock = ResourceLockStatus(
                    resourceState = ResourceState.AWAITING_CUSTODIAN_CONFIRMATION,
                    operatorWebApprovalState = CustodianWebApprovalState.PENDING_OPERATOR_WEB_ACTION
                )
            )
            1 -> CheckpointStatus(
                assignmentId = assignmentId,
                deviceId = "LIRA-EDGE-01",
                state = CheckpointStateV9.TAG_MATCHED,
                rfidState = CheckpointRfidState.TAG_MATCHED,
                bayPresent = true,
                lastEventAt = currentTime(),
                resourceLock = ResourceLockStatus(
                    resourceState = ResourceState.CHECKOUT_ACCEPTED,
                    operatorWebApprovalState = CustodianWebApprovalState.APPROVED_ON_WEB
                )
            )
            2 -> CheckpointStatus(
                assignmentId = assignmentId,
                deviceId = "LIRA-EDGE-01",
                state = PhysicalState.PHYSICAL_NO_SHOW,
                rfidState = CheckpointRfidState.NOT_SCANNED,
                bayPresent = false,
                lastEventAt = currentTime(),
                resourceLock = ResourceLockStatus(
                    resourceState = ResourceState.QUARANTINED,
                    rejectionCode = "RESOURCE_DAMAGED",
                    recoveryReviewStatus = "RECOVERY UNDER REVIEW",
                    terminalAvailability = "HOLD_FOR_INSPECTION"
                ),
                tier3aRfidObservation = Tier3ARfidState.TAG_MISMATCH,
                tier4CustodianWebAttestation = Tier4CustodianWebState.EXCEPTION_REPORTED_ON_WEB,
                tier1PortProof = CheckpointStateV9.RECOVERY_REQUIRED
            )
            3 -> CheckpointStatus(
                assignmentId = assignmentId,
                deviceId = "LIRA-EDGE-01",
                state = PhysicalState.EXPECTED,
                rfidState = CheckpointRfidState.NOT_SCANNED,
                bayPresent = false,
                lastEventAt = null
            )
            else -> CheckpointStatus(
                assignmentId = assignmentId,
                deviceId = "LIRA-EDGE-01",
                state = PhysicalState.EXPECTED,
                rfidState = CheckpointRfidState.NOT_SCANNED,
                bayPresent = false,
                lastEventAt = null
            )
        }
    }

    fun verifyQrCustody(assignmentId: String, qrPayload: String): VerifyQrCustodyResponse {
        val verified = qrPayload.startsWith("LIRA-QR")
        return VerifyQrCustodyResponse(
            verified = verified,
            tokenValid = verified,
            assignmentId = assignmentId,
            message = if (verified) "Hardware-to-Mobile Chain-of-Custody Verified via HMAC-8 Token." else "Invalid custody token signature.",
            verifiedAt = currentTime(),
            chainOfCustodyStatus = if (verified) "CUSTODY_ATTESTED_HMAC8_MATCH" else "TOKEN_REJECTED",
            ratchet = 42L,
            hmac8 = "B78E49A1"
        )
    }
}
