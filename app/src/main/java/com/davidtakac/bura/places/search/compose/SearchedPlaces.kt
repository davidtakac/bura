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

package com.davidtakac.bura.places.search.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.places.compose.PlacesScaffold
import com.davidtakac.bura.places.picker.PlacePickerResults
import com.davidtakac.bura.places.search.SearchedPlace

private val horizontalPadding = 16.dp

@Composable
fun SearchedPlaces(
    state: PlacePickerResults.SearchedPlaces,
    loading: Boolean,
    onPlaceClick: (SearchedPlace) -> Unit
) {
    PlacesScaffold(loading) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(id = R.string.place_picker_title_search_results),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = horizontalPadding, top = 24.dp)
            )
            val places = state.places
            when {
                places == null -> {
                    Text(
                        text = stringResource(id = R.string.place_picker_error_no_internet),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = horizontalPadding, top = 8.dp)
                    )
                }

                places.isEmpty() -> {
                    Text(
                        text = stringResource(
                            R.string.place_picker_error_value_no_results_for,
                            state.query
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = horizontalPadding, top = 8.dp)
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(places) { idx, item ->
                            SearchedPlaceItem(
                                state = item,
                                onClick = { onPlaceClick(item) },
                                modifier = Modifier
                                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
                                    .fillMaxWidth()
                            )
                            HorizontalDivider()
                        }
                        item {
                            Text(
                                text = stringResource(id = R.string.credit_geocoding),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    horizontal = horizontalPadding,
                                    vertical = 16.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
