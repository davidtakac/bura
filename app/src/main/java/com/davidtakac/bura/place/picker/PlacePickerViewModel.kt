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

package com.davidtakac.bura.place.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.davidtakac.bura.App
import com.davidtakac.bura.place.Place
import com.davidtakac.bura.place.saved.DeletePlace
import com.davidtakac.bura.place.saved.SavedPlace
import com.davidtakac.bura.place.saved.GetSavedPlaces
import com.davidtakac.bura.place.search.SearchPlaces
import com.davidtakac.bura.place.selected.SelectPlace
import com.davidtakac.bura.place.selected.SelectedPlaceRepository
import com.davidtakac.bura.units.SelectedUnitsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class PlacePickerViewModel(
    private val selectedPlaceRepo: SelectedPlaceRepository,
    private val selectedUnitsRepo: SelectedUnitsRepository,
    private val selectPlace: SelectPlace,
    private val getSavedPlaces: GetSavedPlaces,
    private val searchPlaces: SearchPlaces,
    private val deletePlace: DeletePlace
) : ViewModel() {
    private val _state = MutableStateFlow(
        PlacePickerState(
            loading = false,
            selectedPlace = null,
            results = PlacePickerResults.Initial
        )
    )
    val state get() = _state.asStateFlow()

    fun getSelectedPlace() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val place = selectedPlaceRepo.getSelectedPlace()
            _state.value = _state.value.copy(
                loading = false,
                selectedPlace = place
            )
        }
    }

    fun selectPlace(place: Place) {
        viewModelScope.launch {
            selectPlace.invoke(place)
            _state.value = _state.value.copy(
                loading = false,
                selectedPlace = place
            )
        }
    }

    fun getSavedPlaces() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(loading = false, results = getSavedPlacesResults())
        }
    }

    fun searchPlaces(query: String, languageCode: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val results = searchPlaces.invoke(query, languageCode)
            _state.value = _state.value.copy(
                loading = false,
                results = PlacePickerResults.SearchedPlaces(query, results)
            )
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            deletePlace.invoke(place)
            _state.value = _state.value.copy(
                loading = false,
                results = getSavedPlacesResults(),
                selectedPlace = selectedPlaceRepo.getSelectedPlace()
            )
        }
    }

    private suspend fun getSavedPlacesResults(): PlacePickerResults.SavedPlaces {
        val selectedUnits = selectedUnitsRepo.getSelectedUnits()
        val selectedPlace = selectedPlaceRepo.getSelectedPlace()
        val places = getSavedPlaces.invoke(
            selectedPlace = selectedPlace,
            selectedUnits = selectedUnits,
            now = Instant.now()
        )
        return PlacePickerResults.SavedPlaces(places)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return PlacePickerViewModel(
                    container.selectedPlaceRepo,
                    container.selectedUnitsRepo,
                    container.selectPlace,
                    container.getSavedPlaces,
                    container.searchPlaces,
                    container.deletePlace
                ) as T
            }
        }
    }
}

data class PlacePickerState(
    val loading: Boolean,
    val selectedPlace: Place?,
    val results: PlacePickerResults
)

sealed interface PlacePickerResults {
    data object Initial : PlacePickerResults
    data class SavedPlaces(val places: List<SavedPlace>) : PlacePickerResults
    data class SearchedPlaces(val query: String, val places: List<Place>?) : PlacePickerResults
}