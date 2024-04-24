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

package com.davidtakac.bura

import android.content.Context
import android.content.SharedPreferences
import com.davidtakac.bura.common.UserAgentProvider
import com.davidtakac.bura.condition.ConditionRepository
import com.davidtakac.bura.condition.EagerConditionRepository
import com.davidtakac.bura.condition.StaticConditionRepository
import com.davidtakac.bura.forecast.ForecastConverter
import com.davidtakac.bura.forecast.ForecastDataCacher
import com.davidtakac.bura.forecast.ForecastDataDownloader
import com.davidtakac.bura.forecast.ForecastRepository
import com.davidtakac.bura.graphs.pop.GetPopGraphs
import com.davidtakac.bura.graphs.precipitation.GetPrecipitationGraphs
import com.davidtakac.bura.graphs.precipitation.GetPrecipitationTotals
import com.davidtakac.bura.graphs.temperature.GetTemperatureGraphSummaries
import com.davidtakac.bura.graphs.temperature.GetTemperatureGraphs
import com.davidtakac.bura.graphs.uvindex.GetUvIndexGraphs
import com.davidtakac.bura.gust.EagerGustRepository
import com.davidtakac.bura.gust.GustRepository
import com.davidtakac.bura.humidity.EagerHumidityRepository
import com.davidtakac.bura.humidity.HumidityRepository
import com.davidtakac.bura.place.saved.DeletePlace
import com.davidtakac.bura.place.saved.FileSavedPlacesRepository
import com.davidtakac.bura.place.saved.GetSavedPlaces
import com.davidtakac.bura.place.saved.SavedPlacesRepository
import com.davidtakac.bura.place.search.SearchPlaces
import com.davidtakac.bura.place.selected.PrefsSelectedPlaceRepository
import com.davidtakac.bura.place.selected.SelectPlace
import com.davidtakac.bura.place.selected.SelectedPlaceRepository
import com.davidtakac.bura.pop.EagerPopRepository
import com.davidtakac.bura.pop.PopRepository
import com.davidtakac.bura.precipitation.EagerPrecipitationRepository
import com.davidtakac.bura.precipitation.PrecipitationRepository
import com.davidtakac.bura.pressure.EagerPressureRepository
import com.davidtakac.bura.pressure.PressureRepository
import com.davidtakac.bura.summary.daily.GetDailySummary
import com.davidtakac.bura.summary.feelslike.GetFeelsLikeSummary
import com.davidtakac.bura.summary.hourly.GetHourlySummary
import com.davidtakac.bura.summary.humidity.GetHumiditySummary
import com.davidtakac.bura.summary.now.GetNowSummary
import com.davidtakac.bura.summary.precipitation.GetPrecipitationSummary
import com.davidtakac.bura.summary.pressure.GetPressureSummary
import com.davidtakac.bura.summary.sun.GetSunSummary
import com.davidtakac.bura.summary.uvindex.GetUvIndexSummary
import com.davidtakac.bura.summary.visibility.GetVisibilitySummary
import com.davidtakac.bura.summary.wind.GetWindSummary
import com.davidtakac.bura.sun.EagerSunRepository
import com.davidtakac.bura.sun.SunRepository
import com.davidtakac.bura.temperature.EagerDewPointRepository
import com.davidtakac.bura.temperature.EagerFeelsLikeRepository
import com.davidtakac.bura.temperature.EagerTemperatureRepository
import com.davidtakac.bura.temperature.StaticTemperatureRepository
import com.davidtakac.bura.temperature.TemperatureRepository
import com.davidtakac.bura.units.PrefsSelectedUnitsRepository
import com.davidtakac.bura.units.SelectedUnitsRepository
import com.davidtakac.bura.uvindex.EagerUvIndexRepository
import com.davidtakac.bura.uvindex.UvIndexRepository
import com.davidtakac.bura.visibility.EagerVisibilityRepository
import com.davidtakac.bura.visibility.VisibilityRepository
import com.davidtakac.bura.wind.EagerWindRepository
import com.davidtakac.bura.wind.WindRepository

class AppContainer(private val appContext: Context) {
    val prefs: SharedPreferences get() = appContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    private val root get() = appContext.filesDir
    private val userAgentProvider get() = UserAgentProvider(appContext)

