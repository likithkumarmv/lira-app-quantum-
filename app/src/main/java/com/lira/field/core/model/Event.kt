package com.lira.field.core.model

import com.squareup.moshi.Json

data class DriverEvent(
    @Json(name = "event_id") val eventId: String,
    @Json(name = "sequence") val sequence: Long,
    @Json(name = "type") val type: String,
    @Json(name = "assignment_id") val assignmentId: String? = null,
    @Json(name = "incident_id") val incidentId: String? = null,
    @Json(name = "origin") val origin: String? = null,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "payload") val payload: Map<String, Any?>? = null,
    @Json(name = "source_actor") val sourceActor: String = "LIRA_CENTRAL_ORCHESTRATOR",
    @Json(name = "is_local_pending") val isLocalPending: Boolean = false
)

data class IncidentGroup(
    val incidentId: String,
    val origin: String = "DISPATCHER",
    val title: String = "Incident $incidentId",
    val events: List<DriverEvent>
)

fun List<DriverEvent>.groupByIncident(): List<IncidentGroup> {
    if (isEmpty()) return emptyList()
    return groupBy {
        it.incidentId
            ?: (it.payload?.get("incident_id") as? String)
            ?: it.assignmentId
            ?: "INC-DEFAULT"
    }.map { (id, evs) ->
        val origin = evs.firstNotNullOfOrNull {
            it.origin ?: (it.payload?.get("origin") as? String)
        } ?: "DISPATCHER"
        IncidentGroup(
            incidentId = id,
            origin = origin,
            title = if (id == "INC-DEFAULT") "Standard Dispatch" else "Incident $id",
            events = evs
        )
    }
}

data class DriverEventsResponse(
    @Json(name = "items") val items: List<DriverEvent>,
    @Json(name = "next_sequence") val nextSequence: Long
)

object EventType {
    // 14 Canonical Events per PRD Screen G
    const val DOCUMENT_INGESTED = "DOCUMENT_INGESTED"
    const val BLOCKER_IDENTIFIED = "BLOCKER_IDENTIFIED"
    const val VERIFICATION_REQUESTED = "VERIFICATION_REQUESTED"
    const val RECOVERY_GENERATED = "RECOVERY_GENERATED"
    const val PLAN_APPROVED = "PLAN_APPROVED"
    const val PLAN_DISPATCHED = "PLAN_DISPATCHED"
    const val DRIVER_ACKNOWLEDGED = "DRIVER_ACKNOWLEDGED"
    const val DRIVER_CANNOT_COMPLY = "DRIVER_CANNOT_COMPLY"
    const val ETA_UPDATED = "ETA_UPDATED"
    const val EDGE_RFID_VERIFIED = "EDGE_RFID_VERIFIED"
    const val EDGE_MISMATCH = "EDGE_MISMATCH"
    const val RECOVERY_WAVE_TRIGGERED = "RECOVERY_WAVE_TRIGGERED"
    const val NEW_PLAN_DISPATCHED = "NEW_PLAN_DISPATCHED"
    const val PLAN_COMPLETED = "PLAN_COMPLETED"

    // Backward compatibility aliases
    const val DRIVER_REPORTED_ETA = "ETA_UPDATED"
    const val PHYSICAL_MISMATCH = "PHYSICAL_MISMATCH"
    const val RECOVERY_STARTED = "RECOVERY_STARTED"
    const val PLAN_REPLACED = "PLAN_REPLACED"
    const val STAGING_EVIDENCED = "STAGING_EVIDENCED"

    // Contract v13 Events
    const val BRANCH_ACTIVATED = "BRANCH_ACTIVATED"
    const val PLAN_SUPERSEDED = "PLAN_SUPERSEDED"
    const val CALL_COMPLETED = "CALL_COMPLETED"
    const val CALL_FAILED = "CALL_FAILED"
    const val VOICE_REPORT_SENT = "VOICE_REPORT_SENT"
    const val INTERCHANGE_COMMITTED = "INTERCHANGE_COMMITTED"
    const val INTERCHANGE_HOLD = "INTERCHANGE_HOLD"
    const val RECONCILED_STALE = "RECONCILED_STALE"
}
