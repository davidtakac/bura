/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.graphs.precipitation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.davidtakac.bura.R
import com.davidtakac.bura.precipitation.MixedPrecipitation
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import com.davidtakac.bura.precipitation.string
import com.davidtakac.bura.precipitation.unitString
import com.davidtakac.bura.precipitation.valueString
import com.davidtakac.bura.summary.ValueAndUnit

@Composable
fun PrecipitationGraphSummary(
    value: @Composable () -> Unit,
    inHours: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineMedium,
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            content = value
        )
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyLarge,
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
            content = inHours
        )
    }
}

@Composable
fun PrecipitationGraphTodaySummary(
    state: PrecipitationTotal.Today,
    modifier: Modifier = Modifier
) {
    PrecipitationGraphSummary(
        modifier = modifier,
        value = {
            if (state.past.total.unit == Precipitation.Unit.Inches) {
                Text(text = state.past.total.string())
            } else {
                ValueAndUnit(
                    value = state.past.total.valueString(),
                    unit = state.past.total.unitString()
                )
            }
        },
        inHours = {
            Text(
                stringResource(
                    when (state.past.total) {
                        is MixedPrecipitation -> R.string.precip_screen_value_total_precip_in_last_hours
                        is Rain -> R.string.precip_screen_value_total_rain_in_last_hours
                        is Showers -> R.string.precip_screen_value_total_showers_in_last_hours
                        is Snow -> R.string.precip_screen_value_total_snow_in_last_hours
                    },
                    "${state.past.hours}"
                )
            )
        }
    )
}

@Composable
fun PrecipitationGraphOtherDaySummary(
    state: PrecipitationTotal.OtherDay,
    modifier: Modifier = Modifier
) {
    PrecipitationGraphSummary(
        modifier = modifier,
        value = {
            if (state.total.unit == Precipitation.Unit.Inches) {
                Text(text = state.total.string())
            } else {
                ValueAndUnit(
                    value = state.total.valueString(),
                    unit = state.total.unitString()
                )
            }
        },
        inHours = {
            Text(
                stringResource(
                    when (state.total) {
                        is MixedPrecipitation -> R.string.precip_screen_value_total_precip_of_day
                        is Rain -> R.string.precip_screen_value_total_rain_of_day
                        is Showers -> R.string.precip_screen_value_total_showers_of_day
                        is Snow -> R.string.precip_screen_value_total_snow_of_day
                    }
                )
            )
        }
    )
}