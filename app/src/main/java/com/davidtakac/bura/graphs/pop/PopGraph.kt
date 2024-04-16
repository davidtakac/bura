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

package com.davidtakac.bura.graphs.pop

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.pop.string
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun PopGraph(state: PopGraph, args: GraphArgs, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()
    val plotColor = AppTheme.colors.popColor
    Canvas(modifier) {
        drawVerticalAxis(
            context = context,
            measurer = measurer,
            args = args,
        )
        drawHorizontalAxisAndPlot(
            state = state,
            context = context,
            measurer = measurer,
            plotColor = plotColor,
            args = args,
        )
    }
}

private fun DrawScope.drawHorizontalAxisAndPlot(
    state: PopGraph,
    context: Context,
    measurer: TextMeasurer,
    plotColor: Color,
    args: GraphArgs,
) {
    val range = 100f
    val plotPath = Path()
    val plotFillPath = Path()
    fun movePlot(x: Float, y: Float) {
        with(plotPath) { if (isEmpty) moveTo(x, y) else lineTo(x, y) }
        with(plotFillPath) { if (isEmpty) moveTo(x, y) else lineTo(x, y) }
    }

    var nowCenter: Offset? = null
    var maxCenter: Pair<Offset, Pop>? = null
    var lastX = 0f

    drawTimeAxis(
        measurer = measurer,
        args = args
    ) { i, x ->
        // Plot line
        val point = state.points.getOrNull(i) ?: return@drawTimeAxis
        val pop = point.pop.value
        val minY = args.topGutter + args.axisWidth + (args.plotWidth / 2)
        val maxY = size.height - args.bottomGutter - args.axisWidth - (args.plotWidth / 2)
        val y = (((1 - (pop.value / range)) * (size.height - args.bottomGutter - args.topGutter)) + args.topGutter).toFloat().coerceIn(minY, maxY)
        movePlot(x, y)
        lastX = x

        // Max and now indicator are drawn after the plot so it's on top of it
        if (point.pop.meta == GraphPop.Meta.Maximum) maxCenter = Offset(x, y) to point.pop.value
        if (point.time.meta == GraphTime.Meta.Present) nowCenter = Offset(x, y)
    }

    // Draw the plot line and fill under it
    val plotBottom = size.height - args.bottomGutter
    plotFillPath.lineTo(x = lastX, y = plotBottom)
    plotFillPath.lineTo(x = args.startGutter, y = plotBottom)
    plotFillPath.close()
    drawPath(
        plotPath,
        color = plotColor,
        style = Stroke(
            width = args.plotWidth,
            join = StrokeJoin.Round
        )
    )
    drawPath(
        plotFillPath,
        color = plotColor,
        alpha = args.plotFillAlpha
    )
    maxCenter?.let { (offset, pop) ->
        drawLabeledPoint(
            label = pop.string(context, args.numberFormat),
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
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    drawVerticalAxis(
        steps = 5,
        args = args
    ) { frac, endX, y ->
        val pop = Pop(frac * 100.0)
        val popMeasured = measurer.measure(
            text = pop.string(context, args.numberFormat),
            style = args.axisTextStyle
        )
        drawText(
            textLayoutResult = popMeasured,
            color = args.axisColor,
            topLeft = Offset(
                x = endX + args.endAxisTextPaddingStart,
                y = y - (popMeasured.size.height / 2)
            )
        )
    }
}

@Preview
@Composable
private fun PopGraphPreview() {
    AppTheme(darkTheme = false) {
        PopGraph(
            state = previewState, modifier = Modifier
                .height(300.dp)
                .width(400.dp)
                .background(MaterialTheme.colorScheme.background),
            args = GraphArgs.rememberPopArgs()
        )
    }
}

@Preview
@Composable
private fun PopGraphNowStartPreview() {
    AppTheme {
        PopGraph(
            state = previewState.copy(points = previewState.points.mapIndexed { idx, pt ->
                pt.copy(
                    time = GraphTime(
                        pt.time.value,
                        meta = if (idx == 0) GraphTime.Meta.Present else GraphTime.Meta.Future
                    )
                )
            }),
            args = GraphArgs.rememberPopArgs(),
            modifier = Modifier
                .width(400.dp)
                .height(300.dp)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Preview
@Composable
private fun ConditionGraphNowEndPreview() {
    AppTheme {
        PopGraph(
            state = previewState.copy(points = previewState.points.mapIndexed { idx, pt ->
                pt.copy(
                    time = GraphTime(
                        pt.time.value,
                        meta = if (idx == previewState.points.lastIndex) GraphTime.Meta.Present else GraphTime.Meta.Past
                    )
                )
            }),
            args = GraphArgs.rememberPopArgs(),
            modifier = Modifier
                .width(400.dp)
                .height(300.dp)
                .background(MaterialTheme.colorScheme.background),
        )
    }
}

private val previewState = PopGraph(
    day = LocalDate.parse("1970-01-01"),
    points = listOf(
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("00:00"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("01:00"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("02:00"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("03:00"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("04:00"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("05:00"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("06:00"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(5.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("07:00"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(5.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("08:00"),
                meta = GraphTime.Meta.Present
            ),
            pop = GraphPop(
                Pop(5.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("09:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(10.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("10:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(12.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("11:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(12.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("12:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("13:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("14:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("15:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(50.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("16:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(70.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("17:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(100.0),
                meta = GraphPop.Meta.Maximum
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("18:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(100.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("19:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(100.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("20:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(100.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("21:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(90.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("22:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(90.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("23:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(90.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
        PopGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("00:00"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(90.0),
                meta = GraphPop.Meta.Regular
            ),
        ),
    )
)