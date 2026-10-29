package com.lira.field.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.core.model.FreightCaseType
import com.lira.field.core.model.FreightExceptionCase
import com.lira.field.ui.theme.IndustrialShape
import com.lira.field.ui.theme.LiraTheme
import com.lira.field.ui.theme.TelemetrySmall
import com.lira.field.ui.theme.TrackedHeaderLabel

@Composable
fun FreightExceptionCard(
    freightCase: FreightExceptionCase,
    onViewEvidence: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors
    val isCritical = freightCase.caseCode == FreightCaseType.C1_CUSTOMS_DOC_HOLD ||
            freightCase.caseCode == FreightCaseType.C2_CARRIER_EDO_HOLD

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = if (isCritical) colors.errorSurface else colors.surfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isCritical) colors.errorBorder else colors.border)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = if (isCritical) LiraIcons.AlertCircle else LiraIcons.Info,
                        contentDescription = null,
                        tint = if (isCritical) colors.error else colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = FreightCaseType.getCaseTitle(freightCase.caseCode),
                        style = TrackedHeaderLabel,
                        color = if (isCritical) colors.error else colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            if (isCritical) colors.error else colors.primaryContainer,
                            IndustrialShape
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = freightCase.status,
                        color = if (isCritical) androidx.compose.ui.graphics.Color.White else colors.primary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hold Reason
            Text(
                text = freightCase.holdReason,
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Driver Instructions Callout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, IndustrialShape)
                    .border(1.dp, colors.border, IndustrialShape)
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "REQUIRED DRIVER ACTION:",
                        color = colors.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = freightCase.driverInstruction,
                        color = colors.textPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    if (!freightCase.canDriverClear) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠ Mandatory: Driver cannot clear this hold unilaterally. Clearance authority rests with ${freightCase.owner}.",
                            color = colors.textTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata row: Owner, Deadline, Evidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("RESPONSIBLE OWNER", color = colors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(freightCase.owner, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("CASE DEADLINE", color = colors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = freightCase.deadline.take(16).replace("T", " "),
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${freightCase.evidenceRef}",
                    color = colors.textTertiary,
                    style = TelemetrySmall
                )
                if (onViewEvidence != null) {
                    TextButton(
                        onClick = onViewEvidence,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("WHY THIS ACTION?", color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(LiraIcons.ChevronRight, contentDescription = null, tint = colors.primary, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
