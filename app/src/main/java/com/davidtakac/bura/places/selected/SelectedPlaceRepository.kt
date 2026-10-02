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

package com.davidtakac.bura.places.selected

import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidtakac.bura.places.Coordinates
import com.davidtakac.bura.places.Location
import com.davidtakac.bura.places.Place
import com.davidtakac.bura.places.current.CurrentLocation
import com.davidtakac.bura.places.saved.SavedPlacesRepository
import java.time.Instant
import java.time.ZoneId

private const val SELECTED_PLACE_KEY = "selected_place_coords"
private const val CURRENT_LOCATION_KEY = "current_location_coords"
private const val CURRENT_LOCATION_UPDATED_AT_KEY = "current_location_updated_at"
// Stored as the selected place instead of coordinates when following the device location
private const val CURRENT_LOCATION = "current_location"

class SelectedPlaceRepository(
    private val prefs: SharedPreferences,
    private val savedPlacesRepository: SavedPlacesRepository
) {
    suspend fun selectPlace(place: Place) =
        prefs.edit { putString(SELECTED_PLACE_KEY, place.location.coordinates.id) }

    suspend fun selectCurrentLocation() =
        prefs.edit { putString(SELECTED_PLACE_KEY, CURRENT_LOCATION) }

    suspend fun isCurrentLocationSelected(): Boolean =
        prefs.getString(SELECTED_PLACE_KEY, null) == CURRENT_LOCATION

    suspend fun getCurrentLocation(): CurrentLocation? {
        val coords = prefs.getString(CURRENT_LOCATION_KEY, null)?.let(Coordinates::fromId) ?: return null
        val updatedAt = Instant.ofEpochSecond(prefs.getLong(CURRENT_LOCATION_UPDATED_AT_KEY, 0))
        return CurrentLocation(coords, updatedAt)
    }

    suspend fun setCurrentLocation(location: CurrentLocation) =
        prefs.edit {
            putString(CURRENT_LOCATION_KEY, location.coordinates.id)
            putLong(CURRENT_LOCATION_UPDATED_AT_KEY, location.updatedAt.epochSecond)
        }

    suspend fun getSelectedPlace(): Place? {
        val selected = prefs.getString(SELECTED_PLACE_KEY, null) ?: return null
        if (selected == CURRENT_LOCATION) return getCurrentLocation()?.coordinates?.let(::currentLocationPlace)
        return savedPlacesRepository.getSavedPlace(Coordinates.fromId(selected))
    }
}

// Unnamed because the UI labels it. The device's time zone is where the user is right now.
private fun currentLocationPlace(coords: Coordinates) = Place(
    name = "",
    admin1 = null,
    admin2 = null,
    admin3 = null,
    admin4 = null,
    countryCode = null,
    countryName = null,
    location = Location(timeZone = ZoneId.systemDefault(), coordinates = coords)
)
