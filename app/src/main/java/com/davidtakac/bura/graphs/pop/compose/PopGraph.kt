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

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.imageBitmap
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.string
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.LineGraph
import com.davidtakac.bura.graphs.common.PlotPoint
import com.davidtakac.bura.graphs.common.ValueTick
import com.davidtakac.bura.graphs.pop.PopGraphPoint
import com.davidtakac.bura.theme.AppTheme
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Composable
fun PopGraph(
    now: Instant,
    points: List<PopGraphPoint>,
    args: GraphArgs,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val plotColor = AppTheme.colors.popColor
    val ticks = (0..120 step 20).map { ValueTick(it.toDouble()) }
    LineGraph(
        plotPoints = points.map {
            PlotPoint(
                value = it.pop.value.toDouble(),
                time = it.time
            )
        },
        plotBrush = SolidColor(plotColor),
        plotUnderfillBrush = SolidColor(plotColor.copy(alpha = args.plotFillAlpha)),
        valueFormatter = {
            if (it > 100) {
                ""
            } else {
                Pop(it).string(context, args.numberFormat)
            }
        },
        valueTicks = ticks,
        gutterIcons = points.map {
            it.condition.imageBitmap(context, args.icons, args.gutterIconSize)
        },
        now = now,
        args = args,
        modifier = modifier
    )
}

@Preview
@Composable
private fun PopGraphPreview() {
    val start = ZonedDateTime.parse("1970-01-01T00:00Z")
    AppTheme {
        PopGraph(
            now = start.plus(5, ChronoUnit.HOURS).toInstant(),
            points = List(24) {
                PopGraphPoint(
                    time = start.plus(it.toLong(), ChronoUnit.HOURS),
                    pop = Pop(preciseValue = Random.nextDouble(0.0, 100.0)),
                    condition = Condition(1, isDay = it >= 7)
                )
            },
            args = GraphArgs.rememberDefaultArgs(),
            modifier = Modifier.width(400.dp).height(300.dp)
        )
    }
}