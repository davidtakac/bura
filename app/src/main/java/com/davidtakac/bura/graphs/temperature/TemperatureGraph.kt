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

package com.davidtakac.bura.graphs.temperature

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.image
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.graphs.common.drawLabeledPoint
import com.davidtakac.bura.graphs.common.drawPastOverlayWithPoint
import com.davidtakac.bura.graphs.common.drawTimeAxis
import com.davidtakac.bura.graphs.common.drawVerticalAxis
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.string
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

@Composable
fun TemperatureGraph(
    state: TemperatureGraph,
    args: GraphArgs,
    absMinTemp: Temperature,
    absMaxTemp: Temperature,
    modifier: Modifier = Modifier
) {
    val paddingC = 3.0
    val maxCelsius = remember(absMaxTemp) { absMaxTemp.convertTo(Temperature.Unit.DegreesCelsius).value + paddingC }
    val minCelsius = remember(absMinTemp) { absMinTemp.convertTo(Temperature.Unit.DegreesCelsius).value - paddingC }
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()
    val plotColors = AppTheme.colors.temperatureColors(minCelsius, maxCelsius)
    Canvas(modifier) {
        drawTempAxis(
            unit = absMinTemp.unit,
            minTempC = minCelsius,
            maxTempC = maxCelsius,
            context = context,
            measurer = measurer,
            args = args
        )
        drawHorizontalAxisAndPlot(
            state = state,
            minCelsius = minCelsius,
            maxCelsius = maxCelsius,
            context = context,
            measurer = measurer,
            plotColors = plotColors,
            args = args
        )
    }
}

private fun DrawScope.drawHorizontalAxisAndPlot(
    state: TemperatureGraph,
    plotColors: List<Color>,
    minCelsius: Double,
    maxCelsius: Double,
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    val iconSize = 24.dp.toPx()
    val iconSizeRound = iconSize.roundToInt()
    val hasSpaceFor12Icons = (size.width - args.startGutter - args.endGutter) - (iconSizeRound * 12) >= (12 * 2.dp.toPx())
    val iconY = ((args.topGutter / 2) - (iconSize / 2)).roundToInt()
    val range = maxCelsius - minCelsius

    val plotPath = Path()
    val plotFillPath = Path()
    fun movePlot(x: Float, y: Float) {
        with(plotPath) { if (isEmpty) moveTo(x, y) else lineTo(x, y) }
        with(plotFillPath) { if (isEmpty) moveTo(x, y) else lineTo(x, y) }
    }

    var minCenter: Pair<Offset, Temperature>? = null
    var maxCenter: Pair<Offset, Temperature>? = null
    var nowCenter: Offset? = null
    var lastX = 0f

    drawTimeAxis(
        measurer = measurer,
        args = args
    ) { i, x ->
        // Temperature line
        val point = state.points.getOrNull(i) ?: return@drawTimeAxis
        val temp = point.temperature
        val tempC = temp.value.convertTo(Temperature.Unit.DegreesCelsius).value
        val y = ((1 - ((tempC - minCelsius) / range)) * (size.height - args.topGutter - args.bottomGutter)).toFloat() + args.topGutter
        movePlot(x, y)
        lastX = x

        // Min, max and now indicators are drawn after the plot so they're on top of it
        if (temp.meta == GraphTemperature.Meta.Minimum) minCenter = Offset(x, y) to temp.value
        if (temp.meta == GraphTemperature.Meta.Maximum) maxCenter = Offset(x, y) to temp.value
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
                colors = plotColors,
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
            colors = plotColors.map { it.copy(alpha = args.plotFillAlpha) },
            startY = gradientStart,
            endY = gradientEnd
        )
    )
    minCenter?.let { (offset, temp) ->
        drawLabeledPoint(
            label = temp.string(context, args.numberFormat),
            center = offset,
            args = args,
            measurer = measurer
        )
    }
    maxCenter?.let { (offset, temp) ->
        drawLabeledPoint(
            label = temp.string(context, args.numberFormat),
            center = offset,
            args = args,
            measurer = measurer
        )
    }
    nowCenter?.let {
        drawPastOverlayWithPoint(it, args)
    }
}

