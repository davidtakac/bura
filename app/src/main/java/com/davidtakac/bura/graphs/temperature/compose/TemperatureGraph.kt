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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.forecast.parameters.condition.imageBitmap
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.string
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.LineGraph
import com.davidtakac.bura.graphs.common.NiceScale
import com.davidtakac.bura.graphs.common.PlotPoint
import com.davidtakac.bura.graphs.common.ValueTick
import com.davidtakac.bura.graphs.temperature.TemperatureGraphPoint
import com.davidtakac.bura.graphs.temperature.TemperatureGraphs
import com.davidtakac.bura.theme.AppTheme
import java.time.Instant

@Composable
fun TemperatureGraph(
    now: Instant,
    points: List<TemperatureGraphPoint>,
    min: Temperature,
    max: Temperature,
    args: GraphArgs,
    modifier: Modifier = Modifier
) {
    val (niceMin, niceMax, ticks) = remember(min, max) {
        getNiceMinMaxAndTicks(min.value, max.value)
    }
    val unit = min.unit
    val plotColors = AppTheme.colors.temperatureColors(
        Temperature(niceMin, unit),
        Temperature(niceMax, unit),
    )

    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val gradientStart = with(density) { maxHeight.toPx() } - args.bottomGutter
        val gradientEnd = args.topGutter

        val context = LocalContext.current
        LineGraph(
            plotPoints = points.map {
                PlotPoint(
                    value = it.temperature.value,
                    time = it.time.value
                )
            },
            plotBrush = Brush.verticalGradient(
                colors = plotColors,
                startY = gradientStart,
                endY = gradientEnd
            ),
            plotUnderfillBrush = Brush.verticalGradient(
                colors = plotColors.map { it.copy(alpha = args.plotFillAlpha) },
                startY = gradientStart,
                endY = gradientEnd
            ),
            gutterIcons = points.map {
                it.condition.imageBitmap(context, args.icons, args.gutterIconSize)
            },
            valueTicks = ticks,
            valueFormatter = {
                Temperature(it, unit).string(context, args.numberFormat)
            },
            now = now,
            args = args,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun getNiceMinMaxAndTicks(
    min: Double,
    max: Double
): Triple<Double, Double, List<ValueTick>> {
    val maxTicks = 5
    // Avoids case where min == max, or where the scale is too small to display nice numbers
    var min = min
    var max = max
    if (max - min < maxTicks) {
        min -= maxTicks / 2.0
        max += maxTicks / 2.0
    }
    val niceScale = NiceScale(min, max, maxTicks)
    // Avoids case where scale min max are equal to extremes (looks bad)
    val niceMin = niceScale.niceMin - niceScale.niceSpacing
    val niceMax = niceScale.niceMax + niceScale.niceSpacing
    val niceSteps = niceScale.niceSteps.toMutableList()
        .apply {
            add(0, niceMin)
            add(niceMax)
        }
        .map(::ValueTick)
    return Triple(niceMin, niceMax, niceSteps)
}

@Preview
@Composable
private fun TemperatureGraphPreview(
    @PreviewParameter(TemperatureGraphsPreviewParameterProvider::class) providedState: Pair<String, TemperatureGraphs>
) {
    val state = providedState.second
    AppTheme {
        Surface {
            Column {
                Text(providedState.first)
                TemperatureGraph(
                    now = state.now,
                    points = state.graphs.first().points,
                    args = GraphArgs.rememberDefaultArgs(),
                    min = state.min,
                    max = state.max,
                    modifier = Modifier.width(400.dp).height(300.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun TemperatureGraphPreviewRtl(
    @PreviewParameter(TemperatureGraphsPreviewParameterProvider::class) providedState: Pair<String, TemperatureGraphs>
) {
    val state = providedState.second
    AppTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface {
                Column {
                    Text(providedState.first)
                    TemperatureGraph(
                        now = state.now,
                        points = state.graphs.first().points,
                        args = GraphArgs.rememberDefaultArgs(),
                        min = state.min,
                        max = state.max,
                        modifier = Modifier.width(400.dp).height(300.dp)
                    )
                }
            }
        }
    }
}