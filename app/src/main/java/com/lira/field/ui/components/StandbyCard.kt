package com.lira.field.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.lira.field.core.model.AssignmentV13
import com.lira.field.ui.theme.LiraTheme

@Composable
fun StandbyCard(
    assignment: AssignmentV13,
    onGotIt: () -> Unit,
    onNotAvailable: () -> Unit,
    onWhyStandby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiraTheme.colors
    val bay = assignment.bay
    val siteName = bay?.siteId ?: "JNPT Bay 4"
    val windowStart = bay?.windowStart?.takeLast(14)?.take(5) ?: "08:00"
    val windowEnd = bay?.windowEnd?.takeLast(14)?.take(5) ?: "10:00"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.border)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STANDBY SCHEDULED",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF6366F1).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "RESERVE: NOT A DISPATCH",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF818CF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Standby tomorrow $windowStart–$windowEnd at $siteName",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Reserved by quantum risk forecast as corridor buffer. You are on standby; do not proceed to port gate unless activated.",
                fontSize = 12.sp,
                color = colors.textSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Why am I on standby? ›",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onWhyStandby() }
                        .padding(vertical = 4.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onNotAvailable,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Not available",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.error
                        )
                    }

                    Button(
                        onClick = onGotIt,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Got it",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

