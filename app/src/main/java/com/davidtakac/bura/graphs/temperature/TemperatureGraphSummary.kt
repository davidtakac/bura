/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.graphs.temperature

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.rememberDateTimeFormatter
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.image
import com.davidtakac.bura.condition.string
import com.davidtakac.bura.summary.now.NowSummary
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.string
import java.time.LocalDate

@Composable
fun TemperatureGraphSummary(state: TemperatureGraphSummary, modifier: Modifier = Modifier) {
    val formatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow_dom_month)
    val now = state.now
    NowSummary(
        date = { Text(if (now == null) formatter.format(state.day) else stringResource(id = R.string.date_time_now)) },
        temperature = {
            when (now) {
                null -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(state.maxTemp.string())
                        Text(
                            text = state.minTemp.string(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> Text(text = now.temp.string())
            }
        },
        icon = {
            Image(
                painter = state.condition.image(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        },
        highLow = {
            when (now) {
                null -> when {
                    state.minTemp.unit == Temperature.Unit.DegreesCelsius -> Text(stringResource(R.string.cond_screen_temp_unit_celsius))
                    else -> Text(stringResource(R.string.cond_screen_temp_unit_fahrenheit))
                }

                else -> Text(
                    text = stringResource(
                        id = R.string.temp_value_high_low,
                        state.maxTemp.string(),
                        state.minTemp.string()
                    )
                )
            }
        },
        feelsLike = {
            if (now != null) Text(
                stringResource(
                    R.string.feels_like_value,
                    now.feelsLike.string()
                )
            )
        },
        condition = { if (now != null) Text(state.condition.string()) },
        modifier = modifier
    )
}

@Preview
@Composable
private fun TemperatureGraphSummaryTodayPreview() {
    AppTheme {
        Surface {
            TemperatureGraphSummary(
                state = TemperatureGraphSummary(
                    day = LocalDate.parse("1970-01-03"),
                    minTemp = Temperature.fromDegreesCelsius(10.0),
                    maxTemp = Temperature.fromDegreesCelsius(30.0),
                    condition = Condition(wmoCode = 53, isDay = true),
                    now = null
                ),
                modifier = Modifier
                    .width(400.dp)
                    .padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun TemperatureGraphSummaryDayPreview() {
    AppTheme {
        Surface {
            TemperatureGraphSummary(
                state = TemperatureGraphSummary(
                    day = LocalDate.parse("1970-01-03"),
                    minTemp = Temperature.fromDegreesCelsius(10.0),
                    maxTemp = Temperature.fromDegreesCelsius(30.0),
                    condition = Condition(wmoCode = 53, isDay = true),
                    now = null,
                ),
                modifier = Modifier
                    .width(400.dp)
                    .padding(16.dp)
            )
        }
    }
}