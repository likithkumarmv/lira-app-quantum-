package com.lira.field.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lira.field.core.model.AssignmentV13
import com.lira.field.core.model.WhyOrigin
import com.lira.field.core.util.ContainerNumberUtils
import com.lira.field.core.voice.LiraTtsHelper
import com.lira.field.ui.theme.LiraTheme

@Composable
fun TakeoverDialog(
    assignment: AssignmentV13,
    language: String,
    ttsHelper: LiraTtsHelper?,
    whySheet: com.lira.field.core.model.WhySheet? = null,
    onAck: () -> Unit,
    onCannotComply: () -> Unit,
    onDismiss: () -> Unit,
    onTakeoverShown: ((assignmentId: String, planVersion: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = LiraTheme.colors
    var voiceMissingHint by remember { mutableStateOf(false) }

    // Trigger vibration and TTS on initial appearance
    LaunchedEffect(assignment.assignmentId, assignment.planVersion) {
        onTakeoverShown?.invoke(assignment.assignmentId, assignment.planVersion)

        // Trigger urgent vibration
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
            }
        } catch (_: Exception) {}

        // Speak aloud
        val instructionToSpeak = assignment.instruction?.text ?: ""
        if (instructionToSpeak.isNotBlank()) {
            ttsHelper?.speak(
                text = instructionToSpeak,
                lang = language,
                onVoiceMissing = { voiceMissingHint = true }
            )
        }
    }

    val headerText = when (language.lowercase()) {
        "hi" -> "नया काम सौंपा गया"
        "mr" -> "नवीन काम सोपवले"
        "ta" -> "புதிய பணி ஒதுக்கப்பட்டது"
        "kn" -> "ಹೊಸ ಕೆಲಸ ನಿಗದಿಪಡಿಸಲಾಗಿದೆ"
        "te" -> "కొత్త పని కేಟాయించబడింది"
        else -> "NEW JOB ASSIGNED"
    }

    val originChipLabel = when (assignment.origin) {
        WhyOrigin.LIVE_CALL -> "LIVE CALL"
        WhyOrigin.STATION_BRANCH -> "STATION PLAN B"
        WhyOrigin.COPILOT -> "COPILOT"
        WhyOrigin.STRESS_PROMOTED -> "STRESS PROMOTED"
        else -> "DISPATCHER"
    }

    val originChipColor = when (assignment.origin) {
        WhyOrigin.LIVE_CALL -> Color(0xFF10B981) // Green
        WhyOrigin.STATION_BRANCH -> Color(0xFFF59E0B) // Amber
        WhyOrigin.COPILOT -> Color(0xFF8B5CF6) // Purple
        else -> Color(0xFF0284C7) // Blue
    }

    val formattedContainer = ContainerNumberUtils.format(assignment.container.containerNo)
    val bayName = assignment.bay?.siteId ?: "—"
    val windowStart = assignment.bay?.windowStart?.takeLast(14)?.take(5) ?: "—"
    val chassisId = assignment.bay?.chassisId ?: "—"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1420))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111F2E), RoundedCornerShape(12.dp))
                    .border(2.dp, colors.primary, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header + Origin Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(originChipColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, originChipColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = originChipLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = originChipColor
                        )
                    }

                    // Speaker Replay Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.surfaceElevated, CircleShape)
                            .clickable {
                                val text = assignment.instruction?.text ?: "Reach Bay 4 by 14:30"
                                ttsHelper?.speak(text, language, onVoiceMissing = { voiceMissingHint = true })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LiraIcons.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = headerText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                if (voiceMissingHint) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Native voice missing · spoke in English",
                        fontSize = 11.sp,
                        color = colors.warning,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Why you?" line
                val whyYouText = com.lira.field.core.model.QpiAiDecisionHelper.formatWhyYou(whySheet?.decision)
                Text(
                    text = "Why you? $whyYouText",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Small provenance chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(colors.surfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = com.lira.field.core.model.QpiAiDecisionHelper.formatProvenanceChip(whySheet?.decision),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textSecondary
                    )
                    if (com.lira.field.core.model.QpiAiDecisionHelper.isSimulated(whySheet?.decision)) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF97316).copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                .border(0.5.dp, Color(0xFFF97316), RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
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

                Spacer(modifier = Modifier.height(18.dp))

                // Detail Matrix
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TRUCK / CHASSIS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = colors.textSecondary)
                            Text("${assignment.truckId} · $chassisId", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = colors.textPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CONTAINER", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = colors.textSecondary)
                            Text(formattedContainer, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = colors.primary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SITE / WINDOW", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = colors.textSecondary)
                            Text("$bayName · $windowStart", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PLAN STATUS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = colors.textSecondary)
                            Text(assignment.status.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = onAck,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text(
                        text = "ACK: I'M GOING",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onCannotComply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.error)
                    )
                ) {
                    Text(
                        text = "CAN'T DO IT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

