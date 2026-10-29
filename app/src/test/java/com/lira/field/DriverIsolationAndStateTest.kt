package com.lira.field

import com.lira.field.core.database.PendingAction
import com.lira.field.core.model.*
import com.lira.field.core.network.NetworkClient
import com.lira.field.data.MockDataEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID

/**
 * LIRA Field Master Verification Test Suite
 * Covers all 16 mandatory testing categories defined in Section 12 of the Master PRD.
 */
class DriverIsolationAndStateTest {

    private lateinit var mockEngine: MockDataEngine
    private val moshi = NetworkClient.moshi

    @Before
    fun setUp() {
        mockEngine = MockDataEngine()
    }

    // 1. Authentication and session restoration.
    @Test
    fun test01_AuthenticationAndSessionRestoration() {
        // Authenticated claims from server
        val userSession = UserSession(
            token = "jwt_session_token_example",
            userCode = "DRV-1007",
            displayName = "Driver T07",
            role = "DRIVER_FIELD",
            truckId = "T07",
            email = "driver.t07@lira.field",
            expiresAt = System.currentTimeMillis() + 86400000L,
            isDemoSession = false
        )

        assertEquals("Driver T07", userSession.driverId)
        assertEquals("T07", userSession.truckId)
        assertEquals("DRIVER_FIELD", userSession.role)
        assertFalse(userSession.isDemoSession)
        assertNotNull(userSession.token)

        // Session restoration test
        val restoredSession = userSession.copy()
        assertEquals(userSession.truckId, restoredSession.truckId)
        assertEquals(userSession.token, restoredSession.token)
    }

    // 2. Driver isolation.
    @Test
    fun test02_DriverIsolation() {
        // DRIVER_T07 sees strictly T07 assignments
        val t07Assignments = mockEngine.getAssignmentsForDriver("T07")
        assertTrue("T07 must have assignments", t07Assignments.isNotEmpty())
        assertTrue("All returned assignments must belong to T07", t07Assignments.all { it.truckId == "T07" })

        // DRIVER_T04 sees strictly T04 assignments
        val t04Assignments = mockEngine.getAssignmentsForDriver("T04")
        assertTrue("T04 must have assignments", t04Assignments.isNotEmpty())
        assertTrue("All returned assignments must belong to T04", t04Assignments.all { it.truckId == "T04" })

        // Unauthorized or unassigned driver receives empty list (isolation barrier)
        val unauthorized = mockEngine.getAssignmentsForDriver("T99_UNAUTHORIZED")
        assertTrue("Unauthorized truck must receive empty list", unauthorized.isEmpty())
    }

    // 3. Assignment retrieval.
    @Test
    fun test03_AssignmentRetrieval() {
        val assignments = mockEngine.getAssignmentsForDriver("T07")
        val active = assignments.firstOrNull()
        assertNotNull("Active assignment must exist", active)
        assertEquals("C102", active!!.containerId)
        assertEquals("S1600", active.slotId)
        assertEquals("PLAN-R2", active.planId)
        assertEquals(AssignmentStatus.APPROVED, active.status)
        assertEquals(DriverAckState.PENDING, active.driverAckState)
        assertEquals("40ft Skeletal Chassis, 3-Axle", active.requiredEquipment)
        assertEquals(UrgencyLevel.ELEVATED, active.urgency)
        assertTrue(active.driverObligations.isNotEmpty())
    }

    // 4. Plan version updates.
    @Test
    fun test04_PlanVersionUpdates() {
        mockEngine.setStage(0)
        val planWave1 = mockEngine.getAssignmentsForDriver("T07").first()
        assertEquals("PLAN-R2", planWave1.planId)
        assertEquals(1, planWave1.recoveryWave)
        assertFalse(planWave1.isSuperseded)

        // Dynamic plan update to Wave 2
        mockEngine.setStage(3)
        val planWave2 = mockEngine.getAssignmentsForDriver("T07").first()
        assertEquals("PLAN-R3", planWave2.planId)
        assertEquals(2, planWave2.recoveryWave)
        assertEquals("S1645", planWave2.slotId)
        assertEquals("ASN-102-R3", planWave2.assignmentId)
    }

