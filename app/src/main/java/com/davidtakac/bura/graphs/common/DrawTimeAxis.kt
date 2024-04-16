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

package com.davidtakac.bura.graphs.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import java.time.LocalTime

fun DrawScope.drawTimeAxis(
    measurer: TextMeasurer,
    args: GraphArgs,
    onStepDrawn: (index: Int, x: Float) -> Unit
) {
    for (i in 0..24) {
        val x = ((i.toFloat() / 24) * (size.width - args.endGutter - args.startGutter)) + args.startGutter
        fun drawTimeHelperLine(dashed: Boolean = true) {
            drawLine(
                color = args.axisColor,
                start = Offset(x, y = args.topGutter),
                end = Offset(x, y = size.height - args.bottomGutter),
                strokeWidth = args.axisWidth,
                pathEffect = if (dashed) PathEffect.dashPathEffect(args.axisDashIntervals.toFloatArray()) else null
            )
        }
        if (i % 4 == 0) {
            val time = LocalTime.of(if (i == 24) 0 else i, 0)
            val label = measurer.measure(
                args.axisTimeFormatter.format(time),
                style = args.axisTextStyle
            )
            drawTimeHelperLine(dashed = i != 0 && i != 24)
            drawText(
                textLayoutResult = label,
                color = args.axisColor,
                topLeft = Offset(
                    x = (x - (label.size.width / 2)).coerceIn(
                        minimumValue = args.startGutter + args.textPaddingMinHorizontal,
                        maximumValue = size.width - args.endGutter - label.size.width - args.textPaddingMinHorizontal
                    ),
                    y = size.height - args.bottomGutter + args.bottomAxisTextPaddingTop
                )
            )
        }
        onStepDrawn(i, x)
    }
}