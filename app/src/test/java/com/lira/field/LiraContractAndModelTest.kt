package com.lira.field

import com.lira.field.core.model.*
import com.lira.field.core.network.ApiErrorDetail
import com.lira.field.core.network.ApiErrorResponse
import com.lira.field.core.network.NetworkClient
import org.junit.Assert.*
import org.junit.Test

class LiraContractAndModelTest {

    private val moshi = NetworkClient.moshi

    @Test
    fun testLockedContractAssignmentsJsonParsing() {
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
            "updated_at": "2026-10-28T15:30:00+05:30"
          }],
          "updated_at": "2026-10-28T15:30:00+05:30"
        }
        """.trimIndent()

        val adapter = moshi.adapter(AssignmentsResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(1, response!!.items.size)
        val item = response.items[0]
        assertEquals("ASN-102-R2", item.assignmentId)
        assertEquals("PLAN-R2", item.planId)
        assertEquals("C102", item.containerId)
        assertEquals("T07", item.truckId)
        assertEquals("S1600", item.slotId)
        assertEquals("APPROVED", item.status)
        assertEquals("PENDING", item.driverAckState)
        assertEquals("EXPECTED", item.physicalState)
        assertEquals(12000L, item.projectedAvoidableCostInr)
        assertEquals(listOf("EV-DOC-102", "EV-CALL-04"), item.evidenceIds)
    }

    @Test
    fun testLockedContractAckResponseParsing() {
        val json = """
        {
          "accepted": true,
          "assignment_id": "ASN-102-R2",
          "driver_ack_state": "ACKNOWLEDGED",
          "event_id": "EVT-120"
        }
        """.trimIndent()

        val adapter = moshi.adapter(AckResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertTrue(response!!.accepted)
        assertEquals("ASN-102-R2", response.assignmentId)
        assertEquals("ACKNOWLEDGED", response.driverAckState)
        assertEquals("EVT-120", response.eventId)
    }

    @Test
    fun testLockedContractDisruptionsForecastRisk() {
        val json = """
        {
          "items": [{
            "event_id":"EXT-01", "type":"HEAVY_RAIN",
            "trust_state":"FORECAST_RISK", "severity":"HIGH",
            "title":"Heavy rain may delay the approach road",
            "source_label":"Weather feed / DEMO",
            "affected_trucks":["T07"],
            "starts_at":"2026-10-28T15:00:00+05:30",
            "ends_at":"2026-10-28T19:00:00+05:30",
            "updated_at":"2026-10-28T14:45:00+05:30"
          }]
        }
        """.trimIndent()

        val adapter = moshi.adapter(DisruptionsResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(1, response!!.items.size)
        val item = response.items[0]
        assertEquals("FORECAST_RISK", item.trustState)
        assertNotEquals("CONFIRMED_DISRUPTION", item.trustState)
        assertEquals("HIGH", item.severity)
        assertEquals(listOf("T07"), item.affectedTrucks)
    }

    @Test
    fun testLockedContractCheckpointStatusParsing() {
        val json = """
        {
          "assignment_id":"ASN-102-R2",
          "device_id":"LIRA-EDGE-01",
          "state":"EXPECTED",
          "rfid_state":"NOT_SCANNED",
          "bay_present":false,
          "last_event_at":null
        }
        """.trimIndent()

        val adapter = moshi.adapter(CheckpointStatus::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("LIRA-EDGE-01", response!!.deviceId)
        assertEquals("EXPECTED", response.state)
        assertEquals("NOT_SCANNED", response.rfidState)
        assertFalse(response.bayPresent)
    }

    @Test
    fun testLockedContractErrorFormatParsing() {
        val json = """{"error":{"code":"VALIDATION_ERROR","message":"This assignment is no longer active","request_id":"REQ-101"}}"""

        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("VALIDATION_ERROR", response!!.error.code)
        assertEquals("This assignment is no longer active", response.error.message)
        assertEquals("REQ-101", response.error.requestId)
    }
}

