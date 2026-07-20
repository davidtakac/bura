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

package com.davidtakac.bura.graphs.pop.compose

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.davidtakac.bura.theme.AppTheme
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.image
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.graphs.common.drawing.closePlotFillPath
import com.davidtakac.bura.graphs.common.drawing.drawLabeledPoint
import com.davidtakac.bura.graphs.common.drawing.drawPastOverlayWithPoint
import com.davidtakac.bura.graphs.common.drawing.drawPlotLinePath
import com.davidtakac.bura.graphs.common.drawing.drawTimeAxis
import com.davidtakac.bura.graphs.common.drawing.drawVerticalAxis
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.string
import com.davidtakac.bura.graphs.pop.GraphPop
import com.davidtakac.bura.graphs.pop.PopGraph
import com.davidtakac.bura.graphs.pop.PopGraphPoint
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.roundToInt

@Composable
fun PopGraph(state: PopGraph, args: GraphArgs, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()
    val plotColor = AppTheme.colors.popColor
    val steps = listOf(0.0, 20.0, 40.0, 60.0, 80.0, 100.0, 120.0)
    Canvas(modifier) {
        drawVerticalAxis(
            context = context,
            steps = steps,
            measurer = measurer,
            args = args,
        )
        drawHorizontalAxisAndPlot(
            state = state,
            max = steps.last(),
            context = context,
            measurer = measurer,
            plotColor = plotColor,
            args = args,
        )
    }
}

private fun DrawScope.drawHorizontalAxisAndPlot(
    state: PopGraph,
    max: Double,
    context: Context,
    measurer: TextMeasurer,
    plotColor: Color,
    args: GraphArgs,
) {
    val iconSize = 24.dp.toPx()
    val iconSizeRound = iconSize.roundToInt()
    val hasSpaceFor12Icons = (size.width - args.startGutter - args.endGutter) - (iconSizeRound * 12) >= (12 * 2.dp.toPx())
    val iconY = ((args.topGutter / 2) - (iconSize / 2)).roundToInt()

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
    ) { i, x, calcY ->
        // Plot line
        val point = state.points.getOrNull(i) ?: return@drawTimeAxis
        val pop = point.pop.value
        val minY = args.topGutter + args.axisWidth + (args.plotWidth / 2)
        val maxY = size.height - args.bottomGutter - args.axisWidth - (args.plotWidth / 2)
        val y = calcY(pop.value / max).top.coerceIn(minY, maxY)
        movePlot(x, y)
        lastX = x

        // Max and now indicator are drawn after the plot so it's on top of it
        if (point.pop.meta == GraphPop.Meta.Maximum) maxCenter = Offset(x, y) to point.pop.value
        if (point.time.meta == GraphTime.Meta.Present) nowCenter = Offset(x, y)

        // Condition icons
        if (i % (if (hasSpaceFor12Icons) 2 else 3) == 1) {
            val iconX = x - (iconSize / 2)
            val iconDrawable = AppCompatResources.getDrawable(context, point.condition.image(context, args.icons))!!
            drawImage(
                image = iconDrawable.toBitmap(width = iconSizeRound, height = iconSizeRound).asImageBitmap(),
                dstOffset = IntOffset(iconX.roundToInt(), y = iconY),
                dstSize = IntSize(width = iconSizeRound, height = iconSizeRound),
            )
        }
    }

    drawPlotLinePath(lastX, args) {
        drawPath(
            plotPath,
            color = plotColor,
            style = Stroke(
                width = args.plotWidth,
                join = StrokeJoin.Round,
                cap = StrokeCap.Square
            )
        )
    }

    closePlotFillPath(plotFillPath, lastX, args)
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
    steps: List<Double>,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    drawVerticalAxis(
        steps = steps,
        args = args,
        measurer = measurer,
    ) { step ->
        step.takeUnless { it > 100 }
            ?.let { Pop(it).string(context, args.numberFormat) }
            ?: ""
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
private fun PopGraphRtlPreview() {
    AppTheme(darkTheme = false) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            PopGraph(
                state = previewState, modifier = Modifier
                    .height(300.dp)
                    .width(400.dp)
                    .background(MaterialTheme.colorScheme.background),
                args = GraphArgs.rememberPopArgs()
            )
        }
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
private fun PopGraphNowEndPreview() {
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
                value = ZonedDateTime.parse("1970-01-01T00:00Z"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular,
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T01:00Z"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T02:00Z"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T03:00Z"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T04:00Z"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T05:00Z"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T06:00Z"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(5.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T07:00Z"),
                meta = GraphTime.Meta.Past
            ),
            pop = GraphPop(
                Pop(5.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T08:00Z"),
                meta = GraphTime.Meta.Present
            ),
            pop = GraphPop(
                Pop(5.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T09:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(10.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T10:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(12.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T11:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(12.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T12:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T13:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T14:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(0.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T15:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(50.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T16:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(70.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T17:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(100.0),
                meta = GraphPop.Meta.Maximum
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T18:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(100.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T19:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(100.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T20:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(100.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T21:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(90.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T22:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(90.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-01T23:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(90.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
        PopGraphPoint(
            time = GraphTime(
                value = ZonedDateTime.parse("1970-01-02T00:00Z"),
                meta = GraphTime.Meta.Future
            ),
            pop = GraphPop(
                Pop(90.0),
                meta = GraphPop.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false)
        ),
    )
)