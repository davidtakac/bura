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

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.davidtakac.bura.graphs.common.GraphArgs
import kotlin.math.roundToInt

fun DrawScope.drawGutterIcon(
    index: Int,
    x: Float,
    icon: ImageBitmap,
    args: GraphArgs
) {
    val iconSize = args.gutterIconSize.roundToInt()
    val desiredIcons = 12
    val hasSpaceForDesiredIcons = (size.width - args.startGutter - args.endGutter) - (iconSize * desiredIcons) >= (desiredIcons * args.gutterIconSpacingMin)
    if (index % (if (hasSpaceForDesiredIcons) 2 else 3) != 1) return

    val iconY = ((args.topGutter / 2) - (args.gutterIconSize / 2)).roundToInt()
    val iconX = (x - (iconSize.toFloat() / 2)).roundToInt()
    drawImage(
        image = icon,
        dstOffset = IntOffset(iconX, y = iconY),
        dstSize = IntSize(width = iconSize, height = iconSize),
    )
}