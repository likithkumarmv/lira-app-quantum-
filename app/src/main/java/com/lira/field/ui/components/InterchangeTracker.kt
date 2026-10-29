package com.lira.field.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import com.lira.field.core.model.Interchange
import com.lira.field.core.model.InterchangeMapping
import com.lira.field.core.model.InterchangeStateV13
import com.lira.field.core.model.InterchangeSummaryV13
import com.lira.field.ui.theme.IndustrialShape
import com.lira.field.ui.theme.LiraTheme

@Composable
fun InterchangeTracker(
    summary: InterchangeSummaryV13?,
    interchange: Interchange? = null,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors
    val currentState = interchange?.state ?: summary?.state ?: InterchangeStateV13.NOT_STARTED
    val reasonCode = interchange?.reasonCode
    val txnId = interchange?.txnId ?: summary?.txnId
    val eirNo = interchange?.eirNo ?: summary?.eirNo
    val challengeCode = interchange?.challengeCode

    val stepState = InterchangeMapping.resolve(
        state = currentState,
        reasonCode = reasonCode,
        txnId = txnId,
        eirNo = eirNo,
        challengeCode = challengeCode
    )

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    androidx.compose.runtime.LaunchedEffect(stepState.isCheckedOut) {
        if (stepState.isCheckedOut) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when {
                    stepState.isCheckedOut -> colors.success
                    stepState.isHold -> colors.error
                    stepState.isMatchPreview -> colors.warning
                    else -> colors.border
                }
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        LiraIcons.SwapHoriz,
                        contentDescription = null,
                        tint = when {
                            stepState.isCheckedOut -> colors.success
                            stepState.isHold -> colors.error
                            stepState.isMatchPreview -> colors.warning
                            else -> colors.primary
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BAY INTERCHANGE TRACKER",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        letterSpacing = 1.sp
                    )
                }

                // Epistemic honesty chip
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                stepState.isCheckedOut -> colors.success.copy(alpha = 0.15f)
                                stepState.isHold -> colors.error.copy(alpha = 0.15f)
                                stepState.isMatchPreview -> colors.warning.copy(alpha = 0.15f)
                                else -> colors.surfaceElevated
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            stepState.isCheckedOut -> "COMMITTED"
                            stepState.isHold -> "HOLD"
                            stepState.isMatchPreview -> "MATCH_PREVIEW"
                            else -> currentState.name
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            stepState.isCheckedOut -> colors.success
                            stepState.isHold -> colors.error
                            stepState.isMatchPreview -> colors.warning
                            else -> colors.textSecondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5-Step Read-Only Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InterchangeMapping.STEP_LABELS.forEachIndexed { index, label ->
                    val isCurrent = index == stepState.currentStepIndex
                    val isCompleted = index < stepState.currentStepIndex || (index == 4 && stepState.isCheckedOut)
                    val stepColor = when {
                        isCompleted && stepState.isCheckedOut -> colors.success
                        isCompleted -> colors.primary
                        isCurrent && stepState.isHold -> colors.error
                        isCurrent && stepState.isMatchPreview -> colors.warning
                        isCurrent -> colors.primary
                        else -> colors.textTertiary.copy(alpha = 0.4f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        val scale by animateFloatAsState(
                            targetValue = if (isCurrent) 1.15f else 1.0f,
                            animationSpec = tween(durationMillis = 300),
                            label = "step_scale"
                        )
                        val alpha by animateFloatAsState(
                            targetValue = if (isCompleted || isCurrent) 1.0f else 0.45f,
                            animationSpec = tween(durationMillis = 300),
                            label = "step_alpha"
                        )

                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .alpha(alpha)
                                .size(20.dp)
                                .background(
                                    color = if (isCompleted || isCurrent) stepColor else Color.Transparent,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .border(1.5.dp, stepColor, androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Text(
                                    text = "✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.White else colors.textTertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 8.5.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) colors.textPrimary else colors.textTertiary,
                            maxLines = 2,
                            lineHeight = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Headline & Description Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = when {
                            stepState.isCheckedOut -> colors.success.copy(alpha = 0.1f)
                            stepState.isHold -> colors.error.copy(alpha = 0.1f)
                            stepState.isMatchPreview -> colors.warning.copy(alpha = 0.1f)
                            else -> colors.surfaceElevated
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        when {
                            stepState.isCheckedOut -> colors.success.copy(alpha = 0.3f)
                            stepState.isHold -> colors.error.copy(alpha = 0.3f)
                            stepState.isMatchPreview -> colors.warning.copy(alpha = 0.3f)
                            else -> colors.border
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = stepState.statusHeadline,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = when {
                            stepState.isCheckedOut -> colors.success
                            stepState.isHold -> colors.error
                            stepState.isMatchPreview -> colors.warning
                            else -> colors.textPrimary
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stepState.statusDescription,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )

                    if (stepState.formattedEir != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stepState.formattedEir,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.success
                        )
                    }
                }
            }
        }
    }
}
