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

package com.davidtakac.bura.graphs.precipitation

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.davidtakac.bura.graphs.common.drawPastOverlay
import com.davidtakac.bura.graphs.common.drawTimeAxis
import com.davidtakac.bura.graphs.common.drawVerticalAxis
import com.davidtakac.bura.precipitation.MixedPrecipitation
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import com.davidtakac.bura.precipitation.string
import com.davidtakac.bura.precipitation.valueString
import java.time.LocalDate
import java.time.LocalDateTime
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
    val rainColor = AppTheme.colors.rainColor
    val showersColor = AppTheme.colors.showersColor
    val snowColor = AppTheme.colors.snowColor
    val maxAdjusted = remember(max) {
        MixedPrecipitation.fromMillimeters(
            rain = Rain.fromMillimeters((max.convertTo(Precipitation.Unit.Millimeters).value * 1.2f).coerceAtLeast(5.0)),
            showers = Showers.Zero,
            snow = Snow.Zero
        ).convertTo(max.unit)
    }
    Canvas(modifier) {
        drawPrecipAxis(
            max = maxAdjusted,
            context = context,
            measurer = measurer,
            args = args
        )
        drawHorizontalAxisAndBars(
            state = state,
            max = maxAdjusted,
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
    max: MixedPrecipitation,
    rainColor: Color,
    showersColor: Color,
    snowColor: Color,
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    val iconSize = 24.dp.toPx()
    val iconSizeRound = iconSize.roundToInt()
    val hasSpaceFor12Icons = (size.width - args.startGutter - args.endGutter) - (iconSizeRound * 12) >= (12 * 2.dp.toPx())
    val iconY = ((args.topGutter / 2) - (iconSize / 2)).roundToInt()
    val range = max.value * 1.2f

    var nowX: Float? = null
    drawTimeAxis(
        measurer = measurer,
        args = args
    ) { i, x ->
        // Temperature line
        val point = state.points.getOrNull(i) ?: return@drawTimeAxis
        if (point.time.meta == GraphTime.Meta.Present) nowX = x

        val precip = point.precip
        val rain = precip.rain.convertTo(max.unit)
        val showers = precip.showers.convertTo(max.unit)
        val snow = precip.snow.convertTo(max.unit)
        val rainHeight = ((rain.value / range) * (size.height - args.topGutter - args.bottomGutter)).toFloat()
        val showersHeight = ((showers.value / range) * (size.height - args.topGutter - args.bottomGutter)).toFloat()
        val snowHeight = ((snow.liquidValue / range) * (size.height - args.topGutter - args.bottomGutter)).toFloat()

        val barSpacing = 1.dp.toPx()
        val desiredBarWidth = 8.dp.toPx()
        val bottomOfGraph = size.height - args.bottomGutter
        val topOfRain = bottomOfGraph - rainHeight

        val barX = if (i == 0) x + desiredBarWidth / 4 else x
        val barWidth = if (i == 0) desiredBarWidth / 2 else desiredBarWidth
        drawLine(
            brush = SolidColor(rainColor),
            start = Offset(barX, bottomOfGraph),
            end = Offset(barX, topOfRain),
            strokeWidth = barWidth
        )

        val bottomOfShowers = topOfRain - if (rainHeight > 0) barSpacing else 0f
        val topOfShowers = bottomOfShowers - showersHeight
        drawLine(
            brush = SolidColor(showersColor),
            start = Offset(barX, bottomOfShowers),
            end = Offset(barX, topOfShowers),
            strokeWidth = barWidth
        )

        val bottomOfSnow = topOfShowers - if (rainHeight > 0 || showersHeight > 0) barSpacing else 0f
        val topOfSnow = bottomOfSnow - snowHeight
        drawLine(
            brush = SolidColor(snowColor),
            start = Offset(barX, bottomOfSnow),
            end = Offset(barX, topOfSnow),
            strokeWidth = barWidth
        )

        // Condition icons
        if (i % (if (hasSpaceFor12Icons) 2 else 3) == 1) {
            val iconX = x - (iconSize / 2)
            val iconDrawable = AppCompatResources.getDrawable(context, point.cond.image(context, args.icons))!!
            drawImage(
                image = iconDrawable.toBitmap(width = iconSizeRound, height = iconSizeRound).asImageBitmap(),
                dstOffset = IntOffset(iconX.roundToInt(), y = iconY),
                dstSize = IntSize(width = iconSizeRound, height = iconSizeRound),
            )
        }
    }

    nowX?.let {
        drawPastOverlay(it, args)
    }
}

private fun DrawScope.drawPrecipAxis(
    max: MixedPrecipitation,
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    val range = max.convertTo(Precipitation.Unit.Millimeters).value
    drawVerticalAxis(
        steps = 7,
        args = args
    ) { frac, endX, y ->
        val rain = Rain.fromMillimeters(value = range * frac).convertTo(max.unit)
        val valueString = rain.valueString(args.numberFormat)
        val labelString = measurer.measure(
            text = if (frac == 0f) rain.string(context, args.numberFormat) else valueString,
            style = args.axisTextStyle
        )
        drawText(
            textLayoutResult = labelString,
            color = args.axisColor,
            topLeft = Offset(
                x = endX + args.endAxisTextPaddingStart,
                y = y - (labelString.size.height / 2)
            )
        )
    }
}

@Preview
@Composable
private fun PrecipitationGraphPreview() {
    val now = remember { LocalDateTime.parse("1970-01-01T08:00") }
    AppTheme {
        PrecipitationGraph(
            state = PrecipitationGraph(
                day = LocalDate.parse("1970-01-01"),
                points = List(24) {
                    PrecipitationGraphPoint(
                        time = GraphTime(
                            hour = LocalDateTime.parse("1970-01-01T00:00")
                                .plus(it.toLong(), ChronoUnit.HOURS),
                            now = now
                        ),
                        precip = MixedPrecipitation.fromMillimeters(
                            rain = Rain.fromMillimeters(Random.nextDouble(until = 5.0)),
                            showers = Showers.fromMillimeters(Random.nextDouble(until = 5.0)),
                            snow = Snow.fromMillimeters(Random.nextDouble(until = 5.0))
                        ),
                        cond = Condition(
                            wmoCode = Random.nextInt(0, 3),
                            isDay = Random.nextBoolean()
                        )
                    )
                }
            ),
            args = GraphArgs.rememberPrecipArgs(),
            max = MixedPrecipitation.fromMillimeters(
                Rain.fromMillimeters(15.0),
                Showers.Zero,
                Snow.Zero
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Preview
@Composable
private fun PrecipitationGraphDarkPreview() {
    val now = remember { LocalDateTime.parse("1970-01-01T08:00") }
    AppTheme(darkTheme = true) {
        PrecipitationGraph(
            state = PrecipitationGraph(
                day = LocalDate.parse("1970-01-01"),
                points = List(24) {
                    PrecipitationGraphPoint(
                        time = GraphTime(
                            hour = LocalDateTime.parse("1970-01-01T00:00")
                                .plus(it.toLong(), ChronoUnit.HOURS),
                            now = now
                        ),
                        precip = MixedPrecipitation.fromMillimeters(
                            rain = Rain.fromMillimeters(Random.nextDouble(until = 5.0)),
                            showers = Showers.fromMillimeters(0.0),
                            snow = Snow.fromMillimeters(Random.nextDouble(until = 5.0))
                        ),
                        cond = Condition(
                            wmoCode = Random.nextInt(0, 3),
                            isDay = Random.nextBoolean()
                        )
                    )
                }
            ),
            args = GraphArgs.rememberPrecipArgs(),
            max = MixedPrecipitation.fromMillimeters(
                Rain.fromMillimeters(15.0),
                Showers.Zero,
                Snow.Zero
            ).convertTo(Precipitation.Unit.Inches),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}