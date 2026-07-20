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

package com.davidtakac.bura.places.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.davidtakac.bura.App
import com.davidtakac.bura.common.util.launchCatching
import com.davidtakac.bura.places.Place
import com.davidtakac.bura.places.saved.DeletePlace
import com.davidtakac.bura.places.saved.GetSavedPlaces
import com.davidtakac.bura.places.saved.SavedPlace
import com.davidtakac.bura.places.search.SearchedPlace
import com.davidtakac.bura.places.search.SearchPlaces
import com.davidtakac.bura.places.selected.SelectPlace
import com.davidtakac.bura.places.selected.SelectedPlaceRepository
import com.davidtakac.bura.unexpectederror.UnexpectedErrorSetter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.zone.ZoneRulesException

class PlacePickerViewModel(
    private val selectedPlaceRepo: SelectedPlaceRepository,
    private val selectPlace: SelectPlace,
    private val getSavedPlaces: GetSavedPlaces,
    private val searchPlaces: SearchPlaces,
    private val deletePlace: DeletePlace,
    private val unexpectedErrorSetter: UnexpectedErrorSetter,
) : ViewModel() {
    private val _state = MutableStateFlow(PlacePickerState())
    val state get() = _state.asStateFlow()

    fun getSelectedPlace() {
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            _state.value = _state.value.copy(loading = true)
            val place = selectedPlaceRepo.getSelectedPlace()
            _state.value = _state.value.copy(
                loading = false,
                selectedPlace = place
            )
        }
    }

    fun selectPlace(place: Place) {
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            selectPlaceActual(place)
        }
    }

    fun selectSearchedPlace(searchedPlace: SearchedPlace) {
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            try {
                selectPlaceActual(place = searchedPlace.toPlace())
            } catch (_: ZoneRulesException) {
                _state.value = _state.value.copy(searchedPlaceBeingEdited = searchedPlace)
            }
        }
    }

    fun cancelSearchedPlaceEdit() {
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            _state.value = _state.value.copy(searchedPlaceBeingEdited = null)
        }
    }

    fun getSavedPlaces() {
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                results = PlacePickerResults.SavedPlaces(getSavedPlaces.invoke(Instant.now())),
                loading = false,
            )
        }
    }

    fun searchPlaces(query: String, languageCode: String) {
        val trimmedQuery = query.trim()
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            _state.value = _state.value.copy(loading = true)
            val results = searchPlaces.invoke(trimmedQuery, languageCode)
            _state.value = _state.value.copy(
                loading = false,
                results = PlacePickerResults.SearchedPlaces(trimmedQuery, results)
            )
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            _state.value = _state.value.copy(loading = true)
            deletePlace.invoke(place)
            _state.value = _state.value.copy(
                loading = false,
                results = PlacePickerResults.SavedPlaces(getSavedPlaces.invoke(Instant.now())),
                selectedPlace = selectedPlaceRepo.getSelectedPlace()
            )
        }
    }

    private suspend fun selectPlaceActual(place: Place) {
        selectPlace.invoke(place)
        _state.value = _state.value.copy(
            selectedPlace = place,
            searchedPlaceBeingEdited = null
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return PlacePickerViewModel(
                    container.selectedPlaceRepo,
                    container.selectPlace,
                    container.getSavedPlaces,
                    container.searchPlaces,
                    container.deletePlace,
                    container.unexpectedErrorSetter
                ) as T
            }
        }
    }
}

data class PlacePickerState(
    val loading: Boolean = false,
    val selectedPlace: Place? = null,
    val searchedPlaceBeingEdited: SearchedPlace? = null,
    val results: PlacePickerResults = PlacePickerResults.Initial
)

sealed interface PlacePickerResults {
    data object Initial : PlacePickerResults
    data class SavedPlaces(val places: List<SavedPlace>) : PlacePickerResults
    data class SearchedPlaces(val query: String, val places: List<SearchedPlace>?) : PlacePickerResults
}