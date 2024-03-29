/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.place.saved

import com.davidtakac.bura.forecast.ForecastDataCacher
import com.davidtakac.bura.place.Place

class DeletePlace(
    private val savedPlacesRepository: SavedPlacesRepository,
    private val forecastDataCacher: ForecastDataCacher
) {
    suspend operator fun invoke(place: Place) {
        savedPlacesRepository.deletePlace(place)
        forecastDataCacher.delete(place.location.coordinates)
    }
}