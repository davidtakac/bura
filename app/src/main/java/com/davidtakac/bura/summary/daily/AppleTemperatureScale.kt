/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.daily

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.common.AppTheme

@Composable
fun AppleTemperatureScale(
    minCelsius: Double,
    nowCelsius: Double?,
    maxCelsius: Double,
    absMinCelsius: Double,
    absMaxCelsius: Double,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val nowColor = MaterialTheme.colorScheme.onSurface
    val nowOutlineThickness = with(LocalDensity.current) { 4.dp.toPx() }
    val gradient = AppTheme.colors.temperatureColors(absMinCelsius, absMaxCelsius)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .then(modifier)
            .clip(RoundedCornerShape(percent = 100))
            .background(Brush.horizontalGradient(gradient))
    ) {
        val range = absMaxCelsius - absMinCelsius
        val pillStart = (((minCelsius - absMinCelsius) / range) * size.width).toFloat()
        val pillEnd = ((1 - ((absMaxCelsius - maxCelsius) / range)) * size.width).toFloat()
        // Coerce makes the pill at least a circle shape when the day's temperature range
        // is very small, like 0-2C difference between min and max temps
        val pillWidth = (pillEnd - pillStart).coerceAtLeast(size.height)
        nowCelsius?.let {
            val start = (((it - absMinCelsius) / range) * size.width).toFloat()
            val radius = size.height / 2
            val edgePadding = radius
            val center = Offset(
                x = (start - radius).coerceIn(
                    minimumValue = pillStart + edgePadding,
                    maximumValue = pillEnd - edgePadding
                ),
                y = radius
            )
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = nowOutlineThickness)
            )
            drawCircle(
                color = nowColor,
                radius = radius,
                center = center
            )
        }
        clipPath(
            path = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset(x = pillStart, y = 0f),
                            size = Size(width = pillWidth, height = size.height)
                        ),
                        cornerRadius = CornerRadius(x = size.height, y = size.height)
                    )
                )
            },
            clipOp = ClipOp.Difference
        ) {
            drawRect(
                color = backgroundColor,
                size = size,
            )
        }
    }
}