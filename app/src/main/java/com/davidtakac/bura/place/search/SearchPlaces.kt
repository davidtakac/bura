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

package com.davidtakac.bura.place.search

import com.davidtakac.bura.common.UserAgentProvider
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.place.Location
import com.davidtakac.bura.place.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.time.ZoneId
import javax.net.ssl.HttpsURLConnection

class SearchPlaces(private val userAgentProvider: UserAgentProvider) {
    suspend operator fun invoke(query: String, languageCode: String): List<Place>? {
        val jsonString = downloadPlacesJson(query, languageCode) ?: return null
        val json = JSONObject(jsonString)
        val results = try {
            json.getJSONArray("results")
        } catch (_: Exception) {
            return emptyList()
        }
        val places = mutableListOf<Place>()
        withContext(Dispatchers.Default) {
            for (i in 0 until results.length()) {
                val currResult = results.getJSONObject(i)
                val countryCode = currResult.getStringOrNull("country_code") ?: continue
                val timeZone = currResult.getStringOrNull("timezone")?.let(ZoneId::of) ?: continue
                places.add(
                    Place(
                        name = currResult.getString("name"),
                        countryName = currResult.getStringOrNull("country"),
                        countryCode = countryCode,
                        admin1 = currResult.getStringOrNull("admin1"),
                        location = Location(
                            timeZone = timeZone,
                            coordinates = Coordinates(
                                latitude = currResult.getDouble("latitude"),
                                longitude = currResult.getDouble("longitude")
                            )
                        )
                    )
                )
            }
        }
        return places
    }

    private suspend fun downloadPlacesJson(query: String, languageCode: String): String? = withContext(Dispatchers.IO) {
        val url = URL(openMeteoUrl(query, languageCode))
        val conn = try {
            url.openConnection() as HttpsURLConnection
        } catch (_: Exception) {
            return@withContext null
        }
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("User-Agent", userAgentProvider.userAgent)
            if (conn.responseCode != 200) return@withContext null
            BufferedReader(InputStreamReader(conn.inputStream)).use(BufferedReader::readText)
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    private fun openMeteoUrl(query: String, languageCode: String): String =
        "https://geocoding-api.open-meteo.com/v1/search" +
                "?name=$query" +
                "&count=30" +
                "&language=$languageCode" +
                "&format=json"

    private fun JSONObject.getStringOrNull(name: String): String? =
        try {
            getString(name)
        } catch (_: Exception) {
            null
        }
}