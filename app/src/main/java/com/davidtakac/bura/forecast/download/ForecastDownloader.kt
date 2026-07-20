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

package com.davidtakac.bura.forecast.download

import com.davidtakac.bura.forecast.Forecast
import com.davidtakac.bura.places.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

class ForecastDownloader(private val userAgent: String) {
    suspend fun get(location: Location): Forecast? =
        downloadForecastJson(location)?.let {
            convertDownloadJsonToForecast(it, location.timeZone)
        }

    private suspend fun downloadForecastJson(location: Location): JSONObject? =
        withContext(Dispatchers.IO) {
            val url = URL(openMeteoUrl(location))
            val conn = try {
                url.openConnection() as HttpsURLConnection
            } catch (_: Exception) {
                return@withContext null
            }

            try {
                conn.requestMethod = "GET"
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.setRequestProperty("User-Agent", userAgent)
                if (conn.responseCode != 200) return@withContext null
                val jsonString =
                    BufferedReader(InputStreamReader(conn.inputStream)).use(BufferedReader::readText)
                JSONObject(jsonString)
            } catch (_: Exception) {
                null
            } finally {
                conn.disconnect()
            }
        }

    private fun openMeteoUrl(location: Location): String =
        "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${formatCoordinate(location.coordinates.latitude)}" +
                "&longitude=${formatCoordinate(location.coordinates.longitude)}" +
                "&hourly=temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,rain,showers,snowfall,weather_code,pressure_msl,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index,is_day" +
                "&daily=sunrise,sunset" +
                "&wind_speed_unit=ms" +
                "&timezone=${location.timeZone.id}" +
                "&timeformat=unixtime" +
                "&past_days=1"

    private fun formatCoordinate(value: Double): String =
        String.format(Locale.ROOT, "%.2f", value)
}