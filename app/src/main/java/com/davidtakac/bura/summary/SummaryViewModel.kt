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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.davidtakac.bura.App
import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.place.selected.SelectedPlaceRepository
import com.davidtakac.bura.summary.daily.DailySummary
import com.davidtakac.bura.summary.daily.GetDailySummary
import com.davidtakac.bura.summary.feelslike.FeelsLikeSummary
import com.davidtakac.bura.summary.feelslike.GetFeelsLikeSummary
import com.davidtakac.bura.summary.hourly.HourSummary
import com.davidtakac.bura.summary.hourly.GetHourlySummary
import com.davidtakac.bura.summary.humidity.HumiditySummary
import com.davidtakac.bura.summary.humidity.GetHumiditySummary
import com.davidtakac.bura.summary.now.NowSummary
import com.davidtakac.bura.summary.now.GetNowSummary
import com.davidtakac.bura.summary.precipitation.PrecipitationSummary
import com.davidtakac.bura.summary.precipitation.GetPrecipitationSummary
import com.davidtakac.bura.summary.pressure.PressureSummary
import com.davidtakac.bura.summary.pressure.GetPressureSummary
import com.davidtakac.bura.summary.sun.SunSummary
import com.davidtakac.bura.summary.sun.GetSunSummary
import com.davidtakac.bura.summary.uvindex.UvIndexSummary
import com.davidtakac.bura.summary.uvindex.GetUvIndexSummary
import com.davidtakac.bura.summary.visibility.VisibilitySummary
import com.davidtakac.bura.summary.visibility.GetVisibilitySummary
import com.davidtakac.bura.summary.wind.WindSummary
import com.davidtakac.bura.summary.wind.GetWindSummary
import com.davidtakac.bura.units.SelectedUnitsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class SummaryViewModel(
    private val placeRepo: SelectedPlaceRepository,
    private val unitsRepo: SelectedUnitsRepository,
    private val getNowSummary: GetNowSummary,
    private val getHourlySummary: GetHourlySummary,
    private val getDailySummary: GetDailySummary,
    private val precipSummaryUseCase: GetPrecipitationSummary,
    private val getUvIndexSummary: GetUvIndexSummary,
    private val getWindSummary: GetWindSummary,
    private val getPressureSummary: GetPressureSummary,
    private val getHumiditySummary: GetHumiditySummary,
    private val visSummaryUseCase: GetVisibilitySummary,
    private val getSunSummary: GetSunSummary,
    private val getFeelsLikeSummary: GetFeelsLikeSummary
) : ViewModel() {
    private val _state = MutableStateFlow<SummaryState>(SummaryState.Loading)
    val state = _state.asStateFlow()

    fun getSummary() {
        viewModelScope.launch {
            if (_state.value !is SummaryState.Success) {
                _state.value = SummaryState.Loading
            }
            _state.value = getState()
        }
    }
    
    private suspend fun getState(): SummaryState {
        val location = placeRepo.getSelectedPlace()?.location ?: return SummaryState.NoSelectedPlace
        val coords = location.coordinates
        val units = unitsRepo.getSelectedUnits()
        val now = Instant.now().atZone(location.timeZone).toLocalDateTime()

        val nowSummary = getNowSummary(coords, units, now)
        when (nowSummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val hourlySummary = getHourlySummary(coords, units, now)
        when (hourlySummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val dailySummary = getDailySummary(coords, units, now)
        when (dailySummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val precipSummary = precipSummaryUseCase(coords, units, now)
        when (precipSummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val uvIndexSummary = getUvIndexSummary(coords, units, now)
        when (uvIndexSummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val windSummary = getWindSummary(coords, units, now)
        when (windSummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val pressureSummary = getPressureSummary(coords, units, now)
        when (pressureSummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val humiditySummary = getHumiditySummary(coords, units, now)
        when (humiditySummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val visSummary = visSummaryUseCase(coords, units, now)
        when (visSummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val sunSummary = getSunSummary(coords, units, now)
        when (sunSummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        val feelsLikeSummary = getFeelsLikeSummary(coords, units, now)
        when (feelsLikeSummary) {
            ForecastResult.FailedToDownload -> return SummaryState.FailedToDownload
            ForecastResult.Outdated -> return SummaryState.Outdated
            is ForecastResult.Success -> Unit
        }

        return SummaryState.Success(
            now = nowSummary.data,
            hourly = hourlySummary.data,
            daily = dailySummary.data,
            precip = precipSummary.data,
            uvIndex = uvIndexSummary.data,
            wind = windSummary.data,
            pressure = pressureSummary.data,
            humidity = humiditySummary.data,
            vis = visSummary.data,
            sun = sunSummary.data,
            feelsLike = feelsLikeSummary.data
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return SummaryViewModel(
                    container.selectedPlaceRepo,
                    container.selectedUnitsRepo,
                    container.getNowSummary,
                    container.getHourlySummary,
                    container.getDailySummary,
                    container.getPrecipitationSummary,
                    container.getUvIndexSummary,
                    container.getWindSummary,
                    container.getPressureSummary,
                    container.getHumiditySummary,
                    container.getVisibilitySummary,
                    container.getSunSummary,
                    container.getFeelsLikeSummary
                ) as T
            }
        }
    }
}

sealed interface SummaryState {
    data class Success(
        val now: NowSummary,
        val hourly: List<HourSummary>,
        val daily: DailySummary,
        val precip: PrecipitationSummary,
        val uvIndex: UvIndexSummary,
        val wind: WindSummary,
        val pressure: PressureSummary,
        val humidity: HumiditySummary,
        val vis: VisibilitySummary,
        val sun: SunSummary,
        val feelsLike: FeelsLikeSummary
    ) : SummaryState

    data object Loading : SummaryState
    data object FailedToDownload : SummaryState
    data object Outdated : SummaryState
    data object NoSelectedPlace : SummaryState
}