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
 * Узор в духе печатной платы — для карты лояльности.
 */
fun Modifier.circuitBackground(
    lineColor: Color,
    cellSize: Dp = 26.dp,
    alpha: Float = 0.35f,
    seed: Int = 7
): Modifier = this.drawWithCache {
    val cellPx = cellSize.toPx()
    val cols = (size.width / cellPx).toInt() + 1
    val rows = (size.height / cellPx).toInt() + 1
    val random = Random(seed)

    val present = Array(rows + 1) { BooleanArray(cols + 1) { random.nextFloat() > 0.4f } }
    val nodes = mutableListOf<CircuitNode>()
    val lines = mutableListOf<CircuitLine>()

    for (r in 0..rows) {
        for (c in 0..cols) {
            if (!present[r][c]) continue
            val x = c * cellPx
            val y = r * cellPx
            nodes += CircuitNode(x, y, random.nextBoolean())

            if (c < cols && present[r][c + 1] && random.nextFloat() > 0.5f) {
                lines += CircuitLine(Offset(x, y), Offset(x + cellPx, y))
            }
            if (r < rows && present[r + 1][c] && random.nextFloat() > 0.5f) {
                lines += CircuitLine(Offset(x, y), Offset(x, y + cellPx))
            }
        }
    }

    val strokeColor = lineColor.copy(alpha = alpha)
    val nodeRadiusPx = 2.5.dp.toPx()
    val strokeWidthPx = 1.4.dp.toPx()

    onDrawBehind {
        lines.forEach { line ->
            drawLine(strokeColor, line.start, line.end, strokeWidth = strokeWidthPx)
        }
        nodes.forEach { node ->
            if (node.isSquare) {
                drawRect(
                    color = strokeColor,
                    topLeft = Offset(node.x - nodeRadiusPx, node.y - nodeRadiusPx),
                    size = Size(nodeRadiusPx * 2, nodeRadiusPx * 2),
                    style = Stroke(width = strokeWidthPx)
                )
            } else {
                drawCircle(strokeColor, radius = nodeRadiusPx, center = Offset(node.x, node.y))
            }
        }
    }
}

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