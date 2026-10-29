package com.lira.field.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.ui.theme.*

@Composable
fun DemoDataBanner(
    isMockMode: Boolean,
    currentStage: Int,
    onSelectStage: (Int) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isMockMode) return

    val colors = LiraTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(
                    text = "DEMO MOCK DATA",
                    backgroundColor = colors.warningSurface,
                    borderColor = colors.warningBorder,
                    textColor = colors.warning
                )
                Spacer(modifier = Modifier.width(10.dp))
                val stageLabel = when (currentStage) {
                    0 -> "Stage 1: Plan R2 Assigned"
                    1 -> "Stage 2: Driver Ack'd"
                    2 -> "Stage 3: Checkpoint Failed"
                    3 -> "Stage 4: Wave 2 Plan R3"
                    else -> "Demo Active"
                }
                Text(
                    text = stageLabel,
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = LiraIcons.Tune,
                        contentDescription = "Demo Controls",
                        tint = colors.warning,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = LiraIcons.Refresh,
                        contentDescription = "Reset Demo",
                        tint = colors.warning,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = "DEMO SCENARIO STAGES (Simulate Hardware & Recovery):",
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DemoStageChip(
                        title = "1. R2 Plan",
                        selected = currentStage == 0,
                        onClick = { onSelectStage(0) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoStageChip(
                        title = "2. Ack",
                        selected = currentStage == 1,
                        onClick = { onSelectStage(1) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoStageChip(
                        title = "3. No-Show",
                        selected = currentStage == 2,
                        onClick = { onSelectStage(2) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoStageChip(
                        title = "4. R3 Recov",
                        selected = currentStage == 3,
                        onClick = { onSelectStage(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoStageChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors

    Box(
        modifier = modifier
            .clip(IndustrialShape)
            .background(if (selected) colors.primary else colors.surface)
            .border(1.dp, if (selected) colors.primary else colors.border, IndustrialShape)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) colors.onPrimary else colors.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
