package com.lira.field.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.core.model.WhyEvidence
import com.lira.field.core.model.WhySheet
import com.lira.field.ui.theme.IndustrialShape
import com.lira.field.ui.theme.LiraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhySheetContent(
    whySheet: WhySheet,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WHY THIS ACTION?",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(colors.surfaceElevated, RoundedCornerShape(3.dp))
                                .border(0.5.dp, colors.primary, RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = when (whySheet.origin) {
                                    com.lira.field.core.model.WhyOrigin.LIVE_CALL -> "LIVE CALL"
                                    com.lira.field.core.model.WhyOrigin.COPILOT -> "COPILOT"
                                    com.lira.field.core.model.WhyOrigin.STRESS_PROMOTED -> "STRESS PROMOTED"
                                    com.lira.field.core.model.WhyOrigin.STATION_BRANCH -> "STATION PLAN B"
                                    else -> "DISPATCHER"
                                },
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    }
                    Text(
                        text = "Evidence & Cost Attribution Dossier",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(LiraIcons.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Insufficient Evidence Warning
            if (whySheet.insufficientEvidence) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.warning.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, colors.warning.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Not enough verified evidence yet: dispatch is checking.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.warning
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // QpiAI Decision Dossier Section per LIRA v13 Spec
            if (whySheet.decision != null) {
                val dec = whySheet.decision
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, RoundedCornerShape(8.dp))
                        .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "QpiAI DECISION MATRIX",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                letterSpacing = 1.sp
                            )
                            if (dec.origin == com.lira.field.core.model.DecisionOrigin.SIMULATED_CALL) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF97316).copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                        .border(1.dp, Color(0xFFF97316), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SIMULATED",
                                        color = Color(0xFFF97316),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Full monospace run_id
                        Column {
                            Text(
                                text = "RUN ID",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textTertiary
                            )
                            Text(
                                text = dec.runId ?: "—",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ENGINE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = colors.textTertiary)
                                Text(dec.engine ?: "—", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            }
                            Column {
                                Text("EXECUTION MODE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = colors.textTertiary)
                                Text(dec.executionMode ?: "—", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            }
                            Column {
                                Text("VALIDATOR", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = colors.textTertiary)
                                Text(dec.validator ?: "—", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (dec.validator == "PASS") colors.success else colors.textPrimary)
                            }
                        }

                        if (!dec.jobId.isNullOrBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("JOB ID", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = colors.textTertiary)
                                Text(dec.jobId, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = colors.textSecondary)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ALTERNATIVES CONSIDERED", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = colors.textTertiary)
                            Text(dec.alternativesConsidered?.toString() ?: "—", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        if (!dec.greedyWouldHave.isNullOrBlank()) {
                            Column {
                                Text("GREEDY WOULD HAVE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = colors.textTertiary)
                                Text(dec.greedyWouldHave, fontSize = 11.sp, color = colors.warning)
                            }
                        }

                        val reasonToShow = dec.reasonLocal?.takeIf { it.isNotBlank() } ?: dec.reasonEn ?: "—"
                        Column {
                            Text("SELECTION REASON", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = colors.textTertiary)
                            Text(reasonToShow, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Driver Calls / Statements Section
            if (whySheet.calls.isNotEmpty()) {
                Text(
                    text = "CALL STATEMENTS (${whySheet.calls.size})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.textSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    whySheet.calls.forEach { call ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surfaceElevated, RoundedCornerShape(4.dp))
                                .border(0.5.dp, colors.border, RoundedCornerShape(4.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = call.role ?: "Driver",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "T4 · DRIVER STATEMENT",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                }
                                if (!call.quoteOriginal.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${call.quoteOriginal}\"",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = colors.textPrimary,
                                        lineHeight = 15.sp
                                    )
                                }
                                if (!call.quoteEn.isNullOrBlank() && call.quoteEn != call.quoteOriginal) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "\"${call.quoteEn}\"",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                                if (!call.executionIdMasked.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Exec: ${call.executionIdMasked}",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colors.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Forecast Provenance for Standby / Quantum Risk Modeled
            if (whySheet.forecastProvenance != null) {
                val fp = whySheet.forecastProvenance!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF6366F1).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FORECAST (MODELED)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF818CF8)
                            )
                            if (fp.qcloudJobId != null) {
                                Text(
                                    text = "Job: ${fp.qcloudJobId}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(0xFFA5B4FC)
                                )
                            }
                        }
                        Text(
                            text = "Model: ${fp.model}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Simulated quantum corridor risk scenario forecast. Reserve standby allocated as preemptive buffer.",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Evidence Section Header
            Text(
                text = "VERIFIED EVIDENCE (${whySheet.evidence.size})",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.textSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                whySheet.evidence.forEach { ev ->
                    EvidenceItemCard(ev)
                }
            }

            // Cost Breakdown Section
            if (whySheet.costBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "COST EXPOSURE BREAKDOWN",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.textSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, RoundedCornerShape(4.dp))
                        .border(0.5.dp, colors.border, RoundedCornerShape(4.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    whySheet.costBreakdown.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.component,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${item.amount}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(colors.surface, RoundedCornerShape(2.dp))
                                        .border(0.5.dp, colors.border, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = item.label,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colors.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Provenance footnote
            val run = whySheet.solverRuns.firstOrNull()
            if (run != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "QpiAI Opt Run ${run.runId} · Mode: ${run.mode ?: "HUBO"} · Validator: ${run.validator ?: "PASS"}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = colors.textTertiary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EvidenceItemCard(evidence: WhyEvidence) {
    val colors = LiraTheme.colors
    val (tierLabel, tierColor) = when (evidence.authorityTier) {
        "T1" -> "T1 Official" to colors.success
        "T2" -> "T2 Site station" to colors.primary
        "T3" -> "T3 Internal" to colors.warning
        else -> "T4 Statement" to colors.textSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated, RoundedCornerShape(4.dp))
            .border(0.5.dp, colors.border, RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = evidence.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(tierColor.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tierLabel,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )
                }
            }

            if (!evidence.quote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = evidence.quote,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 14.sp
                )
            }

            if (!evidence.observedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Observed: ${evidence.observedAt.takeLast(14).take(8)}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textTertiary
                )
            }
        }
    }
}

