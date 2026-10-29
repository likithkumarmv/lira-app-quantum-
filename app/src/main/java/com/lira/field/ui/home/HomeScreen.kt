package com.lira.field.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.lira.field.R
import com.lira.field.core.util.PnrHelper
import com.lira.field.core.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import android.content.Context
import com.lira.field.core.model.*
import com.lira.field.ui.components.*
import com.lira.field.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToEta: () -> Unit,
    onNavigateToCheckpoint: (String) -> Unit,
    onNavigateToDisruptions: () -> Unit
) {
    val assignments by viewModel.assignments.collectAsState()
    val v13Assignments by viewModel.v13Assignments.collectAsState()
    val activeV13 = v13Assignments.firstOrNull { it.assignmentKind == com.lira.field.core.model.AssignmentKind.ACTIVE }
        ?: v13Assignments.firstOrNull()
    val standbyItem = v13Assignments.firstOrNull { it.assignmentKind == com.lira.field.core.model.AssignmentKind.RESERVE_STANDBY }
    val bayBoard by viewModel.bayBoard.collectAsState()
    val interchanges by viewModel.interchanges.collectAsState()
    val calls by viewModel.calls.collectAsState()
    val activeWhySheet by viewModel.activeWhySheet.collectAsState()
    val useLabFixture by viewModel.useLabFixture.collectAsState()
    val takeoverAssignment by viewModel.takeoverAssignment.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val ttsHelper = remember { com.lira.field.core.voice.LiraTtsHelper(context) }
    DisposableEffect(ttsHelper) {
        onDispose { ttsHelper.shutdown() }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmittingAck by viewModel.isSubmittingAck.collectAsState()
    val message by viewModel.message.collectAsState()
    val session by viewModel.session.collectAsState()
    val wave2Alert by viewModel.wave2Alert.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val language by com.lira.field.LiraApp.instance.sessionRepository.language.collectAsState()

    val colors = LiraTheme.colors

    var showCannotComplyDialog by remember { mutableStateOf(false) }
    var showVoiceReportDialog by remember { mutableStateOf(false) }
    var targetAssignmentId by remember { mutableStateOf<String?>(null) }
    var selectedReason by remember { mutableStateOf(CannotComplyReasons.TRUCK_BREAKDOWN) }
    var driverNotes by remember { mutableStateOf("") }

    // Filter to only approved / active dispatches (never unapproved proposals)
    val activeAssignment = assignments.firstOrNull {
        it.status == AssignmentStatus.APPROVED ||
        it.status == AssignmentStatus.ACKNOWLEDGED ||
        it.status == AssignmentStatus.PHYSICAL_NO_SHOW ||
        it.status == AssignmentStatus.SUPERSEDED
    } ?: assignments.firstOrNull()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(language) {
        viewModel.pnrAlertEvent.collect { status ->
            if (status == PnrStatus.PNR_IMMINENT) {
                val ttsMsg = PnrHelper.getPnrImminentTtsMessage(language)
                NotificationHelper.notifyPnrImminent(
                    context = context,
                    title = "POINT OF NO RETURN IMMINENT",
                    body = ttsMsg
                )
                try {
                    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                        vibratorManager?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                    }
                } catch (_: Exception) {}

                ttsHelper.speak(
                    text = ttsMsg,
                    lang = language
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Lab Fixture Persistent Red Ribbon (Section 8)
            if (useLabFixture) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFDC2626))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LAB FIXTURE MODE ACTIVE • STRICT REPRODUCIBILITY",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            // Wave 2 or Replacement Alert Banner
            if (wave2Alert != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.warningSurface)
                        .border(1.dp, colors.warningBorder)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LiraIcons.AlertTriangle,
                                contentDescription = null,
                                tint = colors.warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = wave2Alert!!,
                                color = colors.warning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearWaveAlert() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(LiraIcons.Close, contentDescription = "Dismiss", tint = colors.warning, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Driver Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(1.dp, colors.border)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = LiraIcons.Truck,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = session?.displayName ?: "Field Operator",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Truck: ${session?.truckId ?: "--"} • Role: ${session?.role ?: "DRIVER"}",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val sseState by (viewModel.sseClient?.connectionState ?: MutableStateFlow(com.lira.field.core.network.SseConnectionState.DISCONNECTED)).collectAsState()
                            val (badgeText, badgeColor, badgeBg) = when (sseState) {
                                com.lira.field.core.network.SseConnectionState.CONNECTED -> Triple("LIVE ●", Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
                                com.lira.field.core.network.SseConnectionState.POLLING_FALLBACK -> Triple("POLLING", Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
                                else -> {
                                    val syncStr = if (lastSyncTime != null && lastSyncTime != 0L) {
                                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastSyncTime!!))
                                    } else "—"
                                    Triple("OFFLINE · last sync $syncStr", Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Theme Quick-Toggle Button
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (colors.isDark) LiraIcons.Sun else LiraIcons.Moon,
                            contentDescription = if (colors.isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = colors.primary
                        )
                    }

                    IconButton(onClick = { viewModel.refreshAssignments() }) {
                        Icon(
                            imageVector = LiraIcons.Refresh,
                            contentDescription = "Refresh",
                            tint = colors.primary
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Instruction Card (Top - Single most important line on the screen)
                val instructionText = activeV13?.instruction?.text ?: activeAssignment?.reasonText ?: "—"
                val instructionEn = activeV13?.instruction?.textEn ?: ""

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = IndustrialShape,
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(colors.primary)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DISPATCH INSTRUCTION",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .background(colors.primaryContainer, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = activeV13?.instruction?.source ?: "SARVAM_TRANSLATE",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colors.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = instructionText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                lineHeight = 26.sp
                            )
                            if (!instructionEn.isNullOrBlank() && instructionEn != instructionText) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = instructionEn,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 18.sp
                                )
                            }

                            // Point of No Return (PNR) Section
                            val isLost = activeV13?.pnrStatus == PnrStatus.ALREADY_LOST ||
                                    activeAssignment?.pnrStatus.equals("ALREADY_LOST", ignoreCase = true)

                            if (isLost) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceElevated)
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = LiraIcons.Info,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.pnr_already_lost_title),
                                            color = colors.textSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = colors.border, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                val pnrColor = PnrHelper.getStatusColor(activeV13?.pnrStatus)
                                val actByRaw = activeV13?.actBy ?: activeAssignment?.actBy
                                val countdownText = if (!actByRaw.isNullOrBlank()) {
                                    PnrHelper.formatCountdown(actByIso = actByRaw, language = language)
                                } else "—"
                                val portDeadlineToDisplay = activeV13?.pnr?.takeLast(14)?.take(5)
                                    ?: activeV13?.freightCase?.hardDeadline?.takeLast(14)?.take(5)
                                    ?: activeAssignment?.deadline?.takeLast(14)?.take(5)
                                    ?: "—"

                                val actByTimeToDisplay = activeV13?.actBy?.takeLast(14)?.take(5) ?: "—"
                                val explanation = activeWhySheet?.pnrExplanation
                                    ?: if (actByTimeToDisplay != "—") stringResource(R.string.pnr_explanation_template, actByTimeToDisplay) else "—"

                                // Big Countdown colored by pnr_status
                                Text(
                                    text = countdownText,
                                    color = pnrColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Visible deadline small underneath
                                Text(
                                    text = if (portDeadlineToDisplay != "—") stringResource(R.string.pnr_port_deadline, portDeadlineToDisplay) else "Port deadline —",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // One-line explanation under it
                                Text(
                                    text = explanation,
                                    color = colors.textTertiary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                // "Why you?" Line
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Why you? ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                    Text(
                                        text = com.lira.field.core.model.QpiAiDecisionHelper.formatWhyYou(activeWhySheet?.decision),
                                        fontSize = 13.sp,
                                        color = colors.textPrimary,
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Provenance Chip: "QpiAI run <first 8 chars> · validator <validator>"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.surfaceElevated)
                                        .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                        .clickable {
                                            activeV13?.let { viewModel.openWhySheet(it.assignmentId) }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = com.lira.field.core.model.QpiAiDecisionHelper.formatProvenanceChip(activeWhySheet?.decision),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textSecondary
                                    )
                                    if (com.lira.field.core.model.QpiAiDecisionHelper.isSimulated(activeWhySheet?.decision)) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFF97316).copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                .border(0.5.dp, Color(0xFFF97316), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "SIMULATED",
                                                color = Color(0xFFF97316),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Standby Duty Card (Section 2 - Reserve / Standby from Quantum Risk Forecast)
                if (standbyItem != null) {
                    item {
                        com.lira.field.ui.components.StandbyCard(
                            assignment = standbyItem,
                            onGotIt = {
                                viewModel.postReceipt(standbyItem.assignmentId, standbyItem.planVersion)
                            },
                            onNotAvailable = {
                                targetAssignmentId = standbyItem.assignmentId
                                showCannotComplyDialog = true
                            },
                            onWhyStandby = {
                                viewModel.openWhySheet(standbyItem.assignmentId)
                            }
                        )
                    }
                }

                // 2. Active Branch Banner (Section 3)
                if (activeV13?.activeBranch != null) {
                    val branch = activeV13.activeBranch!!
                    val newWindow = activeV13.bay?.let { "${it.windowStart?.takeLast(14)?.take(5)}–${it.windowEnd?.takeLast(14)?.take(5)}" } ?: "15:00"
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = IndustrialShape,
                            colors = CardDefaults.cardColors(containerColor = colors.warningSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(colors.warning)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PLAN ${branch.branchId} ACTIVE",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.warning
                                    )
                                    if (branch.provisional) {
                                        Box(
                                            modifier = Modifier
                                                .background(colors.warning.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                                .border(1.dp, colors.warning, RoundedCornerShape(3.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PROVISIONAL",
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.warning
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "T11 took the earlier window. Your new window: $newWindow",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "This backup was approved in advance by your manager.",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                if (!branch.preApprovedBy.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Pre-approved by ${branch.preApprovedBy} at ${branch.preApprovedAt?.takeLast(14)?.take(5) ?: "13:00"}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colors.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Plan Superseded Banner (Section 3)
                val isPlanSuperseded = activeV13?.status == AssignmentStatusV13.SUPERSEDED || activeAssignment?.status == AssignmentStatus.SUPERSEDED
                if (isPlanSuperseded) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = IndustrialShape,
                            colors = CardDefaults.cardColors(containerColor = colors.errorSurface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.error))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PLAN REPLACED BY DISPATCH",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.error
                                    )
                                    Button(
                                        onClick = { viewModel.refreshAssignments() },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        shape = IndustrialShape,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("See new job", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Plan replaced by dispatch: v3 → v4",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }

                // 4. Freight Exception Case Card (C1-C6)
                activeAssignment?.freightCase?.let { fCase ->
                    item {
                        FreightExceptionCard(
                            freightCase = fCase,
                            onViewEvidence = { onNavigateToDetails(activeAssignment.assignmentId) }
                        )
                    }
                }

                // 5. Bay Card (Refreshed every 30s)
                item {
                    BayCard(
                        bay = activeV13?.bay,
                        bayBoard = bayBoard,
                        currentTruckId = session?.truckId ?: ""
                    )
                }

                // 6. Interchange Tracker (Read-only 5-step Stepper)
                item {
                    InterchangeTracker(
                        summary = activeV13?.interchange,
                        interchange = interchanges.firstOrNull()
                    )
                }

                // 7. Calls Card
                if (calls.isNotEmpty()) {
                    calls.take(3).forEach { callItem ->
                        item {
                            CallV13Card(
                                call = callItem,
                                onAnswerHereInstead = {
                                    targetAssignmentId = activeAssignment?.assignmentId
                                    showCannotComplyDialog = true
                                }
                            )
                        }
                    }
                } else {
                    activeAssignment?.bolnaVerification?.let { bolna ->
                        if (bolna.status != BolnaCallStatus.CALLING_AGENT_NOT_CONFIGURED) {
                            item {
                                BolnaCallingCard(
                                    verification = bolna,
                                    onAnswerYes = {
                                        viewModel.respondToInvestigationTask(activeAssignment.assignmentId, bolna.callExecutionId, "YES")
                                    },
                                    onAnswerNo = {
                                        viewModel.respondToInvestigationTask(activeAssignment.assignmentId, bolna.callExecutionId, "NO")
                                    }
                                )
                            }
                        }
                    }
                }

                // 8. Approved Assignment Details
                if (activeAssignment != null) {

                    // Unapproved Plan Guard
                    if (activeAssignment.status == "PENDING" || activeAssignment.status == "AWAITING_APPROVAL") {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.warningSurface, IndustrialShape)
                                    .border(1.dp, colors.warningBorder, IndustrialShape)
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(LiraIcons.AlertTriangle, contentDescription = null, tint = colors.warning, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("AWAITING MANAGER APPROVAL", style = TrackedHeaderLabel, color = colors.warning)
                                        Text("Recovery proposal generated by QpiAI solver. Not yet executable by driver until dispatcher approval.", color = colors.textSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ActiveAssignmentCard(
                            assignment = activeAssignment,
                            isSubmittingAck = isSubmittingAck,
                            isSuperseded = isPlanSuperseded,
                            onAcknowledge = { viewModel.acknowledge(activeAssignment.assignmentId) },
                            onCannotComply = {
                                targetAssignmentId = activeAssignment.assignmentId
                                showCannotComplyDialog = true
                            },
                            onViewDetails = { onNavigateToDetails(activeAssignment.assignmentId) },
                            onReportDelay = onNavigateToEta,
                            onOpenWhySheet = { viewModel.openWhySheet(activeAssignment.assignmentId) }
                        )
                    }

                    // Field Operations Quick Actions
                    item {
                        Text(
                            text = "FIELD OPERATIONS",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionCard(
                                title = "Report Delay / ETA",
                                subtitle = "Submit delay reason to dispatcher",
                                icon = LiraIcons.Schedule,
                                tint = colors.primary,
                                onClick = onNavigateToEta,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = "ResourceLock Status",
                                subtitle = "ESP32 RFID & Web Custodian Status",
                                icon = LiraIcons.Sensors,
                                tint = colors.warning,
                                onClick = { onNavigateToCheckpoint(activeAssignment.assignmentId) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        QuickActionCard(
                            title = "Voice Report (Sarvam ASR)",
                            subtitle = "Speak in your language: submit verified T4 report",
                            icon = LiraIcons.Mic,
                            tint = colors.primary,
                            onClick = { showVoiceReportDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        QuickActionCard(
                            title = "Risk Alerts & Terminal Holds",
                            subtitle = "Route weather, cyclone alerts, and gate congestion",
                            icon = LiraIcons.AlertTriangle,
                            tint = colors.warning,
                            onClick = onNavigateToDisruptions,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        EmptyAssignmentCard(
                            truckId = session?.truckId ?: "--",
                            onRefresh = { viewModel.refreshAssignments() }
                        )
                    }
                }
            }
        }
    }

    // Reason dialog for CANNOT_COMPLY with full 9 PRD reasons + notes
    if (showCannotComplyDialog && targetAssignmentId != null) {
        AlertDialog(
            onDismissRequest = { showCannotComplyDialog = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "CANNOT COMPLY — EXCEPTION REASON",
                    color = colors.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    Text(
                        text = "Select primary blocker preventing execution of this assignment:",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(CannotComplyReasons.ALL.size) { idx ->
                            val (code, label) = CannotComplyReasons.ALL[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedReason == code) colors.surfaceElevated else Color.Transparent)
                                    .clickable { selectedReason = code }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedReason == code,
                                    onClick = { selectedReason = code },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.error,
                                        unselectedColor = colors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = label, color = colors.textPrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = driverNotes,
                        onValueChange = { driverNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Optional operational notes...", fontSize = 12.sp) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCannotComplyDialog = false
                        targetAssignmentId?.let { viewModel.cannotComply(it, selectedReason, driverNotes.ifBlank { null }) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = Color.White)
                ) {
                    Text("CONFIRM EXCEPTION", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showCannotComplyDialog = false
                        showVoiceReportDialog = true
                    }) {
                        Text("Speak Voice Note", color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showCannotComplyDialog = false }) {
                        Text("CANCEL", color = colors.textSecondary)
                    }
                }
            }
        )
    }

    if (showVoiceReportDialog) {
        VoiceReportDialog(
            assignmentId = activeAssignment?.assignmentId ?: "ASG-C5-T09-3",
            planVersion = activeV13?.planVersion ?: 3,
            onDismiss = { showVoiceReportDialog = false },
            onConfirmed = { report ->
                viewModel.confirmVoiceReport(report)
                showVoiceReportDialog = false
            }
        )
    }

    activeWhySheet?.let { sheet ->
        WhySheetContent(
            whySheet = sheet,
            onDismiss = { viewModel.closeWhySheet() }
        )
    }

    takeoverAssignment?.let { takeover ->
        com.lira.field.ui.components.TakeoverDialog(
            assignment = takeover,
            language = language,
            ttsHelper = ttsHelper,
            whySheet = activeWhySheet,
            onAck = {
                viewModel.submitAck(
                    assignmentId = takeover.assignmentId,
                    planVersion = takeover.planVersion,
                    action = "ACK"
                )
                viewModel.dismissTakeover()
            },
            onCannotComply = {
                targetAssignmentId = takeover.assignmentId
                showCannotComplyDialog = true
                viewModel.dismissTakeover()
            },
            onDismiss = {
                viewModel.dismissTakeover()
            },
            onTakeoverShown = { asgId, pv ->
                viewModel.postReceipt(asgId, pv)
            }
        )
    }
}

@Composable
private fun ActiveAssignmentCard(
    assignment: Assignment,
    isSubmittingAck: Boolean,
    isSuperseded: Boolean = false,
    onAcknowledge: () -> Unit,
    onCannotComply: () -> Unit,
    onViewDetails: () -> Unit,
    onReportDelay: () -> Unit,
    onOpenWhySheet: () -> Unit = {}
) {
    val colors = LiraTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSuperseded) Modifier.alpha(0.65f) else Modifier),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = if (isSuperseded) colors.surfaceElevated else colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (isSuperseded) colors.errorBorder else colors.border))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Plan Header with Wave # and Urgency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(IndustrialShape)
                            .background(colors.primaryContainer)
                            .border(1.dp, colors.primary.copy(alpha = 0.5f), IndustrialShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "WAVE ${assignment.recoveryWave}",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${assignment.planId} RECOVERY (v${assignment.planVersion})",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(IndustrialShape)
                            .background(if (assignment.urgency == UrgencyLevel.CRITICAL) colors.errorSurface else colors.warningSurface)
                            .border(1.dp, if (assignment.urgency == UrgencyLevel.CRITICAL) colors.errorBorder else colors.warningBorder, IndustrialShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = assignment.urgency,
                            color = if (assignment.urgency == UrgencyLevel.CRITICAL) colors.error else colors.warning,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    AssignmentStatusBadge(status = assignment.status)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Container & Truck Hero
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CONTAINER",
                        color = colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    val formattedContainer = com.lira.field.core.util.ContainerNumberUtils.format(assignment.containerId)
                    Text(
                        text = formattedContainer,
                        color = colors.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "BL: MEDUJN2610447",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TRUCK ID",
                        color = colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = assignment.truckId,
                        color = colors.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route Corridor: Port Terminal -> Destination
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(IndustrialShape)
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border, IndustrialShape)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(LiraIcons.Route, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PORT: ${assignment.portTerminal}",
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "DEST: ${assignment.destination}",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = colors.border)
            Spacer(modifier = Modifier.height(12.dp))

            // Slot, Checkpoint, and Modeled Counterfactual Exposure details
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoColumn(
                    label = "CONFIRMED SLOT",
                    value = "${assignment.slotId} (${formatTime(assignment.slotStart)} - ${formatTime(assignment.slotEnd)})",
                    modifier = Modifier.weight(1f)
                )
                InfoColumn(
                    label = "DEADLINE REMAINING",
                    value = calculateRemainingTime(assignment.deadline),
                    valueColor = colors.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoColumn(
                    label = "CHECKPOINT",
                    value = assignment.checkpointId,
                    modifier = Modifier.weight(1f)
                )
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "MODELED EXPOSURE (₹)",
                        color = colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${assignment.modeledCounterfactualExposureInr} INR",
                        color = colors.warning,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Invoiced Actual: ₹${assignment.invoicedActualInr}",
                        color = colors.textTertiary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (assignment.slotSwapArbitrage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(IndustrialShape)
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border, IndustrialShape)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(LiraIcons.SwapHoriz, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECOVERY EXCHANGE ALLOCATION",
                            color = colors.primary,
                            style = TrackedHeaderLabel
                        )
                    }
                }
            }

            // Reason for Change (Essential WOW element)
            if (!assignment.reasonText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(IndustrialShape)
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border, IndustrialShape)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = LiraIcons.Info,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "REASON FOR CHANGE:",
                                color = colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = assignment.reasonText,
                                color = colors.textPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action / Acknowledge Area with Monotonicity Lockout
            if (assignment.isSuperseded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(IndustrialShape)
                        .background(colors.errorSurface)
                        .border(1.dp, colors.errorBorder, IndustrialShape)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(LiraIcons.Lock, contentDescription = null, tint = colors.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUPERSEDED BY WAVE 2 (PLAN v${assignment.planVersion})",
                                color = colors.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TRIGGER: ${assignment.invalidationTrigger ?: "EDGE_RFID_MISMATCH + OPERATOR_REPORT"}\nActions on this plan are locked out. Central engine dispatched replacement wave.",
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            } else if (assignment.pnrStatus?.equals("ALREADY_LOST", ignoreCase = true) == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(IndustrialShape)
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border, IndustrialShape)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LiraIcons.Info,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.pnr_already_lost_title),
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (assignment.driverAckState == DriverAckState.PENDING) {
                Text(
                    text = "ACTION REQUIRED: CONFIRM OR REJECT ASSIGNMENT",
                    color = colors.warning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAcknowledge,
                        enabled = !isSubmittingAck,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = IndustrialShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        if (isSubmittingAck) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "ACKNOWLEDGE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onCannotComply,
                        enabled = !isSubmittingAck,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = IndustrialShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.error)
                    ) {
                        Text(
                            text = "CANNOT COMPLY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (assignment.driverAckState == DriverAckState.ACKNOWLEDGED) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(IndustrialShape)
                        .background(colors.successSurface)
                        .border(1.dp, colors.successBorder, IndustrialShape)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LiraIcons.CheckCircle,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DISPATCH ACKNOWLEDGED",
                                color = colors.success,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = onReportDelay) {
                            Text("REPORT DELAY", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // View Details Link
            TextButton(
                onClick = onViewDetails,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "VIEW RECOVERY DETAILS & CONTRACT EVIDENCE >",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = colors.border, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Provenance Line (Section 6)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(IndustrialShape)
                    .clickable { onOpenWhySheet() }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Plan v${assignment.planVersion} · ${assignment.solverProvenance ?: "—"}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PhysicalCheckFailedCard(isSuperseded: Boolean = false) {
    val colors = LiraTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = colors.errorSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.errorBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = LiraIcons.AlertCircle,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isSuperseded) "PLAN SUPERSEDED BY RECOVERY WAVE" else "PHYSICAL CHECK FAILED",
                        color = colors.error,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSuperseded) "Previous plan invalidated by central engine" else "Checkpoint event: NO_SHOW",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = colors.errorBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Previous plan is no longer active.\nNew recovery assignment calculated via QpiAI XY-QAOA solver.",
                color = colors.textPrimary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun EmptyAssignmentCard(
    truckId: String,
    onRefresh: () -> Unit
) {
    val colors = LiraTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = LiraIcons.Job,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Active Assignment",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Truck $truckId has no assigned recovery slots at this time. When a dispatcher approves a new plan, it will appear here automatically.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRefresh,
                shape = IndustrialShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
            ) {
                Text("CHECK SERVER", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(IndustrialShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = colors.textSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String, valueColor: Color = LiraTheme.colors.textPrimary, modifier: Modifier = Modifier) {
    val colors = LiraTheme.colors
    Column(modifier = modifier) {
        Text(text = label, color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatTime(isoString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(isoString) ?: return isoString
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) {
        isoString.takeLast(8).take(5)
    }
}

private fun calculateRemainingTime(deadlineIso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(deadlineIso) ?: return "Pending"
        val diffMs = date.time - System.currentTimeMillis()
        if (diffMs <= 0) return "EXPIRED"
        val hours = diffMs / (1000 * 60 * 60)
        val mins = (diffMs / (1000 * 60)) % 60
        "${hours}h ${mins}m left"
    } catch (_: Exception) {
        "2h 30m left"
    }
}

@Composable
private fun QpiAiInvestigationCard(
    task: QpiAiInvestigationTask,
    onAnswer: (String) -> Unit,
    onCannotComply: () -> Unit
) {
    val colors = LiraTheme.colors
    val hasSolverRun = !task.solverRunId.isNullOrEmpty()
    val isPending = task.responseState == "PENDING_DRIVER_RESPONSE"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (isPending) colors.primary else colors.border))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Provenance badge (Only badge QpiAI if valid solverRunId exists)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = LiraIcons.Info,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.headline,
                        style = TrackedHeaderLabel,
                        color = colors.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (hasSolverRun) colors.primaryContainer else colors.surfaceElevated,
                            IndustrialShape
                        )
                        .border(
                            1.dp,
                            if (hasSolverRun) colors.primary else colors.border,
                            IndustrialShape
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (hasSolverRun) "${task.provenanceLabel} #${task.solverRunId}" else "FIELD VERIFICATION",
                        color = if (hasSolverRun) colors.primary else colors.textSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = task.questionText,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Deadline Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deadline: ${task.deadline.take(16).replace("T", " ")}",
                    color = colors.textTertiary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Target: ${task.targetTruckId}",
                    color = colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // One-tap Action Buttons or Status
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAnswer("CONFIRMED_ON_SCHEDULE") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White),
                        shape = IndustrialShape
                    ) {
                        Icon(LiraIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CONFIRM: ON TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onCannotComply,
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                        shape = IndustrialShape
                    ) {
                        Icon(LiraIcons.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CANNOT COMPLY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, IndustrialShape)
                        .border(1.dp, colors.border, IndustrialShape)
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(LiraIcons.CheckCircle, contentDescription = null, tint = colors.success, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESPONSE LOGGED: ${task.responseState}",
                            color = colors.success,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
