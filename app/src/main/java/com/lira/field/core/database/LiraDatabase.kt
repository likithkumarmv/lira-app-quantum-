package com.lira.field.core.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lira.field.core.model.*
import com.lira.field.core.network.NetworkClient
import com.squareup.moshi.Types

class LiraDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val stringListAdapter = NetworkClient.moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE assignments (
                assignment_id TEXT PRIMARY KEY,
                plan_id TEXT NOT NULL,
                plan_version INTEGER NOT NULL DEFAULT 1,
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
                active_branch TEXT,
                bay TEXT,
                instruction TEXT,
                interchange TEXT,
                pnr TEXT,
                act_by TEXT,
                pnr_status TEXT,
                decision TEXT,
                why_sheet TEXT,
                updated_at TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE pending_actions (
                id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                assignment_id TEXT,
                truck_id TEXT NOT NULL,
                payload_json TEXT NOT NULL,
                idempotency_key TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                retry_count INTEGER NOT NULL DEFAULT 0,
                sync_status TEXT NOT NULL,
                last_error TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE disruptions (
                event_id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                trust_state TEXT NOT NULL,
                severity TEXT NOT NULL,
                title TEXT NOT NULL,
                source_label TEXT NOT NULL,
                affected_trucks TEXT,
                starts_at TEXT NOT NULL,
                ends_at TEXT,
                updated_at TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE events (
                event_id TEXT PRIMARY KEY,
                sequence INTEGER NOT NULL,
                type TEXT NOT NULL,
                assignment_id TEXT,
                created_at TEXT NOT NULL,
                payload TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE checkpoint_status (
                assignment_id TEXT PRIMARY KEY,
                device_id TEXT NOT NULL,
                state TEXT NOT NULL,
                rfid_state TEXT NOT NULL,
                bay_present INTEGER NOT NULL,
                last_event_at TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS calls (
                call_id TEXT PRIMARY KEY,
                execution_id TEXT,
                agent_id_masked TEXT,
                direction TEXT NOT NULL,
                language TEXT,
                purpose TEXT,
                question TEXT,
                status TEXT NOT NULL,
                from_number_masked TEXT,
                started_at TEXT,
                ended_at TEXT,
                transcript_excerpt_original TEXT,
                transcript_excerpt_en TEXT,
                extracted_json TEXT,
                verification TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS interchanges (
                session_id TEXT PRIMARY KEY,
                site_id TEXT NOT NULL,
                truck_id TEXT NOT NULL,
                truck_tag_ref_masked TEXT,
                equipment_id TEXT,
                state TEXT NOT NULL,
                local_decision TEXT,
                reason_code TEXT,
                challenge_code TEXT,
                txn_id TEXT,
                eir_no TEXT,
                eir_document_id TEXT,
                observed_at TEXT,
                committed_at TEXT,
                branch_id_applied TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS voice_reports (
                report_id TEXT PRIMARY KEY,
                assignment_id TEXT NOT NULL,
                status TEXT NOT NULL,
                language_detected TEXT,
                transcript_original TEXT,
                transcript_en TEXT,
                interpretation_json TEXT,
                evidence_tier TEXT NOT NULL,
                provider TEXT NOT NULL,
                provider_request_id TEXT,
                created_at TEXT NOT NULL
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Non-destructive Room / SQLite migration for v11 to v13
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN plan_version INTEGER NOT NULL DEFAULT 1")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN active_branch TEXT")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN bay TEXT")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN instruction TEXT")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN interchange TEXT")
            } catch (_: Exception) { }

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS calls (
                    call_id TEXT PRIMARY KEY,
                    execution_id TEXT,
                    agent_id_masked TEXT,
                    direction TEXT NOT NULL,
                    language TEXT,
                    purpose TEXT,
                    question TEXT,
                    status TEXT NOT NULL,
                    from_number_masked TEXT,
                    started_at TEXT,
                    ended_at TEXT,
                    transcript_excerpt_original TEXT,
                    transcript_excerpt_en TEXT,
                    extracted_json TEXT,
                    verification TEXT NOT NULL
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS interchanges (
                    session_id TEXT PRIMARY KEY,
                    site_id TEXT NOT NULL,
                    truck_id TEXT NOT NULL,
                    truck_tag_ref_masked TEXT,
                    equipment_id TEXT,
                    state TEXT NOT NULL,
                    local_decision TEXT,
                    reason_code TEXT,
                    challenge_code TEXT,
                    txn_id TEXT,
                    eir_no TEXT,
                    eir_document_id TEXT,
                    observed_at TEXT,
                    committed_at TEXT,
                    branch_id_applied TEXT
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS voice_reports (
                    report_id TEXT PRIMARY KEY,
                    assignment_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    language_detected TEXT,
                    transcript_original TEXT,
                    transcript_en TEXT,
                    interpretation_json TEXT,
                    evidence_tier TEXT NOT NULL,
                    provider TEXT NOT NULL,
                    provider_request_id TEXT,
                    created_at TEXT NOT NULL
                );
                """.trimIndent()
            )
        }

        if (oldVersion < 3) {
            // Non-destructive Room / SQLite migration for Point of No Return (pnr, act_by, pnr_status)
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN pnr TEXT")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN act_by TEXT")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN pnr_status TEXT")
            } catch (_: Exception) { }
        }

        if (oldVersion < 4) {
            // Non-destructive Room / SQLite migration for QpiAI decision and whySheet caching
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN decision TEXT")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE assignments ADD COLUMN why_sheet TEXT")
            } catch (_: Exception) { }
        }
    }

    val assignmentDao: AssignmentDao = object : AssignmentDao {
        override fun insertOrUpdateAll(assignments: List<Assignment>) {
            val db = writableDatabase
            db.beginTransaction()
            try {
                assignments.forEach { insertOrUpdate(it) }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        override fun insertOrUpdate(assignment: Assignment) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("assignment_id", assignment.assignmentId)
                put("plan_id", assignment.planId)
                put("plan_version", assignment.planVersion)
                put("container_id", assignment.containerId)
                put("truck_id", assignment.truckId)
                put("slot_id", assignment.slotId)
                put("slot_start", assignment.slotStart)
                put("slot_end", assignment.slotEnd)
                put("checkpoint_id", assignment.checkpointId)
                put("status", assignment.status)
                put("driver_ack_state", assignment.driverAckState)
                put("physical_state", assignment.physicalState)
                put("reason_code", assignment.reasonCode)
                put("reason_text", assignment.reasonText)
                put("customs_release", assignment.customsRelease)
                put("deadline", assignment.deadline)
                put("projected_avoidable_cost_inr", assignment.projectedAvoidableCostInr)
                put("evidence_ids", stringListAdapter.toJson(assignment.evidenceIds))
                put("pnr", assignment.pnr)
                put("act_by", assignment.actBy)
                put("pnr_status", assignment.pnrStatus)
                put("updated_at", assignment.updatedAt)
            }
            db.insertWithOnConflict("assignments", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }

        override fun getAssignmentsForTruck(truckId: String): List<Assignment> {
            val db = readableDatabase
            val cursor = db.query(
                "assignments",
                null,
                "truck_id = ?",
                arrayOf(truckId),
                null,
                null,
                "updated_at DESC"
            )
            return cursor.use { readAssignments(it) }
        }

        override fun getAssignmentById(assignmentId: String): Assignment? {
            val db = readableDatabase
            val cursor = db.query(
                "assignments",
                null,
                "assignment_id = ?",
                arrayOf(assignmentId),
                null,
                null,
                null
            )
            return cursor.use {
                if (it.moveToFirst()) readAssignment(it) else null
            }
        }

        override fun updateAckState(assignmentId: String, ackState: String, newStatus: String?) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("driver_ack_state", ackState)
                if (newStatus != null) {
                    put("status", newStatus)
                }
            }
            db.update("assignments", values, "assignment_id = ?", arrayOf(assignmentId))
        }

        override fun clearForTruck(truckId: String) {
            writableDatabase.delete("assignments", "truck_id = ?", arrayOf(truckId))
        }

        override fun clearAll() {
            writableDatabase.delete("assignments", null, null)
        }

        private fun readAssignments(cursor: Cursor): List<Assignment> {
            val list = mutableListOf<Assignment>()
            while (cursor.moveToNext()) {
                list.add(readAssignment(cursor))
            }
            return list
        }

        private fun readAssignment(cursor: Cursor): Assignment {
            val evidenceJson = cursor.getString(cursor.getColumnIndexOrThrow("evidence_ids"))
            val evidenceList = try {
                if (!evidenceJson.isNullOrBlank()) stringListAdapter.fromJson(evidenceJson) ?: emptyList() else emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            return Assignment(
                assignmentId = cursor.getString(cursor.getColumnIndexOrThrow("assignment_id")),
                planId = cursor.getString(cursor.getColumnIndexOrThrow("plan_id")),
                planVersion = if (cursor.getColumnIndex("plan_version") >= 0) cursor.getInt(cursor.getColumnIndex("plan_version")) else 1,
                containerId = cursor.getString(cursor.getColumnIndexOrThrow("container_id")),
                truckId = cursor.getString(cursor.getColumnIndexOrThrow("truck_id")),
                slotId = cursor.getString(cursor.getColumnIndexOrThrow("slot_id")),
                slotStart = cursor.getString(cursor.getColumnIndexOrThrow("slot_start")),
                slotEnd = cursor.getString(cursor.getColumnIndexOrThrow("slot_end")),
                checkpointId = cursor.getString(cursor.getColumnIndexOrThrow("checkpoint_id")),
                status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                driverAckState = cursor.getString(cursor.getColumnIndexOrThrow("driver_ack_state")),
                physicalState = cursor.getString(cursor.getColumnIndexOrThrow("physical_state")),
                reasonCode = cursor.getString(cursor.getColumnIndexOrThrow("reason_code")),
                reasonText = cursor.getString(cursor.getColumnIndexOrThrow("reason_text")),
                customsRelease = cursor.getString(cursor.getColumnIndexOrThrow("customs_release")),
                deadline = cursor.getString(cursor.getColumnIndexOrThrow("deadline")),
                projectedAvoidableCostInr = cursor.getLong(cursor.getColumnIndexOrThrow("projected_avoidable_cost_inr")),
                evidenceIds = evidenceList,
                pnr = if (cursor.getColumnIndex("pnr") >= 0) cursor.getString(cursor.getColumnIndex("pnr")) else null,
                actBy = if (cursor.getColumnIndex("act_by") >= 0) cursor.getString(cursor.getColumnIndex("act_by")) else null,
                pnrStatus = if (cursor.getColumnIndex("pnr_status") >= 0) cursor.getString(cursor.getColumnIndex("pnr_status")) else null,
                updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
            )
        }
    }

    val pendingActionDao: PendingActionDao = object : PendingActionDao {
        override fun insert(action: PendingAction) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("id", action.id)
                put("type", action.type)
                put("assignment_id", action.assignmentId)
                put("truck_id", action.truckId)
                put("payload_json", action.payloadJson)
                put("idempotency_key", action.idempotencyKey)
                put("timestamp", action.timestamp)
                put("retry_count", action.retryCount)
                put("sync_status", action.syncStatus)
                put("last_error", action.lastError)
            }
            db.insertWithOnConflict("pending_actions", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }

        override fun getAllPending(): List<PendingAction> {
            val db = readableDatabase
            val cursor = db.query(
                "pending_actions",
                null,
                null,
                null,
                null,
                null,
                "timestamp ASC"
            )
            val list = mutableListOf<PendingAction>()
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        PendingAction(
                            id = it.getString(it.getColumnIndexOrThrow("id")),
                            type = it.getString(it.getColumnIndexOrThrow("type")),
                            assignmentId = it.getString(it.getColumnIndexOrThrow("assignment_id")),
                            truckId = it.getString(it.getColumnIndexOrThrow("truck_id")),
                            payloadJson = it.getString(it.getColumnIndexOrThrow("payload_json")),
                            idempotencyKey = it.getString(it.getColumnIndexOrThrow("idempotency_key")),
                            timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                            retryCount = it.getInt(it.getColumnIndexOrThrow("retry_count")),
                            syncStatus = it.getString(it.getColumnIndexOrThrow("sync_status")),
                            lastError = it.getString(it.getColumnIndexOrThrow("last_error"))
                        )
                    )
                }
            }
            return list
        }

        override fun delete(id: String) {
            writableDatabase.delete("pending_actions", "id = ?", arrayOf(id))
        }

        override fun updateStatus(id: String, status: String, error: String?) {
            val values = ContentValues().apply {
                put("sync_status", status)
                put("last_error", error)
            }
            writableDatabase.update("pending_actions", values, "id = ?", arrayOf(id))
        }

        override fun incrementRetry(id: String) {
            writableDatabase.execSQL(
                "UPDATE pending_actions SET retry_count = retry_count + 1 WHERE id = ?",
                arrayOf(id)
            )
        }

        override fun getCount(): Int {
            val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM pending_actions", null)
            return cursor.use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
        }
    }

    // Disruptions Cache
    fun saveDisruptions(items: List<Disruption>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("disruptions", null, null)
            items.forEach { d ->
                val v = ContentValues().apply {
                    put("event_id", d.eventId)
                    put("type", d.type)
                    put("trust_state", d.trustState)
                    put("severity", d.severity)
                    put("title", d.title)
                    put("source_label", d.sourceLabel)
                    put("affected_trucks", stringListAdapter.toJson(d.affectedTrucks))
                    put("starts_at", d.startsAt)
                    put("ends_at", d.endsAt)
                    put("updated_at", d.updatedAt)
                }
                db.insertWithOnConflict("disruptions", null, v, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getDisruptions(): List<Disruption> {
        val cursor = readableDatabase.query("disruptions", null, null, null, null, null, "starts_at DESC")
        val list = mutableListOf<Disruption>()
        cursor.use {
            while (it.moveToNext()) {
                val trucksJson = it.getString(it.getColumnIndexOrThrow("affected_trucks"))
                val trucks = try {
                    if (!trucksJson.isNullOrBlank()) stringListAdapter.fromJson(trucksJson) ?: emptyList() else emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
                list.add(
                    Disruption(
                        eventId = it.getString(it.getColumnIndexOrThrow("event_id")),
                        type = it.getString(it.getColumnIndexOrThrow("type")),
                        trustState = it.getString(it.getColumnIndexOrThrow("trust_state")),
                        severity = it.getString(it.getColumnIndexOrThrow("severity")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        sourceLabel = it.getString(it.getColumnIndexOrThrow("source_label")),
                        affectedTrucks = trucks,
                        startsAt = it.getString(it.getColumnIndexOrThrow("starts_at")),
                        endsAt = it.getString(it.getColumnIndexOrThrow("ends_at")),
                        updatedAt = it.getString(it.getColumnIndexOrThrow("updated_at"))
                    )
                )
            }
        }
        return list
    }

    // Events cache
    fun saveEvent(event: DriverEvent) {
        val db = writableDatabase
        val v = ContentValues().apply {
            put("event_id", event.eventId)
            put("sequence", event.sequence)
            put("type", event.type)
            put("assignment_id", event.assignmentId)
            put("created_at", event.createdAt)
            put("payload", event.payload?.let { NetworkClient.moshi.adapter(Map::class.java).toJson(it) })
        }
        db.insertWithOnConflict("events", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getLocalEvents(): List<DriverEvent> {
        val cursor = readableDatabase.query("events", null, null, null, null, null, "sequence DESC")
        val list = mutableListOf<DriverEvent>()
        cursor.use {
            while (it.moveToNext()) {
                val payloadJson = it.getString(it.getColumnIndexOrThrow("payload"))
                val payloadMap: Map<String, Any?>? = try {
                    if (!payloadJson.isNullOrBlank()) {
                        @Suppress("UNCHECKED_CAST")
                        NetworkClient.moshi.adapter(Map::class.java).fromJson(payloadJson) as? Map<String, Any?>
                    } else null
                } catch (_: Exception) { null }

                list.add(
                    DriverEvent(
                        eventId = it.getString(it.getColumnIndexOrThrow("event_id")),
                        sequence = it.getLong(it.getColumnIndexOrThrow("sequence")),
                        type = it.getString(it.getColumnIndexOrThrow("type")),
                        assignmentId = it.getString(it.getColumnIndexOrThrow("assignment_id")),
                        createdAt = it.getString(it.getColumnIndexOrThrow("created_at")),
                        payload = payloadMap,
                        sourceActor = "LIRA_LOCAL",
                        isLocalPending = false
                    )
                )
            }
        }
        return list
    }

    // Checkpoint Status Cache
    fun saveCheckpointStatus(status: CheckpointStatus) {
        val v = ContentValues().apply {
            put("assignment_id", status.assignmentId)
            put("device_id", status.deviceId)
            put("state", status.state)
            put("rfid_state", status.rfidState)
            put("bay_present", if (status.bayPresent) 1 else 0)
            put("last_event_at", status.lastEventAt)
        }
        writableDatabase.insertWithOnConflict("checkpoint_status", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getCheckpointStatus(assignmentId: String): CheckpointStatus? {
        val cursor = readableDatabase.query(
            "checkpoint_status",
            null,
            "assignment_id = ?",
            arrayOf(assignmentId),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                CheckpointStatus(
                    assignmentId = it.getString(it.getColumnIndexOrThrow("assignment_id")),
                    deviceId = it.getString(it.getColumnIndexOrThrow("device_id")),
                    state = it.getString(it.getColumnIndexOrThrow("state")),
                    rfidState = it.getString(it.getColumnIndexOrThrow("rfid_state")),
                    bayPresent = it.getInt(it.getColumnIndexOrThrow("bay_present")) == 1,
                    lastEventAt = it.getString(it.getColumnIndexOrThrow("last_event_at"))
                )
            } else null
        }
    }

    // AssignmentEntity helpers
    fun insertOrUpdateAssignmentEntity(entity: AssignmentEntity) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("assignment_id", entity.assignmentId)
            put("plan_id", entity.planId)
            put("plan_version", entity.planVersion)
            put("container_id", entity.containerId)
            put("truck_id", entity.truckId)
            put("slot_id", entity.slotId)
            put("slot_start", entity.slotStart)
            put("slot_end", entity.slotEnd)
            put("checkpoint_id", entity.checkpointId)
            put("status", entity.status)
            put("driver_ack_state", entity.driverAckState)
            put("physical_state", entity.physicalState)
            put("reason_code", entity.reasonCode)
            put("reason_text", entity.reasonText)
            put("customs_release", entity.customsRelease)
            put("deadline", entity.deadline)
            put("projected_avoidable_cost_inr", entity.projectedAvoidableCostInr)
            put("evidence_ids", entity.evidenceIds)
            put("active_branch", entity.activeBranchJson)
            put("bay", entity.bayJson)
            put("instruction", entity.instructionJson)
            put("interchange", entity.interchangeJson)
            put("updated_at", entity.updatedAt)
        }
        db.insertWithOnConflict("assignments", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAssignmentEntityById(assignmentId: String): AssignmentEntity? {
        val db = readableDatabase
        val cursor = db.query("assignments", null, "assignment_id = ?", arrayOf(assignmentId), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) {
                AssignmentEntity(
                    assignmentId = it.getString(it.getColumnIndexOrThrow("assignment_id")),
                    planId = it.getString(it.getColumnIndexOrThrow("plan_id")),
                    planVersion = if (it.getColumnIndex("plan_version") >= 0) it.getInt(it.getColumnIndex("plan_version")) else 1,
                    containerId = it.getString(it.getColumnIndexOrThrow("container_id")),
                    truckId = it.getString(it.getColumnIndexOrThrow("truck_id")),
                    slotId = it.getString(it.getColumnIndexOrThrow("slot_id")),
                    slotStart = it.getString(it.getColumnIndexOrThrow("slot_start")),
                    slotEnd = it.getString(it.getColumnIndexOrThrow("slot_end")),
                    checkpointId = it.getString(it.getColumnIndexOrThrow("checkpoint_id")),
                    status = it.getString(it.getColumnIndexOrThrow("status")),
                    driverAckState = it.getString(it.getColumnIndexOrThrow("driver_ack_state")),
                    physicalState = it.getString(it.getColumnIndexOrThrow("physical_state")),
                    reasonCode = it.getString(it.getColumnIndexOrThrow("reason_code")),
                    reasonText = it.getString(it.getColumnIndexOrThrow("reason_text")),
                    customsRelease = it.getString(it.getColumnIndexOrThrow("customs_release")),
                    deadline = it.getString(it.getColumnIndexOrThrow("deadline")),
                    projectedAvoidableCostInr = it.getLong(it.getColumnIndexOrThrow("projected_avoidable_cost_inr")),
                    evidenceIds = it.getString(it.getColumnIndexOrThrow("evidence_ids")),
                    activeBranchJson = if (it.getColumnIndex("active_branch") >= 0) it.getString(it.getColumnIndex("active_branch")) else null,
                    bayJson = if (it.getColumnIndex("bay") >= 0) it.getString(it.getColumnIndex("bay")) else null,
                    instructionJson = if (it.getColumnIndex("instruction") >= 0) it.getString(it.getColumnIndex("instruction")) else null,
                    interchangeJson = if (it.getColumnIndex("interchange") >= 0) it.getString(it.getColumnIndex("interchange")) else null,
                    updatedAt = it.getString(it.getColumnIndexOrThrow("updated_at"))
                )
            } else null
        }
    }

    // CallEntity cache
    fun saveCalls(calls: List<CallEntity>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            calls.forEach { call ->
                val v = ContentValues().apply {
                    put("call_id", call.callId)
                    put("execution_id", call.executionId)
                    put("agent_id_masked", call.agentIdMasked)
                    put("direction", call.direction)
                    put("language", call.language)
                    put("purpose", call.purpose)
                    put("question", call.question)
                    put("status", call.status)
                    put("from_number_masked", call.fromNumberMasked)
                    put("started_at", call.startedAt)
                    put("ended_at", call.endedAt)
                    put("transcript_excerpt_original", call.transcriptExcerptOriginal)
                    put("transcript_excerpt_en", call.transcriptExcerptEn)
                    put("extracted_json", call.extractedJson)
                    put("verification", call.verification)
                }
                db.insertWithOnConflict("calls", null, v, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCalls(): List<CallEntity> {
        val cursor = readableDatabase.query("calls", null, null, null, null, null, "started_at DESC")
        val list = mutableListOf<CallEntity>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CallEntity(
                        callId = it.getString(it.getColumnIndexOrThrow("call_id")),
                        executionId = it.getString(it.getColumnIndexOrThrow("execution_id")),
                        agentIdMasked = it.getString(it.getColumnIndexOrThrow("agent_id_masked")),
                        direction = it.getString(it.getColumnIndexOrThrow("direction")),
                        language = it.getString(it.getColumnIndexOrThrow("language")),
                        purpose = it.getString(it.getColumnIndexOrThrow("purpose")),
                        question = it.getString(it.getColumnIndexOrThrow("question")),
                        status = it.getString(it.getColumnIndexOrThrow("status")),
                        fromNumberMasked = it.getString(it.getColumnIndexOrThrow("from_number_masked")),
                        startedAt = it.getString(it.getColumnIndexOrThrow("started_at")),
                        endedAt = it.getString(it.getColumnIndexOrThrow("ended_at")),
                        transcriptExcerptOriginal = it.getString(it.getColumnIndexOrThrow("transcript_excerpt_original")),
                        transcriptExcerptEn = it.getString(it.getColumnIndexOrThrow("transcript_excerpt_en")),
                        extractedJson = it.getString(it.getColumnIndexOrThrow("extracted_json")),
                        verification = it.getString(it.getColumnIndexOrThrow("verification"))
                    )
                )
            }
        }
        return list
    }

    // InterchangeEntity cache
    fun saveInterchanges(interchanges: List<InterchangeEntity>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            interchanges.forEach { ix ->
                val v = ContentValues().apply {
                    put("session_id", ix.sessionId)
                    put("site_id", ix.siteId)
                    put("truck_id", ix.truckId)
                    put("truck_tag_ref_masked", ix.truckTagRefMasked)
                    put("equipment_id", ix.equipmentId)
                    put("state", ix.state)
                    put("local_decision", ix.localDecision)
                    put("reason_code", ix.reasonCode)
                    put("challenge_code", ix.challengeCode)
                    put("txn_id", ix.txnId)
                    put("eir_no", ix.eirNo)
                    put("eir_document_id", ix.eirDocumentId)
                    put("observed_at", ix.observedAt)
                    put("committed_at", ix.committedAt)
                    put("branch_id_applied", ix.branchIdApplied)
                }
                db.insertWithOnConflict("interchanges", null, v, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getInterchanges(): List<InterchangeEntity> {
        val cursor = readableDatabase.query("interchanges", null, null, null, null, null, "observed_at DESC")
        val list = mutableListOf<InterchangeEntity>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    InterchangeEntity(
                        sessionId = it.getString(it.getColumnIndexOrThrow("session_id")),
                        siteId = it.getString(it.getColumnIndexOrThrow("site_id")),
                        truckId = it.getString(it.getColumnIndexOrThrow("truck_id")),
                        truckTagRefMasked = it.getString(it.getColumnIndexOrThrow("truck_tag_ref_masked")),
                        equipmentId = it.getString(it.getColumnIndexOrThrow("equipment_id")),
                        state = it.getString(it.getColumnIndexOrThrow("state")),
                        localDecision = it.getString(it.getColumnIndexOrThrow("local_decision")),
                        reasonCode = it.getString(it.getColumnIndexOrThrow("reason_code")),
                        challengeCode = it.getString(it.getColumnIndexOrThrow("challenge_code")),
                        txnId = it.getString(it.getColumnIndexOrThrow("txn_id")),
                        eirNo = it.getString(it.getColumnIndexOrThrow("eir_no")),
                        eirDocumentId = it.getString(it.getColumnIndexOrThrow("eir_document_id")),
                        observedAt = it.getString(it.getColumnIndexOrThrow("observed_at")),
                        committedAt = it.getString(it.getColumnIndexOrThrow("committed_at")),
                        branchIdApplied = it.getString(it.getColumnIndexOrThrow("branch_id_applied"))
                    )
                )
            }
        }
        return list
    }

    // VoiceReportEntity cache
    fun saveVoiceReport(report: VoiceReportEntity) {
        val db = writableDatabase
        val v = ContentValues().apply {
            put("report_id", report.reportId)
            put("assignment_id", report.assignmentId)
            put("status", report.status)
            put("language_detected", report.languageDetected)
            put("transcript_original", report.transcriptOriginal)
            put("transcript_en", report.transcriptEn)
            put("interpretation_json", report.interpretationJson)
            put("evidence_tier", report.evidenceTier)
            put("provider", report.provider)
            put("provider_request_id", report.providerRequestId)
            put("created_at", report.createdAt)
        }
        db.insertWithOnConflict("voice_reports", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getVoiceReports(): List<VoiceReportEntity> {
        val cursor = readableDatabase.query("voice_reports", null, null, null, null, null, "created_at DESC")
        val list = mutableListOf<VoiceReportEntity>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    VoiceReportEntity(
                        reportId = it.getString(it.getColumnIndexOrThrow("report_id")),
                        assignmentId = it.getString(it.getColumnIndexOrThrow("assignment_id")),
                        status = it.getString(it.getColumnIndexOrThrow("status")),
                        languageDetected = it.getString(it.getColumnIndexOrThrow("language_detected")),
                        transcriptOriginal = it.getString(it.getColumnIndexOrThrow("transcript_original")),
                        transcriptEn = it.getString(it.getColumnIndexOrThrow("transcript_en")),
                        interpretationJson = it.getString(it.getColumnIndexOrThrow("interpretation_json")),
                        evidenceTier = it.getString(it.getColumnIndexOrThrow("evidence_tier")),
                        provider = it.getString(it.getColumnIndexOrThrow("provider")),
                        providerRequestId = it.getString(it.getColumnIndexOrThrow("provider_request_id")),
                        createdAt = it.getString(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return list
    }

    fun clearAllTables() {
        val db = writableDatabase
        db.delete("assignments", null, null)
        db.delete("disruptions", null, null)
        db.delete("events", null, null)
        db.delete("checkpoint_status", null, null)
        db.delete("pending_actions", null, null)
        db.delete("calls", null, null)
        db.delete("interchanges", null, null)
        db.delete("voice_reports", null, null)
    }

    companion object {
        private const val DATABASE_NAME = "lira_field.db"
        const val DATABASE_VERSION = 4

        @Volatile
        private var instance: LiraDatabase? = null

        fun getInstance(context: Context): LiraDatabase {
            return instance ?: synchronized(this) {
                instance ?: LiraDatabase(context).also { instance = it }
            }
        }
    }
}

