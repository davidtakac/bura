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

package com.davidtakac.bura.summary.hourly.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.theme.AppTheme
import com.davidtakac.bura.common.compose.rememberDateTimeHourFormatter
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.image
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.string
import com.davidtakac.bura.summary.common.compose.PopAndDrop
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.string
import com.davidtakac.bura.summary.hourly.HourSummary
import java.time.LocalDateTime

@Composable
fun WeatherHourSummary(state: HourSummary.Weather, modifier: Modifier = Modifier) {
    val formatter = rememberDateTimeHourFormatter()
    HourSummary(
        time = { Text(if (state.isNow) stringResource(R.string.date_time_now) else state.time.format(formatter)) },
        icon = {
            Image(
                painter = state.desc.image(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        },
        pop = state.pop?.string()?.let {
            @Composable {
                PopAndDrop(it)
            }
        },
        temperature = { Text(state.temp.string()) },
        modifier = modifier
    )
}

@Preview
@Composable
private fun WeatherSummaryPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeatherHourSummary(
                state = HourSummary.Weather(
                    time = LocalDateTime.parse("2023-01-01T14:00"),
                    isNow = true,
                    temp = Temperature(20.0, Temperature.Unit.DegreesCelsius),
                    pop = Pop(50.0),
                    desc = Condition(wmoCode = 51, isDay = true)
                ),
            )
            WeatherHourSummary(
                state = HourSummary.Weather(
                    time = LocalDateTime.parse("2023-01-01T15:00"),
                    isNow = false,
                    temp = Temperature(20.0, Temperature.Unit.DegreesCelsius),
                    pop = null,
                    desc = Condition(wmoCode = 1, isDay = true)
                )
            )
        }
    }
}