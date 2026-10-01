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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.LayoutDirection
import com.davidtakac.bura.graphs.common.GraphArgs

fun DrawScope.drawPoint(
    center: Offset,
    args: GraphArgs
) {
    val outlineRadius = args.pointCenterRadius + (args.pointOutlineWidth / 2)
    drawCircle(
        color = args.pointOutlineColor,
        radius = outlineRadius,
        center = center,
        style = Stroke(width = args.pointOutlineWidth)
    )
    drawCircle(
        color = args.pointCenterColor,
        radius = args.pointCenterRadius,
        center = center,
        style = Fill
    )
}

fun DrawScope.drawLabeledPoint(
    label: String,
    center: Offset,
    args: GraphArgs,
) {
    drawPointLabel(
        label,
        pointCenter = center,
        args = args
    )
    drawPoint(
        center,
        args = args
    )
}

private fun DrawScope.drawPointLabel(
    text: String,
    pointCenter: Offset,
    args: GraphArgs,
) {
    val labelMeasured = args.textMeasurer.measure(text, args.axisTextStyle.copy(color = args.pointLabelColor))
    val textTopLeftX = pointCenter.x - labelMeasured.size.width / 2
    val textTopLeftXMin =
        if (layoutDirection == LayoutDirection.Ltr) args.startGutter + args.textPaddingMinHorizontal
        else args.endGutter + args.textPaddingMinHorizontal
    val textTopLeftXMax =
        if (layoutDirection == LayoutDirection.Ltr) size.width - labelMeasured.size.width - args.endGutter - args.textPaddingMinHorizontal
        else size.width - labelMeasured.size.width - args.startGutter - args.textPaddingMinHorizontal
    drawText(
        textLayoutResult = labelMeasured,
        topLeft = Offset(
            x = textTopLeftX.coerceIn(
                minimumValue = textTopLeftXMin,
                maximumValue = textTopLeftXMax
            ),
            y = pointCenter.y - (labelMeasured.size.height) - args.pointTextPaddingBottom
        )
    )
}