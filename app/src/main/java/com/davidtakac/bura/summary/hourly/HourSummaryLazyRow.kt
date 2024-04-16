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

package com.davidtakac.bura.summary.hourly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.sun.SunEvent
import com.davidtakac.bura.temperature.Temperature
import java.time.LocalDateTime

private val contentPadding = 16.dp

@Composable
fun HourSummaryLazyRow(
    state: List<HourSummary>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            val density = LocalDensity.current
            var dummyHeight by remember { mutableStateOf(0.dp) }
            HourSummaryMaxHeightDummy(
                modifier = Modifier
                    .padding(vertical = contentPadding)
                    .onSizeChanged { dummyHeight = with(density) { it.height.toDp() } }
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(contentPadding),
                modifier = Modifier.height(dummyHeight + contentPadding * 2)
            ) {
                items(state) {
                    when (it) {
                        is HourSummary.Weather -> WeatherHourSummary(it, Modifier.fillMaxHeight())
                        is HourSummary.Sun -> SunHourSummary(it, Modifier.fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
fun HourSummaryLazyRowSkeleton(color: State<Color>, modifier: Modifier = Modifier) {
    Box(modifier.background(color = color.value, shape = MaterialTheme.shapes.medium)) {
        HourSummaryMaxHeightDummy(modifier = Modifier.padding(vertical = contentPadding))
    }
}

@Preview
@Composable
private fun HourlySummaryPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            HourSummaryLazyRow(
                state = listOf(
                    HourSummary.Weather(
                        time = LocalDateTime.parse("2023-01-01T18:00"),
                        isNow = true,
                        temp = Temperature.fromDegreesCelsius(2.0),
                        pop = Pop(75.0),
                        desc = Condition(wmoCode = 1, isDay = true)
                    ),
                    HourSummary.Weather(
                        time = LocalDateTime.parse("2023-01-01T19:00"),
                        isNow = false,
                        temp = Temperature.fromDegreesCelsius(3.0),
                        pop = Pop(0.0),
                        desc = Condition(wmoCode = 2, isDay = true)
                    ),
                    HourSummary.Sun(
                        time = LocalDateTime.parse("2023-01-01T19:31"),
                        event = SunEvent.Sunset
                    ),
                    HourSummary.Weather(
                        time = LocalDateTime.parse("2023-01-01T20:00"),
                        isNow = false,
                        temp = Temperature.fromDegreesCelsius(5.0),
                        pop = Pop(0.0),
                        desc = Condition(wmoCode = 2, isDay = false)
                    ),
                ), onClick = {}
            )
        }
    }
}