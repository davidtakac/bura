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

import com.davidtakac.bura.forecast.cache.ForecastCacher
import com.davidtakac.bura.forecast.download.ForecastDownloader
import com.davidtakac.bura.forecast.download.InternetChecker
import com.davidtakac.bura.places.Coordinates
import com.davidtakac.bura.forecast.units.Units
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
        updateFrequency: UpdateFrequency = UpdateFrequency.App
    ): Forecast? =
        coordsToMutex.getOrPut(location.coordinates, defaultValue = { Mutex() }).withLock {
            val cached = cacher.get(location.coordinates)
            if (shouldUpdate(cached, updateFrequency)) {
                val downloaded = downloader.get(location)
                if (downloaded != null) {
                    cacher.save(location.coordinates, downloaded)
                    downloaded
                } else {
                    cached
                }
            } else {
                cached
            }
        }?.convertTo(units)

    private fun shouldUpdate(cached: Forecast?, updateFrequency: UpdateFrequency): Boolean {
        val expiresAfter = updateFrequency.expiresAfter ?: return false
        val shouldUpdate = cached == null
                || Duration.between(
                        cached.timestamp,
                        Instant.now()
                ) >= expiresAfter
        return shouldUpdate && internetChecker.hasInternet()
    }
}

enum class UpdateFrequency(val expiresAfter: Duration?) {
    App(expiresAfter = Duration.ofHours(1)),
    Never(expiresAfter = null)
}