private fun DrawScope.drawTempAxis(
    unit: Temperature.Unit,
    maxTempC: Double,
    minTempC: Double,
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    val steps = 7
    val rangeC = maxTempC - minTempC
    drawVerticalAxis(
        steps = steps,
        args = args
    ) { step, x, y ->
        val frac = step / steps.toFloat()
        val temp = measurer.measure(
            text = Temperature
                .fromDegreesCelsius(value = (rangeC * frac) + minTempC)
                .convertTo(unit)
                .string(context, args.numberFormat),
            style = args.axisTextStyle
        )
        drawText(
            textLayoutResult = temp,
            color = args.axisColor,
            topLeft = Offset(
                x = x + args.endAxisTextPaddingStart,
                y = y - (temp.size.height / 2)
            )
        )
    }
}

@Preview
@Composable
private fun ConditionGraphNowMiddlePreview() {
    AppTheme {
        TemperatureGraph(
            state = previewState,
            absMinTemp = previewState.points.minOf { it.temperature.value },
            absMaxTemp = previewState.points.maxOf { it.temperature.value },
            args = GraphArgs.rememberTemperatureArgs(),
            modifier = Modifier
                .width(400.dp)
                .height(300.dp)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Preview
@Composable
private fun ConditionGraphNowStartPreview() {
    AppTheme {
        TemperatureGraph(
            state = previewState.copy(points = previewState.points.mapIndexed { idx, pt ->
                pt.copy(
                    time = GraphTime(
                        pt.time.value,
                        meta = if (idx == 0) GraphTime.Meta.Present else GraphTime.Meta.Future
                    )
                )
            }),
            absMinTemp = previewState.points.minOf { it.temperature.value },
            absMaxTemp = previewState.points.maxOf { it.temperature.value },
            args = GraphArgs.rememberTemperatureArgs(),
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
        TemperatureGraph(
            state = previewState.copy(points = previewState.points.mapIndexed { idx, pt ->
                pt.copy(
                    time = GraphTime(
                        pt.time.value,
                        meta = if (idx == previewState.points.lastIndex) GraphTime.Meta.Present else GraphTime.Meta.Past
                    )
                )
            }),
            absMinTemp = previewState.points.minOf { it.temperature.value },
            absMaxTemp = previewState.points.maxOf { it.temperature.value },
            args = GraphArgs.rememberTemperatureArgs(),
            modifier = Modifier
                .width(400.dp)
                .height(300.dp)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

private val previewState = TemperatureGraph(
    day = LocalDate.parse("2023-01-01"),
    points = listOf(
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("00:00"),
                meta = GraphTime.Meta.Past
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-5.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("01:00"),
                meta = GraphTime.Meta.Past
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-6.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("02:00"),
                meta = GraphTime.Meta.Past
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-6.5),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("03:00"),
                meta = GraphTime.Meta.Past
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-7.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("04:00"),
                meta = GraphTime.Meta.Past
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-9.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("05:00"),
                meta = GraphTime.Meta.Past
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-10.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("06:00"),
                meta = GraphTime.Meta.Past
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-10.0),
                meta = GraphTemperature.Meta.Minimum
            ),
            condition = Condition(wmoCode = 0, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("07:00"),
                meta = GraphTime.Meta.Past
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-8.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 0, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("08:00"),
                meta = GraphTime.Meta.Present
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-5.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("09:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-3.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("10:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(0.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("11:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(0.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("12:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(1.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("13:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(1.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("14:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(2.0),
                meta = GraphTemperature.Meta.Maximum
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("15:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(0.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("16:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-1.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = true),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("17:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-3.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("18:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-2.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("19:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-5.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("20:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-6.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("21:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-7.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("22:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-7.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("23:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-7.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = false),

            ),
        TemperatureGraphPoint(
            time = GraphTime(
                value = LocalTime.parse("00:00"),
                meta = GraphTime.Meta.Future
            ),
            temperature = GraphTemperature(
                value = Temperature.fromDegreesCelsius(-8.0),
                meta = GraphTemperature.Meta.Regular
            ),
            condition = Condition(wmoCode = 3, isDay = false),
        )
    )
)