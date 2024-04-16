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

import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.place.Place
import com.davidtakac.bura.place.saved.SavedPlacesRepository

class FakeSavedPlacesRepository(private val cannedPlaces: List<Place>) : SavedPlacesRepository {
    override suspend fun getSavedPlaces(): List<Place> = cannedPlaces

    override suspend fun savePlace(place: Place) {
        // no-op
    }

    override suspend fun getSavedPlace(coords: Coordinates): Place? =
        cannedPlaces.firstOrNull { it.location.coordinates == coords }

    override suspend fun deletePlace(place: Place) {
        // no-op
    }
}