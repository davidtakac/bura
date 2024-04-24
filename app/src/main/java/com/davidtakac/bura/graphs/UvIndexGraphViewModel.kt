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

package com.davidtakac.bura.graphs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.davidtakac.bura.App
import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.graphs.uvindex.GetUvIndexGraphs
import com.davidtakac.bura.graphs.uvindex.UvIndexGraph
import com.davidtakac.bura.place.selected.SelectedPlaceRepository
import com.davidtakac.bura.units.SelectedUnitsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class UvIndexGraphViewModel (
    private val placeRepo: SelectedPlaceRepository,
    private val unitsRepo: SelectedUnitsRepository,
    private val getGraphs: GetUvIndexGraphs
) : ViewModel() {
    private val _state = MutableStateFlow<UvIndexGraphState>(UvIndexGraphState.Loading)
    val state = _state.asStateFlow()

    fun getGraphs() {
        viewModelScope.launch {
            if (_state.value !is UvIndexGraphState.Success) {
                _state.value = UvIndexGraphState.Loading
            }
            _state.value = getState()
        }
    }

    private suspend fun getState(): UvIndexGraphState {
        val location = placeRepo.getSelectedPlace()?.location ?: return UvIndexGraphState.NoSelectedPlace
        val coords = location.coordinates
        val units = unitsRepo.getSelectedUnits()
        val now = Instant.now().atZone(location.timeZone).toLocalDateTime()

        val graphs = getGraphs(coords, units, now)
        when (graphs) {
            ForecastResult.FailedToDownload -> return UvIndexGraphState.FailedToDownload
            ForecastResult.Outdated -> return UvIndexGraphState.Outdated
            is ForecastResult.Success -> Unit
        }

        return UvIndexGraphState.Success(
            graphs = graphs.data
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return UvIndexGraphViewModel(
                    container.selectedPlaceRepo,
                    container.selectedUnitsRepo,
                    container.getUvIndexGraphs
                ) as T
            }
        }
    }
}

sealed interface UvIndexGraphState {
    data class Success(
        val graphs: List<UvIndexGraph>
    ) : UvIndexGraphState

    data object Loading : UvIndexGraphState
    data object FailedToDownload : UvIndexGraphState
    data object Outdated : UvIndexGraphState
    data object NoSelectedPlace : UvIndexGraphState
}
