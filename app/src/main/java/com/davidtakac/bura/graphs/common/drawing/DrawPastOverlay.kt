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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection
import com.davidtakac.bura.graphs.common.GraphArgs

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
    val topLeftX = if (layoutDirection == LayoutDirection.Ltr) 0f else nowX
    val overlayWidth = if (layoutDirection == LayoutDirection.Ltr) nowX else size.width - nowX
    drawRect(
        color = args.pastOverlayColor,
        topLeft = Offset(topLeftX, 0f),
        size = Size(width = overlayWidth, height = size.height)
    )
}

fun DrawScope.drawPastOverlayWithPoint(
    nowCenter: Offset,
    args: GraphArgs
) {
    drawPastOverlay(nowCenter.x, args)
    drawPoint(nowCenter, args)
}