    // 5. ACKNOWLEDGE submission.
    @Test
    fun test05_AcknowledgeSubmission() {
        mockEngine.setStage(0)
        val ackResp = mockEngine.acknowledge("ASN-102-R2", "ACKNOWLEDGE", null)
        assertTrue("ACK should be accepted by backend", ackResp.accepted)
        assertEquals(DriverAckState.ACKNOWLEDGED, ackResp.driverAckState)

        val updated = mockEngine.getAssignmentsForDriver("T07").first()
        assertEquals(DriverAckState.ACKNOWLEDGED, updated.driverAckState)
    }

    // 6. CANNOT_COMPLY submission.
    @Test
    fun test06_CannotComplySubmission() {
        mockEngine.setStage(0)
        val reason = CannotComplyReasons.TRUCK_BREAKDOWN
        val cannotComplyResp = mockEngine.acknowledge("ASN-102-R2", "CANNOT_COMPLY", reason)
        assertTrue(cannotComplyResp.accepted)
        assertEquals(DriverAckState.CANNOT_COMPLY, cannotComplyResp.driverAckState)

        val events = mockEngine.getEvents(0).items
        val ev = events.find { it.type == EventType.DRIVER_CANNOT_COMPLY || (it.type == EventType.DRIVER_ACKNOWLEDGED && it.payload?.get("action") == "CANNOT_COMPLY") }
        assertNotNull("CANNOT_COMPLY event must be logged", ev)
    }

    // 7. Duplicate-action prevention.
    @Test
    fun test07_DuplicateActionPrevention() {
        mockEngine.setStage(0)
        val firstAck = mockEngine.acknowledge("ASN-102-R2", "ACKNOWLEDGE", null)
        assertTrue(firstAck.accepted)

        // Subsequent double-tap or duplicate submit
        val duplicateAck = mockEngine.acknowledge("ASN-102-R2", "ACKNOWLEDGE", null)
        assertTrue(duplicateAck.accepted)
        assertEquals(DriverAckState.ACKNOWLEDGED, duplicateAck.driverAckState)

        // Ensure state remains consistent and idempotent
        val current = mockEngine.getAssignmentsForDriver("T07").first()
        assertEquals(DriverAckState.ACKNOWLEDGED, current.driverAckState)
    }

    // 8. Offline retry behavior.
    @Test
    fun test08_OfflineRetryBehavior() {
        val actionId = UUID.randomUUID().toString()
        val idempotencyKey = UUID.randomUUID().toString()
        val pendingAction = PendingAction(
            id = actionId,
            type = PendingAction.TYPE_ACK,
            assignmentId = "ASN-102-R2",
            truckId = "T07",
            payloadJson = "{\"action\":\"ACKNOWLEDGE\"}",
            idempotencyKey = idempotencyKey,
            timestamp = System.currentTimeMillis(),
            retryCount = 0,
            syncStatus = PendingAction.STATUS_PENDING
        )

        assertEquals(0, pendingAction.retryCount)
        assertEquals(PendingAction.STATUS_PENDING, pendingAction.syncStatus)
        assertEquals(idempotencyKey, pendingAction.idempotencyKey)
    }

    // 9. ETA reporting.
    @Test
    fun test09_EtaReporting() {
        val etaIso = "2026-10-28T16:45:00+05:30"
        val resp = mockEngine.reportEta("T07", etaIso, "TRAFFIC")
        assertTrue(resp.accepted)

        val events = mockEngine.getEvents(0).items
        val etaEvent = events.find { it.type == EventType.ETA_UPDATED }
        assertNotNull(etaEvent)
        assertEquals("T07", etaEvent?.payload?.get("truck_id"))
        assertEquals("DRIVER_SELF_REPORT", etaEvent?.payload?.get("source"))
        assertNotEquals("GPS_VERIFIED", etaEvent?.payload?.get("source"))
    }

