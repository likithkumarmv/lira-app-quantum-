package com.lira.field.core.model

import com.squareup.moshi.Json

/**
 * LIRA Field PRD v9.0 Disruption & Corridor Risk Telemetry
 * Provides driver-scoped backend risk projections (Weather, Traffic, Port Holds)
 * Epistemic Rules:
 * - Weather & traffic routing are PREDICTIONS, never authoritative port closures or truck presence.
 * - Stale feeds get a clear warning and cannot become CONFIRMED.
 * - Zero OpenWeather or Google Maps API keys in the APK; consumed via FastAPI backend.
 */
data class Disruption(
    @Json(name = "event_id") val eventId: String,
    @Json(name = "type") val type: String,
    @Json(name = "trust_state") val trustState: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "title") val title: String,
    @Json(name = "source_label") val sourceLabel: String,
    @Json(name = "affected_trucks") val affectedTrucks: List<String> = emptyList(),
    @Json(name = "starts_at") val startsAt: String,
    @Json(name = "ends_at") val endsAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String,

    // Extended fields per PRD v9.0
    @Json(name = "source_authority") val sourceAuthority: String = "Port Operations & Traffic Command",
    @Json(name = "affected_route") val affectedRoute: String? = "NH-348 / Port Approach Corridor",
    @Json(name = "recommendation") val recommendation: String? = "Proceed with caution; buffer 30 mins before gate arrival.",

    // Weather & Traffic Risk Projections (PRD v9.0 Addendum)
    @Json(name = "provider") val provider: String = "OpenWeather / TomTom via FastAPI Control Tower",
    @Json(name = "corridor") val corridor: String = "Nhava Sheva / JNPT Expressway Corridor",
    @Json(name = "affected_assignment_id") val affectedAssignmentId: String? = "ASN-102-R2",
    @Json(name = "ttl_minutes") val ttlMinutes: Int = 30,
    @Json(name = "data_source_mode") val dataSourceMode: String = DataSourceMode.LIVE_EXTERNAL,
    @Json(name = "freshness_seconds") val freshnessSeconds: Long = 120L,
    @Json(name = "predicted_delay_minutes") val predictedDelayMinutes: Int = 20,
    @Json(name = "revised_eta") val revisedEta: String? = "2026-10-29T16:20:00+05:30",
    @Json(name = "booked_slot_cutoff") val bookedSlotCutoff: String? = "2026-10-29T16:30:00+05:30",
    @Json(name = "risk_level_label") val riskLevelLabel: String = "PREDICTED TRAFFIC DELAY (NON-AUTHORITATIVE)",
    @Json(name = "is_feed_available") val isFeedAvailable: Boolean = true,
    @Json(name = "is_stale") val isStale: Boolean = false
)

object DataSourceMode {
    const val LIVE_EXTERNAL = "LIVE_EXTERNAL"
    const val LAB_FIXTURE = "LAB_FIXTURE"
}

data class DisruptionsResponse(
    @Json(name = "items") val items: List<Disruption>
)

object TrustState {
    const val FORECAST_RISK = "FORECAST_RISK"
    const val CONFIRMED_DISRUPTION = "CONFIRMED_DISRUPTION"
    const val CANCELLED = "CANCELLED"
    const val UNKNOWN = "UNKNOWN"
}

object Severity {
    const val CRITICAL = "CRITICAL"
    const val HIGH = "HIGH"
    const val MEDIUM = "MEDIUM"
    const val LOW = "LOW"
}

object DisruptionType {
    const val HEAVY_RAIN = "HEAVY_RAIN"
    const val CYCLONE_WARNING = "CYCLONE_WARNING"
    const val TRAFFIC_CONGESTION = "TRAFFIC_CONGESTION"
    const val HIGHWAY_CLOSURE = "HIGHWAY_CLOSURE"
    const val CONFIRMED_PORT_CLOSURE = "CONFIRMED_PORT_CLOSURE"
    const val TERMINAL_CONGESTION = "TERMINAL_CONGESTION"
    const val TERMINAL_SYSTEM_OUTAGE = "TERMINAL_SYSTEM_OUTAGE"
    const val DRIVER_VEHICLE_RESTRICTION = "DRIVER_VEHICLE_RESTRICTION"
    const val ROUTE_DISRUPTION = "ROUTE_DISRUPTION"

    fun formatLabel(type: String): String = when (type) {
        HEAVY_RAIN -> "Heavy Rain / Flooding Advisory"
        CYCLONE_WARNING -> "Cyclone Coastal Warning"
        TRAFFIC_CONGESTION -> "Predicted Highway Traffic Congestion"
        HIGHWAY_CLOSURE -> "Highway / Corridor Closure"
        CONFIRMED_PORT_CLOSURE -> "Confirmed Official Port Closure"
        TERMINAL_CONGESTION -> "Terminal Gate Staging Congestion"
        TERMINAL_SYSTEM_OUTAGE -> "Terminal System Outage"
        DRIVER_VEHICLE_RESTRICTION -> "Vehicle Movement Restriction"
        ROUTE_DISRUPTION -> "General Route Disruption"
        else -> type.replace("_", " ")
    }
}
