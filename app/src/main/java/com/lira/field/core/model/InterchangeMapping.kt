package com.lira.field.core.model

/**
 * Maps InterchangeStateV13 to the 5-step read-only stepper:
 * Step 0: Tap truck card
 * Step 1: Tap chassis tag
 * Step 2: Station check
 * Step 3: Marshal confirms
 * Step 4: Checked out ✓
 *
 * Epistemic Honesty Guarantee:
 * Never show green checkout before state == COMMITTED.
 * MATCH_PREVIEW is amber "Matched, waiting for marshal".
 */
data class InterchangeStepState(
    val state: InterchangeStateV13,
    val currentStepIndex: Int,
    val isCheckedOut: Boolean,
    val isHold: Boolean,
    val isMatchPreview: Boolean,
    val statusHeadline: String,
    val statusDescription: String,
    val formattedEir: String? = null
)

object InterchangeMapping {

    val STEP_LABELS = listOf(
        "Tap truck card",
        "Tap chassis tag",
        "Station check",
        "Marshal confirms",
        "Checked out ✓"
    )

    fun resolve(
        state: InterchangeStateV13,
        reasonCode: String? = null,
        txnId: String? = null,
        eirNo: String? = null,
        challengeCode: String? = null
    ): InterchangeStepState {
        return when (state) {
            InterchangeStateV13.NOT_STARTED -> InterchangeStepState(
                state = state,
                currentStepIndex = 0,
                isCheckedOut = false,
                isHold = false,
                isMatchPreview = false,
                statusHeadline = "Ready for Gate Scan",
                statusDescription = "Arrive at staging bay and tap truck RFID card."
            )
            InterchangeStateV13.TRUCK_OBSERVED -> InterchangeStepState(
                state = state,
                currentStepIndex = 1,
                isCheckedOut = false,
                isHold = false,
                isMatchPreview = false,
                statusHeadline = "Truck Card Scanned",
                statusDescription = "Truck card observed. Tap chassis tag on reader within 30s."
            )
            InterchangeStateV13.EQUIPMENT_OBSERVED -> InterchangeStepState(
                state = state,
                currentStepIndex = 2,
                isCheckedOut = false,
                isHold = false,
                isMatchPreview = false,
                statusHeadline = "Station Validating",
                statusDescription = "RFID tags observed. Bay Station verifying decision capsule."
            )
            InterchangeStateV13.MATCH_PREVIEW -> InterchangeStepState(
                state = state,
                currentStepIndex = 3,
                isCheckedOut = false,
                isHold = false,
                isMatchPreview = true,
                statusHeadline = "Matched, waiting for marshal",
                statusDescription = if (!challengeCode.isNullOrBlank()) {
                    "Tags verified. Present challenge code $challengeCode to bay marshal."
                } else {
                    "Tags match plan. Waiting for physical marshal confirmation."
                }
            )
            InterchangeStateV13.HOLD -> {
                val plainReason = formatHoldReason(reasonCode)
                InterchangeStepState(
                    state = state,
                    currentStepIndex = 2,
                    isCheckedOut = false,
                    isHold = true,
                    isMatchPreview = false,
                    statusHeadline = "Station Hold Active",
                    statusDescription = plainReason
                )
            }
            InterchangeStateV13.AWAITING_CUSTODIAN -> InterchangeStepState(
                state = state,
                currentStepIndex = 3,
                isCheckedOut = false,
                isHold = false,
                isMatchPreview = false,
                statusHeadline = "Marshal Review Required",
                statusDescription = if (!challengeCode.isNullOrBlank()) {
                    "Show challenge code $challengeCode to gate marshal (valid 120s)."
                } else {
                    "Marshal physical inspection required before checkout."
                }
            )
            InterchangeStateV13.COMMITTED -> {
                val eirSlip = eirNo ?: (txnId?.let { "TXN-$it" } ?: "DIGITAL-EIR")
                InterchangeStepState(
                    state = state,
                    currentStepIndex = 4,
                    isCheckedOut = true,
                    isHold = false,
                    isMatchPreview = false,
                    statusHeadline = "Checked out ✓",
                    statusDescription = "Statutory custody verified. Interchange committed.",
                    formattedEir = "Digital EIR $eirSlip"
                )
            }
            InterchangeStateV13.REJECTED -> InterchangeStepState(
                state = state,
                currentStepIndex = 2,
                isCheckedOut = false,
                isHold = true,
                isMatchPreview = false,
                statusHeadline = "Interchange Rejected",
                statusDescription = "Scan rejected by station custodian: ${formatHoldReason(reasonCode)}"
            )
            InterchangeStateV13.EXPIRED -> InterchangeStepState(
                state = state,
                currentStepIndex = 0,
                isCheckedOut = false,
                isHold = true,
                isMatchPreview = false,
                statusHeadline = "Session Expired",
                statusDescription = "Interchange window timed out (30s limit exceeded). Please re-scan."
            )
            InterchangeStateV13.UNKNOWN -> InterchangeStepState(
                state = state,
                currentStepIndex = 0,
                isCheckedOut = false,
                isHold = false,
                isMatchPreview = false,
                statusHeadline = "Interchange Status Unknown",
                statusDescription = "Waiting for station telematics update."
            )
        }
    }

    private fun formatHoldReason(code: String?): String {
        return when (code) {
            "TAG_MISMATCH" -> "Wrong chassis tag. Bring assigned chassis."
            "NO_VERIFIED_FRESH_POLICY" -> "Station capsule expired. Dispatch re-synchronizing."
            "UNKNOWN_TAG" -> "Unrecognized RFID tag scanned. Check equipment tag."
            "RESOURCE_NOT_AVAILABLE" -> "Resource currently unavailable or reserved."
            "STALE_WORK_ORDER" -> "Plan version superseded. Re-syncing new assignment."
            null, "" -> "Station hold active. Contact dispatch."
            else -> "Hold code $code. Dispatch has been notified."
        }
    }
}

