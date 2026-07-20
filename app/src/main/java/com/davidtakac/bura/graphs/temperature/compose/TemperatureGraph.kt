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

package com.davidtakac.bura.graphs.temperature.compose

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.condition.image
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.graphs.common.NiceScale
import com.davidtakac.bura.graphs.common.drawing.closePlotFillPath
import com.davidtakac.bura.graphs.common.drawing.drawLabeledPoint
import com.davidtakac.bura.graphs.common.drawing.drawPastOverlayWithPoint
import com.davidtakac.bura.graphs.common.drawing.drawPlotLinePath
import com.davidtakac.bura.graphs.common.drawing.drawTimeAxis
import com.davidtakac.bura.graphs.common.drawing.drawVerticalAxis
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.forecast.parameters.temperature.string
import com.davidtakac.bura.graphs.temperature.GraphTemperature
import com.davidtakac.bura.graphs.temperature.TemperatureGraph
import com.davidtakac.bura.graphs.temperature.TemperatureGraphs
import com.davidtakac.bura.graphs.temperature.getTemperatureGraphs
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun TemperatureGraph(
    state: TemperatureGraph,
    args: GraphArgs,
    absMinTemp: Temperature,
    absMaxTemp: Temperature,
    modifier: Modifier = Modifier
) {
    val unit = absMinTemp.unit
    val (min, max, scale) = remember(absMinTemp, absMaxTemp) {
        val maxTicks = 5
        // Avoids case where min == max, or where the scale is too small to display nice numbers
        var absMinTempValue = absMinTemp.value
        var absMaxTempValue = absMaxTemp.value
        if (absMaxTempValue - absMinTempValue < maxTicks) {
            absMinTempValue -= maxTicks / 2.0
            absMaxTempValue += maxTicks / 2.0
        }
        val niceScale = NiceScale(absMinTempValue, absMaxTempValue, maxTicks)
        // Avoids case where scale min max are equal to extremes (looks bad)
        val niceMin = niceScale.niceMin - niceScale.niceSpacing
        val niceMax = niceScale.niceMax + niceScale.niceSpacing
        val niceSteps = niceScale.niceSteps.toMutableList().apply {
            add(0, niceMin)
            add(niceMax)
        }
        // Converts scale to Temperature objects for further use
        Triple(
            Temperature(niceMin, unit),
            Temperature(niceMax, unit),
            niceSteps.map { Temperature(it, unit) }
        )
    }
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()
    val plotColors = AppTheme.colors.temperatureColors(min, max)
    Canvas(modifier) {
        drawTempAxis(
            steps = scale,
            context = context,
            measurer = measurer,
            args = args
        )
        drawHorizontalAxisAndPlot(
            state = state,
            min = min,
            max = max,
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
    min: Temperature,
    max: Temperature,
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    val iconSize = 24.dp.toPx()
    val iconSizeRound = iconSize.roundToInt()
    val hasSpaceFor12Icons =
        (size.width - args.startGutter - args.endGutter) - (iconSizeRound * 12) >= (12 * 2.dp.toPx())
    val iconY = ((args.topGutter / 2) - (iconSize / 2)).roundToInt()
    val range = max.value - min.value

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
        times = state.points.map { it.time.value },
        args = args
    ) { i, x, calcY ->
        // Temperature line
        val point = state.points.getOrNull(i) ?: return@drawTimeAxis
        val temp = point.temperature
        val y = calcY((temp.value.value - min.value) / range).top
        movePlot(x, y)
        lastX = x

        // Min, max and now indicators are drawn after the plot so they're on top of it
        if (temp.meta == GraphTemperature.Meta.Minimum) minCenter = Offset(x, y) to temp.value
        if (temp.meta == GraphTemperature.Meta.Maximum) maxCenter = Offset(x, y) to temp.value
        if (point.time.meta == GraphTime.Meta.Present) nowCenter = Offset(x, y)

        // Condition icons
        if (i % (if (hasSpaceFor12Icons) 2 else 3) == 1) {
            val iconX = x - (iconSize / 2)
            val iconDrawable = AppCompatResources.getDrawable(
                context,
                point.condition.image(context, args.icons)
            )!!
            drawImage(
                image = iconDrawable.toBitmap(width = iconSizeRound, height = iconSizeRound)
                    .asImageBitmap(),
                dstOffset = IntOffset(iconX.roundToInt(), y = iconY),
                dstSize = IntSize(width = iconSizeRound, height = iconSizeRound),
            )
        }
    }

    val gradientStart = size.height - args.bottomGutter
    val gradientEnd = args.topGutter
    drawPlotLinePath(lastX, args) {
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

    closePlotFillPath(plotFillPath, lastX, args)
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
    steps: List<Temperature>,
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    drawVerticalAxis(
        steps = steps,
        args = args,
        measurer = measurer,
    ) { it.string(context, args.numberFormat) }
}

@Preview
@Composable
private fun ConditionGraphNowMiddlePreview() {
    AppTheme {
        val state = generateRegularDayState()
        TemperatureGraph(
            state = state.graphs.first(),
            absMinTemp = state.minTemp,
            absMaxTemp = state.maxTemp,
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
private fun ConditionGraphNowMiddleSpringForwardPreview() {
    AppTheme {
        val springForwardState = generateSpringForwardDayState()
        TemperatureGraph(
            state = springForwardState.graphs.first(),
            absMinTemp = springForwardState.minTemp,
            absMaxTemp = springForwardState.maxTemp,
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
private fun ConditionGraphNowMiddleFallBackPreview() {
    AppTheme {
        val fallBackState = generateFallBackDayState()
        TemperatureGraph(
            state = fallBackState.graphs.first(),
            absMinTemp = fallBackState.minTemp,
            absMaxTemp = fallBackState.maxTemp,
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
private fun ConditionGraphNowMiddleRtlPreview() {
    val state = generateRegularDayState()
    AppTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            TemperatureGraph(
                state = state.graphs.first(),
                absMinTemp = state.minTemp,
                absMaxTemp = state.maxTemp,
                args = GraphArgs.rememberTemperatureArgs(),
                modifier = Modifier
                    .width(400.dp)
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@Preview
@Composable
private fun ConditionGraphNowStartPreview() {
    val state = generateRegularDayState(nowOffset = 0L)
    AppTheme {
        TemperatureGraph(
            state = state.graphs.first(),
            absMinTemp = state.minTemp,
            absMaxTemp = state.maxTemp,
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
    val state = generateRegularDayState(nowOffset = 23L)
    AppTheme {
        TemperatureGraph(
            state = state.graphs.first(),
            absMinTemp = state.minTemp,
            absMaxTemp = state.maxTemp,
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
private fun ConditionGraphFlatPreview() {
    // TODO: How do I flatten this more elegantly?
    val state = generateRegularDayState()
    val flatState = state.graphs.first().points.map { it.copy(temperature = state.graphs.first().points.first().temperature) }
    AppTheme {
        TemperatureGraph(
            state = TemperatureGraph(
                day = state.graphs.first().day,
                points = flatState
            ),
            absMinTemp = flatState.minOf { it.temperature.value },
            absMaxTemp = flatState.maxOf { it.temperature.value },
            args = GraphArgs.rememberTemperatureArgs(),
            modifier = Modifier
                .width(400.dp)
                .height(300.dp)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

private const val momentCount = 30
private val temps = buildList(momentCount) {
    repeat(momentCount) {
        add(
            Temperature(
                value = Random.nextDouble(-2.0, 1.0),
                unit = Temperature.Unit.DegreesCelsius
            )
        )
    }
}

private fun generateRegularDayState(
    nowOffset: Long = 12L
): TemperatureGraphs {
    val startingTime: ZonedDateTime = ZonedDateTime.parse("2023-01-01T00:00Z")
    val times = buildList<ZonedDateTime> {
        repeat(momentCount) {
            add(startingTime.plusHours(it.toLong()))
        }
    }
    val temperatures = TemperaturePeriod(
        times.mapIndexed { index, time ->
            TemperatureMoment(
                timeZdt = time,
                temperature = temps[index]
            )
        }
    )
    val condition = ConditionPeriod(
        times.map {
            ConditionMoment(
                timeZdt = it,
                condition = Condition(
                    wmoCode = 0,
                    isDay = it.toLocalTime().hour >= 7
                )
            )
        }
    )
    val graphs = getTemperatureGraphs(
        now = startingTime.plusHours(nowOffset),
        tempPeriod = temperatures,
        condPeriod = condition
    )
    return graphs!!
}

private fun generateSpringForwardDayState(nowOffset: Long = 12L): TemperatureGraphs {
    val summerTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(2))
    val standardTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(1))
    val startingTime = Instant.ofEpochSecond(1774825200) // 2026-03-29T23:00Z so that applying the offset starts us at 2026-03-30T00:00
    val times = buildList {
        repeat(momentCount) {
            add(
                startingTime
                    .plus(it.toLong(), ChronoUnit.HOURS)
                    .atZone(if (it <= 2) standardTimeZone else summerTimeZone)
            )
        }
    }
    val temperatures = TemperaturePeriod(
        times.mapIndexed { index, time ->
            TemperatureMoment(
                timeZdt = time,
                temperature = temps[index]
            )
        }
    )
    val condition = ConditionPeriod(
        times.map {
            ConditionMoment(
                timeZdt = it,
                condition = Condition(
                    wmoCode = 0,
                    isDay = it.toLocalTime().hour >= 7
                )
            )
        }
    )
    val graphs = getTemperatureGraphs(
        now = startingTime.plus(nowOffset, ChronoUnit.HOURS).atZone(times.first().zone),
        tempPeriod = temperatures,
        condPeriod = condition
    )
    return graphs!!
}

private fun generateFallBackDayState(nowOffset: Long = 12L): TemperatureGraphs {
    val summerTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(2))
    val standardTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(1))
    val startingTime = Instant.ofEpochSecond(1792965600) // 2026-10-25T22:00Z so that applying the offset starts us at 2026-10-26T00:00
    val times = buildList {
        repeat(momentCount) {
            add(
                startingTime
                    .plus(it.toLong(), ChronoUnit.HOURS)
                    .atZone(if (it <= 3) summerTimeZone else standardTimeZone)
            )
        }
    }
    val temperatures = TemperaturePeriod(
        times.mapIndexed { index, time ->
            TemperatureMoment(
                timeZdt = time,
                temperature = temps[index]
            )
        }
    )
    val condition = ConditionPeriod(
        times.map {
            ConditionMoment(
                timeZdt = it,
                condition = Condition(
                    wmoCode = 0,
                    isDay = it.toLocalTime().hour >= 7
                )
            )
        }
    )
    val graphs = getTemperatureGraphs(
        now = startingTime.plus(nowOffset, ChronoUnit.HOURS).atZone(times.first().zone),
        tempPeriod = temperatures,
        condPeriod = condition
    )
    return graphs!!
}