/*
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
    nowCenter: Offset,
    args: GraphArgs
) {
    val nowX = nowCenter.x
    drawLine(
        color = args.axisColor,
        start = Offset(x = nowX, y = 0f),
        end = Offset(x = nowX, y = size.height),
        strokeWidth = 2f
    )
    if (nowX > args.startGutter) {
        drawRect(
            color = args.pastOverlayColor,
            topLeft = Offset.Zero,
            size = Size(width = nowX, height = size.height)
        )
    }
    drawPoint(
        nowCenter,
        args = args
    )
}