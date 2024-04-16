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

package com.davidtakac.bura.place.saved

import com.davidtakac.bura.common.getStringOrNull
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.place.Location
import com.davidtakac.bura.place.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.ZoneId

class FileSavedPlacesRepository(private val root: File) : SavedPlacesRepository {
    private var memoryCache: MutableList<Place>? = null

    override suspend fun savePlace(place: Place) {
        if (getSavedPlace(place.location.coordinates) != null) return

        val file = File(getDir(), place.location.coordinates.id)
        val json = convertPlaceToJson(place)
        withContext(Dispatchers.IO) { file.writeText(json) }
        memoryCache?.add(place)
    }

    override suspend fun getSavedPlaces(): List<Place> {
        val fromMemory = memoryCache
        if (fromMemory != null) return fromMemory

        val fromFiles = getDir().listFiles()?.map { convertFileToPlace(it) } ?: emptyList()
        this.memoryCache = mutableListOf<Place>().apply { addAll(fromFiles) }
        return fromFiles
    }

    override suspend fun getSavedPlace(coords: Coordinates): Place? =
        getSavedPlaces().firstOrNull { it.location.coordinates == coords }

    override suspend fun deletePlace(place: Place) {
        val file = findPlaceFile(place.location.coordinates) ?: return
        withContext(Dispatchers.IO) { file.delete() }
        memoryCache?.remove(place)
    }

    private suspend fun convertFileToPlace(file: File): Place =
        withContext(Dispatchers.IO) {
            val jsonString = file.readText()
            val record = JSONObject(jsonString)
            Place(
                name = record.getString("name"),
                admin1 = record.getStringOrNull("admin1"),
                countryCode = record.getString("countryCode"),
                countryName = record.getStringOrNull("countryName"),
                location = Location(
                    timeZone = ZoneId.of(record.getString("timeZone")),
                    coordinates = Coordinates(
                        latitude = record.getDouble("latitude"),
                        longitude = record.getDouble("longitude")
                    ),
                )
            )
        }

    private suspend fun convertPlaceToJson(place: Place): String =
        withContext(Dispatchers.Default) {
            JSONObject().apply {
                put("name", place.name)
                put("admin1", place.admin1 ?: JSONObject.NULL)
                put("countryCode", place.countryCode)
                put("countryName", place.countryName ?: JSONObject.NULL)
                put("timeZone", place.location.timeZone.id)
                put("latitude", place.location.coordinates.latitude)
                put("longitude", place.location.coordinates.longitude)
            }.toString()
        }

    private suspend fun findPlaceFile(coords: Coordinates): File? =
        withContext(Dispatchers.IO) {
            val allFiles = getDir().listFiles()
            val targetName = coords.id
            allFiles?.firstOrNull { it.name == targetName }
        }

    private suspend fun getDir(): File =
        withContext(Dispatchers.IO) { File(root, "places").apply { mkdir() } }
}