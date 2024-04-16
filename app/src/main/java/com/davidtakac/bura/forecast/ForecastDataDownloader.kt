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

package com.davidtakac.bura.forecast

import com.davidtakac.bura.common.UserAgentProvider
import com.davidtakac.bura.common.mapToList
import com.davidtakac.bura.humidity.Humidity
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.pop.Pop
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

class ForecastDataDownloader(private val userAgentProvider: UserAgentProvider) {
    suspend fun downloadForecast(coords: Coordinates): ForecastData? =
        downloadForecastJson(coords)?.let { json -> convertJsonToData(json) }

    private suspend fun downloadForecastJson(coords: Coordinates): String? =
        withContext(Dispatchers.IO) {
            val url = URL(openMeteoUrl(coords))
            val conn = try {
                url.openConnection() as HttpsURLConnection
            } catch (_: Exception) {
                return@withContext null
            }

            try {
                conn.requestMethod = "GET"
                conn.connectTimeout = 5_000
                conn.readTimeout = 5_000
                conn.setRequestProperty("User-Agent", userAgentProvider.userAgent)
                if (conn.responseCode != 200) return@withContext null
                BufferedReader(InputStreamReader(conn.inputStream)).use(BufferedReader::readText)
            } catch (_: Exception) {
                null
            } finally {
                conn.disconnect()
            }
        }

    private suspend fun convertJsonToData(jsonString: String): ForecastData =
        withContext(Dispatchers.Default) {
            val json = JSONObject(jsonString)

            val daily = json.getJSONObject("daily")
            // When a day has no sunrise or sunset, Open-Meteo returns 1970-01-01, but the app
            // expects an omitted timestamp. These filters drop such placeholders.
            val sunrises = daily.getJSONArray("sunrise").mapToList(LocalDateTime::parse).filter { it != LocalDateTime.MIN }
            val sunsets = daily.getJSONArray("sunset").mapToList(LocalDateTime::parse).filter { it != LocalDateTime.MIN }

            val hourly = json.getJSONObject("hourly")

            // Open-Meteo sometimes returns only the first hour of the last day. The app expects
            // full 0-23h days, so this slicing is a way to drop such incomplete days.
            val times = hourly.getJSONArray("time").mapToList(LocalDateTime::parse)
            val indexOfLast23HourInstant = times.indexOfLast { it.toLocalTime() == LocalTime.parse("23:00") }
            val timesProcessed = times.slice(0..indexOfLast23HourInstant)

            val temperature = hourly.getJSONArray("temperature_2m").mapToList { Temperature.fromDegreesCelsius(it.toDouble()) }
            val feelsLikeTemperature = hourly.getJSONArray("apparent_temperature").mapToList { Temperature.fromDegreesCelsius(it.toDouble()) }
            val dewPointTemperature = hourly.getJSONArray("dew_point_2m").mapToList { Temperature.fromDegreesCelsius(it.toDouble()) }
            val wmoCode = hourly.getJSONArray("weather_code").mapToList(String::toInt)
            val isDay = hourly.getJSONArray("is_day").mapToList(String::toInt).map { it == 1 }
            val pop = hourly.getJSONArray("precipitation_probability").mapToList { Pop(it.toDouble()) }
            val rain = hourly.getJSONArray("rain").mapToList { Rain.fromMillimeters(it.toDouble()) }
            val showers = hourly.getJSONArray("showers").mapToList { Showers.fromMillimeters(it.toDouble()) }
            // Open-Meteo returns snow in centimeters
            val snowfall = hourly.getJSONArray("snowfall").mapToList { Snow.fromMillimeters(value = it.toDouble() * 10) }
            val uvIndex = hourly.getJSONArray("uv_index").mapToList { UvIndex(it.toDouble().toInt()) }
            val windSpeed = hourly.getJSONArray("wind_speed_10m").mapToList { WindSpeed.fromMetersPerSecond(it.toDouble()) }
            val windDirection = hourly.getJSONArray("wind_direction_10m").mapToList { WindDirection(it.toDouble()) }
            val gustSpeed = hourly.getJSONArray("wind_gusts_10m").mapToList { WindSpeed.fromMetersPerSecond(it.toDouble()) }
            val visibility = hourly.getJSONArray("visibility").mapToList { Visibility.fromMeters(it.toDouble()) }
            val humidity = hourly.getJSONArray("relative_humidity_2m").mapToList { Humidity(it.toDouble()) }
            val pressure = hourly.getJSONArray("pressure_msl").mapToList { Pressure.fromHectopascal(it.toDouble()) }

            ForecastData(
                timestamp = Instant.now(),
                times = timesProcessed,
                temperature = temperature,
                feelsLikeTemperature = feelsLikeTemperature,
                dewPointTemperature = dewPointTemperature,
                sunrises = sunrises,
                sunsets = sunsets,
                pop = pop,
                rain = rain,
                showers = showers,
                snow = snowfall,
                uvIndex = uvIndex,
                windSpeed = windSpeed,
                windDirection = windDirection,
                gustSpeed = gustSpeed,
                pressure = pressure,
                visibility = visibility,
                humidity = humidity,
                wmoCode = wmoCode,
                isDay = isDay
            )
        }

    private fun openMeteoUrl(coords: Coordinates): String =
        "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${formatCoordinate(coords.latitude)}" +
                "&longitude=${formatCoordinate(coords.longitude)}" +
                "&hourly=temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,rain,showers,snowfall,weather_code,pressure_msl,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index,is_day" +
                "&daily=sunrise,sunset" +
                "&wind_speed_unit=ms" +
                // timezone=auto returns whole days for the desired location
                "&timezone=auto" +
                "&past_days=1"

    private fun formatCoordinate(value: Double): String = String.format(Locale.ROOT, "%.2f", value)
}
