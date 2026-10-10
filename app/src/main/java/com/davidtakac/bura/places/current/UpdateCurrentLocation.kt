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

package com.davidtakac.bura.places.current

import com.davidtakac.bura.forecast.cache.ForecastCacher
import com.davidtakac.bura.places.Coordinates
import com.davidtakac.bura.places.selected.SelectedPlaceRepository
import java.time.Duration
import java.time.Instant
import kotlin.math.cos
import kotlin.math.sqrt

// Forecast models have a resolution of a few kilometers, so smaller moves don't change the forecast
private const val MIN_UPDATE_DISTANCE_KM = 15.0
// Limits forecast downloads when traveling fast
private val minUpdateInterval = Duration.ofMinutes(30)
private const val EARTH_RADIUS_KM = 6371.0

class UpdateCurrentLocation(
    private val locator: DeviceLocator,
    private val selectedPlaceRepo: SelectedPlaceRepository,
    private val forecastCacher: ForecastCacher,
) {
    suspend operator fun invoke(now: Instant) {
        val current = selectedPlaceRepo.getCurrentLocation()
        val fix = locator.locate() ?: return
        if (!shouldUpdate(current, fix, now)) return
        selectedPlaceRepo.setCurrentLocation(CurrentLocation(fix, updatedAt = now))
        // The previous location's forecast won't be used again, don't let them pile up while traveling
        if (current != null) forecastCacher.delete(current.coordinates)
    }
}

fun shouldUpdate(current: CurrentLocation?, fix: Coordinates, now: Instant): Boolean {
    if (current == null) return true
    val waitedLongEnough = Duration.between(current.updatedAt, now) >= minUpdateInterval
    val movedFarEnough = current.coordinates.distanceKmTo(fix) >= MIN_UPDATE_DISTANCE_KM
    return waitedLongEnough && movedFarEnough
}

// Equirectangular approximation, accurate enough at the distances that matter here
private fun Coordinates.distanceKmTo(other: Coordinates): Double {
    val x = Math.toRadians(other.longitude - longitude) * cos(Math.toRadians((latitude + other.latitude) / 2))
    val y = Math.toRadians(other.latitude - latitude)
    return sqrt(x * x + y * y) * EARTH_RADIUS_KM
}
