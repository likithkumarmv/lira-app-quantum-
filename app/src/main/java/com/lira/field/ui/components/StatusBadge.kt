package com.lira.field.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.core.model.AssignmentStatus
import com.lira.field.core.model.DriverAckState
import com.lira.field.core.model.PhysicalState
import com.lira.field.core.model.TrustState
import com.lira.field.ui.theme.*

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(IndustrialShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, IndustrialShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun AssignmentStatusBadge(status: String, modifier: Modifier = Modifier) {
    val colors = LiraTheme.colors
    when (status) {
        AssignmentStatus.APPROVED -> StatusBadge(
            text = "APPROVED",
            backgroundColor = colors.primaryContainer,
            borderColor = colors.primary,
            textColor = colors.primary,
            modifier = modifier
        )
        AssignmentStatus.ACKNOWLEDGED -> StatusBadge(
            text = "ACKNOWLEDGED",
            backgroundColor = colors.successSurface,
            borderColor = colors.successBorder,
            textColor = colors.success,
            modifier = modifier
        )
        AssignmentStatus.REASSIGNED -> StatusBadge(
            text = "REASSIGNED",
            backgroundColor = colors.warningSurface,
            borderColor = colors.warningBorder,
            textColor = colors.warning,
            modifier = modifier
        )
        AssignmentStatus.PHYSICAL_NO_SHOW -> StatusBadge(
            text = "PHYSICAL NO-SHOW",
            backgroundColor = colors.errorSurface,
            borderColor = colors.errorBorder,
            textColor = colors.error,
            modifier = modifier
        )
        AssignmentStatus.STAGING_EVIDENCED -> StatusBadge(
            text = "STAGING EVIDENCED",
            backgroundColor = colors.successSurface,
            borderColor = colors.successBorder,
            textColor = colors.success,
            modifier = modifier
        )
        else -> StatusBadge(
            text = status,
            backgroundColor = colors.surfaceElevated,
            borderColor = colors.border,
            textColor = colors.textSecondary,
            modifier = modifier
        )
    }
}

@Composable
fun AckStateBadge(ackState: String, modifier: Modifier = Modifier) {
    val colors = LiraTheme.colors
    when (ackState) {
        DriverAckState.PENDING -> StatusBadge(
            text = "ACK: PENDING",
            backgroundColor = colors.warningSurface,
            borderColor = colors.warningBorder,
            textColor = colors.warning,
            modifier = modifier
        )
        DriverAckState.ACKNOWLEDGED -> StatusBadge(
            text = "ACKNOWLEDGED",
            backgroundColor = colors.successSurface,
            borderColor = colors.successBorder,
            textColor = colors.success,
            modifier = modifier
        )
        DriverAckState.CANNOT_COMPLY -> StatusBadge(
            text = "CANNOT COMPLY",
            backgroundColor = colors.errorSurface,
            borderColor = colors.errorBorder,
            textColor = colors.error,
            modifier = modifier
        )
        else -> StatusBadge(
            text = ackState,
            backgroundColor = colors.surfaceElevated,
            borderColor = colors.border,
            textColor = colors.textSecondary,
            modifier = modifier
        )
    }
}

@Composable
fun PhysicalStateBadge(state: String, modifier: Modifier = Modifier) {
    val colors = LiraTheme.colors
    when (state) {
        PhysicalState.EXPECTED -> StatusBadge(
            text = "PHYSICAL: EXPECTED",
            backgroundColor = colors.primaryContainer,
            borderColor = colors.primary,
            textColor = colors.primary,
            modifier = modifier
        )
        PhysicalState.STAGING_EVIDENCED -> StatusBadge(
            text = "STAGING EVIDENCED",
            backgroundColor = colors.successSurface,
            borderColor = colors.successBorder,
            textColor = colors.success,
            modifier = modifier
        )
        PhysicalState.RFID_MISMATCH, PhysicalState.BAY_ABSENT_WARNING -> StatusBadge(
            text = "CHECKPOINT WARNING",
            backgroundColor = colors.warningSurface,
            borderColor = colors.warningBorder,
            textColor = colors.warning,
            modifier = modifier
        )
        PhysicalState.PHYSICAL_NO_SHOW -> StatusBadge(
            text = "CHECK FAILED / NO-SHOW",
            backgroundColor = colors.errorSurface,
            borderColor = colors.errorBorder,
            textColor = colors.error,
            modifier = modifier
        )
        else -> StatusBadge(
            text = state,
            backgroundColor = colors.surfaceElevated,
            borderColor = colors.border,
            textColor = colors.textSecondary,
            modifier = modifier
        )
    }
}

@Composable
fun TrustStateBadge(trustState: String, modifier: Modifier = Modifier) {
    val colors = LiraTheme.colors
    when (trustState) {
        TrustState.FORECAST_RISK -> {
            Row(
                modifier = modifier
                    .clip(IndustrialShape)
                    .background(colors.warningSurface)
                    .border(1.dp, colors.warningBorder, IndustrialShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(IndustrialShape)
                        .background(colors.warning)
                )
                Text(
                    text = " FORECAST RISK (POTENTIAL)",
                    color = colors.warning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
        TrustState.CONFIRMED_DISRUPTION -> {
            Row(
                modifier = modifier
                    .clip(IndustrialShape)
                    .background(colors.errorSurface)
                    .border(1.dp, colors.errorBorder, IndustrialShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(IndustrialShape)
                        .background(colors.error)
                )
                Text(
                    text = " CONFIRMED DISRUPTION",
                    color = colors.error,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
        else -> StatusBadge(
            text = trustState,
            backgroundColor = colors.surfaceElevated,
            borderColor = colors.border,
            textColor = colors.textSecondary,
            modifier = modifier
        )
    }
}
