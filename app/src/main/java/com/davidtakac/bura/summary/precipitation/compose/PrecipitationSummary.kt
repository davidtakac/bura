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

package com.davidtakac.bura.summary.precipitation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import com.davidtakac.bura.forecast.parameters.precipitation.MixedPrecipitation
import com.davidtakac.bura.forecast.parameters.precipitation.Precipitation
import com.davidtakac.bura.forecast.parameters.precipitation.Rain
import com.davidtakac.bura.forecast.parameters.precipitation.Showers
import com.davidtakac.bura.forecast.parameters.precipitation.Snow
import com.davidtakac.bura.forecast.parameters.precipitation.string
import com.davidtakac.bura.forecast.parameters.precipitation.typeString
import com.davidtakac.bura.forecast.parameters.precipitation.unitString
import com.davidtakac.bura.forecast.parameters.precipitation.valueString
import com.davidtakac.bura.summary.common.compose.SummaryTile
import com.davidtakac.bura.summary.common.compose.ValueAndUnit
import com.davidtakac.bura.theme.AppTheme
import com.davidtakac.bura.common.compose.rememberDateTimeFormatter
import com.davidtakac.bura.common.compose.rememberNumberFormat
import com.davidtakac.bura.summary.precipitation.FuturePrecipitation
import com.davidtakac.bura.summary.precipitation.PastPrecipitation
import com.davidtakac.bura.summary.precipitation.PrecipitationSummary
import java.time.LocalDate

@Composable
fun PrecipitationSummary(
    state: PrecipitationSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SummaryTile(
        label = { Label(past = state.past) },
        value = { Value(past = state.past) },
        supportingValue = {
            Text(
                stringResource(
                    R.string.precip_value_in_last_hours,
                    rememberNumberFormat().format(state.past.inHours)
                )
            )
        },
        bottom = { Bottom(future = state.future) },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun Label(past: PastPrecipitation) {
    Text(past.total.typeString())
}

@Composable
private fun Value(past: PastPrecipitation) {
    if (past.total.unit == Precipitation.Unit.Inches) {
        Text(text = past.total.string())
    } else {
        ValueAndUnit(
            value = past.total.valueString(),
            unit = past.total.unitString()
        )
    }
}

@Composable
fun Bottom(future: FuturePrecipitation) {
    val formatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow)
    Text(
        text = when (future) {
            is FuturePrecipitation.InHours -> stringResource(
                when (future.total) {
                    is MixedPrecipitation -> R.string.precip_value_mixed_in_next_hours
                    is Rain -> R.string.precip_value_rain_in_next_hours
                    is Showers -> R.string.precip_value_showers_in_next_hours
                    is Snow -> R.string.precip_value_snow_in_next_hours
                },
                future.total.string(),
                rememberNumberFormat().format(future.inHours)
            )

            is FuturePrecipitation.OnDay -> stringResource(
                when (future.total) {
                    is MixedPrecipitation -> R.string.precip_value_mixed_on_day
                    is Rain -> R.string.precip_value_rain_on_day
                    is Showers -> R.string.precip_value_showers_on_day
                    is Snow -> R.string.precip_value_snow_on_day
                },
                future.total.string(),
                formatter.format(future.onDay)
            )

            is FuturePrecipitation.None -> stringResource (
                R.string.precip_value_none_in_next_days,
                rememberNumberFormat().format(future.inDays)
            )
        }
    )
}

@Preview
@Composable
private fun PrecipitationSummaryPreview() {
    AppTheme {
        Surface {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .width(200.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrecipitationSummary(
                    state = PrecipitationSummary(
                        past = PastPrecipitation(
                            inHours = 12,
                            total = Rain(12.59, Precipitation.Unit.Millimeters)
                        ),
                        future = FuturePrecipitation.OnDay(
                            onDay = LocalDate.parse("2023-01-01"),
                            total = Rain(5.0, Precipitation.Unit.Millimeters)
                        )
                    ),
                    onClick = {},
                    modifier = Modifier.aspectRatio(1f)
                )
                PrecipitationSummary(
                    state = PrecipitationSummary(
                        past = PastPrecipitation(
                            inHours = 24,
                            total = Rain.ZeroMillimeters
                        ),
                        future = FuturePrecipitation.InHours(
                            inHours = 24,
                            total = Rain.ZeroMillimeters
                        ),
                    ),
                    onClick = {},
                    modifier = Modifier.aspectRatio(1f)
                )
                PrecipitationSummary(
                    state = PrecipitationSummary(
                        past = PastPrecipitation(
                            inHours = 24,
                            total = Snow(100.0, Precipitation.Unit.Millimeters).convertTo(
                                Precipitation.Unit.Inches
                            )
                        ),
                        future = FuturePrecipitation.None(inDays = 7)
                    ),
                    onClick = {},
                    modifier = Modifier.aspectRatio(1f)
                )
            }
        }
    }
}