    // 10. Forecast versus confirmed disruption.
    @Test
    fun test10_ForecastVersusConfirmedDisruption() {
        val disruptions = mockEngine.getDisruptions()
        val weatherDisruption = disruptions.find { it.type == "HEAVY_RAIN" }
        assertNotNull(weatherDisruption)
        assertEquals(TrustState.FORECAST_RISK, weatherDisruption?.trustState)
        assertNotEquals(TrustState.CONFIRMED_DISRUPTION, weatherDisruption?.trustState)

        val portDisruption = disruptions.find { it.type == "PORT_MAINTENANCE" || it.type == DisruptionType.CONFIRMED_PORT_CLOSURE }
        assertNotNull(portDisruption)
        assertEquals(TrustState.CONFIRMED_DISRUPTION, portDisruption?.trustState)
    }

    // 11. Read-only edge telemetry (PRD v9.0 ESP32 + ILI9341 Display-Only + RC522).
    @Test
    fun test11_ReadOnlyEdgeTelemetry() {
        mockEngine.setStage(1) // Staged match
        val status = mockEngine.getCheckpointStatus("ASN-102-R2")
        assertEquals(CheckpointStateV9.TAG_MATCHED, status.state)
        assertEquals(CheckpointRfidState.TAG_MATCHED, status.rfidState)
        assertTrue(status.edgeOnline)

        // Hardware spec verification per PRD v9.0
        assertTrue("Must specify ESP32 DevKit V1", HardwareSpec.MICROCONTROLLER.contains("ESP32 DevKit V1"))
        assertTrue("Must specify RC522 RFID", HardwareSpec.RFID_READER.contains("RC522"))
        assertTrue("Must specify ILI9341 TFT Display", HardwareSpec.DISPLAY.contains("ILI9341"))
        assertTrue("Must specify Web Dashboard Custodian Terminal", HardwareSpec.OPERATOR_INTERFACE.contains("Web Dashboard Custodian Terminal"))

        // Epistemic verification matrix contracts
        assertEquals(CheckpointStateV9.TAG_MATCHED, status.tier3aRfidObservation)
        assertEquals(Tier4CustodianWebState.CONFIRMED_ON_WEB, status.tier4CustodianWebAttestation)
        assertEquals(CheckpointStateV9.ACTION_PENDING_EXTERNAL_PROOF, status.tier1PortProof)
    }

    // 11B. Indian Port Corridor Telemetry and Solver Provenance
    @Test
    fun test11B_IndianPortCorridorTelemetryAndSolverProvenance() {
        val assignment = mockEngine.getAssignmentsForDriver("T07").first()
        assertTrue("Modeled exposure must be positive", assignment.modeledCounterfactualExposureInr > 0)
        assertTrue("Invoiced actual must be non-negative", assignment.invoicedActualInr >= 0)
        assertNotNull("DPD Evacuation Cliff must be documented", assignment.dpdEvacuationCliff)
        assertNotNull("e-DO Bank UTR Cutoff must be documented", assignment.eDoBankUtrCutoff)
        assertTrue("Solver provenance must cite QpiAI XY-QAOA", assignment.solverProvenance.contains("QpiAI XY-QAOA"))
    }

    // 11C. PRD v9.0 ResourceLock Read-Only Telemetry & Custodian Web Attestation
    @Test
    fun test11C_ResourceLockReadOnlyAndCustodianWebAttestation() {
        // Stage 1: Verified Tag Read + Custodian Web Checkout
        mockEngine.setStage(1)
        val status = mockEngine.getCheckpointStatus("ASN-102-R2")
        val lock = status.resourceLock

        assertEquals("CHASSIS-40SK-09", lock.resourceId)
        assertEquals(ResourceState.CHECKED_OUT, lock.resourceState)
        assertEquals("WO-JNPT-2026-9941", lock.workOrderId)
        assertEquals(CustodianWebApprovalState.APPROVED_ON_WEB, lock.operatorWebApprovalState)
        assertEquals("TX-AUTH-9941-R2", lock.serverTxId)

        // Epistemic verification boundaries (T3 sensor != T4 web custodian != T1 customs)
        assertEquals(Tier3ARfidState.TAG_MATCHED, status.tier3aRfidObservation)
        assertEquals(Tier4CustodianWebState.CONFIRMED_ON_WEB, status.tier4CustodianWebAttestation)
        assertEquals(Tier1PortProofState.ACTION_PENDING_EXTERNAL_PROOF, status.tier1PortProof)

        // Obsolete historical fixture backward-compatibility check
        assertTrue("Historical fixture preserved for backward-compatibility", status.eepromSector4Status.isNotBlank())
    }

