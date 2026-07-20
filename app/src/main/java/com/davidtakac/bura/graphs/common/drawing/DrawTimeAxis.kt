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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.LayoutDirection
import com.davidtakac.bura.graphs.common.GraphArgs
import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun DrawScope.drawTimeAxis(
    measurer: TextMeasurer,
    args: GraphArgs,
    onStepDrawn: (
        i: Int,
        x: Float,
        calculateY: (percent: Double) -> YData,
    ) -> Unit
) {
    for (i in 0..24) {
        val xPercent = if (layoutDirection == LayoutDirection.Ltr) i / 24f else 1 - (i / 24f)
        val xOffset = if (layoutDirection == LayoutDirection.Ltr) args.startGutter else args.endGutter - args.startGutter
        val plotWidth = size.width - args.endGutter - args.startGutter
        val x = xPercent * plotWidth + xOffset
        fun drawTimeHelperLine(onEdge: Boolean) {
            drawLine(
                color = args.axisColor,
                start = Offset(x, y = if (onEdge) 0f else args.topGutter),
                end = Offset(x, y = size.height),
                strokeWidth = args.axisWidth,
                pathEffect = if (!onEdge) PathEffect.dashPathEffect(args.axisDashIntervals.toFloatArray()) else null
            )
        }
        if (i % 6 == 0) {
            val time = LocalTime.of(if (i == 24) 0 else i, 0)
            val label = measurer.measure(
                args.axisTimeFormatter.format(time),
                style = args.axisTextStyle
            )
            drawTimeHelperLine(onEdge = i == 0 || i == 24)
            if (i != 24) {
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
        }
        onStepDrawn(i, x) { percent ->
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

fun DrawScope.drawTimeAxis(
    measurer: TextMeasurer,
    times: List<ZonedDateTime>,
    args: GraphArgs,
    drawData: (
        i: Int,
        x: Float,
        calculateY: (percent: Double) -> YData,
    ) -> Unit
) {
    val range = ChronoUnit.SECONDS.between(
        times.first().toInstant(),
        times.last().toInstant()
    )
    // Four graph divisions which will result in lines and labels at 00, 06, 12, 18, 00
    // for 24h days. Days with other numbers of hours will have equally spaced lines as well, but
    // non-whole-hour labels.
    val graphDivisions = 4
    val labelInterval = range / graphDivisions
    // Times in the dataset can be assumed to be multiples of 5 minutes, hence the step
    for (seconds in times.first().toInstant().epochSecond..times.last().toInstant().epochSecond step 300) {
        val secondsFromFirst = seconds - times.first().toInstant().epochSecond
        val rangePercent = secondsFromFirst / range.toFloat()

        val xPercent = if (layoutDirection == LayoutDirection.Ltr) rangePercent else 1 - rangePercent
        val xOffset = if (layoutDirection == LayoutDirection.Ltr) args.startGutter else args.endGutter - args.startGutter
        val plotWidth = size.width - args.endGutter - args.startGutter
        val x = xPercent * plotWidth + xOffset

        if (secondsFromFirst % labelInterval == 0L) {
            val time = Instant.ofEpochSecond(seconds).atZone(times.first().zone)
            drawTimeLabelAndHelperLine(
                args = args,
                x = x,
                time = time,
                atStart = secondsFromFirst == 0L,
                atEnd = secondsFromFirst == range,
                measurer = measurer
            )
        }

        // If the current time is one of the passed in times, the caller of this method wants to
        // draw its data point.
        times
            .indexOfFirst { it.toInstant() == Instant.ofEpochSecond(seconds) }
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

private fun DrawScope.drawTimeLabelAndHelperLine(
    x: Float,
    time: ZonedDateTime,
    atStart: Boolean,
    atEnd: Boolean,
    measurer: TextMeasurer,
    args: GraphArgs,
) {
    val drawSolidLine = atStart || atEnd
    drawLine(
        color = args.axisColor,
        start = Offset(x, y = if (drawSolidLine) 0f else args.topGutter),
        end = Offset(x, y = size.height),
        strokeWidth = args.axisWidth,
        pathEffect = if (!drawSolidLine) PathEffect.dashPathEffect(args.axisDashIntervals.toFloatArray()) else null
    )

    if (atEnd) return
    val label = measurer.measure(args.axisTimeFormatter.format(time), style = args.axisTextStyle)
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