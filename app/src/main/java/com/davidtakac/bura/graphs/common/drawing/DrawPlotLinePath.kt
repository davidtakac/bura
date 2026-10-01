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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.LayoutDirection
import com.davidtakac.bura.graphs.common.GraphArgs

fun DrawScope.drawPlotLinePath(lastX: Float, args: GraphArgs, block: DrawScope.() -> Unit) {
    clipPath(
        path = Path().apply {
            val rectStartX =
                if (layoutDirection == LayoutDirection.Ltr) args.startGutter
                else lastX
            val rectWidth =
                if (layoutDirection == LayoutDirection.Ltr) lastX - args.startGutter
                else size.width - args.startGutter
            addRect(
                Rect(
                    offset = Offset(x = rectStartX, y = args.topGutter),
                    size = Size(
                        width = rectWidth,
                        height = size.height - args.topGutter - args.bottomGutter
                    )
                )
            )
        },
        block = block
    )
}