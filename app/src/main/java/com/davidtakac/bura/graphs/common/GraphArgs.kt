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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppIcons
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.rememberDateTimeFormatter
import com.davidtakac.bura.common.rememberNumberFormat
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

data class GraphArgs(
    val startGutter: Float,
    val endGutter: Float,
    val topGutter: Float,
    val bottomGutter: Float,

    val plotWidth: Float,
    val plotFillAlpha: Float,

    val axisWidth: Float,
    val axisTextStyle: TextStyle,
    val axisColor: Color,
    val axisDashIntervals: List<Float>,
    val bottomAxisTextPaddingTop: Float,
    val endAxisTextPaddingStart: Float,
    val pointTextPaddingBottom: Float,
    val textPaddingMinHorizontal: Float,
    val numberFormat: NumberFormat,
    val axisTimeFormatter: DateTimeFormatter,

    val pointCenterRadius: Float,
    val pointOutlineWidth: Float,
    val pointOutlineColor: Color,
    val pointCenterColor: Color,
    val pointLabelColor: Color,

    val pastOverlayColor: Color,
    val icons: AppIcons
) {
    companion object {
        private fun default(
            density: Density,
            dateTimeFormatter: DateTimeFormatter,
            numberFormat: NumberFormat,
            typography: Typography,
            colorScheme: ColorScheme,
            icons: AppIcons,
        ) = with(density) {
            val pointCenterRadius = 2.dp.toPx()
            val pointOutlineWidth = 2.dp.toPx()
            val axisTextStyle = typography.bodySmall
            val axisTextPadding = 6.dp.toPx()
            GraphArgs(
                startGutter = 0f,
                endGutter = 48.dp.toPx(),
                topGutter = 32.dp.toPx(),
                bottomGutter = axisTextStyle.lineHeight.toPx() + (2 * axisTextPadding),
                plotWidth = 4.dp.toPx(),
                plotFillAlpha = 0.66f,
                axisWidth = Dp.Hairline.toPx(),
                axisTextStyle = typography.bodySmall,
                axisColor = colorScheme.onSurfaceVariant,
                axisDashIntervals = listOf(4.dp, 2.dp).map { it.toPx() },
                bottomAxisTextPaddingTop = axisTextPadding,
                endAxisTextPaddingStart = axisTextPadding,
                pointTextPaddingBottom = axisTextPadding,
                textPaddingMinHorizontal = 2.dp.toPx(),
                numberFormat = numberFormat,
                axisTimeFormatter = dateTimeFormatter,
                pointCenterRadius = pointCenterRadius,
                pointOutlineWidth = pointOutlineWidth,
                pointOutlineColor = colorScheme.surfaceColorAtElevation(1.dp),
                pointCenterColor = colorScheme.onSurface,
                pointLabelColor = colorScheme.onSurface,
                pastOverlayColor = colorScheme.scrim.copy(alpha = 0.1f),
                icons = icons
            )
        }

        @Composable
        fun rememberTemperatureArgs(): GraphArgs {
            val density = LocalDensity.current
            val colorScheme = MaterialTheme.colorScheme
            val typography = MaterialTheme.typography
            val dateTimeFormatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_hour)
            val numberFormat = rememberNumberFormat()
            val icons = AppTheme.icons
            return remember(density, colorScheme, typography, dateTimeFormatter, numberFormat, icons) {
                default(density, dateTimeFormatter, numberFormat, typography, colorScheme, icons)
            }
        }

        @Composable
        fun rememberPopArgs(): GraphArgs {
            val density = LocalDensity.current
            val colorScheme = MaterialTheme.colorScheme
            val typography = MaterialTheme.typography
            val dateTimeFormatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_hour)
            val numberFormat = rememberNumberFormat()
            val icons = AppTheme.icons
            return remember(density, colorScheme, typography, dateTimeFormatter, numberFormat, icons) {
                default(density, dateTimeFormatter, numberFormat, typography, colorScheme, icons)
            }
        }

        @Composable
        fun rememberPrecipArgs(): GraphArgs {
            val density = LocalDensity.current
            val colorScheme = MaterialTheme.colorScheme
            val typography = MaterialTheme.typography
            val dateTimeFormatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_hour)
            val numberFormat = rememberNumberFormat()
            val icons = AppTheme.icons
            return remember(density, colorScheme, typography, dateTimeFormatter, numberFormat, icons) {
                val default = default(density, dateTimeFormatter, numberFormat, typography, colorScheme, icons)
                default.copy(
                    startGutter = with(density) { 8.dp.toPx() },
                    endAxisTextPaddingStart = default.endAxisTextPaddingStart + with(density) { 4.dp.toPx() }
                )
            }
        }
    }
}