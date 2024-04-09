/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidtakac.bura.common.rememberAppLocale
import com.davidtakac.bura.place.picker.PlacePickerViewModel
import java.time.LocalDate

@Composable
fun SummaryDestination(
    onHourlySectionClick: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onSettingsButtonClick: () -> Unit,
    onPrecipitationClick: () -> Unit
) {
    val placePickerVM = viewModel<PlacePickerViewModel>(factory = PlacePickerViewModel.Factory)
    val summaryVM = viewModel<SummaryViewModel>(factory = SummaryViewModel.Factory)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            placePickerVM.getSelectedPlace()
            summaryVM.getSummary()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pickerState = placePickerVM.state.collectAsState().value
    val selectedPlace = pickerState.selectedPlace
    var searchActive by remember(selectedPlace) { mutableStateOf(false) }
    var searchQuery by remember(searchActive, selectedPlace) {
        mutableStateOf(
            if (searchActive) ""
            else selectedPlace?.name ?: ""
        )
    }
    LaunchedEffect(searchActive) {
        if (!searchActive) {
            searchQuery = selectedPlace?.name ?: ""
            summaryVM.getSummary()
        } else {
            placePickerVM.getSavedPlaces()
        }
    }

    val appLocale = rememberAppLocale()

    SummaryScreen(
        summaryState = summaryVM.state.collectAsState().value,
        onHourlySectionClick = onHourlySectionClick,
        onDayClick = onDayClick,
        onSettingsButtonClick = onSettingsButtonClick,
        onPrecipitationClick = onPrecipitationClick,

        pickerState = pickerState,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        searchActive = searchActive,
        onSearchActiveChange = { searchActive = it },
        onSearchQueryClearClick = { searchQuery = "" },
        onSearch = { placePickerVM.searchPlaces(query = searchQuery, languageCode = appLocale.language) },
        onPlaceClick = placePickerVM::selectPlace,
        onPlaceDeleteClick = placePickerVM::deletePlace,

        onTryAgainClick = summaryVM::getSummary,
        onSelectPlaceClick = { searchActive = true }
    )
}