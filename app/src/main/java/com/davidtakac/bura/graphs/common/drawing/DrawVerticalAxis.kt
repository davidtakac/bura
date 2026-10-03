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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.LayoutDirection
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.MeasuredTickLabel
import com.davidtakac.bura.graphs.common.ValueTick
import com.davidtakac.bura.graphs.common.VerticalAxisDrawLater

fun DrawScope.drawVerticalAxis(
    valueTicks: List<ValueTick>,
    args: GraphArgs,
    valueFormatter: (Double) -> String?,
): VerticalAxisDrawLater {
    val lineX =
        if (layoutDirection == LayoutDirection.Ltr) size.width - args.endGutter
        else args.endGutter
    val measuredTickLabels = mutableListOf<MeasuredTickLabel>()
    for (i in 0..valueTicks.lastIndex) {
        val valueTick = valueTicks[i]
        val stepFraction = i.toDouble() / valueTicks.lastIndex
        val plotBottom = size.height - args.bottomGutter
        val plotHeight = size.height - args.topGutter - args.bottomGutter
        val stepY = (plotBottom - plotHeight * stepFraction).toFloat()

        val horizontalLineStartX =
            if (layoutDirection == LayoutDirection.Ltr) args.startGutter
            else size.width - args.startGutter
        drawLine(
            color = args.axisColor,
            start = Offset(horizontalLineStartX, stepY),
            end = Offset(lineX, stepY)
        )
        valueTick.label?.let {
            val measuredLabel = args.textMeasurer.measure(
                text = it,
                style = args.axisTextStyle
            )
            measuredTickLabels.add(
                MeasuredTickLabel(
                    textLayoutResult = measuredLabel,
                    topLeft = Offset(
                        if (layoutDirection == LayoutDirection.Ltr) {
                            horizontalLineStartX + args.textPaddingMinHorizontal
                        } else {
                            horizontalLineStartX - args.textPaddingMinHorizontal - measuredLabel.size.width
                        },
                        stepY
                    ),
                )
            )
        }

        val measuredText = args.textMeasurer.measure(
            text = valueFormatter(valueTick.value) ?: continue,
            style = args.axisTextStyle
        )
        val textTopLeftX =
            if (layoutDirection == LayoutDirection.Ltr) lineX + args.endAxisTextPaddingStart
            else lineX - args.endAxisTextPaddingStart - measuredText.size.width
        drawText(
            textLayoutResult = measuredText,
            color = args.axisColor,
            topLeft = Offset(
                x = textTopLeftX,
                y = (stepY - measuredText.size.height / 2).let {
                    // This step adjusts the terminal texts so they're above or below their guides
                    when (i) {
                        0 -> it - measuredText.size.height / 4
                        valueTicks.lastIndex -> it + measuredText.size.height / 4
                        else -> it
                    }
                }
            )
        )
    }
    return VerticalAxisDrawLater(measuredTickLabels)
}
