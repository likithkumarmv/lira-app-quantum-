package com.lira.field.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.lira.field.ui.theme.*

@Composable
fun LiraBrandLogo(
    modifier: Modifier = Modifier,
    size: Int = 84
) {
    val colors = LiraTheme.colors

    val gradientBrush = Brush.linearGradient(
        colors = listOf(colors.primary, Color(0xFF2563EB)),
        start = Offset(0f, 0f),
        end = Offset(size * 3f, size * 3f)
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(2.dp, gradientBrush, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((size * 0.65f).dp)) {
            val w = this.size.width
            val h = this.size.height

            // Isometric 3D Container Emblem
            // Top Face
            val topPath = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                lineTo(w * 0.88f, h * 0.32f)
                lineTo(w * 0.5f, h * 0.52f)
                lineTo(w * 0.12f, h * 0.32f)
                close()
            }
            drawPath(
                path = topPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF38BDF8))
                )
            )

            // Left Face
            val leftPath = Path().apply {
                moveTo(w * 0.12f, h * 0.32f)
                lineTo(w * 0.5f, h * 0.52f)
                lineTo(w * 0.5f, h * 0.88f)
                lineTo(w * 0.12f, h * 0.68f)
                close()
            }
            drawPath(
                path = leftPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                )
            )

            // Right Face
            val rightPath = Path().apply {
                moveTo(w * 0.5f, h * 0.52f)
                lineTo(w * 0.88f, h * 0.32f)
                lineTo(w * 0.88f, h * 0.68f)
                lineTo(w * 0.5f, h * 0.88f)
                close()
            }
            drawPath(
                path = rightPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                )
            )

            // Center Convergence Quantum Point
            drawCircle(
                color = Color.White,
                radius = w * 0.05f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
        }
    }
}
