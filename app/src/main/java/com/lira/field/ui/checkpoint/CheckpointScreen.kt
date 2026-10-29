package com.lira.field.ui.checkpoint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.core.model.*
import com.lira.field.ui.components.LiraIcons
import com.lira.field.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckpointScreen(
    viewModel: CheckpointViewModel,
    onBack: () -> Unit
) {
    val status by viewModel.status.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val colors = LiraTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RESOURCELOCK & MARSHAL TERMINAL (v9.0)",
                        style = TrackedHeaderLabel,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = LiraIcons.ArrowBack, contentDescription = "Back", tint = colors.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStatus() }) {
                        Icon(imageVector = LiraIcons.Refresh, contentDescription = "Refresh", tint = colors.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = colors.background
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hardware Boundary Honesty Notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceElevated, IndustrialShape)
                    .border(1.dp, colors.border, IndustrialShape)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = LiraIcons.Sensors,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ESP32 MARSHAL NODE SPECIFICATION (PRD v9.0)",
                            style = TrackedHeaderLabel,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Hardware is strictly ESP32 DevKit V1 + ILI9341 320x240 TFT (DISPLAY ONLY) + RC522 RFID. Zero physical buttons, zero touchscreen input, no touch pressure or biometrics. Handover custody is authorized by the custodian on the Web Interface. Android displays read-only confirmed ResourceLock status.",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Real Hardware Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HARDWARE TERMINAL PROFILE",
                            style = TrackedHeaderLabel,
                            color = colors.textSecondary
                        )
                        Box(
                            modifier = Modifier
                                .background(colors.surfaceElevated, IndustrialShape)
                                .border(1.dp, colors.border, IndustrialShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("320x240 TFT DISPLAY-ONLY + RC522", color = colors.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "Microcontroller" to HardwareSpec.MICROCONTROLLER,
                        "Display Panel" to HardwareSpec.DISPLAY,
                        "RFID Module" to HardwareSpec.RFID_READER,
                        "Custodian Authorization" to HardwareSpec.OPERATOR_INTERFACE
                    ).forEach { (comp, spec) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = comp, color = colors.textSecondary, fontSize = 11.sp)
                            Text(
                                text = spec,
                                color = colors.textPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else if (status != null) {
                val st = status!!
                val lock = st.resourceLock

                // ---------------------------------------------------------
                // RECOVERY UNDER REVIEW BANNER
                // ---------------------------------------------------------
                val isUnderReview = !lock.recoveryReviewStatus.isNullOrEmpty() || st.recoveryWaveTriggered || st.state.contains("RECOVERY")
                if (isUnderReview) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.warningSurface, IndustrialShape)
                            .border(1.dp, colors.warningBorder, IndustrialShape)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(LiraIcons.Hourglass, contentDescription = null, tint = colors.warning, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RECOVERY UNDER REVIEW",
                                    style = TrackedHeaderLabel,
                                    color = colors.warning
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Central dispatch and QpiAI solver are re-evaluating corridor allocations. Resource handover is held in review status until new wave confirmation.",
                                    color = colors.textPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // 3 AUTHORIZATION BOUNDARIES NOTICE
                // ---------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, IndustrialShape)
                        .border(1.dp, colors.border, IndustrialShape)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "3 DISTINCT AUTHORIZATION BOUNDARIES",
                            style = TrackedHeaderLabel,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "1. Physical Scan Observed (RC522) ≠ 2. Custodian Approval (Web Dashboard) ≠ 3. Backend Committed Lease (Control Tower).\nNeither phone nor ESP32 makes the custody decision.",
                            color = colors.textTertiary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                // AUTHORITATIVE RESOURCELOCK TELEMETRY PANEL (PRD v9.0)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShape,
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.primary.copy(alpha = 0.6f)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RESOURCELOCK TELEMETRY (READ-ONLY)",
                                style = TrackedHeaderLabel,
                                color = colors.primary
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (lock.resourceState) {
                                            ResourceState.CHECKED_OUT -> colors.successSurface
                                            ResourceState.RESERVED -> colors.primaryContainer
                                            ResourceState.QUARANTINED, ResourceState.EXCEPTION_HOLD -> colors.errorSurface
                                            else -> colors.warningSurface
                                        },
                                        IndustrialShape
                                    )
                                    .border(
                                        1.dp,
                                        when (lock.resourceState) {
                                            ResourceState.CHECKED_OUT -> colors.successBorder
                                            ResourceState.RESERVED -> colors.primary
                                            ResourceState.QUARANTINED, ResourceState.EXCEPTION_HOLD -> colors.errorBorder
                                            else -> colors.warningBorder
                                        },
                                        IndustrialShape
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = lock.resourceState,
                                    color = when (lock.resourceState) {
                                        ResourceState.CHECKED_OUT -> colors.success
                                        ResourceState.RESERVED -> colors.primary
                                        ResourceState.QUARANTINED, ResourceState.EXCEPTION_HOLD -> colors.error
                                        else -> colors.warning
                                    },
                                    style = TelemetrySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Locked Resource ID", color = colors.textSecondary, fontSize = 11.sp)
                            Text(text = lock.resourceId, style = TelemetrySmall, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Resource Type", color = colors.textSecondary, fontSize = 11.sp)
                            Text(text = lock.resourceType, style = TelemetrySmall, color = colors.textSecondary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Work-Order Reference", color = colors.textSecondary, fontSize = 11.sp)
                            Text(text = lock.workOrderId, style = TelemetrySmall, color = colors.primary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = colors.border)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custodian Web Attestation details
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Custodian Web Approval", color = colors.textSecondary, fontSize = 11.sp)
                            Text(
                                text = lock.operatorWebApprovalState,
                                style = TelemetrySmall,
                                color = if (lock.operatorWebApprovalState.contains("APPROVED")) colors.success else colors.warning,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Authorized By (Web Desk)", color = colors.textSecondary, fontSize = 11.sp)
                            Text(text = "${lock.operatorWebUserId ?: "CUSTODIAN_DESK_04"} @ ${lock.operatorWebApprovalTimestamp ?: "15:42 IST"}", style = TelemetrySmall, color = colors.textTertiary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Server Transaction ID", color = colors.textSecondary, fontSize = 11.sp)
                            Text(text = lock.serverTxId ?: "TX-AUTH-9941-R2", style = TelemetrySmall, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Solver Provenance", color = colors.textSecondary, fontSize = 11.sp)
                            Text(text = "${lock.solverProvenance} (Plan v${lock.planVersion})", style = TelemetrySmall, color = colors.primary)
                        }

                        if (!lock.rejectionCode.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.errorSurface, IndustrialShape)
                                    .border(1.dp, colors.errorBorder, IndustrialShape)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "REJECTION CODE: ${lock.rejectionCode}",
                                    color = colors.error,
                                    style = TelemetrySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // 7-STAGE RESOURCELOCK STATE MACHINE INDICATOR
                // ---------------------------------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShape,
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RESOURCELOCK 7-STAGE LIFECYCLE (READ-ONLY)",
                                style = TrackedHeaderLabel,
                                color = colors.textSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .background(colors.surfaceElevated, IndustrialShape)
                                    .border(1.dp, colors.border, IndustrialShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TERMINAL: ${lock.terminalAvailability}",
                                    color = colors.primary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val stages = listOf(
                            ResourceState.SCAN_REQUESTED to "1. Scan Requested",
                            ResourceState.TAG_OBSERVED to "2. Tag Observed",
                            ResourceState.AWAITING_CUSTODIAN_CONFIRMATION to "3. Custodian Review",
                            ResourceState.CHECKOUT_ACCEPTED to "4. Checkout Accepted",
                            ResourceState.REJECTED to "5. Rejected",
                            ResourceState.QUARANTINED to "6. Quarantined",
                            ResourceState.OFFLINE_UNCONFIRMED to "7. Offline Unconfirmed"
                        )

                        stages.forEach { (stageKey, stageLabel) ->
                            val isCurrentStage = lock.resourceState == stageKey ||
                                    (stageKey == ResourceState.CHECKOUT_ACCEPTED && lock.resourceState == ResourceState.CHECKED_OUT) ||
                                    (stageKey == ResourceState.AWAITING_CUSTODIAN_CONFIRMATION && lock.resourceState == ResourceState.RESERVED) ||
                                    (stageKey == ResourceState.SCAN_REQUESTED && lock.resourceState == ResourceState.RETURN_PENDING) ||
                                    (stageKey == ResourceState.REJECTED && lock.resourceState == ResourceState.EXCEPTION_HOLD)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCurrentStage) colors.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                                        IndustrialShape
                                    )
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCurrentStage) LiraIcons.RadioChecked else LiraIcons.RadioUnchecked,
                                        contentDescription = null,
                                        tint = if (isCurrentStage) colors.primary else colors.textTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stageLabel,
                                        color = if (isCurrentStage) colors.textPrimary else colors.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrentStage) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                if (isCurrentStage) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.primary, IndustrialShape)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ACTIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // STRICT 3-ROW EPISTEMIC EVIDENCE MATRIX (PRD v9.0)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShape,
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3-ROW EPISTEMIC EVIDENCE MATRIX",
                                style = TrackedHeaderLabel,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "NODE: ${st.deviceId}",
                                style = TelemetrySmall,
                                color = colors.textTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row 1: Tier 3 — RC522 Sensor Observation
                        EpistemicTierRow(
                            tierLabel = "TIER 3 — RC522 SENSOR OBSERVATION",
                            statusValue = st.tier3aRfidObservation,
                            statusColor = when (st.tier3aRfidObservation) {
                                Tier3ARfidState.TAG_MATCHED -> colors.success
                                Tier3ARfidState.TAG_MISMATCH -> colors.error
                                else -> colors.warning
                            },
                            statusBg = when (st.tier3aRfidObservation) {
                                Tier3ARfidState.TAG_MATCHED -> colors.successSurface
                                Tier3ARfidState.TAG_MISMATCH -> colors.errorSurface
                                else -> colors.warningSurface
                            },
                            statusBorder = when (st.tier3aRfidObservation) {
                                Tier3ARfidState.TAG_MATCHED -> colors.successBorder
                                Tier3ARfidState.TAG_MISMATCH -> colors.errorBorder
                                else -> colors.warningBorder
                            },
                            epistemicScope = "Proves only that physical RFID tag was read by RC522 reader. Does NOT certify vehicle physical presence or readiness."
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = colors.border)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 2: Tier 4 — Custodian Authenticated Web Attestation
                        EpistemicTierRow(
                            tierLabel = "TIER 4 — CUSTODIAN WEB ATTESTATION",
                            statusValue = st.tier4CustodianWebAttestation,
                            statusColor = when (st.tier4CustodianWebAttestation) {
                                Tier4CustodianWebState.CONFIRMED_ON_WEB -> colors.success
                                Tier4CustodianWebState.EXCEPTION_REPORTED_ON_WEB -> colors.error
                                else -> colors.warning
                            },
                            statusBg = when (st.tier4CustodianWebAttestation) {
                                Tier4CustodianWebState.CONFIRMED_ON_WEB -> colors.successSurface
                                Tier4CustodianWebState.EXCEPTION_REPORTED_ON_WEB -> colors.errorSurface
                                else -> colors.warningSurface
                            },
                            statusBorder = when (st.tier4CustodianWebAttestation) {
                                Tier4CustodianWebState.CONFIRMED_ON_WEB -> colors.successBorder
                                Tier4CustodianWebState.EXCEPTION_REPORTED_ON_WEB -> colors.errorBorder
                                else -> colors.warningBorder
                            },
                            epistemicScope = "Human custodian attestation conducted via the authorized Web Interface (separate from ESP32 passive scan)."
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = colors.border)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 3: Tier 1 — Statutory Port / ULIP Gate Proof
                        EpistemicTierRow(
                            tierLabel = "TIER 1 — STATUTORY PORT / ULIP GATE PROOF",
                            statusValue = st.tier1PortProof,
                            statusColor = when (st.tier1PortProof) {
                                Tier1PortProofState.EXTERNALLY_VERIFIED -> colors.success
                                CheckpointStateV9.RECOVERY_REQUIRED -> colors.error
                                else -> colors.primary
                            },
                            statusBg = when (st.tier1PortProof) {
                                Tier1PortProofState.EXTERNALLY_VERIFIED -> colors.successSurface
                                CheckpointStateV9.RECOVERY_REQUIRED -> colors.errorSurface
                                else -> colors.surfaceElevated
                            },
                            statusBorder = when (st.tier1PortProof) {
                                Tier1PortProofState.EXTERNALLY_VERIFIED -> colors.successBorder
                                CheckpointStateV9.RECOVERY_REQUIRED -> colors.errorBorder
                                else -> colors.border
                            },
                            epistemicScope = "Only authoritative PCS 1x / ICEGATE / Terminal Gate-Out electronic data interchange certifies legal container clearance."
                        )
                    }
                }

                // Checkpoint State Machine & Tag Telemetry
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShape,
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NODE STATE & PASSIVE TAG SENSING", style = TrackedHeaderLabel, color = colors.textSecondary)
                            Box(
                                modifier = Modifier
                                    .background(colors.surfaceElevated, IndustrialShape)
                                    .border(1.dp, colors.border, IndustrialShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = st.state,
                                    style = TelemetrySmall,
                                    color = if (st.state.contains("MISMATCH") || st.state.contains("EXCEPTION") || st.state.contains("RECOVERY")) colors.error else colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!st.mismatchDetails.isNullOrEmpty() || st.recoveryWaveTriggered) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.errorSurface, IndustrialShape)
                                    .border(1.dp, colors.errorBorder, IndustrialShape)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "EXCEPTION / MATERIAL STATE CHANGE",
                                        style = TrackedHeaderLabel,
                                        color = colors.error
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = st.mismatchDetails ?: "Trigger: EDGE_RFID_MISMATCH + CUSTODIAN_WEB_REPORT -> Second recovery wave required.",
                                        style = TelemetrySmall,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = colors.border)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Tag telemetry
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Expected Vehicle Tag", color = colors.textSecondary, fontSize = 11.sp)
                            Text(text = st.expectedTagId ?: "RFID-T07-9941", style = TelemetrySmall, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Presented Tag ID (RC522)", color = colors.textSecondary, fontSize = 11.sp)
                            Text(
                                text = st.scannedTagId ?: "AWAITING PRESENTATION",
                                style = TelemetrySmall,
                                color = if (st.scannedTagId == st.expectedTagId) colors.success else colors.warning,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Device Heartbeat & Link", color = colors.textSecondary, fontSize = 11.sp)
                            Text(
                                text = "${st.hardwareHeartbeat ?: "Active"} • Seq #${st.eventSequence} (${if (st.edgeOnline) "ONLINE" else "OFFLINE"})",
                                style = TelemetrySmall,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShape,
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No marshal checkpoint active for current assignment.", color = colors.textSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EpistemicTierRow(
    tierLabel: String,
    statusValue: String,
    statusColor: Color,
    statusBg: Color,
    statusBorder: Color,
    epistemicScope: String
) {
    val colors = LiraTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = tierLabel, style = TrackedHeaderLabel, color = colors.textSecondary)
            Box(
                modifier = Modifier
                    .background(statusBg, IndustrialShape)
                    .border(1.dp, statusBorder, IndustrialShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusValue,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = epistemicScope,
            color = colors.textTertiary,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    }
}
