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

package com.davidtakac.bura.forecast.cache

import com.davidtakac.bura.common.util.getStringOrNull
import com.davidtakac.bura.forecast.Forecast
import com.davidtakac.bura.places.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class ForecastCacher(
    private val root: File,
    private val appVersionName: String,
) {
    private val coordsToData = mutableMapOf<Coordinates, Forecast?>()

    suspend fun get(coords: Coordinates): Forecast? {
        val fromMemory = coordsToData[coords]
        if (fromMemory != null) return fromMemory

        val fromFile = findForecastFile(coords)
            ?.let { fileToForecast(it) }
            ?: return null
        coordsToData[coords] = fromFile

        return fromFile
    }

    suspend fun save(coords: Coordinates, data: Forecast) {
        val file = findForecastFile(coords) ?: File(getDir(), coords.id)
        val jsonString = forecastToJsonString(data)
        withContext(Dispatchers.IO) {
            file.writeText(jsonString)
        }
        coordsToData[coords] = data
    }

    suspend fun delete(coords: Coordinates) {
        val file = findForecastFile(coords) ?: return
        withContext(Dispatchers.IO) {
            file.delete()
        }
        coordsToData.remove(coords)
    }

    private suspend fun findForecastFile(coords: Coordinates): File? =
        withContext(Dispatchers.IO) {
            getDir().listFiles()
        }?.firstOrNull { it.name == coords.id }

    private suspend fun fileToForecast(file: File): Forecast? {
        val json = JSONObject(
            withContext(Dispatchers.IO) {
                file.readText()
            }
        )
        return if (json.getStringOrNull(CacheJsonSerialNames.APP_VERSION_NAME) == null) {
            null
        } else {
            convertCacheJsonToForecast(json)
        }
    }

    private suspend fun forecastToJsonString(data: Forecast): String =
        withContext(Dispatchers.Default) {
            convertForecastToCacheJson(data).apply {
                put(CacheJsonSerialNames.APP_VERSION_NAME, appVersionName)
            }.toString()
        }

    private suspend fun getDir(): File =
        withContext(Dispatchers.IO) {
            File(root, "forecasts").apply {
                mkdir()
            }
        }
}