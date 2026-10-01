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

package com.davidtakac.bura.graphs.common.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.LayoutDirection
import com.davidtakac.bura.graphs.common.GraphArgs
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun DrawScope.drawTimeAxis(
    times: List<ZonedDateTime>,
    args: GraphArgs,
    drawData: (
        i: Int,
        x: Float,
        calculateY: (percent: Double) -> YData,
    ) -> Unit
) {
    val plotWidth = size.width - args.endGutter - args.startGutter
    val xOffset = if (layoutDirection == LayoutDirection.Ltr) args.startGutter else args.endGutter - args.startGutter
    val totalSeconds = ChronoUnit.SECONDS.between(
        times.first().toInstant(),
        times.last().toInstant()
    )
    // Times in the dataset can be assumed to be multiples of 5 minutes, hence the step
    // This should save resources compared to iterating over each second
    for (secondsAbsolute in times.first().toInstant().epochSecond..times.last().toInstant().epochSecond step 300) {
        val seconds = secondsAbsolute - times.first().toInstant().epochSecond
        val percentOfTotal = if (layoutDirection == LayoutDirection.Ltr) seconds / totalSeconds.toFloat() else 1 - (seconds / totalSeconds.toFloat())
        val x = percentOfTotal * plotWidth + xOffset
        val time = Instant.ofEpochSecond(secondsAbsolute).atZone(times.first().zone)
        if (seconds == 0L) {
            drawTimeHelperLine(args, x, drawSolidLine = true)
            drawTimeLabel(args, x, time)
        } else if (seconds == totalSeconds) {
            drawTimeHelperLine(args, x, drawSolidLine = true)
            // We do not draw the end label to avoid clashing with first Y axis label
        } else if (time.toLocalTime().run { minute == 0 && hour in listOf(6, 12, 18) }) {
            drawTimeHelperLine(args, x, drawSolidLine = false)
            drawTimeLabel(args, x, time)
        }

        // If the current time is one of the passed in times, the caller of this method wants to
        // draw its data point.
        times
            .indexOfFirst { it.toInstant() == Instant.ofEpochSecond(secondsAbsolute) }
            .takeUnless { it < 0 }
            ?.let { i ->
                drawData(i, x) { percent ->
                    // Flip is necessary because Canvas coordinate system is top to bottom, while real
                    // world graphs are bottom to top
                    val percentFromBottom = 1 - percent
                    val plotHeight = size.height - args.topGutter - args.bottomGutter
                    val yOffset = args.topGutter
                    YData(
                        top = ((percentFromBottom * plotHeight) + yOffset).toFloat(),
                        bot = size.height - args.bottomGutter
                    )
                }
            }
    }
}

private fun DrawScope.drawTimeHelperLine(
    args: GraphArgs,
    x: Float,
    drawSolidLine: Boolean,
) {
    drawLine(
        color = args.axisColor,
        start = Offset(x, y = args.topGutter),
        end = Offset(x, y = size.height),
        strokeWidth = args.axisWidth,
        pathEffect = if (!drawSolidLine) PathEffect.dashPathEffect(args.axisDashIntervals.toFloatArray()) else null
    )
}

private fun DrawScope.drawTimeLabel(
    args: GraphArgs,
    x: Float,
    time: ZonedDateTime
) {
    val label = args.textMeasurer.measure(args.axisTimeFormatter.format(time), style = args.axisTextStyle)
    val textTopLeftX =
        if (layoutDirection == LayoutDirection.Ltr) x + args.bottomAxisTextPaddingHorizontal
        else x - label.size.width - args.bottomAxisTextPaddingHorizontal
    val textTopLeftXMin =
        if (layoutDirection == LayoutDirection.Ltr) args.startGutter + args.bottomAxisTextPaddingHorizontal
        else args.endGutter + args.bottomAxisTextPaddingHorizontal
    val textTopLeftXMax =
        if (layoutDirection == LayoutDirection.Ltr) size.width - args.endGutter - label.size.width - args.bottomAxisTextPaddingHorizontal
        else size.width - args.startGutter - label.size.width - args.bottomAxisTextPaddingHorizontal
    drawText(
        textLayoutResult = label,
        color = args.axisColor,
        topLeft = Offset(
            x = textTopLeftX.coerceIn(
                minimumValue = textTopLeftXMin,
                maximumValue = textTopLeftXMax
            ),
            y = size.height - args.bottomGutter + args.bottomAxisTextPaddingTop
        )
    )
}

data class YData(
    val top: Float,
    val bot: Float,
) {
    val height = bot - top
}