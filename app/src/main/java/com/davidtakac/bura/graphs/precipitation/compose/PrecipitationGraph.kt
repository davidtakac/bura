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

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.forecast.parameters.condition.imageBitmap
import com.davidtakac.bura.forecast.parameters.precipitation.MixedPrecipitation
import com.davidtakac.bura.forecast.parameters.precipitation.Precipitation
import com.davidtakac.bura.forecast.parameters.precipitation.Rain
import com.davidtakac.bura.forecast.parameters.precipitation.Showers
import com.davidtakac.bura.forecast.parameters.precipitation.Snow
import com.davidtakac.bura.forecast.parameters.precipitation.string
import com.davidtakac.bura.forecast.parameters.precipitation.valueString
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.HorizontalAxisDrawLater
import com.davidtakac.bura.graphs.common.NiceScale
import com.davidtakac.bura.graphs.common.ValueTick
import com.davidtakac.bura.graphs.common.VerticalAxisDrawLater
import com.davidtakac.bura.graphs.common.drawing.drawGutterIcon
import com.davidtakac.bura.graphs.common.drawing.drawMeasuredTickLabel
import com.davidtakac.bura.graphs.common.drawing.drawPastOverlay
import com.davidtakac.bura.graphs.common.drawing.drawTimeAxis
import com.davidtakac.bura.graphs.common.drawing.drawVerticalAxis
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphPoint
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphs
import com.davidtakac.bura.theme.AppTheme
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

@Composable
fun PrecipitationGraph(
    now: Instant,
    points: List<PrecipitationGraphPoint>,
    args: GraphArgs,
    max: MixedPrecipitation,
    modifier: Modifier = Modifier
) {
    val maxValue = max.value
    val unit = max.unit
    val rainColor = AppTheme.colors.rainColor
    val showersColor = AppTheme.colors.showersColor
    val snowColor = AppTheme.colors.snowColor
    val resources = LocalResources.current
    val (steps, newMax) = remember(maxValue, unit, resources) {
        val valueTicks = getValueTicks(unit, maxValue, resources)
        val newMax = MixedPrecipitation(
            rain = Rain(valueTicks.last().value, unit),
            snow = Snow.ZeroMillimeters,
            showers = Showers.ZeroMillimeters,
            unit = unit
        )
        valueTicks to newMax
    }
    Canvas(modifier) {
        val verticalAxisDrawLater = drawPrecipAxis(
            steps = steps,
            args = args,
            unit = unit,
        )
        val horizontalAxisDrawLater = drawHorizontalAxisAndBars(
            now = now,
            points = points,
            gutterIcons = points.map {
                it.cond.imageBitmap(args.context, args.icons, args.gutterIconSize)
            },
            max = newMax,
            rainColor = rainColor,
            showersColor = showersColor,
            snowColor = snowColor,
            args = args
        )
        verticalAxisDrawLater.measuredTickLabels.forEach {
            drawMeasuredTickLabel(args, it)
        }
        horizontalAxisDrawLater.nowCenter?.let {
            drawPastOverlay(nowX = it.x, args = args)
        }
    }
}

private fun DrawScope.drawHorizontalAxisAndBars(
    now: Instant,
    points: List<PrecipitationGraphPoint>,
    gutterIcons: List<ImageBitmap>,
    max: Precipitation,
    rainColor: Color,
    showersColor: Color,
    snowColor: Color,
    args: GraphArgs
): HorizontalAxisDrawLater {
    val range = max.value
    var nowX: Float? = null
    drawTimeAxis(
        times = points.map { it.time },
        args = args
    ) { i, x, calcY ->
        val point = points.getOrNull(i) ?: return@drawTimeAxis
        if (point.time.toInstant() == now.truncatedTo(ChronoUnit.HOURS)) nowX = x

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
            if (layoutDirection == LayoutDirection.Ltr) {
                desiredBarWidth
            } else {
                -desiredBarWidth
            } / 4
        val barX = if (i == 0) x + barXOffset else if (i == points.lastIndex) x - barXOffset else x
        val barWidth = if (i == 0 || i == points.lastIndex) desiredBarWidth / 2 else desiredBarWidth
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
        drawGutterIcon(i, x, gutterIcons[i], args)
    }

    return HorizontalAxisDrawLater(
        nowCenter = nowX?.let {
            Offset(
                x = it,
                // y doesn't matter because we won't be drawing a point anyway
                y = 0f
            )
        }
    )
}

