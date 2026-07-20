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

package com.davidtakac.bura.places.saved.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.theme.AppTheme
import com.davidtakac.bura.common.compose.HighLowText
import com.davidtakac.bura.common.compose.rememberDateTimeHourMinuteFormatter
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.image
import com.davidtakac.bura.places.Coordinates
import com.davidtakac.bura.places.Location
import com.davidtakac.bura.places.Place
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.string
import com.davidtakac.bura.places.saved.SavedPlace
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
    Column(
        Modifier
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .then(modifier)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            PlaceName(
                place = state.place.name,
                selected = state.selected,
                modifier = Modifier.weight(1f)
            )
            state.conditions?.let {
                TemperatureAndCondition(
                    temperature = it.temp,
                    condition = it.condition
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DetailsAndTime(
                country = state.place.countryName ?: state.place.countryCode,
                admin1 = state.place.admin1,
                time = state.time,
                modifier = Modifier.weight(1f)
            )
            state.conditions?.let {
                HighLowText(
                    high = it.maxTemp.string(),
                    low = it.minTemp.string(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PlaceName(
    place: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (selected) {
            Icon(
                painter = painterResource(id = R.drawable.location_on),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 2.dp)
                    .size(16.dp)
            )
        }
        Text(
            text = place,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TemperatureAndCondition(
    temperature: Temperature,
    condition: Condition,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = temperature.string(),
            style = MaterialTheme.typography.titleLarge
        )
        Image(
            painter = condition.image(),
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f),
            contentDescription = null
        )
    }
}

@Composable
private fun DetailsAndTime(
    country: String?,
    admin1: String?,
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = listOfNotNull(country, admin1).joinToString(", "),
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
            val formatter = rememberDateTimeHourMinuteFormatter()
            Text(
                text = formatter.format(time),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
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
                        countryName = "Croatia",
                        countryCode = "HR",
                        admin1 = "Osijek-Baranja",
                        admin2 = "",
                        admin3 = null,
                        admin4 = "Admin4",
                        location = Location(ZoneOffset.UTC, Coordinates(0.0, 0.0))
                    ),
                    time = LocalTime.parse("08:32"),
                    selected = true,
                    conditions = SavedPlace.Conditions(
                        temp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                        minTemp = Temperature(5.0, Temperature.Unit.DegreesCelsius),
                        maxTemp = Temperature(20.0, Temperature.Unit.DegreesCelsius),
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