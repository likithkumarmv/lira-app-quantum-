package com.lira.field.core.model

import com.squareup.moshi.Json

/**
 * LIRA-Q Freight Exception Cases (C1 through C6)
 * Aligned with GENAI-01 Master Problem Track:
 * - C1: Customs / Document / Exam Hold
 * - C2: Carrier Freight & e-DO Commercial Release
 * - C3: Weather / Vessel Bunching / Yard Capacity
 * - C4: Terminal Restricted Slot / Chassis Shortage
 * - C5: Driver Fallout / Missed Staging Window
 * - C6: Empty Return / Detention Tracking
 */
data class FreightExceptionCase(
    @Json(name = "case_id") val caseId: String,
    @Json(name = "case_code") val caseCode: String,
    @Json(name = "title") val title: String,
    @Json(name = "hold_reason") val holdReason: String,
    @Json(name = "owner") val owner: String,
    @Json(name = "deadline") val deadline: String,
    @Json(name = "driver_instruction") val driverInstruction: String,
    @Json(name = "evidence_source_type") val evidenceSourceType: String,
    @Json(name = "evidence_ref") val evidenceRef: String,
    @Json(name = "status") val status: String,
    @Json(name = "last_updated") val lastUpdated: String,
    @Json(name = "can_driver_clear") val canDriverClear: Boolean = false
)

object FreightCaseType {
    const val C1_CUSTOMS_DOC_HOLD = "C1_CUSTOMS_DOC_HOLD"
    const val C2_CARRIER_EDO_HOLD = "C2_CARRIER_EDO_HOLD"
    const val C3_WEATHER_CAPACITY_HOLD = "C3_WEATHER_CAPACITY_HOLD"
    const val C4_TERMINAL_RESOURCE_HOLD = "C4_TERMINAL_RESOURCE_HOLD"
    const val C5_DRIVER_FALLOUT_HOLD = "C5_DRIVER_FALLOUT_HOLD"
    const val C6_EMPTY_RETURN_DETENTION = "C6_EMPTY_RETURN_DETENTION"

    fun getCaseTitle(code: String): String = when (code) {
        C1_CUSTOMS_DOC_HOLD -> "C1: Customs & Regulatory Documentation Hold"
        C2_CARRIER_EDO_HOLD -> "C2: Carrier Freight & e-DO Commercial Release Pending"
        C3_WEATHER_CAPACITY_HOLD -> "C3: Route Weather & Terminal Staging Advisory"
        C4_TERMINAL_RESOURCE_HOLD -> "C4: Terminal Slot & Chassis ResourceLock Allocation"
        C5_DRIVER_FALLOUT_HOLD -> "C5: Tender Staging Window & Driver Re-routing"
        C6_EMPTY_RETURN_DETENTION -> "C6: Post-Delivery Empty Return & Detention Tracking"
        else -> "Active Corridor Hold"
    }
}

