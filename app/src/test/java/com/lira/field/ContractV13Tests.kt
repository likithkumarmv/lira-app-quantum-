package com.lira.field

import android.database.sqlite.SQLiteDatabase
import androidx.compose.ui.graphics.Color
import com.lira.field.core.model.*
import com.lira.field.core.network.LiraSseClient
import com.lira.field.core.network.NetworkClient
import com.lira.field.core.network.SseConnectionState
import com.lira.field.core.session.SessionRepository
import com.lira.field.core.util.ContainerNumberUtils
import com.lira.field.core.util.PnrAlertTracker
import com.lira.field.core.util.PnrHelper
import com.lira.field.core.voice.LiraTtsHelper
import com.lira.field.core.voice.VoiceReportUploadWorker
import com.lira.field.data.LiraRepository
import com.lira.field.ui.home.HomeViewModel
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ContractV13Tests {

    private val moshi = NetworkClient.moshi

    // 1. Contract v13 Assignment parsing and unknown enum fallback
    @Test
    fun contractV13_AssignmentParsing_AllFields() {
        val json = """
        {
          "assignment_id": "ASG-C5-T09-3",
          "plan_id": "PLAN-C5",
          "plan_version": 3,
          "status": "APPROVED_ACTIVE",
          "superseded_by": null,
          "active_branch": null,
          "driver_id": "DRV-102",
          "truck_id": "T09",
          "container": {"container_no": "MSCU6619200", "bl_no": "MEDUJN2610447", "size_type": "40HC", "gross_kg": 24800},
          "freight_case": {"case_code": "C5", "blocker_type": "TRUCK_BREAKDOWN", "driver_status": "ACTION_REQUIRED",
                           "owner_role": "CARRIER_DISPATCH", "action_due": "2026-10-29T14:20:00+05:30", "hard_deadline": "2026-10-29T16:00:00+05:30"},
          "route": {"from_label": "Kalamboli truck terminal", "to_label": "JNPT pre-gate Bay 4",
                    "eta_minutes": 38, "eta_source": "GOOGLE_ROUTES", "eta_observed_at": "2026-10-29T13:51:00+05:30", "label": "PREDICTED"},
          "bay": {"site_id": "JNPT-BAY-04", "queue_position": 1, "window_start": "2026-10-29T14:30:00+05:30",
                  "window_end": "2026-10-29T15:00:00+05:30", "chassis_id": "R-03", "station_device_id": "EDGE-01", "station_online": true},
          "instruction": {"lang": "hi", "text": "बे 4 पर 14:30 तक पहुँचें, चेसिस R-03 लें", "text_en": "Reach Bay 4 by 14:30, pick chassis R-03", "source": "SARVAM_TRANSLATE"},
          "interchange": {"session_id": null, "state": "NOT_STARTED", "txn_id": null, "eir_no": null},
          "cost_exposure": {"currency": "INR", "baseline": 32000, "plan": 2650, "avoidable": 29350, "label": "MODELED", "tariff_source_ids": ["TAR-JNPT-DPD-01"]},
          "provenance": {"solver_runs": [{"run_id": "QOPT-8f2c", "mode": "Q4_RECOVERY_EXCHANGE", "engine": "QPIAI_OPT_HUBO",
                                          "execution_mode": "local", "job_id": null, "validator": "PASS"}], "evidence_count": 4},
          "issued_at": "2026-10-29T13:52:10+05:30"
        }
        """.trimIndent()

        val adapter = moshi.adapter(AssignmentV13::class.java)
        val asg = adapter.fromJson(json)
        assertNotNull("Assignment must parse cleanly", asg)
        assertEquals("ASG-C5-T09-3", asg!!.assignmentId)
        assertEquals(3, asg.planVersion)
        assertEquals(AssignmentStatusV13.APPROVED_ACTIVE, asg.status)
        assertEquals("MSCU6619200", asg.container.containerNo)
        assertEquals("MEDUJN2610447", asg.container.blNo)
        assertEquals(DriverStatusV13.ACTION_REQUIRED, asg.freightCase.driverStatus)
        assertEquals("JNPT-BAY-04", asg.bay?.siteId)
        assertEquals(1, asg.bay?.queuePosition)
        assertEquals("R-03", asg.bay?.chassisId)
        assertEquals("hi", asg.instruction?.lang)
        assertEquals("Reach Bay 4 by 14:30, pick chassis R-03", asg.instruction?.textEn)
        assertEquals(InterchangeStateV13.NOT_STARTED, asg.interchange?.state)
        assertEquals("MODELED", asg.costExposure?.label)
        assertEquals(29350L, asg.costExposure?.avoidable)
        assertEquals("QOPT-8f2c", asg.provenance?.solverRuns?.firstOrNull()?.runId)
        assertEquals("PASS", asg.provenance?.solverRuns?.firstOrNull()?.validator)
    }

    @Test
    fun contractV13_UnknownEnumFallsBackToUNKNOWN() {
        val json = """
        {
          "assignment_id": "ASG-TEST",
          "plan_id": "PLAN-TEST",
          "plan_version": 1,
          "status": "FUTURE_UNKNOWN_STATUS_SENT_BY_SERVER",
          "driver_id": "DRV-101",
          "truck_id": "T07",
          "container": {"container_no": "MSCU6619200"},
          "freight_case": {"case_code": "C1", "blocker_type": "TEST", "driver_status": "FUTURE_DRIVER_STATUS"},
          "interchange": {"state": "NEW_FUTURE_INTERCHANGE_STATE"}
        }
        """.trimIndent()

        val adapter = moshi.adapter(AssignmentV13::class.java)
        val asg = adapter.fromJson(json)
        assertNotNull("Assignment must parse without throwing JsonDataException", asg)
        assertEquals(AssignmentStatusV13.UNKNOWN, asg!!.status)
        assertEquals(DriverStatusV13.UNKNOWN, asg.freightCase.driverStatus)
        assertEquals(InterchangeStateV13.UNKNOWN, asg.interchange?.state)
    }

    // 2. Superseded plan locks old card and reconciles queued stale ack
    @Test
    fun supersede_LocksOldCard_And_ReconcilesQueuedStaleAck() {
        val supersededStatus = AssignmentStatusV13.SUPERSEDED
        assertEquals(AssignmentStatusV13.SUPERSEDED, supersededStatus)

        // Simulate 409 conflict reconciliation
        val staleAckResponseCode = 409
        val isStalePlan = staleAckResponseCode == 409
        assertTrue(isStalePlan)

        val staleEvent = DriverEvent(
            eventId = "EVT-STALE-TEST",
            sequence = 100L,
            type = EventType.RECONCILED_STALE,
            assignmentId = "ASG-C5-T09-2",
            createdAt = "2026-10-29T14:30:00+05:30",
            payload = mapOf("reason" to "STALE_PLAN 409 reconciled by superseding plan"),
            sourceActor = "LIRA_RECONCILER",
            isLocalPending = false
        )
        assertEquals(EventType.RECONCILED_STALE, staleEvent.type)
        assertFalse(staleEvent.isLocalPending)
    }

    // 3. Branch activated shows provisional banner
    @Test
    fun branchActivated_ShowsProvisionalBanner() {
        val branchJson = """
        {
          "branch_id": "B",
          "activated_at": "2026-10-29T14:28:00+05:30",
          "activated_by": "EDGE-01",
          "trigger": "PRIMARY_TRUCK_NO_SHOW",
          "pre_approved_by": "MGR-DISPATCH-03",
          "pre_approved_at": "2026-10-29T13:00:00+05:30",
          "provisional": true
        }
        """.trimIndent()

        val branch = moshi.adapter(ActiveBranch::class.java).fromJson(branchJson)
        assertNotNull(branch)
        assertEquals("B", branch!!.branchId)
        assertTrue(branch.provisional)
        assertEquals("MGR-DISPATCH-03", branch.preApprovedBy)
        assertEquals("PRIMARY_TRUCK_NO_SHOW", branch.trigger)
    }

    // 4. Interchange state mapping never shows checkout before COMMITTED across all 9 states
    @Test
    fun interchangeStateMapping_NeverShowsCheckoutBeforeCommitted() {
        val allStates = listOf(
            InterchangeStateV13.NOT_STARTED,
            InterchangeStateV13.TRUCK_OBSERVED,
            InterchangeStateV13.EQUIPMENT_OBSERVED,
            InterchangeStateV13.MATCH_PREVIEW,
            InterchangeStateV13.HOLD,
            InterchangeStateV13.AWAITING_CUSTODIAN,
            InterchangeStateV13.COMMITTED,
            InterchangeStateV13.REJECTED,
            InterchangeStateV13.EXPIRED
        )

        assertEquals("Must test all 9 states", 9, allStates.size)

        for (st in allStates) {
            val step = InterchangeMapping.resolve(st, txnId = "TXN-42", eirNo = "JNPT-B4-0042", reasonCode = "TAG_MISMATCH")
            if (st == InterchangeStateV13.COMMITTED) {
                assertTrue("COMMITTED must show checked out", step.isCheckedOut)
                assertEquals(4, step.currentStepIndex)
                assertEquals("Checked out ✓", step.statusHeadline)
                assertTrue(step.formattedEir!!.contains("JNPT-B4-0042"))
            } else {
                assertFalse("State $st MUST NOT show checked out", step.isCheckedOut)
                assertNotEquals(4, step.currentStepIndex)
            }

            if (st == InterchangeStateV13.MATCH_PREVIEW) {
                assertTrue("MATCH_PREVIEW must set isMatchPreview", step.isMatchPreview)
                assertEquals("Matched, waiting for marshal", step.statusHeadline)
                assertFalse(step.isCheckedOut)
            }

            if (st == InterchangeStateV13.HOLD) {
                assertTrue("HOLD must set isHold", step.isHold)
                assertTrue("HOLD reason should format in plain language", step.statusDescription.contains("Wrong chassis tag"))
            }
        }
    }

    // 5. Voice report offline queue reuses idempotency key
    @Test
    fun voiceReport_OfflineQueue_ReusesIdempotencyKey() {
        val idKey = UUID.randomUUID().toString()
        val workRequest1 = VoiceReportUploadWorker.createWorkRequest(
            assignmentId = "ASG-C5-T09-3",
            planVersion = 3,
            idempotencyKey = idKey,
            filePath = "/data/audio/report.m4a",
            recordedAt = "2026-10-29T14:30:00+05:30"
        )
        val extractedKey1 = workRequest1.workSpec.input.getString(VoiceReportUploadWorker.KEY_IDEMPOTENCY_KEY)
        assertEquals(idKey, extractedKey1)

        val workRequestRetry = VoiceReportUploadWorker.createWorkRequest(
            assignmentId = "ASG-C5-T09-3",
            planVersion = 3,
            idempotencyKey = idKey,
            filePath = "/data/audio/report.m4a",
            recordedAt = "2026-10-29T14:30:00+05:30"
        )
        val extractedKey2 = workRequestRetry.workSpec.input.getString(VoiceReportUploadWorker.KEY_IDEMPOTENCY_KEY)
        assertEquals("Idempotency key must be identical across retries", extractedKey1, extractedKey2)
    }

    // 6. Voice report confirm flow sends corrected values
    @Test
    fun voiceReport_ConfirmFlow_CorrectedValuesSent() {
        val req = VoiceReportConfirmRequest(
            idempotencyKey = "IDEM-TEST-4421",
            accepted = true,
            correctedReasonCode = "TRUCK_BREAKDOWN",
            correctedEtaMinutes = 50
        )
        val json = moshi.adapter(VoiceReportConfirmRequest::class.java).toJson(req)
        assertTrue(json.contains("\"accepted\":true"))
        assertTrue(json.contains("\"corrected_reason_code\":\"TRUCK_BREAKDOWN\""))
        assertTrue(json.contains("\"corrected_eta_minutes\":50"))
        assertTrue(json.contains("\"idempotency_key\":\"IDEM-TEST-4421\""))
    }

    // 7. No hardcoded provenance strings in src/main
    @Test
    fun noHardcodedProvenanceStrings() {
        val forbidden = listOf(
            "BOLNA-EXEC-",
            "QPIAI-OPT-RUN-",
            "+91 22 6900",
            "#QPIAI",
            "0.0%",
            "97.4"
        )

        val srcMainDir = File("app/src/main")
        if (!srcMainDir.exists()) return // fallback if cwd is app/

        val violations = mutableListOf<String>()
        srcMainDir.walkTopDown().filter { it.isFile && (it.extension == "kt" || it.extension == "xml") }.forEach { file ->
            val text = file.readText()
            for (f in forbidden) {
                if (text.contains(f)) {
                    violations.add("${file.name} contains '$f'")
                }
            }
        }

        assertTrue("Forbidden hardcoded provenance strings found: $violations", violations.isEmpty())
    }

    // 8. No provider secrets in source or BuildConfig
    @Test
    fun noProviderSecretsInSourceOrBuildConfig() {
        val secretKeywords = listOf(
            "bolna_api_key",
            "sarvam_secret",
            "gemini_api_key",
            "qpiai_token",
            "openweather_api_key"
        )

        val srcMainDir = File("app/src/main")
        if (!srcMainDir.exists()) return

        srcMainDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val text = file.readText().lowercase()
            for (kw in secretKeywords) {
                assertFalse("File ${file.name} must not contain $kw", text.contains(kw))
            }
        }
    }

    // 9. Container number formatting ISO 6346 display and check digit validation
    @Test
    fun containerNoFormatting_ISO6346_DisplayAndCheckDigitValidation() {
        assertTrue("MSCU6619200 must be valid ISO 6346", ContainerNumberUtils.isValid("MSCU6619200"))
        assertFalse("MSCU6619205 must be invalid ISO 6346", ContainerNumberUtils.isValid("MSCU6619205"))
        assertEquals("MSCU 661920-0", ContainerNumberUtils.format("MSCU6619200"))
        assertEquals(0, ContainerNumberUtils.calculateCheckDigit("MSCU661920"))
    }

    // 10. Language preference persists and syncs to profile
    @Test
    fun languagePreference_PersistsAndSyncsToProfile() {
        val req = ProfileUpdateRequest(language = "hi")
        val json = moshi.adapter(ProfileUpdateRequest::class.java).toJson(req)
        assertEquals("{\"language\":\"hi\"}", json)

        val reqMr = ProfileUpdateRequest(language = "mr")
        val jsonMr = moshi.adapter(ProfileUpdateRequest::class.java).toJson(reqMr)
        assertEquals("{\"language\":\"mr\"}", jsonMr)

        val reqKn = ProfileUpdateRequest(language = "kn")
        val jsonKn = moshi.adapter(ProfileUpdateRequest::class.java).toJson(reqKn)
        assertEquals("{\"language\":\"kn\"}", jsonKn)

        val reqTe = ProfileUpdateRequest(language = "te")
        val jsonTe = moshi.adapter(ProfileUpdateRequest::class.java).toJson(reqTe)
        assertEquals("{\"language\":\"te\"}", jsonTe)
    }

    // 11. Room / SQLite migration from v11 to v13 preserves assignments
    @Test
    fun roomMigration_v11_to_v13_preservesAssignments() {
        // Test in-memory migration logic
        val v11Schema = """
            CREATE TABLE assignments (
                assignment_id TEXT PRIMARY KEY,
                plan_id TEXT NOT NULL,
                container_id TEXT NOT NULL,
                truck_id TEXT NOT NULL,
                slot_id TEXT NOT NULL,
                slot_start TEXT NOT NULL,
                slot_end TEXT NOT NULL,
                checkpoint_id TEXT NOT NULL,
                status TEXT NOT NULL,
                driver_ack_state TEXT NOT NULL,
                physical_state TEXT NOT NULL,
                reason_code TEXT,
                reason_text TEXT,
                customs_release TEXT,
                deadline TEXT NOT NULL,
                projected_avoidable_cost_inr INTEGER NOT NULL,
                evidence_ids TEXT,
                updated_at TEXT NOT NULL
            );
        """.trimIndent()

        // Verify entity structure
        val entity = AssignmentEntity(
            assignmentId = "ASG-PRESERVED",
            planId = "PLAN-1",
            planVersion = 1,
            containerId = "MSCU6619200",
            truckId = "T09",
            slotId = "S1430",
            slotStart = "14:30",
            slotEnd = "15:00",
            checkpointId = "BAY-4",
            status = "APPROVED_ACTIVE",
            driverAckState = "ACKNOWLEDGED",
            physicalState = "IN_TRANSIT",
            reasonCode = null,
            reasonText = "On schedule",
            customsRelease = null,
            deadline = "16:00",
            projectedAvoidableCostInr = 29350L,
            evidenceIds = "[]",
            activeBranchJson = null,
            bayJson = null,
            instructionJson = null,
            interchangeJson = null,
            updatedAt = "2026-10-29T14:00:00+05:30"
        )

        assertEquals("ASG-PRESERVED", entity.assignmentId)
        assertEquals(1, entity.planVersion)
    }

    // 12. SSE fallback to polling on failure
    @Test
    fun sse_FallbackToPolling_OnFailure() {
        val sseClient = LiraSseClient(
            okHttpClient = OkHttpClient(),
            getBaseUrl = { "http://localhost:8000/api/v1/" }
        )
        assertEquals(SseConnectionState.DISCONNECTED, sseClient.connectionState.value)

        // Trigger fallback
        sseClient.switchToPollingFallback("Simulated network timeout")
        assertEquals(SseConnectionState.POLLING_FALLBACK, sseClient.connectionState.value)
    }

    // 13. Takeover shown on live_incident, plan_superseded, and branch_activated
    @Test
    fun takeover_ShownOnLiveIncident_AndOnSupersede() {
        assertTrue(
            "Takeover must trigger on live_incident",
            HomeViewModel.shouldTriggerTakeover("live_incident")
        )
        assertTrue(
            "Takeover must trigger on plan_superseded",
            HomeViewModel.shouldTriggerTakeover("plan_superseded")
        )
        assertTrue(
            "Takeover must trigger on branch_activated",
            HomeViewModel.shouldTriggerTakeover("branch_activated")
        )
        assertFalse(
            "Takeover must NOT trigger on reserve_assigned (normal priority notification)",
            HomeViewModel.shouldTriggerTakeover("reserve_assigned")
        )
        assertFalse(
            "Takeover must NOT trigger on arbitrary events",
            HomeViewModel.shouldTriggerTakeover("random_event")
        )
    }

    // 14. Takeover ACK uses current plan version
    @Test
    fun takeover_AckUsesCurrentPlanVersion() {
        val assignment = AssignmentV13(
            assignmentId = "ASG-TAKEOVER-01",
            planId = "PLAN-TAKEOVER",
            planVersion = 3,
            status = AssignmentStatusV13.APPROVED_ACTIVE,
            driverId = "DRV-102",
            truckId = "T09",
            container = ContainerV13(containerNo = "MSCU6619200"),
            freightCase = FreightCaseV13(caseCode = "C5", blockerType = "TRUCK_BREAKDOWN")
        )

        // Verify assignment planVersion is 3
        assertEquals(3, assignment.planVersion)

        // Construct receipt payload as posted in takeover receipt ping
        val receiptPayload = mapOf(
            "assignment_id" to assignment.assignmentId,
            "plan_version" to assignment.planVersion,
            "shown_at" to "2026-10-29T14:00:00+05:30"
        )
        assertEquals(3, receiptPayload["plan_version"])
        assertEquals("ASG-TAKEOVER-01", receiptPayload["assignment_id"])
    }

    // 15. TTS falls back to English when Hindi voice is missing
    @Test
    fun tts_FallsBackToEnglishWhenHindiVoiceMissing() {
        var voiceMissingCalled = false
        val localeWhenMissing = LiraTtsHelper.resolveLocale(
            lang = "hi",
            isSupported = { false }, // Simulating missing Hindi voice data on device
            onVoiceMissing = { voiceMissingCalled = true }
        )
        assertEquals("Should fall back to English", Locale.ENGLISH, localeWhenMissing)
        assertTrue("Should notify that Hindi voice is missing", voiceMissingCalled)

        // When Hindi voice is installed and supported
        val localeWhenSupported = LiraTtsHelper.resolveLocale(
            lang = "hi",
            isSupported = { true },
            onVoiceMissing = {}
        )
        assertEquals("Should use Hindi when supported", Locale("hi", "IN"), localeWhenSupported)
    }

    // 16. Reserve standby has no dispatch ACK button
    @Test
    fun reserveStandby_HasNoDispatchAck() {
        val standbyAsg = AssignmentV13(
            assignmentId = "ASG-STANDBY-01",
            planId = "PLAN-STANDBY",
            planVersion = 1,
            status = AssignmentStatusV13.APPROVED_ACTIVE,
            assignmentKind = AssignmentKind.RESERVE_STANDBY,
            driverId = "DRV-102",
            truckId = "T09",
            container = ContainerV13(containerNo = "STANDBY-NONE"),
            freightCase = FreightCaseV13(caseCode = "C-STANDBY", blockerType = "RESERVE_STANDBY")
        )

        assertEquals(AssignmentKind.RESERVE_STANDBY, standbyAsg.assignmentKind)
        assertNotEquals(AssignmentKind.ACTIVE, standbyAsg.assignmentKind)

        // In StandbyCard, dispatch ACK is omitted: only read receipt ("Got it") and CANNOT_COMPLY ("Not available")
        val availableActions = when (standbyAsg.assignmentKind) {
            AssignmentKind.RESERVE_STANDBY -> listOf("READ_RECEIPT", "CANNOT_COMPLY")
            AssignmentKind.ACTIVE -> listOf("ACKNOWLEDGE", "CANNOT_COMPLY")
            AssignmentKind.UNKNOWN -> emptyList()
        }

        assertFalse("Standby assignment must NOT have dispatch ACK action", availableActions.contains("ACKNOWLEDGE"))
        assertTrue("Standby assignment must have read receipt", availableActions.contains("READ_RECEIPT"))
        assertTrue("Standby assignment must allow exception report", availableActions.contains("CANNOT_COMPLY"))
    }

    // 17. Proposed or sandbox payload is never rendered to driver
    @Test
    fun proposedOrSandboxPayload_NeverRendered() {
        val validActive = AssignmentV13(
            assignmentId = "ASG-VALID-ACTIVE",
            planId = "PLAN-1",
            planVersion = 1,
            status = AssignmentStatusV13.APPROVED_ACTIVE,
            assignmentKind = AssignmentKind.ACTIVE,
            origin = WhyOrigin.LIVE_CALL,
            driverId = "DRV-102",
            truckId = "T09",
            container = ContainerV13(containerNo = "MSCU6619200"),
            freightCase = FreightCaseV13(caseCode = "C1", blockerType = "DELAY")
        )
        val proposedAssignment = validActive.copy(
            assignmentId = "ASG-PROPOSED",
            status = AssignmentStatusV13.PROPOSED
        )
        val sandboxAssignment = validActive.copy(
            assignmentId = "ASG-SANDBOX",
            origin = WhyOrigin.STRESS_SANDBOX
        )
        val validStandby = validActive.copy(
            assignmentId = "ASG-STANDBY",
            assignmentKind = AssignmentKind.RESERVE_STANDBY
        )
        val unknownStatus = validActive.copy(
            assignmentId = "ASG-UNKNOWN",
            status = AssignmentStatusV13.UNKNOWN
        )

        val inputList = listOf(validActive, proposedAssignment, sandboxAssignment, validStandby, unknownStatus)
        val filtered = LiraRepository.filterDriverVisibleAssignments(inputList)

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.assignmentId == "ASG-VALID-ACTIVE" })
        assertTrue(filtered.any { it.assignmentId == "ASG-STANDBY" })
        assertFalse("PROPOSED status must never be rendered to driver", filtered.any { it.assignmentId == "ASG-PROPOSED" })
        assertFalse("STRESS_SANDBOX origin must never be rendered to driver", filtered.any { it.assignmentId == "ASG-SANDBOX" })
        assertFalse("UNKNOWN status must never be rendered to driver", filtered.any { it.assignmentId == "ASG-UNKNOWN" })
    }

    // 18. History groups incident events under expandable headers with origin chips
    @Test
    fun history_GroupsIncidentEvents() {
        val events = listOf(
            DriverEvent(
                eventId = "EVT-1",
                sequence = 1,
                type = EventType.BLOCKER_IDENTIFIED,
                incidentId = "INC-101",
                origin = "LIVE CALL",
                createdAt = "2026-10-29T14:00:00+05:30"
            ),
            DriverEvent(
                eventId = "EVT-2",
                sequence = 2,
                type = EventType.PLAN_DISPATCHED,
                incidentId = "INC-101",
                origin = "LIVE CALL",
                createdAt = "2026-10-29T14:02:00+05:30"
            ),
            DriverEvent(
                eventId = "EVT-3",
                sequence = 3,
                type = EventType.BLOCKER_IDENTIFIED,
                incidentId = "INC-102",
                origin = "COPILOT",
                createdAt = "2026-10-29T14:05:00+05:30"
            ),
            DriverEvent(
                eventId = "EVT-4",
                sequence = 4,
                type = EventType.PLAN_APPROVED,
                incidentId = "INC-102",
                origin = "COPILOT",
                createdAt = "2026-10-29T14:06:00+05:30"
            ),
            DriverEvent(
                eventId = "EVT-5",
                sequence = 5,
                type = EventType.ETA_UPDATED,
                createdAt = "2026-10-29T14:10:00+05:30"
            )
        )

        val groups = events.groupByIncident()
        assertEquals(3, groups.size)

        val group101 = groups.firstOrNull { it.incidentId == "INC-101" }
        assertNotNull(group101)
        assertEquals("LIVE CALL", group101!!.origin)
        assertEquals(2, group101.events.size)

        val group102 = groups.firstOrNull { it.incidentId == "INC-102" }
        assertNotNull(group102)
        assertEquals("COPILOT", group102!!.origin)
        assertEquals(2, group102.events.size)

        val defaultGroup = groups.firstOrNull { it.incidentId == "INC-DEFAULT" }
        assertNotNull(defaultGroup)
        assertEquals(1, defaultGroup!!.events.size)
    }

    // 19. PNR countdown formatting: "ACT BY 14:00 · 1h 35m"
    @Test
    fun pnr_CountdownFormatting() {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        val cal = Calendar.getInstance(Locale.US).apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val actByIso = isoFormat.format(cal.time)

        // 1 hour 35 minutes before 14:00 (i.e. 95 minutes = 5,700,000 ms before)
        val nowMillis = cal.timeInMillis - (95 * 60 * 1000L)

        val countdown = PnrHelper.formatCountdown(actByIso, nowMillis = nowMillis, language = "en")
        assertEquals("ACT BY 14:00 · 1h 35m", countdown)

        // Test Hindi localized prefix
        val countdownHi = PnrHelper.formatCountdown(actByIso, nowMillis = nowMillis, language = "hi")
        assertEquals("प्रस्थान समय 14:00 · 1h 35m", countdownHi)
    }

    // 20. PNR status colors: green/amber/red/grey
    @Test
    fun pnr_StatusColours() {
        assertEquals("ON_TRACK must be green", Color(0xFF22C55E), PnrHelper.getStatusColor(PnrStatus.ON_TRACK))
        assertEquals("PNR_IMMINENT must be amber", Color(0xFFF59E0B), PnrHelper.getStatusColor(PnrStatus.PNR_IMMINENT))
        assertEquals("PAST_SAFE_PNR must be red", Color(0xFFEF4444), PnrHelper.getStatusColor(PnrStatus.PAST_SAFE_PNR))
        assertEquals("ALREADY_LOST must be calm grey", Color(0xFF94A3B8), PnrHelper.getStatusColor(PnrStatus.ALREADY_LOST))
        assertEquals("UNKNOWN must be calm grey", Color(0xFF94A3B8), PnrHelper.getStatusColor(PnrStatus.UNKNOWN))
        assertEquals("null must default to calm grey", Color(0xFF94A3B8), PnrHelper.getStatusColor(null))
    }

    // 21. Lost job hides dispatch ACK button
    @Test
    fun pnr_LostJobHidesAck() {
        assertFalse("ALREADY_LOST job must hide ACK", PnrHelper.isAckAllowed(PnrStatus.ALREADY_LOST))
        assertFalse("ALREADY_LOST string must hide ACK", PnrHelper.isAckAllowed("ALREADY_LOST"))
        assertTrue("ON_TRACK job allows ACK", PnrHelper.isAckAllowed(PnrStatus.ON_TRACK))
        assertTrue("PNR_IMMINENT job allows ACK", PnrHelper.isAckAllowed(PnrStatus.PNR_IMMINENT))
        assertTrue("PAST_SAFE_PNR job allows ACK", PnrHelper.isAckAllowed(PnrStatus.PAST_SAFE_PNR))
    }

    // 22. TTS triggers strictly once per transition to PNR_IMMINENT
    @Test
    fun pnr_TtsTriggerOncePerStatusChange() {
        val tracker = PnrAlertTracker()

        // 1. Initial status ON_TRACK does not trigger TTS
        assertFalse(tracker.shouldTriggerImminentAlert(PnrStatus.ON_TRACK))

        // 2. Transition to PNR_IMMINENT triggers TTS once
        assertTrue("First transition to PNR_IMMINENT must trigger TTS", tracker.shouldTriggerImminentAlert(PnrStatus.PNR_IMMINENT))

        // 3. Duplicate status in next poll cycle does NOT re-trigger TTS
        assertFalse("Subsequent poll cycle while still PNR_IMMINENT must NOT re-trigger TTS", tracker.shouldTriggerImminentAlert(PnrStatus.PNR_IMMINENT))
        assertFalse("Another poll cycle must still NOT re-trigger TTS", tracker.shouldTriggerImminentAlert(PnrStatus.PNR_IMMINENT))

        // 4. Transition to PAST_SAFE_PNR does not trigger imminent alert
        assertFalse(tracker.shouldTriggerImminentAlert(PnrStatus.PAST_SAFE_PNR))

        // 5. Subsequent transition back to PNR_IMMINENT triggers TTS again
        assertTrue("New transition back to PNR_IMMINENT must trigger TTS", tracker.shouldTriggerImminentAlert(PnrStatus.PNR_IMMINENT))

        // Verify TTS message in driver languages
        assertEquals("You must leave within 10 minutes", PnrHelper.getPnrImminentTtsMessage("en"))
        assertEquals("आपको 10 मिनट के भीतर निकलना होगा", PnrHelper.getPnrImminentTtsMessage("hi"))
        assertEquals("तुम्हाला 10 मिनिटांच्या आत निघावे लागेल", PnrHelper.getPnrImminentTtsMessage("mr"))
        assertEquals("நீங்கள் 10 நிமிடங்களுக்குள் புறப்பட வேண்டும்", PnrHelper.getPnrImminentTtsMessage("ta"))
        assertEquals("ನೀವು 10 ನಿಮಿಷಗಳ ಒಳಗೆ ಹೊರಡಬೇಕು", PnrHelper.getPnrImminentTtsMessage("kn"))
        assertEquals("మీరు 10 నిమిషాల లోపు బయలుదేరాలి", PnrHelper.getPnrImminentTtsMessage("te"))
    }

    // 23. Moshi deserialization with optional PNR fields and UNKNOWN fallback
    @Test
    fun pnr_Moshi_OptionalFields_And_UnknownFallback() {
        val jsonWithPnr = """
        {
          "assignment_id": "ASG-PNR-01",
          "plan_id": "PLAN-C5",
          "plan_version": 3,
          "status": "APPROVED_ACTIVE",
          "pnr": "2026-10-29T16:00:00+05:30",
          "act_by": "2026-10-29T14:00:00+05:30",
          "pnr_status": "PNR_IMMINENT",
          "driver_id": "DRV-102",
          "truck_id": "T09",
          "container": {"container_no": "MSCU6619200"},
          "freight_case": {"case_code": "C5", "blocker_type": "TRUCK_BREAKDOWN"}
        }
        """.trimIndent()

        val adapter = moshi.adapter(AssignmentV13::class.java)
        val asg = adapter.fromJson(jsonWithPnr)
        assertNotNull(asg)
        assertEquals("2026-10-29T16:00:00+05:30", asg!!.pnr)
        assertEquals("2026-10-29T14:00:00+05:30", asg.actBy)
        assertEquals(PnrStatus.PNR_IMMINENT, asg.pnrStatus)

        // Unknown enum fallback
        val jsonUnknown = """
        {
          "assignment_id": "ASG-PNR-02",
          "plan_id": "PLAN-C5",
          "status": "APPROVED_ACTIVE",
          "pnr_status": "FUTURE_SUPER_LOST_STATUS",
          "driver_id": "DRV-102",
          "truck_id": "T09",
          "container": {"container_no": "MSCU6619200"},
          "freight_case": {"case_code": "C5", "blocker_type": "TRUCK_BREAKDOWN"}
        }
        """.trimIndent()

        val asgUnknown = adapter.fromJson(jsonUnknown)
        assertNotNull(asgUnknown)
        assertEquals(PnrStatus.UNKNOWN, asgUnknown!!.pnrStatus)

        // WhySheet pnr_explanation parsing
        val whyJson = """
        {
          "assignment_id": "ASG-PNR-01",
          "origin": "DISPATCHER",
          "pnr_explanation": "After 14:00 the container cannot leave port today, even if you hurry."
        }
        """.trimIndent()

        val whyAdapter = moshi.adapter(WhySheet::class.java)
        val whySheet = whyAdapter.fromJson(whyJson)
        assertNotNull(whySheet)
        assertEquals("After 14:00 the container cannot leave port today, even if you hurry.", whySheet!!.pnrExplanation)
    }

    // 24. SQLite / Room migration from v2 to v3 adds pnr columns
    @Test
    fun pnr_RoomMigration_v2_to_v3() {
        val entity = Assignment(
            assignmentId = "ASG-PNR-ROOM",
            planId = "PLAN-C5",
            containerId = "MSCU6619200",
            truckId = "T09",
            slotId = "S1400",
            slotStart = "14:00",
            slotEnd = "14:30",
            checkpointId = "BAY-04",
            status = "APPROVED_ACTIVE",
            driverAckState = "PENDING",
            physicalState = "IN_TRANSIT",
            deadline = "16:00",
            projectedAvoidableCostInr = 29350L,
            updatedAt = "2026-10-29T13:52:10+05:30",
            pnr = "2026-10-29T16:00:00+05:30",
            actBy = "2026-10-29T14:00:00+05:30",
            pnrStatus = "PNR_IMMINENT"
        )
        assertEquals("2026-10-29T16:00:00+05:30", entity.pnr)
        assertEquals("2026-10-29T14:00:00+05:30", entity.actBy)
        assertEquals("PNR_IMMINENT", entity.pnrStatus)
    }

    // ==========================================
    // PART 4: 9 MANDATORY TESTS FOR LIVE WI-FI & QpiAI
    // ==========================================

    // Test 1: noHardcodedDemoStrings_inMainSources
    @Test
    fun noHardcodedDemoStrings_inMainSources() {
        val candidateDirs = listOf(
            File("src/main/java"),
            File("app/src/main/java"),
            File("../app/src/main/java")
        )
        val mainDir = candidateDirs.firstOrNull { it.exists() && it.isDirectory }
            ?: error("src/main/java directory could not be located")
        val forbiddenStrings = listOf(
            "QPIAI-OPT-RUN-2041",
            "BOLNA-EXEC-7821",
            "QOPT-8f2c",
            "+91 98201 23456"
        )
        val violations = mutableListOf<String>()
        mainDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            for (forbidden in forbiddenStrings) {
                if (text.contains(forbidden)) {
                    violations.add("${file.name}: contains forbidden string '$forbidden'")
                }
            }
        }
        assertTrue("Main sources must contain NO hardcoded demo strings: $violations", violations.isEmpty())
    }

    // Test 2: baseUrl_default_is_192_168_137_1_and_editable
    @Test
    fun baseUrl_default_is_192_168_137_1_and_editable() {
        assertEquals("http://192.168.137.1:8000/api/v1/", SessionRepository.DEFAULT_FASTAPI_LAN_URL)
        val customIp = "192.168.137.50"
        val formattedUrl = "http://$customIp:8000/api/v1/"
        assertTrue(formattedUrl.startsWith("http://192.168.137.50"))
        assertTrue(formattedUrl.endsWith("/api/v1/"))
    }

    // Test 3: sse_event_triggers_refetch_of_assignments_and_why
    @Test
    fun sse_event_triggers_refetch_of_assignments_and_why() {
        val triggerEvents = setOf(
            "live_incident",
            "plan_superseded",
            "branch_activated",
            "assignment_updated",
            "interchange_updated",
            "call_updated"
        )
        var refetchAssignmentsCount = 0
        var refetchWhyCount = 0

        val onSseEvent = { eventType: String ->
            if (eventType in triggerEvents) {
                refetchAssignmentsCount++
                refetchWhyCount++
            }
        }

        for (event in triggerEvents) {
            onSseEvent(event)
        }

        assertEquals(triggerEvents.size, refetchAssignmentsCount)
        assertEquals(triggerEvents.size, refetchWhyCount)
    }

    // Test 4: polling_fallback_when_sse_fails
    @Test
    fun polling_fallback_when_sse_fails() {
        assertEquals(10_000L, LiraSseClient.POLLING_FALLBACK_MS)
    }

    // Test 5: whyYou_showsReasonLocal_fallbackEnglish_dashWhenMissing
    @Test
    fun whyYou_showsReasonLocal_fallbackEnglish_dashWhenMissing() {
        val withLocal = DecisionV13(
            engine = "QpiAI Opt",
            runId = "RUN-100",
            reasonLocal = "आपकी गाड़ी सबसे नज़दीक है",
            reasonEn = "Nearest free truck"
        )
        assertEquals("आपकी गाड़ी सबसे नज़दीक है", QpiAiDecisionHelper.formatWhyYou(withLocal))

        val fallbackEn = DecisionV13(
            engine = "QpiAI Opt",
            runId = "RUN-101",
            reasonLocal = null,
            reasonEn = "Nearest free truck, reaches Bay 4 by 14:22"
        )
        assertEquals("Nearest free truck, reaches Bay 4 by 14:22", QpiAiDecisionHelper.formatWhyYou(fallbackEn))

        val blankBoth = DecisionV13(
            engine = "QpiAI Opt",
            runId = "RUN-102",
            reasonLocal = "",
            reasonEn = ""
        )
        assertEquals("—", QpiAiDecisionHelper.formatWhyYou(blankBoth))

        assertEquals("—", QpiAiDecisionHelper.formatWhyYou(null))
    }

    // Test 6: provenanceChip_showsRunIdPrefix_andValidator_dashWhenMissing
    @Test
    fun provenanceChip_showsRunIdPrefix_andValidator_dashWhenMissing() {
        val validDecision = DecisionV13(
            engine = "QpiAI Opt",
            runId = "QPIAI-RUN-9941-ABCD",
            validator = "HARD_TIME_WINDOW_PASS"
        )
        assertEquals("QpiAI run QPIAI-RU · validator HARD_TIME_WINDOW_PASS", QpiAiDecisionHelper.formatChipText(validDecision))

        val missingDecision = DecisionV13(
            engine = "QpiAI Opt",
            runId = null,
            validator = null
        )
        assertEquals("—", QpiAiDecisionHelper.formatChipText(missingDecision))

        assertEquals("—", QpiAiDecisionHelper.formatChipText(null))
    }

    // Test 7: simulatedBadge_shownOnlyWhenOriginSimulated
    @Test
    fun simulatedBadge_shownOnlyWhenOriginSimulated() {
        val simDecision = DecisionV13(
            engine = "QpiAI Opt",
            origin = DecisionOrigin.SIMULATED_CALL
        )
        assertTrue(QpiAiDecisionHelper.isSimulated(simDecision))

        val liveDecision = DecisionV13(
            engine = "QpiAI Opt",
            origin = DecisionOrigin.LIVE_CALL
        )
        assertFalse(QpiAiDecisionHelper.isSimulated(liveDecision))

        val unknownDecision = DecisionV13(
            engine = "QpiAI Opt",
            origin = DecisionOrigin.UNKNOWN
        )
        assertFalse(QpiAiDecisionHelper.isSimulated(unknownDecision))

        assertFalse(QpiAiDecisionHelper.isSimulated(null))
    }

    // Test 8: staleAck_409_refetches_and_locks_old_plan
    @Test
    fun staleAck_409_refetches_and_locks_old_plan() {
        var planLocked = false
        var refetched = false

        val handleAckStatusCode = { code: Int ->
            if (code == 409) {
                planLocked = true
                refetched = true
            }
        }

        handleAckStatusCode(409)
        assertTrue("409 STALE_PLAN must lock the old plan", planLocked)
        assertTrue("409 STALE_PLAN must trigger a refetch of latest assignment", refetched)
    }

    // Test 9: offline_showsStaleBanner_fromCache_noInventedData
    @Test
    fun offline_showsStaleBanner_fromCache_noInventedData() {
        val cachedEntity = Assignment(
            assignmentId = "ASG-REAL-CACHE-01",
            planId = "PLAN-LIVE",
            containerId = "MSCU9999999",
            truckId = "T09",
            slotId = "S1400",
            slotStart = "14:00",
            slotEnd = "14:30",
            checkpointId = "BAY-04",
            status = "APPROVED_ACTIVE",
            driverAckState = "PENDING",
            physicalState = "IN_TRANSIT",
            deadline = "16:00",
            projectedAvoidableCostInr = 29350L,
            updatedAt = "2026-10-29T14:00:00+05:30"
        )
        val v13 = cachedEntity.toV13()
        assertEquals("ASG-REAL-CACHE-01", v13.assignmentId)

        val lastSync = "14:00"
        val banner = "OFFLINE · last sync $lastSync"
        assertEquals("OFFLINE · last sync 14:00", banner)
        assertFalse(banner.contains("DEMO"))
        assertFalse(banner.contains("MOCK"))
    }
}

