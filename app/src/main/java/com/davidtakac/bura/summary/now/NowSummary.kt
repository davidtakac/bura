/*
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.image
import com.davidtakac.bura.condition.string
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.string

@Composable
fun NowSummary(state: NowSummary, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        Column {
            Text(
                text = stringResource(R.string.date_time_now),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.temp.string(),
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = state.cond.image(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
            Text(
                text = stringResource(
                    id = R.string.temp_value_high_low,
                    state.maxTemp.string(),
                    state.minTemp.string()
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = state.cond.string(),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.feels_like_value, state.feelsLike.string()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NowSummarySkeleton(color: State<Color>, modifier: Modifier = Modifier) {
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