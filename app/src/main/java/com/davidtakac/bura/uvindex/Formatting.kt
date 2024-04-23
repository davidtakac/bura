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

package com.davidtakac.bura.uvindex

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.davidtakac.bura.R
import com.davidtakac.bura.common.rememberNumberFormat
import java.text.NumberFormat

fun UvIndex.valueString(numberFormat: NumberFormat): String = numberFormat.format(value)

private fun UvIndex.riskString(context: Context): String = context.getString(
    when (risk) {
        UvIndex.Risk.Low -> R.string.uv_index_risk_low
        UvIndex.Risk.Moderate -> R.string.uv_index_risk_moderate
        UvIndex.Risk.High -> R.string.uv_index_risk_high
        UvIndex.Risk.VeryHigh -> R.string.uv_index_risk_very_high
        UvIndex.Risk.Extreme -> R.string.uv_index_risk_extreme
    }
)

@Composable
fun UvIndex.valueString(): String = valueString(rememberNumberFormat())

@Composable
fun UvIndex.riskString(): String = riskString(LocalContext.current)