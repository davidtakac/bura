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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.rememberDateTimeFormatter
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.image
import com.davidtakac.bura.condition.string
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.string
import java.time.LocalDate

@Composable
fun TemperatureGraphSummary(state: TemperatureGraphSummary, modifier: Modifier = Modifier) {
    val formatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow_dom_month)
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        val now = state.now
        Column {
            Text(
                text = if (now == null) formatter.format(state.day) else stringResource(id = R.string.date_time_now),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.displayMedium) {
                    when (now) {
                        null -> {
                            Text(text = state.maxTemp.string())
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.minTemp.string(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> Text(text = now.temp.string())
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = state.condition.image(),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
            ) {
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
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (now != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = state.condition.string(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.feels_like_value, now.feelsLike.string()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TemperatureGraphSummarySkeleton(color: State<Color>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(16.dp)
                    .background(color = color.value, shape = MaterialTheme.shapes.small)
            ) {}
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(60.dp)
                    .background(color = color.value, shape = MaterialTheme.shapes.medium)
            ) {}
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(20.dp)
                    .background(color = color.value, shape = MaterialTheme.shapes.small)
            ) {}
        }
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(48.dp)
                .background(color = color.value, shape = MaterialTheme.shapes.medium)
        ) {}
    }
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