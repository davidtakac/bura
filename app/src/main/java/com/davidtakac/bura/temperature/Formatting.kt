/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.temperature

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.davidtakac.bura.R
import com.davidtakac.bura.common.rememberNumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

fun Temperature.string(context: Context, numberFormat: NumberFormat): String = context.getString(
    R.string.temp_value_degree,
    numberFormat.format(BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP))
)

@Composable
fun Temperature.string(): String = string(LocalContext.current, rememberNumberFormat())