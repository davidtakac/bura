/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.place.saved

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.rememberDateTimeFormatter
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.image
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.place.Location
import com.davidtakac.bura.place.Place
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.string
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedPlaceItem(
    state: SavedPlace,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    Row(
        Modifier
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.selected) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_on),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .size(16.dp)
                    )
                }
                Text(
                    text = state.place.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.place.countryName ?: state.place.countryCode,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(
                            max = with(LocalDensity.current) {
                                val maxWidth = this@BoxWithConstraints.constraints.maxWidth
                                (maxWidth * .75f).toDp()
                            }
                        )
                    )
                    VerticalDivider(modifier = Modifier.height(12.dp))
                    val formatter =
                        rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_hour_minute)
                    Text(
                        text = formatter.format(state.time),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                }
            }
        }
        state.conditions?.let {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    it.temp?.let {
                        Text(
                            text = it.string(),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Image(
                        painter = it.condition.image(),
                        modifier = Modifier.size(28.dp),
                        contentDescription = null
                    )
                }
                Text(
                    text = stringResource(
                        id = R.string.temp_value_high_low,
                        it.maxTemp.string(),
                        it.minTemp.string()
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun SavedPlaceCardPreview() {
    AppTheme(darkTheme = true) {
        Surface(tonalElevation = 3.dp) {
            SavedPlaceItem(
                state = SavedPlace(
                    place = Place(
                        name = "Osijek",
                        countryName = "Croatia but very very very very very long long",
                        countryCode = "HR",
                        admin1 = "Osječko-baranjska",
                        location = Location(ZoneOffset.UTC, Coordinates(0.0, 0.0))
                    ),
                    time = LocalTime.parse("08:32"),
                    selected = true,
                    conditions = SavedPlace.Conditions(
                        temp = Temperature.fromDegreesCelsius(10.0),
                        minTemp = Temperature.fromDegreesCelsius(5.0),
                        maxTemp = Temperature.fromDegreesCelsius(20.0),
                        condition = Condition(1, true)
                    )
                ),
                onClick = {},
                onLongClick = {},
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            )
        }
    }
}