package com.methane.eco.trans.presentation.mainscreen

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private data class CircuitNode(val x: Float, val y: Float, val isSquare: Boolean)
private data class CircuitLine(val start: Offset, val end: Offset)

/**
 * Узор из силуэтов листьев — для эко-блока.
 */
fun Modifier.leafPatternBackground(
    leafColor: Color,
    spacing: Dp = 42.dp,
    alpha: Float = 0.22f
): Modifier = this.drawWithCache {
    val spacingPx = spacing.toPx()
    val cols = (size.width / spacingPx).toInt() + 2
    val rows = (size.height / spacingPx).toInt() + 2
    val leafW = spacingPx * 0.5f
    val leafH = spacingPx * 0.8f
    val color = leafColor.copy(alpha = alpha)
    val strokeWidthPx = 1.2.dp.toPx()

    onDrawBehind {
        for (r in 0 until rows) {
            val rowOffset = if (r % 2 == 0) 0f else spacingPx / 2f
            for (c in 0 until cols) {
                val cx = c * spacingPx + rowOffset
                val cy = r * spacingPx
                val angle = if ((r + c) % 2 == 0) 25f else -25f

                rotate(degrees = angle, pivot = Offset(cx, cy)) {
                    val path = Path().apply {
                        moveTo(cx, cy - leafH / 2)
                        quadraticTo(cx + leafW / 2, cy, cx, cy + leafH / 2)
                        quadraticTo(cx - leafW / 2, cy, cx, cy - leafH / 2)
                        close()
                    }
                    drawPath(path, color = color, style = Stroke(width = strokeWidthPx))
                    // прожилка листа
                    drawLine(
                        color = color,
                        start = Offset(cx, cy - leafH / 2),
                        end = Offset(cx, cy + leafH / 2),
                        strokeWidth = strokeWidthPx
                    )
                }
            }
        }
    }
}