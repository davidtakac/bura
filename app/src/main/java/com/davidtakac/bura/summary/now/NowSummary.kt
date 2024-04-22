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

package com.davidtakac.bura.summary.now

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.davidtakac.bura.common.HighLowText
import com.davidtakac.bura.common.TextSkeleton
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.image
import com.davidtakac.bura.condition.string
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.string

@Composable
fun NowSummary(state: NowSummary, modifier: Modifier = Modifier) {
    NowSummary(
        date = { Text(stringResource(id = R.string.date_time_now)) },
        temperature = { Text(state.temp.string()) },
        icon = {
            Image(
                painter = state.cond.image(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        },
        highLow = {
            HighLowText(
                high = state.maxTemp.string(),
                low = state.minTemp.string()
            )
        },
        feelsLike = {
            Text(
                stringResource(
                    id = R.string.feels_like_value,
                    state.feelsLike.string()
                )
            )
        },
        condition = { Text(state.cond.string()) },
        modifier = modifier
    )
}

@Composable
fun NowSummary(
    temperature: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    highLow: @Composable () -> Unit,
    feelsLike: @Composable () -> Unit,
    condition: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    date: (@Composable () -> Unit)? = null,
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        Column {
            date?.let {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleMedium,
                    LocalContentColor provides MaterialTheme.colorScheme.secondary,
                    content = it
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.displayMedium,
                    content = temperature
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    icon()
                }
            }
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                content = highLow
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                content = condition
            )
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                content = feelsLike
            )
        }
    }
}

@Composable
fun NowSummarySkeleton(
    color: State<Color>,
    modifier: Modifier = Modifier,
    withDate: Boolean = false
) {
    NowSummary(
        date = if (withDate) {
            @Composable {
                TextSkeleton(
                    color = color,
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(vertical = 2.dp),
                    modifier = Modifier.width(64.dp)
                )
            }
        } else null,
        temperature = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(160.dp)
            )
        },
        icon = {},
        highLow = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(150.dp)
            )
        },
        feelsLike = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(80.dp)
            )
        },
        condition = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(64.dp)
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun NowSummaryPreview() {
    AppTheme(darkTheme = true) {
        Surface(modifier = Modifier.width(400.dp)) {
            NowSummary(
                state = NowSummary(
                    temp = Temperature.fromDegreesCelsius(20.0),
                    feelsLike = Temperature.fromDegreesCelsius(18.0),
                    minTemp = Temperature.fromDegreesCelsius(15.0),
                    maxTemp = Temperature.fromDegreesCelsius(25.0),
                    cond = Condition(
                        wmoCode = 53,
                        isDay = true
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}