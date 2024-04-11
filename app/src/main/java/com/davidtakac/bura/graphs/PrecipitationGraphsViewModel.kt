/*
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
import com.davidtakac.bura.graphs.pop.GetPopGraphs
import com.davidtakac.bura.graphs.pop.PopGraph
import com.davidtakac.bura.graphs.precipitation.GetPrecipitationGraphs
import com.davidtakac.bura.graphs.precipitation.GetPrecipitationTotals
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphs
import com.davidtakac.bura.graphs.precipitation.PrecipitationTotal
import com.davidtakac.bura.place.selected.SelectedPlaceRepository
import com.davidtakac.bura.units.SelectedUnitsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class PrecipitationGraphsViewModel (
    private val placeRepo: SelectedPlaceRepository,
    private val unitsRepo: SelectedUnitsRepository,
    private val getPrecipitationTotals: GetPrecipitationTotals,
    private val getPrecipitationGraphs: GetPrecipitationGraphs,
    private val getPopGraphs: GetPopGraphs,
) : ViewModel() {
    private val _state = MutableStateFlow<PrecipitationGraphsState>(PrecipitationGraphsState.Loading)
    val state = _state.asStateFlow()

    fun getGraphs() {
        viewModelScope.launch {
            if (_state.value !is PrecipitationGraphsState.Success) {
                _state.value = PrecipitationGraphsState.Loading
            }
            _state.value = getState()
        }
    }

    private suspend fun getState(): PrecipitationGraphsState {
        val location = placeRepo.getSelectedPlace()?.location ?: return PrecipitationGraphsState.NoSelectedPlace
        val coords = location.coordinates
        val units = unitsRepo.getSelectedUnits()
        val now = Instant.now().atZone(location.timeZone).toLocalDateTime()

        val totals = getPrecipitationTotals(coords, units, now)
        when (totals) {
            ForecastResult.FailedToDownload -> return PrecipitationGraphsState.FailedToDownload
            ForecastResult.Outdated -> return PrecipitationGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val precipGraphs = getPrecipitationGraphs(coords, units, now)
        when (precipGraphs) {
            ForecastResult.FailedToDownload -> return PrecipitationGraphsState.FailedToDownload
            ForecastResult.Outdated -> return PrecipitationGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val popGraphs = getPopGraphs(coords, units, now)
        when (popGraphs) {

        }

        return PrecipitationGraphsState.Success(
            totals = totals.data,
            precipGraphs = precipGraphs.data
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return PrecipitationGraphsViewModel(
                    container.selectedPlaceRepo,
                    container.selectedUnitsRepo,
                    container.getPrecipitationTotals,
                    container.getPrecipitationGraphs,
                    container.getPopGraphs
                ) as T
            }
        }
    }
}

sealed interface PrecipitationGraphsState {
    data class Success(
        val totals: List<PrecipitationTotal>,
        val precipGraphs: PrecipitationGraphs,
        val popGraphs: List<PopGraph>
    ) : PrecipitationGraphsState

    data object Loading : PrecipitationGraphsState
    data object FailedToDownload : PrecipitationGraphsState
    data object Outdated : PrecipitationGraphsState
    data object NoSelectedPlace : PrecipitationGraphsState
}