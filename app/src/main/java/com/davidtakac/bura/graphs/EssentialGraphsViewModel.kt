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
import com.davidtakac.bura.graphs.pop.PopGraph
import com.davidtakac.bura.graphs.pop.GetPopGraphs
import com.davidtakac.bura.graphs.precipitation.GetPrecipitationGraphs
import com.davidtakac.bura.graphs.precipitation.PrecipitationTotal
import com.davidtakac.bura.graphs.precipitation.GetPrecipitationTotals
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphs
import com.davidtakac.bura.graphs.temperature.GetTemperatureGraphSummaries
import com.davidtakac.bura.graphs.temperature.TemperatureGraphSummary
import com.davidtakac.bura.graphs.temperature.TemperatureGraphs
import com.davidtakac.bura.graphs.temperature.GetTemperatureGraphs
import com.davidtakac.bura.place.selected.SelectedPlaceRepository
import com.davidtakac.bura.units.SelectedUnitsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class EssentialGraphsViewModel(
    private val placeRepo: SelectedPlaceRepository,
    private val unitsRepo: SelectedUnitsRepository,
    private val getTempGraphSummaries: GetTemperatureGraphSummaries,
    private val getTempGraphs: GetTemperatureGraphs,
    private val getPopGraphs: GetPopGraphs,
    private val getPrecipGraphs: GetPrecipitationGraphs,
    private val getPrecipTotals: GetPrecipitationTotals
) : ViewModel() {
    private val _state = MutableStateFlow<EssentialGraphsState>(EssentialGraphsState.Loading)
    val state = _state.asStateFlow()

    fun getGraphs() {
        viewModelScope.launch {
            if (_state.value !is EssentialGraphsState.Success) {
                _state.value = EssentialGraphsState.Loading
            }
            _state.value = getState()
        }
    }

    private suspend fun getState(): EssentialGraphsState {
        val location = placeRepo.getSelectedPlace()?.location ?: return EssentialGraphsState.NoSelectedPlace
        val coords = location.coordinates
        val units = unitsRepo.getSelectedUnits()
        val now = Instant.now().atZone(location.timeZone).toLocalDateTime()

        val tempGraphSummaries = getTempGraphSummaries(coords, units, now)
        when (tempGraphSummaries) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val tempGraphs = getTempGraphs(coords, units, now)
        when (tempGraphs) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val popGraphs = getPopGraphs(coords, units, now)
        when (popGraphs) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val precipGraphs = getPrecipGraphs(coords, units, now)
        when (precipGraphs) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val precipTotals = getPrecipTotals(coords, units, now)
        when (precipTotals) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        return EssentialGraphsState.Success(
            tempGraphSummaries = tempGraphSummaries.data,
            tempGraphs = tempGraphs.data,
            popGraphs = popGraphs.data,
            precipGraphs = precipGraphs.data,
            precipTotals = precipTotals.data
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return EssentialGraphsViewModel(
                    container.selectedPlaceRepo,
                    container.selectedUnitsRepo,
                    container.getTemperatureGraphSummaries,
                    container.getTemperatureGraphs,
                    container.getPopGraphs,
                    container.getPrecipitationGraphs,
                    container.getPrecipitationTotals
                ) as T
            }
        }
    }
}

sealed interface EssentialGraphsState {
    data class Success(
        val tempGraphSummaries: List<TemperatureGraphSummary>,
        val tempGraphs: TemperatureGraphs,
        val popGraphs: List<PopGraph>,
        val precipGraphs: PrecipitationGraphs,
        val precipTotals: List<PrecipitationTotal>
    ) : EssentialGraphsState

    data object Loading : EssentialGraphsState
    data object FailedToDownload : EssentialGraphsState
    data object Outdated : EssentialGraphsState
    data object NoSelectedPlace : EssentialGraphsState
}