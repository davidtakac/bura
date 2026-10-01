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

package com.davidtakac.bura.places.saved

import com.davidtakac.bura.forecast.cache.ForecastCacher
import com.davidtakac.bura.places.Place

class DeletePlace(
    private val savedPlacesRepository: SavedPlacesRepository,
    private val forecastCacher: ForecastCacher
) {
    suspend operator fun invoke(place: Place) {
        savedPlacesRepository.deletePlace(place)
        forecastCacher.delete(place.location.coordinates)
    }
}