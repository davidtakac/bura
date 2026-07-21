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

package com.davidtakac.bura.graphs.precipitation.compose

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.image
import com.davidtakac.bura.forecast.parameters.precipitation.MixedPrecipitation
import com.davidtakac.bura.forecast.parameters.precipitation.Precipitation
import com.davidtakac.bura.forecast.parameters.precipitation.Rain
import com.davidtakac.bura.forecast.parameters.precipitation.Showers
import com.davidtakac.bura.forecast.parameters.precipitation.Snow
import com.davidtakac.bura.forecast.parameters.precipitation.string
import com.davidtakac.bura.forecast.parameters.precipitation.valueString
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.graphs.common.NiceScale
import com.davidtakac.bura.graphs.common.drawing.drawPastOverlay
import com.davidtakac.bura.graphs.common.drawing.drawTimeAxis
import com.davidtakac.bura.graphs.common.drawing.drawVerticalAxis
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraph
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphPoint
import com.davidtakac.bura.theme.AppTheme
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun PrecipitationGraph(
    state: PrecipitationGraph,
    args: GraphArgs,
    max: MixedPrecipitation,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()
    val unit = max.unit
    val rainColor = AppTheme.colors.rainColor
    val showersColor = AppTheme.colors.showersColor
    val snowColor = AppTheme.colors.snowColor
    val (steps, newMax) = remember(max) {
        val leastMax = MixedPrecipitation(
            rain = Rain(4.0, Precipitation.Unit.Millimeters),
            showers = Showers.ZeroMillimeters,
            snow = Snow.ZeroMillimeters,
            unit = Precipitation.Unit.Millimeters
        )
        val niceScale = NiceScale(
            min = 0.0,
            max = (if (max < leastMax) leastMax.convertTo(unit) else max).value,
            maxTicks = 5
        )
        val niceMax = niceScale.niceMax + niceScale.niceSpacing
        val niceSteps = niceScale.niceSteps.toMutableList().apply { add(niceMax) }
        niceSteps.map {
            MixedPrecipitation(
                rain = Rain(it, max.unit),
                showers = Showers.ZeroMillimeters,
                snow = Snow.ZeroMillimeters,
                unit = unit
            )
        } to MixedPrecipitation(
            rain = Rain(niceMax, max.unit),
            snow = Snow.ZeroMillimeters,
            showers = Showers.ZeroMillimeters,
            unit = unit
        )
    }
    Canvas(modifier) {
        drawPrecipAxis(
            context = context,
            steps = steps,
            measurer = measurer,
            args = args
        )
        drawHorizontalAxisAndBars(
            state = state,
            max = newMax,
            context = context,
            measurer = measurer,
            rainColor = rainColor,
            showersColor = showersColor,
            snowColor = snowColor,
            args = args
        )
    }
}

private fun DrawScope.drawHorizontalAxisAndBars(
    state: PrecipitationGraph,
    max: Precipitation,
    rainColor: Color,
    showersColor: Color,
    snowColor: Color,
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    val iconSize = 24.dp.toPx()
    val iconSizeRound = iconSize.roundToInt()
    val hasSpaceFor12Icons =
        (size.width - args.startGutter - args.endGutter) - (iconSizeRound * 12) >= (12 * 2.dp.toPx())
    val iconY = ((args.topGutter / 2) - (iconSize / 2)).roundToInt()
    val range = max.value

    var nowX: Float? = null
    drawTimeAxis(
        measurer = measurer,
        args = args
    ) { i, x, calcY ->
        val point = state.points.getOrNull(i) ?: return@drawTimeAxis
        if (point.time.meta == GraphTime.Meta.Present) nowX = x

        val precip = point.precip
        val rain = precip.rain.convertTo(max.unit)
        val showers = precip.showers.convertTo(max.unit)
        val snow = precip.snow.convertTo(max.unit)

        val rainY = calcY(rain.value / range)
        val showersY = calcY(showers.value / range)
        val snowY = calcY(snow.liquidValue / range)

        val barSpacing = 1.dp.toPx()
        val desiredBarWidth = 8.dp.toPx()

        val barXOffset =
            (if (layoutDirection == LayoutDirection.Ltr) desiredBarWidth else -desiredBarWidth) / 4
        val barX = if (i == 0) x + barXOffset else x
        val barWidth = if (i == 0) desiredBarWidth / 2 else desiredBarWidth
        drawLine(
            brush = SolidColor(rainColor),
            start = Offset(barX, rainY.bot),
            end = Offset(barX, rainY.top),
            strokeWidth = barWidth
        )

        val bottomOfShowers = rainY.top - if (rainY.height > 0) barSpacing else 0f
        val topOfShowers = bottomOfShowers - showersY.height
        drawLine(
            brush = SolidColor(showersColor),
            start = Offset(barX, bottomOfShowers),
            end = Offset(barX, topOfShowers),
            strokeWidth = barWidth
        )

        val bottomOfSnow =
            topOfShowers - if (rainY.height > 0 || showersY.height > 0) barSpacing else 0f
        val topOfSnow = bottomOfSnow - snowY.height
        drawLine(
            brush = SolidColor(snowColor),
            start = Offset(barX, bottomOfSnow),
            end = Offset(barX, topOfSnow),
            strokeWidth = barWidth
        )

        // Condition icons
        if (i % (if (hasSpaceFor12Icons) 2 else 3) == 1) {
            val iconX = x - (iconSize / 2)
            val iconDrawable =
                AppCompatResources.getDrawable(context, point.cond.image(context, args.icons))!!
            drawImage(
                image = iconDrawable.toBitmap(
                    width = iconSizeRound,
                    height = iconSizeRound
                ).asImageBitmap(),
                dstOffset = IntOffset(iconX.roundToInt(), y = iconY),
                dstSize = IntSize(width = iconSizeRound, height = iconSizeRound),
            )
        }
    }

    nowX?.let {
        drawPastOverlay(nowX = it, args = args)
    }
}

