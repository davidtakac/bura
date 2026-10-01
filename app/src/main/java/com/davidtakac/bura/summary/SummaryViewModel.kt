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
import com.davidtakac.bura.common.util.launchCatching
import com.davidtakac.bura.forecast.ForecastRepository
import com.davidtakac.bura.forecast.units.SelectedUnitsRepository
import com.davidtakac.bura.places.selected.SelectedPlaceRepository
import com.davidtakac.bura.summary.daily.DailySummary
import com.davidtakac.bura.summary.daily.getDailySummary
import com.davidtakac.bura.summary.feelslike.FeelsLikeSummary
import com.davidtakac.bura.summary.feelslike.getFeelsLikeSummary
import com.davidtakac.bura.summary.hourly.HourSummary
import com.davidtakac.bura.summary.hourly.getHourlySummary
import com.davidtakac.bura.summary.humidity.HumiditySummary
import com.davidtakac.bura.summary.humidity.getHumiditySummary
import com.davidtakac.bura.summary.now.NowSummary
import com.davidtakac.bura.summary.now.getNowSummary
import com.davidtakac.bura.summary.precipitation.PrecipitationSummary
import com.davidtakac.bura.summary.precipitation.getPrecipitationSummary
import com.davidtakac.bura.summary.pressure.PressureSummary
import com.davidtakac.bura.summary.pressure.getPressureSummary
import com.davidtakac.bura.summary.sun.SunSummary
import com.davidtakac.bura.summary.sun.getSunSummary
import com.davidtakac.bura.summary.uvindex.UvIndexSummary
import com.davidtakac.bura.summary.uvindex.getUvIndexSummary
import com.davidtakac.bura.summary.visibility.VisibilitySummary
import com.davidtakac.bura.summary.visibility.getVisibilitySummary
import com.davidtakac.bura.summary.wind.WindSummary
import com.davidtakac.bura.summary.wind.getWindSummary
import com.davidtakac.bura.unexpectederror.UnexpectedErrorSetter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class SummaryViewModel(
    private val placeRepo: SelectedPlaceRepository,
    private val unitsRepo: SelectedUnitsRepository,
    private val forecastRepo: ForecastRepository,
    private val unexpectedErrorSetter: UnexpectedErrorSetter
) : ViewModel() {
    private val _state = MutableStateFlow<SummaryState>(SummaryState.Initial)
    val state = _state.asStateFlow()

    fun getSummary() {
        viewModelScope.launchCatching(unexpectedErrorSetter) {
            if (_state.value == SummaryState.Loading) {
                return@launchCatching
            }
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
        val now = Instant.now().atZone(location.timeZone)
        val forecast = forecastRepo.get(location, units) ?: return SummaryState.FailedToDownload

        val nowSummary = getNowSummary(
            now = now,
            tempPeriod = forecast.temperature,
            feelsPeriod = forecast.feelsLike,
            condPeriod = forecast.condition
        ) ?: return SummaryState.Outdated

        val hourlySummary = getHourlySummary(
            now = now,
            tempPeriod = forecast.temperature,
            popPeriod = forecast.pop,
            condPeriod = forecast.condition,
            sunPeriod = forecast.sun
        ) ?: return SummaryState.Outdated

        val dailySummary = getDailySummary(
            now = now,
            tempPeriod = forecast.temperature,
            condPeriod = forecast.condition,
            popPeriod = forecast.pop
        ) ?: return SummaryState.Outdated

        val precipSummary = getPrecipitationSummary(
            now = now,
            precipPeriod = forecast.precipitation
        ) ?: return SummaryState.Outdated

        val uvIndexSummary = getUvIndexSummary(
            now = now,
            uvIndexPeriod = forecast.uvIndex
        ) ?: return SummaryState.Outdated

        val windSummary = getWindSummary(
            now = now,
            windPeriod = forecast.wind,
            gustPeriod = forecast.gust
        ) ?: return SummaryState.Outdated

        val pressureSummary = getPressureSummary(
            now = now,
            pressurePeriod = forecast.pressure
        ) ?: return SummaryState.Outdated

        val humiditySummary = getHumiditySummary(
            now = now,
            humidityPeriod = forecast.humidity,
            dewPointPeriod = forecast.dewPoint
        ) ?: return SummaryState.Outdated

        val visSummary = getVisibilitySummary(
            now = now,
            visPeriod = forecast.visibility
        ) ?: return SummaryState.Outdated

        val sunSummary = getSunSummary(
            now = now,
            sunPeriod = forecast.sun,
            condPeriod = forecast.condition
        ) ?: return SummaryState.Outdated

        val feelsLikeSummary = getFeelsLikeSummary(
            now = now,
            tempPeriod = forecast.temperature,
            feelsPeriod = forecast.feelsLike
        ) ?: return SummaryState.Outdated

        return SummaryState.Success(
            now = nowSummary,
            hourly = hourlySummary,
            daily = dailySummary,
            precip = precipSummary,
            uvIndex = uvIndexSummary,
            wind = windSummary,
            pressure = pressureSummary,
            humidity = humiditySummary,
            vis = visSummary,
            sun = sunSummary,
            feelsLike = feelsLikeSummary,
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
                    container.forecastRepo,
                    container.unexpectedErrorSetter
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
    data object Initial : SummaryState
}