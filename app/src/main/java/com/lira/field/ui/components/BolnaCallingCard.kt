package com.lira.field.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.core.model.BolnaCallStatus
import com.lira.field.core.model.BolnaCallVerification
import com.lira.field.ui.theme.IndustrialShape
import com.lira.field.ui.theme.LiraTheme
import com.lira.field.ui.theme.TelemetrySmall
import com.lira.field.ui.theme.TrackedHeaderLabel

@Composable
fun BolnaCallingCard(
    verification: BolnaCallVerification,
    onAnswerHereInstead: (() -> Unit)? = null,
    onAnswerYes: (() -> Unit)? = null,
    onAnswerNo: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors
    var showInlineAnswerSheet by remember { mutableStateOf(false) }

    val isCompleted = verification.status == BolnaCallStatus.COMPLETED
    val isQueuedOrRinging = verification.status == BolnaCallStatus.SCHEDULED ||
            verification.status == "QUEUED" ||
            verification.status == BolnaCallStatus.DIALING ||
            verification.status == BolnaCallStatus.RINGING
    val isFailedOrNoAnswer = verification.status == BolnaCallStatus.NO_ANSWER ||
            verification.status == BolnaCallStatus.FAILED ||
            verification.status == "BUSY"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isQueuedOrRinging) colors.primary else colors.border)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: AI Telephony badge & Call Execution ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = LiraIcons.BotAgent,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VERIFICATION REQUESTED BY LIRA",
                        style = TrackedHeaderLabel,
                        color = colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (verification.callExecutionId.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(colors.primaryContainer, IndustrialShape)
                            .border(1.dp, colors.primary, IndustrialShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = verification.callExecutionId,
                            color = colors.primary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline & Intent
            Text(
                text = verification.intentHeadline,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Target: ${verification.verificationTarget}",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Telephony Status Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceElevated, IndustrialShape)
                    .border(1.dp, colors.border, IndustrialShape)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (verification.callerMask.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Caller Line:", color = colors.textSecondary, fontSize = 10.sp)
                            Text(
                                text = verification.callerMask,
                                color = colors.textPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Call Status:", color = colors.textSecondary, fontSize = 10.sp)
                        Text(
                            text = BolnaCallStatus.formatStatus(verification.status),
                            color = if (isCompleted) colors.success else if (isFailedOrNoAnswer) colors.error else colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (verification.deadline.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Response Deadline:", color = colors.textSecondary, fontSize = 10.sp)
                            Text(
                                text = verification.deadline.take(16).replace("T", " "),
                                color = colors.warning,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!verification.transcriptSnippet.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Excerpt: ${verification.transcriptSnippet}",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Informational Notice per Section 1 (No fake phone answering inside app)
            if (isQueuedOrRinging) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primaryContainer.copy(alpha = 0.5f), IndustrialShape)
                        .padding(8.dp)
                ) {
                    val maskText = if (verification.callerMask.isNotEmpty()) verification.callerMask else "LIRA Dispatch"
                    Text(
                        text = "You will get a call from LIRA in Hindi in the next few minutes, from $maskText.",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else if (isFailedOrNoAnswer) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.errorSurface, IndustrialShape)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Call unreachable or missed. Please answer directly below.",
                        color = colors.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Epistemic Disclaimer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = LiraIcons.ShieldCheck,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DRIVER-REPORTED (T4) • Voice check is not statutory custody proof",
                    color = colors.textTertiary,
                    style = TelemetrySmall
                )
            }

            // "Can't talk? Answer here instead" button
            if (isQueuedOrRinging || isFailedOrNoAnswer) {
                Spacer(modifier = Modifier.height(8.dp))
                if (!showInlineAnswerSheet) {
                    TextButton(
                        onClick = {
                            showInlineAnswerSheet = true
                            onAnswerHereInstead?.invoke()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Can't talk? Answer here instead",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Inline one-tap YES/NO answering
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated, IndustrialShape)
                            .border(1.dp, colors.border, IndustrialShape)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Quick Response (Driver-Reported T4):",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showInlineAnswerSheet = false
                                    onAnswerYes?.invoke()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = IndustrialShape
                            ) {
                                Text("YES", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    showInlineAnswerSheet = false
                                    onAnswerNo?.invoke()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.error),
                                shape = IndustrialShape
                            ) {
                                Text("NO", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