    private val forecastCacher by lazy { ForecastDataCacher(root) }
    private val forecastRepo by lazy {
        ForecastRepository(
            cacher = forecastCacher,
            downloader = ForecastDataDownloader(userAgentProvider),
            converter = ForecastConverter()
        )
    }

    private val tempRepo: TemperatureRepository get() = EagerTemperatureRepository(forecastRepo)
    private val feelsRepo: TemperatureRepository get() = EagerFeelsLikeRepository(forecastRepo)
    private val conditionRepo: ConditionRepository get() = EagerConditionRepository(forecastRepo)
    private val sunRepo: SunRepository get() = EagerSunRepository(forecastRepo)
    private val popRepo: PopRepository get() = EagerPopRepository(forecastRepo)
    private val precipRepo: PrecipitationRepository get() = EagerPrecipitationRepository(forecastRepo)
    private val uvIndexRepo: UvIndexRepository get() = EagerUvIndexRepository(forecastRepo)
    private val windRepo: WindRepository get() = EagerWindRepository(forecastRepo)
    private val gustRepo: GustRepository get() = EagerGustRepository(forecastRepo)
    private val pressureRepo: PressureRepository get() = EagerPressureRepository(forecastRepo)
    private val humidityRepo: HumidityRepository get() = EagerHumidityRepository(forecastRepo)
    private val dewPointRepo: TemperatureRepository get() = EagerDewPointRepository(forecastRepo)
    private val visibilityRepo: VisibilityRepository get() = EagerVisibilityRepository(forecastRepo)

    private val staticTempRepo: TemperatureRepository get() = StaticTemperatureRepository(forecastRepo)
    private val staticConditionRepo: ConditionRepository get() = StaticConditionRepository(forecastRepo)

    val selectedPlaceRepo: SelectedPlaceRepository by lazy { PrefsSelectedPlaceRepository(prefs, savedPlacesRepo) }
    val selectedUnitsRepo: SelectedUnitsRepository by lazy { PrefsSelectedUnitsRepository(prefs) }

    val getNowSummary get() = GetNowSummary(tempRepo, feelsRepo, conditionRepo)
    val getHourlySummary get() = GetHourlySummary(tempRepo, popRepo, conditionRepo, sunRepo)
    val getDailySummary get() = GetDailySummary(tempRepo, conditionRepo, popRepo)
    val getPrecipitationSummary get() = GetPrecipitationSummary(precipRepo)
    val getUvIndexSummary get() = GetUvIndexSummary(uvIndexRepo)
    val getWindSummary get() = GetWindSummary(windRepo, gustRepo)
    val getSunSummary get() = GetSunSummary(sunRepo, conditionRepo)
    val getPressureSummary get() = GetPressureSummary(pressureRepo)
    val getHumiditySummary get() = GetHumiditySummary(humidityRepo, dewPointRepo)
    val getFeelsLikeSummary get() = GetFeelsLikeSummary(tempRepo, feelsRepo)
    val getVisibilitySummary get() = GetVisibilitySummary(visibilityRepo)

    val getTemperatureGraphs get() = GetTemperatureGraphs(tempRepo, conditionRepo)
    val getPopGraphs get() = GetPopGraphs(popRepo)
    val getPrecipitationTotals get() = GetPrecipitationTotals(precipRepo)
    val getTemperatureGraphSummaries get() = GetTemperatureGraphSummaries(tempRepo, conditionRepo, feelsRepo)
    val getPrecipitationGraphs get() = GetPrecipitationGraphs(precipRepo, conditionRepo)
    val getUvIndexGraphs get() = GetUvIndexGraphs(uvIndexRepo)

    private val savedPlacesRepo: SavedPlacesRepository by lazy { FileSavedPlacesRepository(root) }
    val getSavedPlaces get() = GetSavedPlaces(savedPlacesRepo, staticTempRepo, staticConditionRepo)
    val searchPlaces get() = SearchPlaces(userAgentProvider)
    val selectPlace get() = SelectPlace(selectedPlaceRepo, savedPlacesRepo)
    val deletePlace get() = DeletePlace(savedPlacesRepo, forecastCacher)
}