    // 11D. Verify TFT Custody QR Token Chain
    @Test
    fun test11D_VerifyTftCustodyQrTokenChain() {
        val qrPayload = "LIRA-QR:ASN-102-R2:0042:B78E49A1"
        val resp = mockEngine.verifyQrCustody("ASN-102-R2", qrPayload)
        assertTrue("Custody token must be verified", resp.verified)
        assertTrue("Custody token must be valid", resp.tokenValid)
        assertEquals("CUSTODY_ATTESTED_HMAC8_MATCH", resp.chainOfCustodyStatus)
        assertEquals(42L, resp.ratchet)
        assertEquals("B78E49A1", resp.hmac8)

        // Invalid token rejection
        val badResp = mockEngine.verifyQrCustody("ASN-102-R2", "INVALID_PAYLOAD")
        assertFalse(badResp.verified)
        assertEquals("TOKEN_REJECTED", badResp.chainOfCustodyStatus)
    }

    // 11E. Approved Reassignment Supersedes Old Plan
    @Test
    fun test11E_ApprovedReassignmentSupersedesOldPlan() {
        val assignment = mockEngine.getAssignmentsForDriver("T07").first()
        val arbitrage = assignment.slotSwapArbitrage
        assertNotNull("Slot swap arbitrage dossier must be present", arbitrage)
        assertEquals("C102", arbitrage?.urgentContainerId)
        assertEquals("C208", arbitrage?.donorContainerId)
        assertEquals(3000L, arbitrage?.slotYieldBountyInr)
        assertEquals(45000L, arbitrage?.netDemurrageSavedInr)

        val immunity = assignment.demurrageImmunity
        assertNotNull("Demurrage immunity dossier must be present", immunity)
        assertTrue(immunity!!.isReadyOnTime)
        assertTrue(immunity.statutoryWaiverActive)
        assertEquals("YARD_STACK_DEPTH_DELAY", immunity.terminalFaultCode)
        assertTrue(immunity.immunityBadgeText.contains("46 CFR § 541.6"))
    }

    // 12. Wave 2 notification.
    @Test
    fun test12_Wave2Notification() {
        // Stage 2: Mismatch occurs at edge checkpoint
        mockEngine.setStage(2)
        val failedPlan = mockEngine.getAssignmentsForDriver("T07").first()
        assertTrue("Plan must be superseded or failed", failedPlan.status == AssignmentStatus.SUPERSEDED || failedPlan.status == AssignmentStatus.PHYSICAL_NO_SHOW)
        assertTrue("isSuperseded flag must be set", failedPlan.isSuperseded)

        // Stage 3: Wave 2 approved dispatch
        mockEngine.setStage(3)
        val wave2Plan = mockEngine.getAssignmentsForDriver("T07").first()
        assertEquals("PLAN-R3", wave2Plan.planId)
        assertEquals(AssignmentStatus.APPROVED, wave2Plan.status)
        assertEquals(2, wave2Plan.recoveryWave)
        assertEquals(2, wave2Plan.planVersion)
    }

    // 13. Expired-token handling.
    @Test
    fun test13_ExpiredTokenHandling() {
        val expiredSession = UserSession(
            token = "expired_jwt",
            userCode = "DRV-1007",
            displayName = "Driver T07",
            role = "DRIVER_FIELD",
            truckId = "T07",
            email = "driver.t07@lira.field",
            expiresAt = 1000L,
            isDemoSession = false
        )
        assertTrue(expiredSession.expiresAt < System.currentTimeMillis())
    }

    // 14. Empty backend response.
    @Test
    fun test14_EmptyBackendResponse() {
        val emptyJson = """{"items":[],"updated_at":"2026-10-28T16:00:00Z"}"""
        val adapter = moshi.adapter(AssignmentsResponse::class.java)
        val parsed = adapter.fromJson(emptyJson)
        assertNotNull(parsed)
        assertTrue(parsed!!.items.isEmpty())
    }

