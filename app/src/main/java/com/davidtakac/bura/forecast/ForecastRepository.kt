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

package com.davidtakac.bura.forecast

import com.davidtakac.bura.forecast.cache.CachedForecast
import com.davidtakac.bura.forecast.cache.ForecastCacher
import com.davidtakac.bura.forecast.download.ForecastDownloader
import com.davidtakac.bura.forecast.download.InternetChecker
import com.davidtakac.bura.forecast.units.Units
import com.davidtakac.bura.places.Coordinates
import com.davidtakac.bura.places.Location
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant

class ForecastRepository(
    private val cacher: ForecastCacher,
    private val downloader: ForecastDownloader,
    private val internetChecker: InternetChecker
) {
    private val coordsToMutex = mutableMapOf<Coordinates, Mutex>()

    suspend fun get(
        location: Location,
        units: Units,
        updateFrequency: UpdateFrequency = UpdateFrequency.Regular
    ): Forecast? =
        coordsToMutex.getOrPut(location.coordinates, defaultValue = { Mutex() }).withLock {
            val cached = cacher.get(location.coordinates)
            if (shouldUpdate(cached, updateFrequency)) {
                val downloadedForecast = downloader.get(location)
                if (downloadedForecast != null) {
                    cacher.save(location.coordinates, downloadedForecast)
                    downloadedForecast
                } else {
                    cached.forecastOrNull
                }
            } else {
                cached.forecastOrNull
            }
        }?.convertTo(units)

    private fun shouldUpdate(cached: CachedForecast, updateFrequency: UpdateFrequency): Boolean {
        if (updateFrequency == UpdateFrequency.Never) return false
        if (cached == CachedForecast.UnsupportedFormat) return true
        if (cached == CachedForecast.Miss) return true

        val cacheTimestamp = (cached as CachedForecast.Hit).forecast.timestamp
        val cacheExpiresAfter = (updateFrequency as UpdateFrequency.Regular).expiresAfter
        val cacheOldEnough = Duration.between(cacheTimestamp, Instant.now()) >= cacheExpiresAfter
        return cacheOldEnough && internetChecker.hasInternet()
    }
}

sealed interface UpdateFrequency {
    data object Regular : UpdateFrequency {
        val expiresAfter: Duration = Duration.ofHours(1)
    }
    data object Never : UpdateFrequency
}
