package com.lira.field.ui.assignments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.core.model.Assignment
import com.lira.field.core.model.OperationalCondition
import com.lira.field.ui.components.AckStateBadge
import com.lira.field.ui.components.AssignmentStatusBadge
import com.lira.field.ui.components.LiraIcons
import com.lira.field.ui.components.PhysicalStateBadge
import com.lira.field.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailsScreen(
    assignment: Assignment?,
    onBack: () -> Unit
) {
    val colors = LiraTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ASSIGNMENT SPECIFICATION (PRD v9.0)",
                        style = TrackedHeaderLabel,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = LiraIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = colors.background
    ) { padding ->
        if (assignment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Assignment record not found", color = colors.textSecondary)
            }
            return@Scaffold
        }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Plan Monotonicity Lockout Banner (PRD v9.0)
            if (assignment.isSuperseded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShape,
                    colors = CardDefaults.cardColors(containerColor = colors.errorSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.errorBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = LiraIcons.AlertTriangle, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SUPERSEDED BY WAVE 2 (PLAN v${assignment.planVersion + 1})",
                                style = TrackedHeaderLabel,
                                color = colors.error
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = assignment.invalidationTrigger ?: "TRIGGER: EDGE_RFID_MISMATCH + CUSTODIAN_REPORT",
                            style = TelemetrySmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Actions locked out on Plan v${assignment.planVersion}. Dispatcher has issued an updated slot schedule.",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Solver Outage / Feasibility Blocker Banner
            if (!assignment.isSolverAvailable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.warningSurface, IndustrialShape)
                        .border(1.dp, colors.warningBorder, IndustrialShape)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = LiraIcons.AlertCircle, contentDescription = null, tint = colors.warning, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("SOLVER UNAVAILABLE — AUTHORITATIVE HOLD ACTIVE", style = TrackedHeaderLabel, color = colors.warning)
                            Text("Backend QpiAI engine temporarily unreachable. Current plan remains locked until connection is restored.", color = colors.textSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (assignment.noFeasiblePlan) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.errorSurface, IndustrialShape)
                        .border(1.dp, colors.errorBorder, IndustrialShape)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = LiraIcons.AlertCircle, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("NO FEASIBLE PLAN FOUND", style = TrackedHeaderLabel, color = colors.error)
                            Text("Port constraints exceeded mathematical feasibility. Escalated to manual dispatcher hold.", color = colors.textPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Plan & Container Header Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${assignment.planId} (PLAN v${assignment.planVersion})",
                                color = colors.primary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(colors.surfaceElevated, IndustrialShape)
                                    .border(1.dp, colors.border, IndustrialShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "RECOVERY WAVE #${assignment.recoveryWave}",
                                    color = colors.secondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        AssignmentStatusBadge(status = assignment.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Container: ${assignment.containerId}",
                        style = TelemetryHeadline,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Assigned Truck: ${assignment.truckId}",
                        style = TelemetryMedium,
                        color = colors.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AckStateBadge(ackState = assignment.driverAckState)
                        PhysicalStateBadge(state = assignment.physicalState)
                    }
                }
            }

            // -------------------------------------------------------------
            // P0 EXPLAINABILITY FROM RAG: "WHY THIS ACTION?"
            // -------------------------------------------------------------
            val rag = assignment.ragDisclosure
            if (rag != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShape,
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (rag.ragStatus == "INSUFFICIENT_EVIDENCE") colors.warningBorder else colors.primary.copy(alpha = 0.7f)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = LiraIcons.Info,
                                    contentDescription = null,
                                    tint = if (rag.ragStatus == "INSUFFICIENT_EVIDENCE") colors.warning else colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WHY THIS ACTION? (RAG EXPLAINABILITY)",
                                    style = TrackedHeaderLabel,
                                    color = if (rag.ragStatus == "INSUFFICIENT_EVIDENCE") colors.warning else colors.primary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (rag.ragStatus == "INSUFFICIENT_EVIDENCE") colors.warningSurface else colors.primaryContainer,
                                        IndustrialShape
                                    )
                                    .border(
                                        1.dp,
                                        if (rag.ragStatus == "INSUFFICIENT_EVIDENCE") colors.warningBorder else colors.primary,
                                        IndustrialShape
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (rag.ragStatus == "INSUFFICIENT_EVIDENCE") "UNRESOLVED EVIDENCE" else "GROUNDED EVIDENCE",
                                    color = if (rag.ragStatus == "INSUFFICIENT_EVIDENCE") colors.warning else colors.primary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (rag.ragStatus == "INSUFFICIENT_EVIDENCE") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.warningSurface, IndustrialShape)
                                    .border(1.dp, colors.warningBorder, IndustrialShape)
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(LiraIcons.AlertTriangle, contentDescription = null, tint = colors.warning, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unresolved task — pending verified evidence. No automated justification hallucinated.",
                                        color = colors.textPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "REASON FOR CHANGED ASSIGNMENT:",
                                color = colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = rag.reasonForAction,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "BLOCKER DESCRIPTION:",
                                color = colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = rag.blockerDescription,
                                color = colors.error,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            DetailRow("Appointment Deadline", rag.appointmentDeadline.take(16).replace("T", " "), colors.primary, isMonospace = true)

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = colors.border)
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "TRUSTED EVIDENCE CITATION:",
                                color = colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${rag.evidenceSourceTitle} (${rag.evidenceTimestamp.take(16).replace("T", " ")})",
                                color = colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surfaceElevated, IndustrialShape)
                                    .border(1.dp, colors.border, IndustrialShape)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = rag.exactQuote,
                                        color = colors.textSecondary,
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Span Reference: [${rag.spanReferenceId}]",
                                        color = colors.primary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // P2 ROBUST RECOVERY INSIGHT (PLAN RISK)
            // -------------------------------------------------------------
            val robust = assignment.robustRecoverySummary
            if (robust != null) {
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
                                text = "PLAN RISK ASSESSMENT",
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
                                    text = robust.modeledRiskLabel,
                                    color = colors.warning,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow("Stress Scenario", robust.scenarioName, colors.textPrimary)
                        DetailRow("Risk Evaluation", robust.planRiskAssessment, colors.warning)
                        DetailRow("Server Verification", "${robust.solverRunId} (Verified)", colors.primary, isMonospace = true)
                    }
                }
            }

            // Work Order & Task Ownership Card (PRD v9.0)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "WORK ORDER & TASK OWNERSHIP",
                        style = TrackedHeaderLabel,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DetailRow("Work-Order Reference", assignment.workOrderReference, colors.primary, isMonospace = true)
                    DetailRow("Next Field Task", assignment.nextTask, colors.textPrimary)
                    DetailRow("Responsible Task Owner", assignment.taskOwner, colors.textPrimary)
                    DetailRow("Resource Lock State", assignment.resourceHandoverStatus, colors.success, isMonospace = true)
                    DetailRow("Server Sync Timestamp", assignment.lastSync, colors.textTertiary, isMonospace = true)
                }
            }

            // 2026 FINANCIAL RISK TELEMETRY (LABELED ESTIMATES VS ACTUALS)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "FINANCIAL EXPOSURE TELEMETRY (JNPT CORRIDOR)",
                        style = TrackedHeaderLabel,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Explicit Modeled Exposure vs Invoiced Actual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("MODELED COUNTERFACTUAL ESTIMATE", color = colors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "₹${assignment.modeledCounterfactualExposureInr}",
                                style = TelemetryHeadline,
                                color = colors.warning
                            )
                            Text("Label: ${assignment.exposureFinancialLabel}", color = colors.textTertiary, fontSize = 9.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("INVOICED ACTUAL CHARGE", color = colors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "₹${assignment.invoicedActualInr}",
                                style = TelemetryHeadline,
                                color = colors.success
                            )
                            Text("Label: ACTUAL INVOICE", color = colors.textTertiary, fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Breakdown Badges: DPD Cliff, e-DO Bank UTR Cutoff, Solver Provenance
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PortTelemetryBadge(
                            label = "DPD 48H EVACUATION CLIFF",
                            detail = assignment.dpdEvacuationCliff,
                            badgeColor = colors.warning,
                            badgeBg = colors.warningSurface,
                            badgeBorder = colors.warningBorder
                        )

                        PortTelemetryBadge(
                            label = "e-DO BANK UTR CUTOFF",
                            detail = assignment.eDoBankUtrCutoff,
                            badgeColor = colors.primary,
                            badgeBg = colors.surfaceElevated,
                            badgeBorder = colors.border
                        )

                        PortTelemetryBadge(
                            label = "SOLVER PROVENANCE (BACKEND SERVER)",
                            detail = assignment.solverProvenance,
                            badgeColor = colors.secondary,
                            badgeBg = colors.surfaceElevated,
                            badgeBorder = colors.border
                        )
                    }
                }
            }

            // Multi-Container Slot-Swap Arbitrage Card (QpiAI HUBO Barter Matrix)
            val arbitrage = assignment.slotSwapArbitrage ?: com.lira.field.core.model.SlotSwapArbitrage()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.warningBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QpiAI HUBO SLOT-SWAP ARBITRAGE",
                            style = TrackedHeaderLabel,
                            color = colors.warning
                        )
                        Box(
                            modifier = Modifier
                                .background(colors.warningSurface, IndustrialShape)
                                .border(1.dp, colors.warningBorder, IndustrialShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(arbitrage.swapStatus, color = colors.warning, style = TelemetrySmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = arbitrage.swapStatusLabel,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Zero empty terminal slots available. Quantum solver proposed a bilateral slot swap between urgent cold-chain and non-critical dry cargo. Bounties are modeled estimates pending terminal concurrence:",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Swap Matrix Details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated, IndustrialShape)
                            .border(1.dp, colors.border, IndustrialShape)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("URGENT (RECIPIENT)", color = colors.error, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${arbitrage.urgentContainerId} (${arbitrage.urgentContainerType})", color = colors.textPrimary, style = TelemetrySmall, fontWeight = FontWeight.Bold)
                            Text(text = "₹${arbitrage.urgentCliffInr} Cliff • ${arbitrage.urgentCliffWindow}", color = colors.error, style = TelemetrySmall)
                        }

                        Icon(
                            imageVector = LiraIcons.SwapHoriz,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .align(Alignment.CenterVertically)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text("DONOR CONTAINER", color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${arbitrage.donorContainerId} (${arbitrage.donorContainerType})", color = colors.textPrimary, style = TelemetrySmall, fontWeight = FontWeight.Bold)
                            Text(text = arbitrage.donorFreeTimeRemaining, color = colors.textSecondary, style = TelemetrySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Modeled Penalty Exposure
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated, IndustrialShape)
                            .border(1.dp, colors.border, IndustrialShape)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("MODELED PENALTY EXPOSURE", color = colors.warning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "₹${arbitrage.urgentCliffInr} INR Projected", color = colors.warning, style = TelemetryHeadline)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("CLIFF HORIZON", color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = arbitrage.urgentCliffWindow, color = colors.textPrimary, style = TelemetryMedium)
                        }
                    }
                }
            }

            // Gate Delay & Staging Arrival Dossier
            val immunity = assignment.demurrageImmunity ?: com.lira.field.core.model.DemurrageImmunityDossier()
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
                            text = "GATE ARRIVAL & DELAY AUDIT RECORD",
                            style = TrackedHeaderLabel,
                            color = colors.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(colors.surfaceElevated, IndustrialShape)
                                .border(1.dp, colors.border, IndustrialShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("LOGGED", color = colors.textSecondary, style = TelemetrySmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notice on Legal Immunity Prohibition
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated, IndustrialShape)
                            .border(1.dp, colors.border, IndustrialShape)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "STAGING AUDIT RECORD • Presentation of gate arrival proof for terminal dispute review. Does not constitute unilateral statutory immunity or automatic detention waiver.",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailRow("Driver Staging Arrival", immunity.driverStagingTimestamp, colors.textPrimary, isMonospace = true)
                    DetailRow("Slot Cutoff Deadline", immunity.slotCutoffTimestamp, colors.primary, isMonospace = true)
                    DetailRow("Terminal Delay Trigger", immunity.terminalFaultReason, colors.error, isMonospace = false)
                    DetailRow("Audit Proof Hash", immunity.hmacProof.take(28) + "...", colors.textTertiary, isMonospace = true)
                }
            }

            // Transit Corridor & Routing
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TERMINAL APPOINTMENT & DELIVERY CORRIDOR",
                        style = TrackedHeaderLabel,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DetailRow("Pickup Terminal", assignment.portTerminal, colors.textPrimary)
                    DetailRow("Delivery Destination", assignment.destination, colors.textPrimary)
                    DetailRow("Gate Slot Window", "${assignment.slotId} (${assignment.slotStart.takeLast(8).take(5)} - ${assignment.slotEnd.takeLast(8).take(5)})", colors.primary, isMonospace = true)
                    DetailRow("Appointment Cutoff", assignment.deadline, colors.error, isMonospace = true)
                    DetailRow("Marshal Bay Gate", assignment.checkpointId, colors.textPrimary, isMonospace = true)
                }
            }

            // Plan Comparison: Original vs Revised
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SOLVER REASSIGNMENT REASONING",
                        style = TrackedHeaderLabel,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ORIGINAL DISPATCH", color = colors.textTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(assignment.previousPlanSummary ?: "T04 @ 16:00 (Slot S1600)", color = colors.textSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("Status: CONFLICT", color = colors.warning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Icon(
                            imageVector = LiraIcons.ArrowForward,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .align(Alignment.CenterVertically)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text("OPTIMIZED RECOVERY", color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("${assignment.truckId} @ ${assignment.slotId}", color = colors.textPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Status: ${assignment.status}", color = colors.success, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("SOLVER RESOLUTION RATIONALE:", style = TrackedHeaderLabel, color = colors.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = assignment.reasonText ?: "Slot optimized by QpiAI XY-QAOA + Cubic HUBO validator on server.",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Equipment, Obligations & Release Prerequisites
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "EQUIPMENT & DRIVER OBLIGATIONS",
                        style = TrackedHeaderLabel,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DetailRow("Required Equipment", assignment.requiredEquipment, colors.textPrimary)
                    DetailRow("Driver Obligations", assignment.driverObligations, colors.textPrimary)
                    DetailRow("Release Prerequisites", assignment.releasePrerequisites, colors.primary)
                }
            }

            // Grounded Evidence Identifiers
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "GROUNDED EVIDENCE IDENTIFIERS",
                        style = TrackedHeaderLabel,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (assignment.evidenceIds.isEmpty()) {
                        Text("No external evidence linked.", color = colors.textTertiary, fontSize = 12.sp)
                    } else {
                        assignment.evidenceIds.forEach { evId ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = LiraIcons.ShieldCheck,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = evId,
                                    color = colors.textPrimary,
                                    style = TelemetrySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (evId.contains("DOC")) "Shipping Document Verification" else "Driver Agent Fact Check",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortTelemetryBadge(
    label: String,
    detail: String,
    badgeColor: Color,
    badgeBg: Color,
    badgeBorder: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(badgeBg, IndustrialShape)
            .border(1.dp, badgeBorder, IndustrialShape)
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, style = TrackedHeaderLabel, color = badgeColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                color = LiraTheme.colors.textPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = LiraTheme.colors.textPrimary,
    isMonospace: Boolean = false
) {
    val colors = LiraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = colors.textSecondary, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.sp,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
