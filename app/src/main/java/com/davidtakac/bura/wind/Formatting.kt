/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.wind

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.davidtakac.bura.R
import com.davidtakac.bura.common.rememberNumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

private fun WindSpeed.bftString(context: Context): String = context.getString(
    when (beaufort) {
        0 -> R.string.wind_bft_0
        1 -> R.string.wind_bft_1
        2 -> R.string.wind_bft_2
        3 -> R.string.wind_bft_3
        4 -> R.string.wind_bft_4
        5 -> R.string.wind_bft_5
        6 -> R.string.wind_bft_6
        7 -> R.string.wind_bft_7
        8 -> R.string.wind_bft_8
        9 -> R.string.wind_bft_9
        10 -> R.string.wind_bft_10
        11 -> R.string.wind_bft_11
        else -> R.string.wind_bft_12
    }
)

private fun WindSpeed.valueString(numberFormat: NumberFormat): String =
    numberFormat.format(
        BigDecimal.valueOf(value).setScale(
            when (unit) {
                WindSpeed.Unit.MetersPerSecond -> 1
                else -> 0
            },
            RoundingMode.HALF_UP
        )
    )

private fun WindSpeed.unitString(context: Context): String = context.getString(
    when (unit) {
        WindSpeed.Unit.MetersPerSecond -> R.string.wind_unit_mps
        WindSpeed.Unit.KilometersPerHour -> R.string.wind_unit_kph
        WindSpeed.Unit.MilesPerHour -> R.string.wind_unit_mph
        WindSpeed.Unit.Knots -> R.string.wind_unit_kn
    }
)

private fun WindSpeed.string(context: Context, numberFormat: NumberFormat): String =
    context.getString(
        when (unit) {
            WindSpeed.Unit.MetersPerSecond -> R.string.wind_value_mps
            WindSpeed.Unit.KilometersPerHour -> R.string.wind_value_kph
            WindSpeed.Unit.MilesPerHour -> R.string.wind_value_mph
            WindSpeed.Unit.Knots -> R.string.wind_value_kn
        },
        valueString(numberFormat)
    )

@Composable
fun WindSpeed.valueString(): String = valueString(rememberNumberFormat())

@Composable
fun WindSpeed.unitString(): String = unitString(LocalContext.current)

@Composable
fun WindSpeed.bftString(): String = bftString(LocalContext.current)

@Composable
fun WindSpeed.string(): String = string(LocalContext.current, rememberNumberFormat())