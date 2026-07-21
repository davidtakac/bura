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
import com.davidtakac.bura.common.util.launchCatching
import com.davidtakac.bura.forecast.ForecastRepository
import com.davidtakac.bura.forecast.units.SelectedUnitsRepository
import com.davidtakac.bura.graphs.pop.PopGraphs
import com.davidtakac.bura.graphs.pop.getPopGraphs
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphs
import com.davidtakac.bura.graphs.precipitation.PrecipitationTotal
import com.davidtakac.bura.graphs.precipitation.getPrecipitationGraphs
import com.davidtakac.bura.graphs.precipitation.getPrecipitationTotals
import com.davidtakac.bura.graphs.temperature.TemperatureGraphSummary
import com.davidtakac.bura.graphs.temperature.TemperatureGraphs
import com.davidtakac.bura.graphs.temperature.getTemperatureGraphSummaries
import com.davidtakac.bura.graphs.temperature.getTemperatureGraphs
import com.davidtakac.bura.places.selected.SelectedPlaceRepository
import com.davidtakac.bura.unexpectederror.UnexpectedErrorSetter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class EssentialGraphsViewModel(
    private val placeRepo: SelectedPlaceRepository,
    private val unitsRepo: SelectedUnitsRepository,
    private val forecastRepo: ForecastRepository,
    private val unexpectedErrorSetter: UnexpectedErrorSetter
) : ViewModel() {
    private val _state = MutableStateFlow<EssentialGraphsState>(EssentialGraphsState.Initial)
    val state = _state.asStateFlow()

    fun getGraphs() {
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            if (_state.value == EssentialGraphsState.Loading) {
                return@launchCatching
            }
            if (_state.value !is EssentialGraphsState.Success) {
                _state.value = EssentialGraphsState.Loading
            }
            _state.value = getState()
        }
    }

    private suspend fun getState(): EssentialGraphsState {
        val location = placeRepo.getSelectedPlace()?.location ?: return EssentialGraphsState.NoSelectedPlace
        val units = unitsRepo.getSelectedUnits()
        val now = Instant.now().atZone(location.timeZone)
        val forecast = forecastRepo.get(location, units) ?: return EssentialGraphsState.FailedToDownload

        val tempGraphSummaries = getTemperatureGraphSummaries(
            now = now,
            tempPeriod = forecast.temperature,
            feelsPeriod = forecast.feelsLike,
            condPeriod = forecast.condition
        ) ?: return EssentialGraphsState.Outdated

        val tempGraphs = getTemperatureGraphs(
            now = now,
            tempPeriod = forecast.temperature,
            condPeriod = forecast.condition
        ) ?: return EssentialGraphsState.Outdated

        val popGraphs = getPopGraphs(
            now = now,
            popPeriod = forecast.pop,
            conditionPeriod = forecast.condition
        ) ?: return EssentialGraphsState.Outdated

        val precipGraphs = getPrecipitationGraphs(
            now = now,
            precipPeriod = forecast.precipitation,
            condPeriod = forecast.condition
        ) ?: return EssentialGraphsState.Outdated

        val precipTotals = getPrecipitationTotals(
            now = now,
            precipPeriod = forecast.precipitation
        ) ?: return EssentialGraphsState.Outdated

        return EssentialGraphsState.Success(
            tempGraphSummaries = tempGraphSummaries,
            tempGraphs = tempGraphs,
            popGraphs = popGraphs,
            precipGraphs = precipGraphs,
            precipTotals = precipTotals
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
                    container.forecastRepo,
                    container.unexpectedErrorSetter
                ) as T
            }
        }
    }
}

sealed interface EssentialGraphsState {
    data class Success(
        val tempGraphSummaries: List<TemperatureGraphSummary>,
        val tempGraphs: TemperatureGraphs,
        val popGraphs: PopGraphs,
        val precipGraphs: PrecipitationGraphs,
        val precipTotals: List<PrecipitationTotal>
    ) : EssentialGraphsState

    data object Loading : EssentialGraphsState
    data object FailedToDownload : EssentialGraphsState
    data object Outdated : EssentialGraphsState
    data object NoSelectedPlace : EssentialGraphsState
    data object Initial : EssentialGraphsState
}