private fun DrawScope.drawPrecipAxis(
    unit: Precipitation.Unit,
    steps: List<ValueTick>,
    args: GraphArgs
): VerticalAxisDrawLater =
    drawVerticalAxis(
        valueTicks = steps,
        args = args,
    ) { stepValue ->
        val step = Rain(stepValue, unit)
        if (stepValue == steps[0].value) {
            step.string(args.context, args.numberFormat)
        } else {
            step.valueString(args.numberFormat)
        }
    }

private fun getValueTicks(
    unit: Precipitation.Unit,
    max: Double,
    resources: Resources,
): List<ValueTick> {
    val usualStepDelta = when (unit) {
        Precipitation.Unit.Millimeters -> 3.0
        Precipitation.Unit.Centimeters -> 0.3
        Precipitation.Unit.Inches -> 0.1
    }
    val usualLightStep = usualStepDelta * 1
    val usualModerateStep = usualStepDelta * 2
    val usualHeavyStep = usualStepDelta * 3
    val max = if (max < usualHeavyStep) {
        usualHeavyStep
    } else if (max == usualHeavyStep) {
        // Avoids the case where pillar goes to the very top of the graph
        max + 0.01
    } else {
        max
    }
    val stepsFromHeavyUntilMax = ceil((max - usualHeavyStep) / usualStepDelta).toInt()
    return if (stepsFromHeavyUntilMax <= 3) {
        // Everyday cases where light-moderate-heavy labels can be displayed, and
        // heavier cases where three or fewer steps happen above heavy
        buildList {
            add(ValueTick(0.0))
            add(ValueTick(usualLightStep, resources.getString(R.string.precip_label_light)))
            add(ValueTick(usualModerateStep, resources.getString(R.string.precip_label_moderate)))
            add(ValueTick(usualHeavyStep, resources.getString(R.string.precip_label_heavy)))
            repeat(stepsFromHeavyUntilMax) {
                add(ValueTick(usualHeavyStep + (usualStepDelta * (it + 1))))
            }
        }
    } else {
        // Unusual cases where there are more than three steps above heavy, which would crowd
        // the graph if the everyday case was used
        var usedLight = false
        var usedModerate = false
        var usedHeavy = false
        NiceScale(min = 0.0, max = max, maxTicks = 5)
            .niceSteps
            .map { niceStep ->
                ValueTick(
                    value = niceStep,
                    label = when {
                        usedHeavy -> null
                        niceStep >= usualHeavyStep -> resources.getString(R.string.precip_label_heavy).also { usedHeavy = true }
                        usedModerate -> null
                        niceStep >= usualModerateStep -> resources.getString(R.string.precip_label_moderate).also { usedModerate = true }
                        usedLight -> null
                        niceStep >= usualLightStep -> resources.getString(R.string.precip_label_light).also { usedLight = true }
                        else -> null
                    }
                )
            }
    }
}

@Preview
@Composable
private fun PrecipitationGraphPreview(
    @PreviewParameter(PrecipitationGraphsPreviewParameterProvider ::class) providedState: Pair<String, PrecipitationGraphs>
) {
    val state = providedState.second
    AppTheme {
        Surface {
            Column {
                Text(providedState.first)
                PrecipitationGraph(
                    now = state.now,
                    points = state.graphs.first().points,
                    args = GraphArgs.rememberDefaultArgs(),
                    max = state.max,
                    modifier = Modifier.width(400.dp).height(300.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PrecipitationGraphPreviewRtl(
    @PreviewParameter(PrecipitationGraphsPreviewParameterProvider ::class) providedState: Pair<String, PrecipitationGraphs>
) {
    val state = providedState.second
    AppTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface {
                Column {
                    Text(providedState.first)
                    PrecipitationGraph(
                        now = state.now,
                        points = state.graphs.first().points,
                        args = GraphArgs.rememberDefaultArgs(),
                        max = state.max,
                        modifier = Modifier.width(400.dp).height(300.dp)
                    )
                }
            }
        }
    }
}