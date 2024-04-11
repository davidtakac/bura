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

import com.davidtakac.bura.common.mapToJSONArray
import com.davidtakac.bura.common.mapToList
import com.davidtakac.bura.humidity.Humidity
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.uvindex.UvIndex
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.wind.WindDirection
import com.davidtakac.bura.wind.WindSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.LocalDateTime

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
                times = record.getJSONArray("times").mapToList(LocalDateTime::parse),
                temperature = record.getJSONArray("temperature").mapToList { Temperature.fromDegreesCelsius(it.toDouble()) },
                feelsLikeTemperature = record.getJSONArray("feelsLike").mapToList { Temperature.fromDegreesCelsius(it.toDouble()) },
                dewPointTemperature = record.getJSONArray("dewPoint").mapToList { Temperature.fromDegreesCelsius(it.toDouble()) },
                sunrises = record.getJSONArray("sunrises").mapToList(LocalDateTime::parse),
                sunsets = record.getJSONArray("sunsets").mapToList(LocalDateTime::parse),
                pop = record.getJSONArray("pop").mapToList { Pop(it.toDouble()) },
                rain = record.getJSONArray("rain").mapToList { Rain.fromMillimeters(it.toDouble()) },
                showers = record.getJSONArray("showers").mapToList { Showers.fromMillimeters(it.toDouble()) },
                snow = record.getJSONArray("snow").mapToList { Snow.fromMillimeters(it.toDouble()) },
                uvIndex = record.getJSONArray("uvIndex").mapToList { UvIndex(it.toInt()) },
                windSpeed = record.getJSONArray("windSpeed").mapToList { WindSpeed.fromMetersPerSecond(it.toDouble()) },
                windDirection = record.getJSONArray("windDirection").mapToList { WindDirection(it.toDouble()) },
                gustSpeed = record.getJSONArray("gustSpeed").mapToList { WindSpeed.fromMetersPerSecond(it.toDouble()) },
                pressure = record.getJSONArray("pressure").mapToList { Pressure.fromHectopascal(it.toDouble()) },
                visibility = record.getJSONArray("visibility").mapToList { Visibility.fromMeters(it.toDouble()) },
                humidity = record.getJSONArray("humidity").mapToList { Humidity(it.toDouble()) },
                wmoCode = record.getJSONArray("wmoCode").mapToList(String::toInt),
                isDay = record.getJSONArray("isDay").mapToList(String::toBoolean)
            )
        }

    private suspend fun convertForecastDataToJson(data: ForecastData): String =
        withContext(Dispatchers.Default) {
            JSONObject().apply {
                put("timestamp", data.timestamp.epochSecond)
                put("times", data.times.mapToJSONArray { it.toString() })
                put("temperature", data.temperature.mapToJSONArray { it.convertTo(Temperature.Unit.DegreesCelsius).value })
                put("feelsLike", data.feelsLikeTemperature.mapToJSONArray { it.convertTo(Temperature.Unit.DegreesCelsius).value })
                put("dewPoint", data.dewPointTemperature.mapToJSONArray { it.convertTo(Temperature.Unit.DegreesCelsius).value })
                put("sunrises", data.sunrises.mapToJSONArray { it.toString() })
                put("sunsets", data.sunsets.mapToJSONArray { it.toString() })
                put("pop", data.pop.mapToJSONArray { it.value })
                put("rain", data.rain.mapToJSONArray { it.convertTo(Precipitation.Unit.Millimeters).value })
                put("showers", data.showers.mapToJSONArray { it.convertTo(Precipitation.Unit.Millimeters).value })
                put("snow", data.snow.mapToJSONArray { it.convertTo(Precipitation.Unit.Millimeters).value })
                put("uvIndex", data.uvIndex.mapToJSONArray { it.value })
                put("windSpeed", data.windSpeed.mapToJSONArray { it.convertTo(WindSpeed.Unit.MetersPerSecond).value })
                put("windDirection", data.windDirection.mapToJSONArray { it.degrees })
                put("gustSpeed", data.gustSpeed.mapToJSONArray { it.convertTo(WindSpeed.Unit.MetersPerSecond).value })
                put("pressure", data.pressure.mapToJSONArray { it.convertTo(Pressure.Unit.Hectopascal).value })
                put("visibility", data.visibility.mapToJSONArray { it.convertTo(Visibility.Unit.Meters).value })
                put("humidity", data.humidity.mapToJSONArray { it.value })
                put("wmoCode", data.wmoCode.mapToJSONArray())
                put("isDay", data.isDay.mapToJSONArray())
            }.toString()
        }

    private suspend fun getDir(): File =
        withContext(Dispatchers.IO) { File(root, "forecasts").apply { mkdir() } }
}