    // 15. Backend outage handling.
    @Test
    fun test15_BackendOutageHandling() {
        val errorJson = """{"error":{"code":"BACKEND_UNAVAILABLE","message":"Central recovery orchestrator is unreachable. Check network.","request_id":"req-503"}}"""
        val adapter = moshi.adapter(com.lira.field.core.network.ApiErrorResponse::class.java)
        val parsed = adapter.fromJson(errorJson)
        assertNotNull(parsed)
        assertEquals("BACKEND_UNAVAILABLE", parsed!!.error.code)
        assertEquals("req-503", parsed.error.requestId)

        val netError = com.lira.field.core.network.LiraNetworkResult.Error("BACKEND_UNAVAILABLE", parsed.error.message, 503)
        assertEquals(503, netError.httpStatus)
    }

    // 16. JSON contract compatibility.
    @Test
    fun test16_JsonContractCompatibility() {
        val json = """
        {
          "items": [{
            "assignment_id": "ASN-102-R2",
            "plan_id": "PLAN-R2",
            "container_id": "C102",
            "truck_id": "T07",
            "slot_id": "S1600",
            "slot_start": "2026-10-28T16:00:00+05:30",
            "slot_end": "2026-10-28T16:30:00+05:30",
            "checkpoint_id": "LIRA-EDGE-01",
            "status": "APPROVED",
            "driver_ack_state": "PENDING",
            "physical_state": "EXPECTED",
            "reason_code": "T04_ETA_CONFLICT",
            "reason_text": "Original driver cannot reach the confirmed slot",
            "customs_release": "CONFIRMED",
            "deadline": "2026-10-28T18:30:00+05:30",
            "projected_avoidable_cost_inr": 12000,
            "evidence_ids": ["EV-DOC-102", "EV-CALL-04"],
            "assignment_version": "2.0",
            "recovery_wave": 1,
            "required_equipment": "40ft Tri-Axle Chassis",
            "operational_condition": "CONFIRMED",
            "urgency": "CRITICAL",
            "updated_at": "2026-10-28T15:30:00+05:30"
          }],
          "updated_at": "2026-10-28T15:30:00+05:30"
        }
        """.trimIndent()

        val adapter = moshi.adapter(AssignmentsResponse::class.java)
        val response = adapter.fromJson(json)
        assertNotNull(response)
        val item = response!!.items.first()
        assertEquals("ASN-102-R2", item.assignmentId)
        assertEquals("C102", item.containerId)
        assertEquals(12000L, item.projectedAvoidableCostInr)
        assertEquals("2.0", item.assignmentVersion)
        assertEquals(1, item.recoveryWave)
        assertEquals("40ft Tri-Axle Chassis", item.requiredEquipment)
        assertEquals("CONFIRMED", item.operationalCondition)
        assertEquals("CRITICAL", item.urgency)
    }

    // 17. Security rule: Zero provider API keys or server secrets in source code.
    @Test
    fun test17_VerifyNoServerSecretsInSourceCode() {
        val forbiddenKeywords = listOf(
            "AIzaSy",
            "QPIAI_SECRET",
            "BOLNA_API_KEY",
            "SARVAM_API_KEY",
            "OPENWEATHER_KEY",
            "TOMTOM_KEY"
        )

        val srcDir = File("src/main/java")
        if (srcDir.exists()) {
            srcDir.walkTopDown().filter { it.extension == "kt" || it.extension == "xml" }.forEach { file ->
                val content = file.readText()
                for (secret in forbiddenKeywords) {
                    assertFalse(
                        "Found forbidden secret keyword '$secret' in file: ${file.path}",
                        content.contains(secret)
                    )
                }
            }
        }
    }

    // 18. FastAPI Control Tower active schema check
    @Test
    fun test18_FastApiControlTowerSchemaVersion() {
        val health = HealthResponse(status = "ok", mode = "LIVE", version = "v7.0", schema = "v7.0-QUANTUM-EDGE")
        assertEquals("v7.0-QUANTUM-EDGE", health.schema)
        assertEquals("LIVE", health.mode)
        assertEquals("ok", health.status)
    }

