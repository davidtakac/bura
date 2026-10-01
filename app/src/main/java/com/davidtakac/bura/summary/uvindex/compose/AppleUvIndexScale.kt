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

package com.davidtakac.bura.summary.uvindex.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.theme.AppTheme
import com.davidtakac.bura.forecast.parameters.uvindex.UvIndex

@Composable
fun AppleUvIndexScale(uvIndexNow: UvIndex, modifier: Modifier = Modifier) {
    val nowColor = MaterialTheme.colorScheme.onSurface
    val nowOutlineColor = MaterialTheme.colorScheme.surfaceVariant
    val nowOutlineThickness = with(LocalDensity.current) { 4.dp.toPx() }
    val layoutDirection = LocalLayoutDirection.current
    Canvas(
        modifier = Modifier
            .height(6.dp)
            .then(modifier)
            .clip(RoundedCornerShape(percent = 100))
            .background(Brush.horizontalGradient(
                colors = AppTheme.colors.uvIndexColors,
                startX = if (layoutDirection == LayoutDirection.Ltr) 0f else Float.POSITIVE_INFINITY,
                endX = if (layoutDirection == LayoutDirection.Ltr) Float.POSITIVE_INFINITY else 0f
            ))
    ) {
        val nowPerc =
            if (this.layoutDirection == LayoutDirection.Ltr) uvIndexNow.value / 11.0
            else 1 - uvIndexNow.value / 11.0
        val nowStart = (nowPerc.coerceIn(0.0, 1.0) * size.width).toFloat()
        val nowRadius = size.height / 2
        val nowOffset =
            if (this.layoutDirection == LayoutDirection.Ltr) -nowRadius
            else nowRadius
        val nowCenter = Offset(
            x = (nowStart + nowOffset).coerceIn(
                minimumValue = nowRadius,
                maximumValue = size.width - nowRadius
            ),
            y = nowRadius
        )
        drawCircle(
            color = nowOutlineColor,
            radius = nowRadius,
            center = nowCenter,
            style = Stroke(width = nowOutlineThickness)
        )
        drawCircle(
            color = nowColor,
            radius = nowRadius,
            center = nowCenter
        )
    }
}

@Preview
@Composable
private fun AppleUvIndexPreview() {
    AppTheme {
        AppleUvIndexScale(uvIndexNow = UvIndex(1.0), modifier = Modifier.width(200.dp))
    }
}

@Preview
@Composable
private fun AppleUvIndexRtlPreview() {
    AppTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AppleUvIndexScale(uvIndexNow = UvIndex(1.0), modifier = Modifier.width(200.dp))
        }
    }
}