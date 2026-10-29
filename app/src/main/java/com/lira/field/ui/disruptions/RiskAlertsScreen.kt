package com.lira.field.ui.disruptions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.core.model.DataSourceMode
import com.lira.field.core.model.Disruption
import com.lira.field.core.model.DisruptionType
import com.lira.field.core.model.TrustState
import com.lira.field.ui.components.LiraIcons
import com.lira.field.ui.components.TrustStateBadge
import com.lira.field.ui.theme.*

import androidx.compose.ui.graphics.Color
import com.lira.field.ui.components.VoiceReportDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAlertsScreen(
    viewModel: RiskAlertsViewModel,
    onBack: () -> Unit
) {
    val disruptions by viewModel.disruptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showVoiceReportDialog by remember { mutableStateOf(false) }

    val colors = LiraTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RISK ALERTS & CORRIDOR INTELLIGENCE",
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
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(imageVector = LiraIcons.Refresh, contentDescription = "Refresh", tint = colors.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = { showVoiceReportDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = IndustrialShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Icon(LiraIcons.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECORD VOICE REPORT (SARVAM ASR)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            item {
                // Informational Banner explaining Forecast vs Confirmed per PRD v9.0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, IndustrialShape)
                        .border(1.dp, colors.border, IndustrialShape)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = LiraIcons.Shield,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CORRIDOR RISK INTELLIGENCE & BOUNDARIES (PRD v9.0)",
                                style = TrackedHeaderLabel,
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "• Weather & Traffic: Projections from backend control tower (TomTom/OpenWeather). Strictly labeled PREDICTED (NON-AUTHORITATIVE).\n• Port & Gate Closures: Only declared when authoritative CONFIRMED_PORT_CLOSURE feed is validated by server.\n• Zero Third-Party Keys: The Android APK contains zero OpenWeather, Google Maps, or TomTom API keys. All ingestion and verification runs on the server.",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
            } else if (disruptions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = IndustrialShape,
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No active route disruptions reported by central dispatch.", color = colors.textSecondary)
                        }
                    }
                }
            } else {
                items(disruptions) { disruption ->
                    DisruptionCard(disruption)
                }
            }
        }

        if (showVoiceReportDialog) {
            VoiceReportDialog(
                assignmentId = "ASG-C5-T09-3",
                onDismiss = { showVoiceReportDialog = false },
                onConfirmed = { report ->
                    showVoiceReportDialog = false
                }
            )
        }
    }
}

@Composable
private fun DisruptionCard(disruption: Disruption) {
    val colors = LiraTheme.colors
    val isConfirmed = disruption.trustState == TrustState.CONFIRMED_DISRUPTION || disruption.type == DisruptionType.CONFIRMED_PORT_CLOSURE
    val cardBorder = if (isConfirmed) colors.errorBorder else colors.warningBorder
    val cardBg = if (isConfirmed) colors.errorSurface else colors.surfaceElevated

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(cardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Type + Prominent PREDICTED / CONFIRMED Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isConfirmed) LiraIcons.Block else LiraIcons.AlertTriangle,
                        contentDescription = null,
                        tint = if (isConfirmed) colors.error else colors.warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DisruptionType.formatLabel(disruption.type),
                        color = if (isConfirmed) colors.error else colors.warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                TrustStateBadge(trustState = disruption.trustState)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Epistemic classification chip
            Box(
                modifier = Modifier
                    .background(
                        if (isConfirmed) colors.errorSurface else colors.warningSurface,
                        IndustrialShape
                    )
                    .border(
                        1.dp,
                        if (isConfirmed) colors.errorBorder else colors.warningBorder,
                        IndustrialShape
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isConfirmed) "CONFIRMED DISRUPTION — OFFICIAL DIRECTIVE" else "PREDICTED (NON-AUTHORITATIVE)",
                    color = if (isConfirmed) colors.error else colors.warning,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Disruption Title
            Text(
                text = disruption.title,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Provider & Data Source Telemetry Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, IndustrialShape)
                    .border(1.dp, colors.border, IndustrialShape)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Backend Provider:", color = colors.textSecondary, fontSize = 10.sp)
                        Text(
                            text = disruption.provider,
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Corridor:", color = colors.textSecondary, fontSize = 10.sp)
                        Text(
                            text = disruption.corridor,
                            color = colors.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Data Mode / TTL:", color = colors.textSecondary, fontSize = 10.sp)
                        Text(
                            text = "${disruption.dataSourceMode} • ${disruption.ttlMinutes}m TTL • ${disruption.freshnessSeconds}s ago",
                            color = colors.textTertiary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (disruption.isStale) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "⚠ STALE FEED: Exceeded TTL window. Cannot promote to confirmed closure.",
                            color = colors.error,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Route ETA & Slot Cutoff Telemetry (if predictions present)
            if (disruption.predictedDelayMinutes > 0 || disruption.revisedEta != null || disruption.bookedSlotCutoff != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, IndustrialShape)
                        .border(1.dp, colors.border, IndustrialShape)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Predicted Delay:", color = colors.textSecondary, fontSize = 11.sp)
                            Text(
                                text = "+${disruption.predictedDelayMinutes} MINS",
                                color = colors.warning,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (disruption.revisedEta != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Revised ETA:", color = colors.textSecondary, fontSize = 11.sp)
                                Text(
                                    text = disruption.revisedEta.take(16).replace("T", " "),
                                    color = colors.textPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (disruption.bookedSlotCutoff != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Booked Slot Cutoff:", color = colors.textSecondary, fontSize = 11.sp)
                                Text(
                                    text = disruption.bookedSlotCutoff.take(16).replace("T", " "),
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Operational Recommendation
            if (!disruption.recommendation.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, IndustrialShape)
                        .border(1.dp, colors.border, IndustrialShape)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = LiraIcons.Tips, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "OPERATIONAL RECOMMENDATION",
                                color = colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = disruption.recommendation,
                                color = colors.textPrimary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = colors.border)
            Spacer(modifier = Modifier.height(8.dp))

            // Metadata Row: Severity, Authority, Route, Affected Trucks
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SEVERITY", color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = disruption.severity,
                        color = if (disruption.severity == "CRITICAL" || disruption.severity == "HIGH") colors.error else colors.warning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1.5f)) {
                    Text("AUTHORITY SOURCE", color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = disruption.sourceAuthority, color = colors.textPrimary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Window: ${disruption.startsAt.take(16).replace("T", " ")} → ${disruption.endsAt?.take(16)?.replace("T", " ") ?: "Indefinite"}",
                    color = colors.textTertiary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Trucks: ${if (disruption.affectedTrucks.isEmpty()) "All Corridors" else disruption.affectedTrucks.joinToString(", ")}",
                    color = colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