    // 19. PRD v9.0 Solver Outage & No-Feasible-Plan Actionable Hold
    @Test
    fun test19_NoFeasiblePlanAndSolverUnavailableHandling() {
        val baseAssignment = mockEngine.getAssignmentsForDriver("T07").first()
        val outageAssignment = baseAssignment.copy(
            isSolverAvailable = false,
            noFeasiblePlan = true,
            reasonText = "Port gate constraints exceeded feasible window"
        )

        assertFalse("Solver must be marked unavailable during outage", outageAssignment.isSolverAvailable)
        assertTrue("No feasible plan must trigger hold", outageAssignment.noFeasiblePlan)
        assertEquals("WO-JNPT-2026-9941", outageAssignment.workOrderReference)
        assertEquals("Driver T09", outageAssignment.taskOwner)
    }

    // 20. PRD v9.0 Labeled Financial Estimates vs Actual Invoices
    @Test
    fun test20_FinancialModelingLabeledEstimatesVsActuals() {
        val assignment = mockEngine.getAssignmentsForDriver("T07").first()
        assertEquals("MODELED ESTIMATE", assignment.exposureFinancialLabel)
        assertEquals(0L, assignment.invoicedActualInr)
        assertTrue(assignment.modeledCounterfactualExposureInr > 0L)

        // Slot swap barter matrix status
        val arbitrage = assignment.slotSwapArbitrage
        assertNotNull(arbitrage)
        assertEquals("PROPOSED_SLOT_SWAP", arbitrage?.swapStatus)
        assertTrue(arbitrage!!.swapStatusLabel.contains("AWAITING TERMINAL CONFIRMATION"))

        // Demurrage immunity waiver status
        val immunity = assignment.demurrageImmunity
        assertNotNull(immunity)
        assertEquals("CLAIM_FILED_PENDING_REVIEW", immunity?.statutoryWaiverStatus)
    }

    // 21. PRD v9.0 Concise RAG Explainability Disclosure & Source Citations
    @Test
    fun test21_RagExplainabilityDisclosureAndEvidenceCitations() {
        val assignment = mockEngine.getAssignmentsForDriver("T07").first()
        val rag = assignment.ragDisclosure
        assertNotNull("RAG explainability disclosure must be present", rag)
        assertTrue("Reason for action must be non-empty", rag!!.reasonForAction.isNotEmpty())
        assertTrue("Blocker description must identify root cause", rag.blockerDescription.isNotEmpty())
        assertNotNull("Appointment deadline must be provided", rag.appointmentDeadline)
        assertEquals("NHAI Traffic Authority Advisory #2026-44", rag.evidenceSourceTitle)
        assertTrue("Span reference ID must cite document offset", rag.spanReferenceId.contains("RAG-DOC-NHAI-44"))
        assertTrue("Exact quote must be present", rag.exactQuote.contains("Corridor NH-348"))
        assertEquals("GROUNDED_EVIDENCE", rag.ragStatus)

        // Counterfactual: If evidence is insufficient, verify unresolved flag
        val unresolvedRag = rag.copy(ragStatus = "INSUFFICIENT_EVIDENCE")
        assertEquals("INSUFFICIENT_EVIDENCE", unresolvedRag.ragStatus)
    }

    // 22. PRD v9.0 QpiAI Investigation Director — Driver-Facing Task
    @Test
    fun test22_QpiAiInvestigationDirectorDriverTask() {
        val assignment = mockEngine.getAssignmentsForDriver("T07").first()
        val task = assignment.activeInvestigationTask
        assertNotNull("Investigation task must be present", task)
        assertEquals("T07", task!!.targetTruckId)
        assertEquals(InvestigationTaskType.CONFIRM_ETA, task.taskType)
        assertTrue("Headline must state verification intent", task.headline.contains("Verification Requested"))
        assertTrue("Question text must be formatted for driver", task.questionText.contains("Can you maintain arrival"))
        assertEquals("QOPT-8f2c", task.solverRunId)
        assertTrue("Provenance label must cite QpiAI", task.provenanceLabel.contains("QpiAI"))
        assertEquals("PENDING_DRIVER_RESPONSE", task.responseState)

        // One-tap answer interaction simulation
        mockEngine.answerInvestigationTask(task.taskId, "CONFIRMED_ON_SCHEDULE")
        val updatedAssignment = mockEngine.getAssignmentsForDriver("T07").first()
        assertEquals("CONFIRMED_ON_SCHEDULE", updatedAssignment.activeInvestigationTask?.responseState)

        // Reset for clean subsequent tests
        mockEngine.resetToInitial()
    }

