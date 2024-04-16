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

package com.davidtakac.bura.visibility

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.davidtakac.bura.R
import com.davidtakac.bura.common.rememberNumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

private fun Visibility.valueString(numberFormat: NumberFormat): String =
    numberFormat.format(
        BigDecimal.valueOf(value).setScale(
            when (unit) {
                Visibility.Unit.Meters, Visibility.Unit.Feet -> 0
                Visibility.Unit.Kilometers, Visibility.Unit.Miles -> 1
            },
            RoundingMode.HALF_UP
        )
    )

private fun Visibility.unitString(context: Context): String = context.getString(
    when (unit) {
        Visibility.Unit.Meters -> R.string.vis_unit_m
        Visibility.Unit.Feet -> R.string.vis_unit_ft
        Visibility.Unit.Kilometers -> R.string.vis_unit_km
        Visibility.Unit.Miles -> R.string.vis_unit_mi
    }
)

@Composable
fun Visibility.valueString() = valueString(rememberNumberFormat())

@Composable
fun Visibility.unitString() = unitString(LocalContext.current)