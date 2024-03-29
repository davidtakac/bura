/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.forecast

import com.davidtakac.bura.common.toBooleans
import com.davidtakac.bura.common.toHumidity
import com.davidtakac.bura.common.toInstants
import com.davidtakac.bura.common.toInts
import com.davidtakac.bura.common.toHectopascalJSONArray
import com.davidtakac.bura.common.toMetersJSONArray
import com.davidtakac.bura.common.toDegreesJSONArray
import com.davidtakac.bura.common.toMetersPerSecondJSONArray
import com.davidtakac.bura.common.toIndexJSONArray
import com.davidtakac.bura.common.toSnowMillimetersJSONArray
import com.davidtakac.bura.common.toShowerMillimetersJSONArray
import com.davidtakac.bura.common.toRainMillimetersJSONArray
import com.davidtakac.bura.common.toPopPercentJSONArray
import com.davidtakac.bura.common.toHumidityPercentJSONArray
import com.davidtakac.bura.common.toCelsiusJSONArray
import com.davidtakac.bura.common.toEpochSecondJSONArray
import com.davidtakac.bura.common.toPop
import com.davidtakac.bura.common.toPressures
import com.davidtakac.bura.common.toRain
import com.davidtakac.bura.common.toShowers
import com.davidtakac.bura.common.toSnowfall
import com.davidtakac.bura.common.toTemperatures
import com.davidtakac.bura.common.toUvIndices
import com.davidtakac.bura.common.toVisibilities
import com.davidtakac.bura.common.toWindDirections
import com.davidtakac.bura.common.toWindSpeeds
import com.davidtakac.bura.place.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant

class ForecastDataCacher(private val root: File) {
    private val coordsToData = mutableMapOf<Coordinates, ForecastData?>()

    suspend fun get(coords: Coordinates): ForecastData? {
        val fromMemory = coordsToData[coords]
        if (fromMemory != null) return fromMemory

        val file = findForecastFile(coords) ?: return null
        val fromFile = convertFileToForecastData(file)
        coordsToData[coords] = fromFile
        return fromFile
    }

    suspend fun save(coords: Coordinates, data: ForecastData) {
        val file = findForecastFile(coords) ?: File(getDir(), coords.id)
        val json = convertForecastDataToJson(data)
        withContext(Dispatchers.IO) { file.writeText(json) }
        coordsToData[coords] = data
    }

    suspend fun delete(coords: Coordinates) {
        val file = findForecastFile(coords) ?: return
        withContext(Dispatchers.IO) { file.delete() }
        coordsToData.remove(coords)
    }

    private suspend fun findForecastFile(coords: Coordinates): File? =
        withContext(Dispatchers.IO) {
            val allFiles = getDir().listFiles()
            val targetName = coords.id
            allFiles?.firstOrNull { it.name == targetName }
        }

    private suspend fun convertFileToForecastData(file: File): ForecastData =
        withContext(Dispatchers.IO) {
            val jsonString = file.readText()
            val record = JSONObject(jsonString)
            ForecastData(
                timestamp = Instant.ofEpochSecond(record.getLong("timestamp")),
                times = record.getJSONArray("times").toInstants(),
                temperature = record.getJSONArray("temperature").toTemperatures(),
                feelsLikeTemperature = record.getJSONArray("feelsLike").toTemperatures(),
                dewPointTemperature = record.getJSONArray("dewPoint").toTemperatures(),
                sunrises = record.getJSONArray("sunrises").toInstants(),
                sunsets = record.getJSONArray("sunsets").toInstants(),
                pop = record.getJSONArray("pop").toPop(),
                rain = record.getJSONArray("rain").toRain(),
                showers = record.getJSONArray("showers").toShowers(),
                snow = record.getJSONArray("snow").toSnowfall(),
                uvIndex = record.getJSONArray("uvIndex").toUvIndices(),
                windSpeed = record.getJSONArray("windSpeed").toWindSpeeds(),
                windDirection = record.getJSONArray("windDirection").toWindDirections(),
                gustSpeed = record.getJSONArray("gustSpeed").toWindSpeeds(),
                pressure = record.getJSONArray("pressure").toPressures(),
                visibility = record.getJSONArray("visibility").toVisibilities(),
                humidity = record.getJSONArray("humidity").toHumidity(),
                wmoCode = record.getJSONArray("wmoCode").toInts(),
                isDay = record.getJSONArray("isDay").toBooleans()
            )
        }

    private suspend fun convertForecastDataToJson(data: ForecastData): String =
        withContext(Dispatchers.Default) {
            JSONObject().apply {
                put("timestamp", data.timestamp.epochSecond)
                put("times", data.times.toEpochSecondJSONArray())
                put("temperature", data.temperature.toCelsiusJSONArray())
                put("feelsLike", data.feelsLikeTemperature.toCelsiusJSONArray())
                put("dewPoint", data.dewPointTemperature.toCelsiusJSONArray())
                put("sunrises", data.sunrises.toEpochSecondJSONArray())
                put("sunsets", data.sunsets.toEpochSecondJSONArray())
                put("pop", data.pop.toPopPercentJSONArray())
                put("rain", data.rain.toRainMillimetersJSONArray())
                put("showers", data.showers.toShowerMillimetersJSONArray())
                put("snow", data.snow.toSnowMillimetersJSONArray())
                put("uvIndex", data.uvIndex.toIndexJSONArray())
                put("windSpeed", data.windSpeed.toMetersPerSecondJSONArray())
                put("windDirection", data.windDirection.toDegreesJSONArray())
                put("gustSpeed", data.gustSpeed.toMetersPerSecondJSONArray())
                put("pressure", data.pressure.toHectopascalJSONArray())
                put("visibility", data.visibility.toMetersJSONArray())
                put("humidity", data.humidity.toHumidityPercentJSONArray())
                put("wmoCode", JSONArray(data.wmoCode))
                put("isDay", JSONArray(data.isDay))
            }.toString()
        }

    private suspend fun getDir(): File =
        withContext(Dispatchers.IO) { File(root, "forecasts").apply { mkdir() } }
}