/*
 * Copyright 2024 David Takač
 *
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.graphs.uvindex

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.graphs.common.drawLabeledPoint
import com.davidtakac.bura.graphs.common.drawPastOverlayWithPoint
import com.davidtakac.bura.graphs.common.drawTimeAxis
import com.davidtakac.bura.graphs.common.drawVerticalAxis
import com.davidtakac.bura.uvindex.UvIndex
import com.davidtakac.bura.uvindex.valueString
import java.time.LocalDateTime
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun UvIndexGraph(
    state: UvIndexGraph,
    args: GraphArgs,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val colors = AppTheme.colors.uvIndexColors(toUvIndex = state.max.value)
    Canvas(modifier) {
        drawVerticalAxis(
            measurer = measurer,
            args = args,
        )
        drawHorizontalAxisAndPlot(
            state = state,
            colors = colors,
            measurer = measurer,
            args = args,
        )
    }
}

private fun DrawScope.drawHorizontalAxisAndPlot(
    state: UvIndexGraph,
    colors: List<Color>,
    measurer: TextMeasurer,
    args: GraphArgs,
) {
    val range = 11f
    val plotPath = Path()
    val plotFillPath = Path()
    fun movePlot(x: Float, y: Float) {
        with(plotPath) { if (isEmpty) moveTo(x, y) else lineTo(x, y) }
        with(plotFillPath) { if (isEmpty) moveTo(x, y) else lineTo(x, y) }
    }

    var nowCenter: Offset? = null
    var maxCenter: Pair<Offset, UvIndex>? = null
    var lastX = 0f

    drawTimeAxis(
        measurer = measurer,
        args = args
    ) { i, x ->
        // Plot line
        val point = state.points.getOrNull(i) ?: return@drawTimeAxis
        val index = point.uvIndex.value.value
        val minY = args.topGutter + args.axisWidth + (args.plotWidth / 2)
        val maxY = size.height - args.bottomGutter - args.axisWidth - (args.plotWidth / 2)
        val y = (((1 - (index / range)) * (size.height - args.bottomGutter - args.topGutter)) + args.topGutter).coerceIn(minY, maxY)
        movePlot(x, y)
        lastX = x

        // Max and now indicator are drawn after the plot so it's on top of it
        if (point.uvIndex.meta == GraphUvIndex.Meta.Maximum) maxCenter = Offset(x, y) to point.uvIndex.value
        if (point.time.meta == GraphTime.Meta.Present) nowCenter = Offset(x, y)
    }

    // Draw the plot line and fill under it
    val plotBottom = size.height - args.bottomGutter
    plotFillPath.lineTo(x = lastX, y = plotBottom)
    plotFillPath.lineTo(x = args.startGutter, y = plotBottom)
    plotFillPath.close()
    val gradientStart = size.height - args.bottomGutter
    val gradientEnd = args.topGutter
    // Clip path makes sure the plot ends are within graph bounds
    clipPath(
        path = Path().apply {
            addRect(
                Rect(
                    offset = Offset(x = args.startGutter, y = args.topGutter),
                    size = Size(
                        width = size.width - args.startGutter - args.endGutter,
                        height = size.height - args.topGutter - args.bottomGutter
                    )
                )
            )
        }
    ) {
        drawPath(
            plotPath,
            brush = Brush.verticalGradient(
                colors = colors,
                startY = gradientStart,
                endY = gradientEnd
            ),
            style = Stroke(
                width = args.plotWidth,
                join = StrokeJoin.Round,
                cap = StrokeCap.Square
            )
        )
    }
    drawPath(
        plotFillPath,
        brush = Brush.verticalGradient(
            colors = colors,
            startY = gradientStart,
            endY = gradientEnd
        ),
        alpha = args.plotFillAlpha
    )
    maxCenter?.let { (offset, index) ->
        drawLabeledPoint(
            label = index.valueString(args.numberFormat),
            center = offset,
            args = args,
            measurer = measurer
        )
    }
    nowCenter?.let {
        drawPastOverlayWithPoint(it, args)
    }
}

private fun DrawScope.drawVerticalAxis(
    measurer: TextMeasurer,
    args: GraphArgs
) {
    val steps = 11
    drawVerticalAxis(
        steps = steps,
        args = args
    ) { frac, endX, y ->
        val index = UvIndex((frac * steps).roundToInt())
        val indexMeasured = measurer.measure(
            text = index.valueString(args.numberFormat),
            style = args.axisTextStyle
        )
        drawText(
            textLayoutResult = indexMeasured,
            color = args.axisColor,
            topLeft = Offset(
                x = endX + args.endAxisTextPaddingStart,
                y = y - (indexMeasured.size.height / 2)
            )
        )
    }
}

@Preview
@Composable
private fun UvIndexGraphPreview() {
    AppTheme(darkTheme = true) {
        UvIndexGraph(
            state = UvIndexGraph(
                max = UvIndex(11),
                points = List(25) {
                    val now = LocalDateTime.parse("2001-01-01T12:00")
                    val hour = LocalDateTime.parse("2001-01-01T00:00").plusHours(it.toLong())
                    val time = GraphTime(hour = hour, now = now)
                    if (it < 8) UvIndexGraphPoint(
                        time = time,
                        uvIndex = GraphUvIndex(
                            value = UvIndex(0),
                            meta = GraphUvIndex.Meta.Regular
                        )
                    ) else if (it < 19) {
                        val index = Random.nextInt(1, 12)
                        UvIndexGraphPoint(
                            time = time,
                            uvIndex = GraphUvIndex(
                                value = UvIndex(index),
                                meta = if (index == 11) GraphUvIndex.Meta.Maximum else GraphUvIndex.Meta.Regular
                            )
                        )
                    } else UvIndexGraphPoint(
                        time = time,
                        uvIndex = GraphUvIndex(
                            value = UvIndex(0),
                            meta = GraphUvIndex.Meta.Regular
                        )
                    )
                }
            ),
            args = GraphArgs.rememberUvIndexArgs(),
            modifier = Modifier
                .size(400.dp)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}