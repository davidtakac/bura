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
import com.davidtakac.bura.common.util.getAppVersionName
import com.davidtakac.bura.common.util.getUserAgent
import com.davidtakac.bura.forecast.ForecastRepository
import com.davidtakac.bura.forecast.cache.ForecastCacher
import com.davidtakac.bura.forecast.download.ForecastDownloader
import com.davidtakac.bura.forecast.download.InternetChecker
import com.davidtakac.bura.forecast.units.SelectedUnitsRepository
import com.davidtakac.bura.places.saved.DeletePlace
import com.davidtakac.bura.places.saved.GetSavedPlaces
import com.davidtakac.bura.places.saved.SavedPlacesRepository
import com.davidtakac.bura.places.search.SearchPlaces
import com.davidtakac.bura.places.selected.SelectPlace
import com.davidtakac.bura.places.selected.SelectedPlaceRepository
import com.davidtakac.bura.unexpectederror.UnexpectedErrorConsumer
import com.davidtakac.bura.unexpectederror.UnexpectedErrorRepository
import com.davidtakac.bura.unexpectederror.UnexpectedErrorSetter

class AppContainer(private val appContext: Context) {
    val prefs: SharedPreferences get() = appContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    private val root get() = appContext.filesDir
    private val userAgent: String get() = getUserAgent(appContext)

    private val forecastCacher by lazy {
        ForecastCacher(
            root = root,
            appVersionName = getAppVersionName(appContext),
        )
    }

    private val unexpectedErrorRepository by lazy { UnexpectedErrorRepository() }
    val unexpectedErrorSetter: UnexpectedErrorSetter = unexpectedErrorRepository
    val unexpectedErrorConsumer: UnexpectedErrorConsumer = unexpectedErrorRepository

    val forecastRepo by lazy {
        ForecastRepository(
            cacher = forecastCacher,
            downloader = ForecastDownloader(userAgent),
            internetChecker = InternetChecker(appContext)
        )
    }

    val selectedPlaceRepo by lazy { SelectedPlaceRepository(prefs, savedPlacesRepo) }
    val selectedUnitsRepo by lazy { SelectedUnitsRepository(prefs) }

    private val savedPlacesRepo by lazy { SavedPlacesRepository(root) }
    val getSavedPlaces get() = GetSavedPlaces(selectedUnitsRepo, selectedPlaceRepo, savedPlacesRepo, forecastRepo)
    val searchPlaces get() = SearchPlaces(userAgent)
    val selectPlace get() = SelectPlace(selectedPlaceRepo, savedPlacesRepo)
    val deletePlace get() = DeletePlace(savedPlacesRepo, forecastCacher)
}