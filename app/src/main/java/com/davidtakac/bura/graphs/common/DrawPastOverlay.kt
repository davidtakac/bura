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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawPastOverlay(
    nowX: Float,
    args: GraphArgs
) {
    drawLine(
        color = args.axisColor,
        start = Offset(x = nowX, y = 0f),
        end = Offset(x = nowX, y = size.height),
        strokeWidth = 2f
    )
    drawRect(
        color = args.pastOverlayColor,
        topLeft = Offset.Zero,
        size = Size(width = nowX, height = size.height)
    )
}

fun DrawScope.drawPastOverlayWithPoint(
    nowCenter: Offset,
    args: GraphArgs
) {
    drawPastOverlay(nowCenter.x, args)
    drawPoint(nowCenter, args)
}