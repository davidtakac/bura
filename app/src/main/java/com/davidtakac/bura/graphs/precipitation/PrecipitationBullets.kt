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

package com.davidtakac.bura.graphs.precipitation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.precipitation.MixedPrecipitation
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import com.davidtakac.bura.precipitation.color
import com.davidtakac.bura.precipitation.string
import com.davidtakac.bura.precipitation.typeString
import com.davidtakac.bura.common.AppTheme

@Composable
fun PrecipitationBullets(state: Precipitation, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (state is MixedPrecipitation && state.value > 0) {
            state.rain.takeIf { it.value > 0 }?.let {
                LabelAndValue(it, modifier = Modifier.fillMaxWidth())
            }
            state.showers.takeIf { it.value > 0 }?.let {
                LabelAndValue(it, modifier = Modifier.fillMaxWidth())
            }
            state.snow.takeIf { it.value > 0 }?.let {
                LabelAndValue(it, modifier = Modifier.fillMaxWidth())
            }
        } else {
            LabelAndValue(state, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LabelAndValue(
    precipitation: Precipitation,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(precipitation.color())
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(precipitation.typeString(), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Text(precipitation.string(), style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun PrecipitationZeroPreview() {
    AppTheme {
        Surface {
            PrecipitationBullets(
                state = Rain.Zero,
                modifier = Modifier
                    .width(400.dp)
                    .padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun PrecipitationRainSnowPreview() {
    AppTheme {
        Surface {
            PrecipitationBullets(
                state = MixedPrecipitation.fromMillimeters(
                    Rain.fromMillimeters(1.0),
                    Showers.Zero,
                    Snow.fromMillimeters(70.0)
                ),
                modifier = Modifier
                    .width(400.dp)
                    .padding(16.dp)
            )
        }
    }
}