private fun DrawScope.drawPrecipAxis(
    context: Context,
    steps: List<Precipitation>,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    drawVerticalAxis(
        steps = steps,
        args = args,
    ) { step ->
        val valueString = step.valueString(args.numberFormat)
        if (step == steps[0]) step.string(context, args.numberFormat) else valueString
    }
}

@Preview
@Composable
private fun PrecipitationGraphPreview() {
    AppTheme {
        PrecipitationGraph(
            state = previewState,
            args = GraphArgs.rememberPrecipitationArgs(),
            max = previewState.points.maxOf { it.precip },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Preview
@Composable
private fun SmallPrecipitationGraphPreview() {
    AppTheme {
        PrecipitationGraph(
            state = smallPreviewState,
            args = GraphArgs.rememberPrecipitationArgs(),
            max = smallPreviewState.points.maxOf { it.precip },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Preview
@Composable
private fun SmallPrecipitationGraphInchesPreview() {
    AppTheme {
        PrecipitationGraph(
            state = smallPreviewStateInches,
            args = GraphArgs.rememberPrecipitationArgs(),
            max = smallPreviewStateInches.points.maxOf { it.precip },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Preview
@Composable
private fun PrecipitationGraphRtlPreview() {
    AppTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            PrecipitationGraph(
                state = previewState,
                args = GraphArgs.rememberPrecipitationArgs(),
                max = previewState.points.maxOf { it.precip },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

@Preview
@Composable
private fun PrecipitationGraphDarkPreview() {
    AppTheme(darkTheme = true) {
        PrecipitationGraph(
            state = previewState,
            args = GraphArgs.rememberPrecipitationArgs(),
            max = previewState.points.maxOf { it.precip },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

private val previewState = PrecipitationGraph(
    day = LocalDate.parse("1970-01-01"),
    points = List(24) {
        PrecipitationGraphPoint(
            time = GraphTime(
                hour = ZonedDateTime.parse("1970-01-01T00:00Z")
                    .plus(it.toLong(), ChronoUnit.HOURS),
                now = ZonedDateTime.parse("1970-01-01T08:00Z").toInstant()
            ),
            precip = MixedPrecipitation(
                rain = Rain(Random.nextDouble(until = 5.0), Precipitation.Unit.Millimeters),
                snow = Snow(Random.nextDouble(until = 5.0), Precipitation.Unit.Millimeters),
                showers = Showers(Random.nextDouble(until = 5.0), Precipitation.Unit.Millimeters),
                unit = Precipitation.Unit.Millimeters
            ),
            cond = Condition(
                wmoCode = Random.nextInt(0, 3),
                isDay = Random.nextBoolean()
            )
        )
    }
)

private val smallPreviewState = PrecipitationGraph(
    day = LocalDate.parse("1970-01-01"),
    points = List(24) {
        PrecipitationGraphPoint(
            time = GraphTime(
                hour = ZonedDateTime.parse("1970-01-01T00:00Z")
                    .plus(it.toLong(), ChronoUnit.HOURS),
                now = ZonedDateTime.parse("1970-01-01T08:00Z").toInstant()
            ),
            precip = MixedPrecipitation(
                rain = Rain(Random.nextDouble(until = 5.0), Precipitation.Unit.Millimeters),
                snow = Snow.ZeroMillimeters,
                showers = Showers.ZeroMillimeters,
                unit = Precipitation.Unit.Millimeters
            ),
            cond = Condition(
                wmoCode = Random.nextInt(0, 3),
                isDay = Random.nextBoolean()
            )
        )
    }
)

private val smallPreviewStateInches = PrecipitationGraph(
    day = LocalDate.parse("1970-01-01"),
    points = List(24) {
        PrecipitationGraphPoint(
            time = GraphTime(
                hour = ZonedDateTime.parse("1970-01-01T00:00Z")
                    .plus(it.toLong(), ChronoUnit.HOURS),
                now = ZonedDateTime.parse("1970-01-01T08:00Z").toInstant()
            ),
            precip = MixedPrecipitation(
                rain = Rain(1.0, Precipitation.Unit.Millimeters),
                snow = Snow.ZeroMillimeters,
                showers = Showers.ZeroMillimeters,
                unit = Precipitation.Unit.Millimeters
            ).convertTo(Precipitation.Unit.Inches),
            cond = Condition(
                wmoCode = Random.nextInt(0, 3),
                isDay = Random.nextBoolean()
            )
        )
    }
)