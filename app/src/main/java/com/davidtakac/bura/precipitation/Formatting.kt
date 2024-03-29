/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.precipitation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.rememberNumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

private fun Precipitation.unitString(context: Context): String =
    context.getString(
        when (unit) {
            Precipitation.Unit.Millimeters -> R.string.precip_unit_mm
            Precipitation.Unit.Centimeters -> R.string.precip_unit_cm
            Precipitation.Unit.Inches -> R.string.precip_unit_in
        }
    )

private fun Precipitation.valueString(numberFormat: NumberFormat): String =
    numberFormat.format(
        BigDecimal.valueOf(value).setScale(
            when (unit) {
                Precipitation.Unit.Millimeters -> 1
                Precipitation.Unit.Centimeters -> 2
                Precipitation.Unit.Inches -> 2
            },
            RoundingMode.HALF_UP
        )
    )

private fun Precipitation.string(context: Context, numberFormat: NumberFormat): String =
    context.getString(
        when (unit) {
            Precipitation.Unit.Millimeters -> R.string.precip_value_mm
            Precipitation.Unit.Centimeters -> R.string.precip_value_cm
            Precipitation.Unit.Inches -> R.string.precip_value_in
        },
        valueString(numberFormat)
    )

private fun Precipitation.typeString(context: Context): String = context.getString(
    when (this) {
        is MixedPrecipitation -> R.string.precip_mixed
        is Rain -> R.string.precip_rain
        is Showers -> R.string.precip_showers
        is Snow -> R.string.precip_snow
    }
)

@Composable
fun Precipitation.valueString(): String = valueString(rememberNumberFormat())

@Composable
fun Precipitation.unitString(): String = unitString(LocalContext.current)

@Composable
fun Precipitation.string(): String = string(LocalContext.current, rememberNumberFormat())

@Composable
fun Precipitation.color(): Color = when (this) {
    is MixedPrecipitation -> AppTheme.colors.precipitationColor
    is Rain -> AppTheme.colors.rainColor
    is Showers -> AppTheme.colors.showersColor
    is Snow -> AppTheme.colors.snowColor
}

@Composable
fun Precipitation.typeString() = typeString(LocalContext.current)