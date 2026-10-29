package com.lira.field.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.lira.field.core.model.CallStatusV13
import com.lira.field.core.model.CallV13
import com.lira.field.core.model.CallVerificationV13
import com.lira.field.ui.theme.IndustrialShape
import com.lira.field.ui.theme.LiraTheme

@Composable
fun CallV13Card(
    call: CallV13,
    onAnswerHereInstead: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors
    val isFailedOrNoAnswer = call.status == CallStatusV13.NO_ANSWER ||
            call.status == CallStatusV13.BUSY ||
            call.status == CallStatusV13.FAILED

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Purpose, Language Chip, Status Chip
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
                        LiraIcons.PhoneCallback,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = call.purpose ?: "VOICE VERIFICATION CALL",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Chip
                    call.language?.let { lang ->
                        Box(
                            modifier = Modifier
                                .background(colors.surfaceElevated, RoundedCornerShape(2.dp))
                                .border(0.5.dp, colors.border, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = lang.uppercase(),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Status Chip
                    val (statusBg, statusFg) = when (call.status) {
                        CallStatusV13.COMPLETED -> colors.success.copy(alpha = 0.15f) to colors.success
                        CallStatusV13.RINGING, CallStatusV13.IN_PROGRESS -> colors.primary.copy(alpha = 0.15f) to colors.primary
                        CallStatusV13.QUEUED, CallStatusV13.PENDING_AUTHORIZATION -> colors.warning.copy(alpha = 0.15f) to colors.warning
                        else -> colors.error.copy(alpha = 0.15f) to colors.error
                    }

                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = call.status.name,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = statusFg
                        )
                    }
                }
            }

            // Monospace Execution ID and Masked Caller
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (!call.executionId.isNullOrBlank()) "id: ${call.executionId}" else "BOLNA VOICE TELEPHONY",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textTertiary
                )
                if (!call.fromNumberMasked.isNullOrBlank()) {
                    Text(
                        text = call.fromNumberMasked,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // What LIRA asked
            if (!call.question.isNullOrBlank()) {
                Text(
                    text = "LIRA ASKED:",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textTertiary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = call.question,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // What you said
            if (!call.transcriptExcerptOriginal.isNullOrBlank()) {
                Text(
                    text = "DRIVER SAID (ORIGINAL):",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textTertiary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\"${call.transcriptExcerptOriginal}\"",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = colors.textSecondary
                )

                if (!call.transcriptExcerptEn.isNullOrBlank() && call.transcriptExcerptEn != call.transcriptExcerptOriginal) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "EN: \"${call.transcriptExcerptEn}\"",
                        fontSize = 11.sp,
                        color = colors.textTertiary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Extracted understanding and Verification Chip
            call.extracted?.let { ext ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UNDERSTOOD:",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textTertiary
                        )
                        val summaryText = buildString {
                            ext.answer?.let { append("Answer: $it") }
                            ext.etaMinutes?.let { append(" · ETA +${it}m") }
                            ext.reasonCode?.let { append(" · $it") }
                        }
                        Text(
                            text = summaryText.ifBlank { "Acknowledged" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }

                    // Verification Chip
                    val (verText, verColor) = when (call.verification) {
                        CallVerificationV13.CORROBORATED -> "CORROBORATED" to colors.success
                        CallVerificationV13.CONTRADICTED -> "CONTRADICTED" to colors.error
                        else -> "T4 · UNVERIFIED" to colors.warning
                    }
                    Box(
                        modifier = Modifier
                            .background(verColor.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                            .border(0.5.dp, verColor, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = verText,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = verColor
                        )
                    }
                }
            }

            // Fallback for Failed / Busy / No-Answer
            if (isFailedOrNoAnswer) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Call unsuccessful (${call.status.name})",
                        fontSize = 11.sp,
                        color = colors.error
                    )
                    TextButton(
                        onClick = { onAnswerHereInstead?.invoke() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Answer here instead",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                }
            }
        }
    }
}

