/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.sun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.summary.SummaryTile
import com.davidtakac.bura.common.rememberDateTimeFormatter
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun SunSummary(state: SunSummary, modifier: Modifier = Modifier) {
    val dayFormatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow)
    val timeFormatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_hour_minute)
    val dayAndTimeFormatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow_hour_minute)

    SummaryTile(
        label = { Text(stringResource(if (state is Sunrise) R.string.sunrise else R.string.sunset)) },
        value = {
            Text(
                text = when (state) {
                    is Sunrise.WithSunsetSoon -> timeFormatter.format(state.time)
                    is Sunset.WithSunriseSoon -> timeFormatter.format(state.time)

                    is Sunrise.Later -> dayFormatter.format(state.time)
                    is Sunset.Later -> dayFormatter.format(state.time)

                    is Sunrise.WithSunsetLater -> timeFormatter.format(state.time)
                    is Sunset.WithSunriseLater -> timeFormatter.format(state.time)

                    is Sunrise.OutOfSight ->
                        if (state.forDuration.toDays() < 1) stringResource(R.string.sunrise_value_more_than_hours_away, "${state.forDuration.toHours()}")
                        else stringResource(R.string.sunrise_value_more_than_days_away, "${state.forDuration.toDays()}")
                    is Sunset.OutOfSight ->
                        if (state.forDuration.toDays() < 1) stringResource(R.string.sunset_value_more_than_hours_away, "${state.forDuration.toHours()}")
                        else stringResource(R.string.sunset_value_more_than_days_away, "${state.forDuration.toDays()}")
                }
            )
        },
        supportingValue = {
            when (state) {
                is Sunrise.Later -> Text(timeFormatter.format(state.time))
                is Sunset.Later -> Text(timeFormatter.format(state.time))
                else -> Unit
            }
        },
        bottom = {
            Text(
                text = when (state) {
                    is Sunrise.WithSunsetSoon -> stringResource(R.string.sunset_value, timeFormatter.format(state.sunset))
                    is Sunset.WithSunriseSoon -> stringResource(R.string.sunrise_value, timeFormatter.format(state.sunrise))

                    is Sunrise.WithSunsetLater -> stringResource(R.string.sunset_value, dayAndTimeFormatter.format(state.sunset))
                    is Sunset.WithSunriseLater -> stringResource(R.string.sunrise_value, dayAndTimeFormatter.format(state.sunrise))

                    is Sunrise.Later, is Sunrise.OutOfSight -> stringResource(R.string.sunrise_not_today)
                    is Sunset.Later, is Sunset.OutOfSight -> stringResource(R.string.sunset_not_today)
                }
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun SunSummaryPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .size(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SunSummary(
                state = Sunrise.WithSunsetSoon(
                    time = LocalTime.of(6, 20),
                    sunset = LocalTime.of(18, 30)
                )
            )
            SunSummary(
                state = Sunrise.WithSunsetLater(
                    time = LocalTime.of(5, 20),
                    sunset = LocalDateTime.parse("2023-01-01T18:30")
                )
            )
            SunSummary(
                state = Sunrise.Later(
                    time = LocalDateTime.parse("2023-01-01T18:30"),
                )
            )
            SunSummary(
                state = Sunrise.OutOfSight(
                    forDuration = Duration.ofDays(1),
                )
            )
        }
    }
}