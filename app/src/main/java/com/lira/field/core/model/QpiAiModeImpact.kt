package com.lira.field.core.model

import com.squareup.moshi.Json

/**
 * Six QpiAI System Modes — Mobile Operational Impact
 * Quantum-classical solvers execute exclusively on the server.
 * Mobile displays only verified operational impacts, provenance, and operator actions.
 */
data class QpiAiModeImpact(
    @Json(name = "mode_name") val modeName: String,
    @Json(name = "mode_code") val modeCode: String,
    @Json(name = "status_summary") val statusSummary: String,
    @Json(name = "operational_headline") val operationalHeadline: String,
    @Json(name = "solver_run_id") val solverRunId: String?,
    @Json(name = "is_active") val isActive: Boolean = true
)

object QpiAiModeCode {
    const val INVESTIGATION_DIRECTOR = "INVESTIGATION_DIRECTOR"
    const val RED_TEAM_ANALYSIS = "RED_TEAM_ANALYSIS"
    const val QPIAI_DIRECTED_ESP32 = "QPIAI_DIRECTED_ESP32"
    const val RECOVERY_EXCHANGE = "RECOVERY_EXCHANGE"
    const val FUTURE_PROOFING = "FUTURE_PROOFING"
    const val QCLOUD_EXPERIMENT = "QCLOUD_EXPERIMENT"
}

