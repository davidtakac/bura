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

package com.davidtakac.bura.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidtakac.bura.common.compose.rememberAppLocale
import com.davidtakac.bura.places.picker.PlacePickerViewModel
import java.time.LocalDate

@Composable
fun SummaryDestination(
    onHourlySectionClick: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onSettingsButtonClick: () -> Unit,
    onPrecipitationClick: () -> Unit,
    onSearchedPlaceEditRequest: () -> Unit,
) {
    val placePickerVM = viewModel<PlacePickerViewModel>(factory = PlacePickerViewModel.Factory)
    val summaryVM = viewModel<SummaryViewModel>(factory = SummaryViewModel.Factory)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            summaryVM.getSummary()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pickerState = placePickerVM.state.collectAsState().value
    LaunchedEffect(pickerState.active) {
        if (!pickerState.active) {
            summaryVM.getSummary()
        }
    }
    LaunchedEffect(pickerState.searchedPlaceToEdit) {
        if (pickerState.searchedPlaceToEdit != null) {
            onSearchedPlaceEditRequest()
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
        searchQuery = pickerState.query,
        onSearchQueryChange = placePickerVM::setQuery,
        searchActive = pickerState.active,
        onSearchActiveChange = placePickerVM::setActive,
        onSearchQueryClearClick = { placePickerVM.setQuery("") },
        onSearch = { placePickerVM.searchPlaces(languageCode = appLocale.language) },
        onPlaceClick = placePickerVM::selectPlace,
        onSearchedPlaceClick = placePickerVM::selectSearchedPlace,
        onPlaceDeleteClick = placePickerVM::deletePlace,

        onTryAgainClick = summaryVM::getSummary,
        onSelectPlaceClick = { placePickerVM.setActive(true) }
    )
}