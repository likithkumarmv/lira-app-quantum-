package com.lira.field.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.lira.field.core.model.VoiceInterpretation
import com.lira.field.core.model.VoiceReport
import com.lira.field.core.model.VoiceReportConfirmRequest
import com.lira.field.core.voice.VoiceRecorderHelper
import com.lira.field.core.voice.VoiceReportUploadWorker
import com.lira.field.ui.theme.IndustrialShape
import com.lira.field.ui.theme.LiraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VoiceReportDialog(
    assignmentId: String,
    planVersion: Int = 1,
    onDismiss: () -> Unit,
    onConfirmed: (VoiceReport) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LiraTheme.colors
    val coroutineScope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    val recorderHelper = remember { VoiceRecorderHelper(context) }
    var isHolding by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableStateOf(0) }
    var currentReportId by remember { mutableStateOf("REP-${UUID.randomUUID().toString().take(8)}") }
    var currentIdempotencyKey by remember { mutableStateOf(UUID.randomUUID().toString()) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadResultReport by remember { mutableStateOf<VoiceReport?>(null) }
    var offlineQueuedMsg by remember { mutableStateOf<String?>(null) }

    // Correction mode ("Fix it")
    var isFixing by remember { mutableStateOf(false) }
    var selectedReasonCode by remember { mutableStateOf("TRUCK_BREAKDOWN") }
    var selectedEtaMinutes by remember { mutableStateOf(40) }

    // Recording timer
    LaunchedEffect(isHolding) {
        if (isHolding) {
            recordingDurationSec = 0
            while (isHolding && recordingDurationSec < 60) {
                delay(1000)
                recordingDurationSec++
            }
            if (recordingDurationSec >= 60 && isHolding) {
                isHolding = false
                val file = recorderHelper.stopRecording()
                if (file != null) {
                    // Trigger upload
                    isUploading = true
                }
            }
        }
    }

    Dialog(onDismissRequest = {
        if (!isHolding) {
            recorderHelper.cancelRecording()
            onDismiss()
        }
    }) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header & T4 Epistemic Honesty Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOICE REPORT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Box(
                        modifier = Modifier
                            .background(colors.warning.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DRIVER-REPORTED (T4)",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colors.warning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uploadResultReport == null) {
                    // Recording State
                    Text(
                        text = "Speak in your language: what happened and how late will you be?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hold-to-Talk Button
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = if (isHolding) colors.error else colors.primary,
                                shape = CircleShape
                            )
                            .border(
                                width = if (isHolding) 4.dp else 2.dp,
                                color = if (isHolding) colors.error.copy(alpha = 0.5f) else colors.primary.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .pointerInput(hasPermission) {
                                detectTapGestures(
                                    onPress = {
                                        if (!hasPermission) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            return@detectTapGestures
                                        }
                                        currentReportId = "REP-${UUID.randomUUID().toString().take(8)}"
                                        currentIdempotencyKey = UUID.randomUUID().toString()
                                        recorderHelper.startRecording(currentReportId)
                                        isHolding = true
                                        tryAwaitRelease()
                                        isHolding = false
                                        val recordedFile = recorderHelper.stopRecording()
                                        if (recordedFile != null && recordedFile.exists()) {
                                            isUploading = true
                                            // Process upload or simulate for lab fixture
                                            coroutineScope.launch {
                                                delay(1200) // simulated Sarvam ASR & translation
                                                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())
                                                val mockReport = VoiceReport(
                                                    reportId = currentReportId,
                                                    status = "TRANSCRIBED",
                                                    languageDetected = "hi-IN",
                                                    transcriptOriginal = "गाड़ी का टायर पंचर हो गया है, 40 मिनट लेट हो जाऊंगा।",
                                                    transcriptEn = "Vehicle tire punctured, will be about 40 minutes late.",
                                                    interpretation = VoiceInterpretation(
                                                        reasonCode = "TRUCK_BREAKDOWN",
                                                        etaMinutes = 40,
                                                        confidence = 0.95
                                                    ),
                                                    evidenceTier = "T4_DRIVER_STATEMENT",
                                                    provider = "SARVAM"
                                                )
                                                uploadResultReport = mockReport
                                                isUploading = false
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                LiraIcons.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (isHolding) "${recordingDurationSec}s / 60s" else "HOLD",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isHolding) "Recording... Release to submit" else "Press and hold to record",
                        fontSize = 11.sp,
                        color = if (isHolding) colors.error else colors.textSecondary
                    )

                    if (isUploading) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Uploading & transcribing via Sarvam...", fontSize = 11.sp, color = colors.textSecondary)
                    }

                } else {
                    // Review State: "We understood: ... Is this right?"
                    val report = uploadResultReport!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated, RoundedCornerShape(6.dp))
                            .border(0.5.dp, colors.border, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ORIGINAL (${report.languageDetected ?: "hi"}):",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textTertiary
                        )
                        Text(
                            text = "\"${report.transcriptOriginal ?: "—"}\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )

                        if (!report.transcriptEn.isNullOrBlank() && report.transcriptEn != report.transcriptOriginal) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "EN: \"${report.transcriptEn}\"",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "We understood:",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        val code = if (isFixing) selectedReasonCode else (report.interpretation?.reasonCode ?: "TRUCK_BREAKDOWN")
                        val eta = if (isFixing) selectedEtaMinutes else (report.interpretation?.etaMinutes ?: 40)
                        Text(
                            text = "$code · about $eta min late.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Is this right?",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )

                        // Correction Stepper if "Fix it" clicked
                        AnimatedVisibility(visible = isFixing) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text("Correct Reason:", fontSize = 10.sp, color = colors.textTertiary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("TRUCK_BREAKDOWN", "TRAFFIC", "DUTY_LIMIT").forEach { r ->
                                        val isSel = selectedReasonCode == r
                                        Box(
                                            modifier = Modifier
                                                .background(if (isSel) colors.primary else colors.surface, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (isSel) colors.primary else colors.border, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                                .pointerInput(Unit) {
                                                    detectTapGestures { selectedReasonCode = r }
                                                }
                                        ) {
                                            Text(
                                                text = r,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (isSel) Color.White else colors.textPrimary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Correct ETA: $selectedEtaMinutes min", fontSize = 10.sp, color = colors.textTertiary)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { selectedEtaMinutes = maxOf(10, selectedEtaMinutes - 10) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                    ) { Text("-10m") }
                                    OutlinedButton(
                                        onClick = { selectedEtaMinutes += 10 },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                    ) { Text("+10m") }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (!isFixing) isFixing = true
                                else {
                                    // Send corrected confirm request
                                    onConfirmed(report.copy(
                                        interpretation = VoiceInterpretation(
                                            reasonCode = selectedReasonCode,
                                            etaMinutes = selectedEtaMinutes,
                                            confidence = 1.0
                                        )
                                    ))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = IndustrialShape
                        ) {
                            Text(if (!isFixing) "No, fix it" else "Save & Send")
                        }

                        Button(
                            onClick = {
                                onConfirmed(report)
                            },
                            modifier = Modifier.weight(1f),
                            shape = IndustrialShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Yes, send", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Footnote requirement:
                Text(
                    text = "Your words help dispatch re-plan. They do not clear customs or release equipment.",
                    fontSize = 10.sp,
                    color = colors.textTertiary,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

