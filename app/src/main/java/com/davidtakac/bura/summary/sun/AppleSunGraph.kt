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

package com.davidtakac.bura.summary.sun

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AppleSunGraph(
    now: LocalTime,
    sunrise: LocalTime,
    sunset: LocalTime,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val width = size.width
        val height = size.height

        fun calcX(time: LocalTime): Float =
            ((time.toSecondOfDay() / 60f) / 1440) * width

        fun calcAngle(x: Float): Float =
            ((x / width) * 2 * PI + (PI / 2)).toFloat()

        fun calcY(angle: Float): Float =
            sin(angle) * (height / 2f) + height / 2f

        val sunriseX = calcX(sunrise)
        val sunsetX = calcX(sunset)
        val sunriseY = calcY(calcAngle(sunriseX))
        val beforeDay = Path()
        val day = Path()
        val afterDay = Path()
        for (xInt in 0..width.toInt()) {
            val x = xInt.toFloat()
            val angle = calcAngle(x)
            val y = calcY(angle)
            val path = if (y > sunriseY) {
                if (x <= sunriseX) {
                    beforeDay
                } else {
                    afterDay
                }
            } else {
                day
            }
            path.run {
                if (isEmpty) moveTo(x, y)
                else lineTo(x, y)
            }
        }

        drawPath(beforeDay, color = Color.Black, style = Stroke(2f))
        drawPath(day, color = Color.White, style = Stroke(2f))
        drawPath(afterDay, color = Color.Black, style = Stroke(2f))
        drawLine(
            Color.White,
            start = Offset(x = 0f, y = sunriseY),
            end = Offset(x = width, y = sunriseY)
        )

        /*val nowX = calcX(
            now,
            wrt = when {
                now < sunrise -> sunriseX
                now < sunset -> sunsetX
                else -> width
            }
        )
        val nowY = calcY(calcAngle(calcY(nowX)))
        drawCircle(color = Color.Red, radius = 4f, center = Offset(nowX, nowY))*/
    }
}

@Preview
@Composable
private fun AppleSunGraphPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppleSunGraph(
                now = LocalTime.of(5, 31),
                sunrise = LocalTime.of(5, 30),
                sunset = LocalTime.of(20, 30),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
            AppleSunGraph(
                now = LocalTime.of(12, 31),
                sunrise = LocalTime.of(5, 30),
                sunset = LocalTime.of(20, 30),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
            AppleSunGraph(
                now = LocalTime.of(12, 31),
                sunrise = LocalTime.of(5, 30),
                sunset = LocalTime.of(20, 30),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }
}