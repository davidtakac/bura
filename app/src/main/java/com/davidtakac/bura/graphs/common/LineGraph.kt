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

package com.davidtakac.bura.graphs.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.davidtakac.bura.graphs.common.drawing.closePlotFillPath
import com.davidtakac.bura.graphs.common.drawing.drawGutterIcon
import com.davidtakac.bura.graphs.common.drawing.drawLabeledPoint
import com.davidtakac.bura.graphs.common.drawing.drawMeasuredTickLabel
import com.davidtakac.bura.graphs.common.drawing.drawPastOverlayWithPoint
import com.davidtakac.bura.graphs.common.drawing.drawPlotLinePath
import com.davidtakac.bura.graphs.common.drawing.drawTimeAxis
import com.davidtakac.bura.graphs.common.drawing.drawVerticalAxis
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Composable
fun LineGraph(
    plotPoints: List<PlotPoint>,
    plotBrush: Brush,
    gutterIcons: List<ImageBitmap>,
    valueTicks: List<ValueTick>,
    valueFormatter: (Double) -> String,
    now: Instant,
    args: GraphArgs,
    modifier: Modifier = Modifier,
    plotUnderfillBrush: Brush? = null,
) {
    Canvas(modifier) {
        val verticalAxisDrawLater = drawVerticalAxis(
            valueTicks = valueTicks,
            args = args,
            valueFormatter = valueFormatter
        )
        val horizontalAxisDrawLater = drawHorizontalAxisAndPlot(
            plotPoints = plotPoints,
            plotBrush = plotBrush,
            plotUnderfillBrush = plotUnderfillBrush,
            gutterIcons = gutterIcons,
            valueFormatter = valueFormatter,
            min = valueTicks.minOf { it.value },
            max = valueTicks.maxOf { it.value },
            now = now,
            args = args
        )
        verticalAxisDrawLater.measuredTickLabels.forEach {
            drawMeasuredTickLabel(args, it)
        }
        horizontalAxisDrawLater.nowCenter?.let {
            drawPastOverlayWithPoint(it, args)
        }
    }
}

private fun DrawScope.drawHorizontalAxisAndPlot(
    plotPoints: List<PlotPoint>,
    plotBrush: Brush,
    plotUnderfillBrush: Brush?,
    gutterIcons: List<ImageBitmap>,
    valueFormatter: (Double) -> String,
    min: Double,
    max: Double,
    now: Instant,
    args: GraphArgs
): HorizontalAxisDrawLater {
    val range = max - min
    val plotPath = Path()
    val plotFillPath = Path()
    fun movePlot(x: Float, y: Float) {
        with(plotPath) { if (isEmpty) moveTo(x, y) else lineTo(x, y) }
        with(plotFillPath) { if (isEmpty) moveTo(x, y) else lineTo(x, y) }
    }

    var minCenter: Pair<Offset, Double>? = null
    var maxCenter: Pair<Offset, Double>? = null
    var nowCenter: Offset? = null
    var lastX = 0f

    drawTimeAxis(
        times = plotPoints.map { it.time },
        args = args
    ) { i, x, calcY ->
        // Temperature line
        val point = plotPoints.getOrNull(i) ?: return@drawTimeAxis
        val y = calcY((point.value - min) / range).top
        movePlot(x, y)
        lastX = x

        // Min, max and now indicators are drawn after the plot so they're on top of it
        if (point.value < (minCenter?.second ?: Double.MAX_VALUE)) minCenter = Offset(x, y) to point.value
        if (point.value > (maxCenter?.second ?: Double.MIN_VALUE)) maxCenter = Offset(x, y) to point.value
        if (point.time.toInstant() == now.truncatedTo(ChronoUnit.HOURS)) nowCenter = Offset(x, y)

        // Condition icons
        drawGutterIcon(i, x, gutterIcons[i], args)
    }

    drawPlotLinePath(lastX, args) {
        drawPath(
            plotPath,
            brush = plotBrush,
            style = Stroke(
                width = args.plotWidth,
                join = StrokeJoin.Round,
                cap = StrokeCap.Square
            )
        )
    }

    plotUnderfillBrush?.let {
        closePlotFillPath(plotFillPath, lastX, args)
        drawPath(
            plotFillPath,
            brush = it
        )
    }

    minCenter?.let { (offset, value) ->
        drawLabeledPoint(
            label = valueFormatter(value),
            center = offset,
            args = args,
        )
    }
    maxCenter?.let { (offset, value) ->
        drawLabeledPoint(
            label = valueFormatter(value),
            center = offset,
            args = args,
        )
    }

    return HorizontalAxisDrawLater(nowCenter)
}

data class PlotPoint(
    val value: Double,
    val time: ZonedDateTime,
)

data class ValueTick(
    val value: Double,
    val label: String? = null,
)