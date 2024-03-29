/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.pressure

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.davidtakac.bura.R
import com.davidtakac.bura.common.rememberNumberFormat
import com.davidtakac.bura.summary.pressure.PressureTrend
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

private fun Pressure.valueString(numberFormat: NumberFormat): String =
    numberFormat.format(
        BigDecimal.valueOf(value).setScale(
            when (unit) {
                Pressure.Unit.Hectopascal -> 0
                Pressure.Unit.MillimetersOfMercury -> 0
                Pressure.Unit.InchesOfMercury -> 2
            },
            RoundingMode.HALF_UP
        )
    )

private fun Pressure.unitString(context: Context): String =
    context.getString(
        when (unit) {
            Pressure.Unit.Hectopascal -> R.string.pressure_unit_hpa
            Pressure.Unit.InchesOfMercury -> R.string.pressure_unit_inhg
            Pressure.Unit.MillimetersOfMercury -> R.string.pressure_unit_mmhg
        }
    )

private fun Pressure.string(context: Context, numberFormat: NumberFormat): String =
    context.getString(
        when (unit) {
            Pressure.Unit.Hectopascal -> R.string.pressure_value_hpa
            Pressure.Unit.InchesOfMercury -> R.string.pressure_value_inhg
            Pressure.Unit.MillimetersOfMercury -> R.string.pressure_value_mmhg
        },
        valueString(numberFormat)
    )

@DrawableRes
private fun PressureTrend.image(context: Context): Int = when (this) {
    PressureTrend.Rising -> R.drawable.trending_up
    PressureTrend.Falling -> R.drawable.trending_down
    PressureTrend.Stable -> R.drawable.trending_flat
}

private fun PressureTrend.string(context: Context) = context.getString(
    when (this) {
        PressureTrend.Rising -> R.string.pressure_trend_rising
        PressureTrend.Falling -> R.string.pressure_trend_falling
        PressureTrend.Stable -> R.string.pressure_trend_steady
    }
)

@Composable
fun Pressure.valueString(): String = valueString(rememberNumberFormat())

@Composable
fun Pressure.unitString(): String = unitString(LocalContext.current)

@Composable
fun Pressure.string(): String = string(LocalContext.current, rememberNumberFormat())

@Composable
fun PressureTrend.image() = painterResource(image(LocalContext.current))

@Composable
fun PressureTrend.string() = string(LocalContext.current)