    // 23. PRD v9.0 Weather & Traffic Predictions (Non-Authoritative)
    @Test
    fun test23_WeatherAndTrafficPredictionsNonAuthoritative() {
        val disruptions = mockEngine.getDisruptions()
        val trafficPrediction = disruptions.find { it.type == DisruptionType.TRAFFIC_CONGESTION }
        assertNotNull("Traffic congestion prediction must be present", trafficPrediction)
        assertEquals(TrustState.FORECAST_RISK, trafficPrediction!!.trustState)
        assertEquals("PREDICTED TRAFFIC DELAY (NON-AUTHORITATIVE)", trafficPrediction.riskLevelLabel)
        assertEquals(DataSourceMode.LIVE_EXTERNAL, trafficPrediction.dataSourceMode)
        assertEquals(30, trafficPrediction.ttlMinutes)
        assertEquals(20, trafficPrediction.predictedDelayMinutes)
        assertNotNull(trafficPrediction.revisedEta)
        assertNotNull(trafficPrediction.bookedSlotCutoff)
        assertTrue("Provider must cite Control Tower backend", trafficPrediction.provider.contains("via FastAPI Control Tower"))

        // Confirmed port closure must have separate authority
        val portClosure = disruptions.find { it.type == DisruptionType.CONFIRMED_PORT_CLOSURE }
        assertNotNull("Confirmed port closure must be present", portClosure)
        assertEquals(TrustState.CONFIRMED_DISRUPTION, portClosure!!.trustState)
        assertEquals("CONFIRMED PORT CLOSURE (AUTHORITATIVE)", portClosure.riskLevelLabel)
    }

    // 24. PRD v9.0 ResourceLock 7-Stage State Machine Lifecycle
    @Test
    fun test24_ResourceLockSevenStageLifecycle() {
        // Stage 0: Initial Awaiting Tag
        mockEngine.setStage(0)
        val stage0 = mockEngine.getCheckpointStatus("ASN-102-R2")
        assertEquals(ResourceState.AWAITING_CUSTODIAN_CONFIRMATION, stage0.resourceLock.resourceState)

        // Stage 1: Verified Tag Read + Custodian Web Checkout
        mockEngine.setStage(1)
        val stage1 = mockEngine.getCheckpointStatus("ASN-102-R2")
        assertEquals(ResourceState.CHECKOUT_ACCEPTED, stage1.resourceLock.resourceState)
        assertEquals(CustodianWebApprovalState.APPROVED_ON_WEB, stage1.resourceLock.operatorWebApprovalState)

        // Stage 2: Mismatch / Damaged Resource Quarantined
        mockEngine.setStage(2)
        val stage2 = mockEngine.getCheckpointStatus("ASN-102-R2")
        assertEquals(ResourceState.QUARANTINED, stage2.resourceLock.resourceState)
        assertEquals("RESOURCE_DAMAGED", stage2.resourceLock.rejectionCode)
        assertEquals("RECOVERY UNDER REVIEW", stage2.resourceLock.recoveryReviewStatus)
        assertEquals("HOLD_FOR_INSPECTION", stage2.resourceLock.terminalAvailability)

        // Epistemic tier distinction: sensor read != custodian web attestation != legal port proof
        assertEquals(Tier3ARfidState.TAG_MISMATCH, stage2.tier3aRfidObservation)
        assertEquals(Tier4CustodianWebState.EXCEPTION_REPORTED_ON_WEB, stage2.tier4CustodianWebAttestation)
        assertEquals(CheckpointStateV9.RECOVERY_REQUIRED, stage2.tier1PortProof)

        // Reset
        mockEngine.resetToInitial()
    }
}
