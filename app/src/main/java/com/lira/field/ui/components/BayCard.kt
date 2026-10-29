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
import com.lira.field.core.model.BayBoard
import com.lira.field.core.model.BayBoardRow
import com.lira.field.core.model.BayV13
import com.lira.field.ui.theme.IndustrialShape
import com.lira.field.ui.theme.LiraTheme

@Composable
fun BayCard(
    bay: BayV13?,
    bayBoard: BayBoard?,
    currentTruckId: String,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors
    val siteId = bay?.siteId ?: bayBoard?.siteId ?: "JNPT-BAY-04"
    val queuePos = bay?.queuePosition ?: 1
    val chassis = bay?.chassisId ?: "R-03"
    val windowStart = bay?.windowStart?.takeLast(14)?.take(5) ?: "14:30"
    val windowEnd = bay?.windowEnd?.takeLast(14)?.take(5) ?: "15:00"
    val isOnline = bay?.stationOnline ?: true

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = IndustrialShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title and Station Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        LiraIcons.LocationPin,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRE-GATE BAY BOARD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (isOnline) colors.success else colors.textTertiary,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOnline) "Station online ●" else "Station offline",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isOnline) colors.success else colors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Driver Position Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "You are #$queuePos at $siteId · window $windowStart–$windowEnd · chassis $chassis",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            // Mini Queue (3 rows matching Station TFT)
            val rows = bayBoard?.rows?.take(3) ?: listOf(
                BayBoardRow(seq = 1, truckId = "T09", containerNo = "MSCU6619200", chassisId = "R-03", windowStart = "14:30", windowEnd = "15:00"),
                BayBoardRow(seq = 2, truckId = "T11", containerNo = "MAEU8819208", chassisId = "R-02", windowStart = "15:00", windowEnd = "15:30"),
                BayBoardRow(seq = 3, truckId = "T04", containerNo = "CMAU7719201", chassisId = "R-07", windowStart = "15:30", windowEnd = "16:00")
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "BAY MINI-QUEUE (STATION TFT SYNC)",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = colors.textSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rows.forEach { row ->
                    val isMyTruck = row.truckId.equals(currentTruckId, ignoreCase = true) || (currentTruckId.isBlank() && row.seq == 1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isMyTruck) colors.primary.copy(alpha = 0.15f) else colors.surfaceElevated,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .border(
                                width = if (isMyTruck) 1.dp else 0.5.dp,
                                color = if (isMyTruck) colors.primary else colors.border,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${row.seq}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isMyTruck) colors.primary else colors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = row.truckId,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isMyTruck) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isMyTruck) colors.primary else colors.textPrimary
                            )
                            if (isMyTruck) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(YOU)",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }

                        Text(
                            text = "${row.chassisId} · ${row.windowStart}–${row.windowEnd}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

