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

import com.davidtakac.bura.common.util.getLongOrNull
import com.davidtakac.bura.forecast.Forecast
import com.davidtakac.bura.places.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

private const val cacheVersion = 1

class ForecastCacher(private val root: File) {
    private val memoryCache = mutableMapOf<Coordinates, Forecast?>()

    suspend fun get(coords: Coordinates): CachedForecast {
        memoryCache[coords]?.let { return CachedForecast.Hit(it) }

        val forecastFile = findForecastFile(coords) ?: return CachedForecast.Miss
        val forecastFromFile = fileToForecast(forecastFile) ?: return CachedForecast.UnsupportedFormat
        memoryCache[coords] = forecastFromFile

        return CachedForecast.Hit(forecastFromFile)
    }

    suspend fun save(coords: Coordinates, data: Forecast) {
        val file = findForecastFile(coords) ?: File(getDir(), coords.id)
        val jsonString = forecastToJsonString(data)
        withContext(Dispatchers.IO) {
            file.writeText(jsonString)
        }
        memoryCache[coords] = data
    }

    suspend fun delete(coords: Coordinates) {
        val file = findForecastFile(coords) ?: return
        withContext(Dispatchers.IO) {
            file.delete()
        }
        memoryCache.remove(coords)
    }

    private suspend fun findForecastFile(coords: Coordinates): File? =
        withContext(Dispatchers.IO) {
            getDir().listFiles()
        }?.firstOrNull { it.name == coords.id }

    private suspend fun fileToForecast(file: File): Forecast? {
        val cacheJson = JSONObject(withContext(Dispatchers.IO) { file.readText() })
        val cacheVersion = cacheJson.getLongOrNull(CacheJsonSerialNames.CACHE_VERSION)
        return if (canParseCache(cacheVersion)) convertCacheJsonToForecast(cacheJson) else null
    }

    private suspend fun forecastToJsonString(data: Forecast): String =
        withContext(Dispatchers.Default) {
            convertForecastToCacheJson(data).apply {
                put(CacheJsonSerialNames.CACHE_VERSION, cacheVersion)
            }.toString()
        }

    private suspend fun getDir(): File =
        withContext(Dispatchers.IO) {
            File(root, "forecasts").apply {
                mkdir()
            }
        }

    private fun canParseCache(version: Long?): Boolean =
        when {
            version == null -> false
            else -> true
        }
}

sealed interface CachedForecast {
    object Miss : CachedForecast
    object UnsupportedFormat : CachedForecast
    data class Hit(val forecast: Forecast) : CachedForecast

    val forecastOrNull: Forecast?
        get() = (this as? Hit)?.forecast
}
