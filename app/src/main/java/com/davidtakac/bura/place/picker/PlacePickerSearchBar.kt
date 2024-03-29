/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.place.picker

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.davidtakac.bura.R
import com.davidtakac.bura.place.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacePickerSearchBar(
    state: PlacePickerState,
    query: String,
    onQueryChange: (query: String) -> Unit,
    onQueryClearClick: () -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onSearchClick: (query: String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onPlaceDeleteClick: (Place) -> Unit,
    onSettingsClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(active) {
        if (active) focusRequester.requestFocus()
        else focusRequester.freeFocus()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearchClick,
            active = active,
            onActiveChange = onActiveChange,
            leadingIcon = {
                PinOrBackButton(
                    active = active,
                    onBackClick = { onActiveChange(false) }
                )
            },
            trailingIcon = {
                SettingsOrClearButton(
                    active = active,
                    onSettingsClick = onSettingsClick,
                    onClearClick = onQueryClearClick
                )
            },
            placeholder = {
                Text(text = stringResource(id = R.string.place_picker_hint_search))
            },
            modifier = Modifier.focusRequester(focusRequester)
        ) {
            PlacePickerResults(
                state = state,
                onPlaceClick = onPlaceClick,
                onPlaceDeleteClick = onPlaceDeleteClick
            )
        }
    }
}

@Composable
private fun PinOrBackButton(active: Boolean, onBackClick: () -> Unit) {
    Crossfade(targetState = active, label = "Pin to back button") {
        if (it) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        } else {
            // minimumInteractiveComponentSize is here so Crossfade can
            // animate without jumps
            Box(modifier = Modifier.minimumInteractiveComponentSize()) {
                Icon(
                    painter = painterResource(id = R.drawable.location_on),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun SettingsOrClearButton(
    active: Boolean,
    onSettingsClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Crossfade(targetState = active, label = "Settings to clear all button") {
        if (it) {
            IconButton(onClick = onClearClick) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = null
                )
            }
        } else {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = null
                )
            }
        }
    }
}