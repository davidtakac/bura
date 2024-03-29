/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.common

import com.davidtakac.bura.humidity.Humidity
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
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

fun JSONObject.getStringOrNull(name: String): String? =
    if (isNull(name)) null else getString(name)

fun JSONArray.toInstants(): List<Instant> =
    toLongs().map(Instant::ofEpochSecond)

fun Collection<Instant>.toEpochSecondJSONArray(): JSONArray =
    JSONArray(map { it.epochSecond })

fun JSONArray.toTemperatures(): List<Temperature> =
    toDoubles().map { Temperature.fromDegreesCelsius(it) }

fun Collection<Temperature>.toCelsiusJSONArray(): JSONArray =
    JSONArray(map { it.convertTo(Temperature.Unit.DegreesCelsius).value })

fun JSONArray.toHumidity(): List<Humidity> =
    toDoubles().map { Humidity(it) }

fun Collection<Humidity>.toHumidityPercentJSONArray(): JSONArray =
    JSONArray(map { it.value })

fun JSONArray.toPop(): List<Pop> =
    toDoubles().map { Pop(value = it) }

fun Collection<Pop>.toPopPercentJSONArray(): JSONArray =
    JSONArray(map { it.value })

fun JSONArray.toRain(): List<Rain> =
    toDoubles().map { Rain.fromMillimeters(it) }

fun Collection<Rain>.toRainMillimetersJSONArray(): JSONArray =
    JSONArray(map { it.convertTo(Precipitation.Unit.Millimeters).value })

fun JSONArray.toShowers(): List<Showers> =
    toDoubles().map { Showers.fromMillimeters(it) }

fun Collection<Showers>.toShowerMillimetersJSONArray(): JSONArray =
    JSONArray(map { it.convertTo(Precipitation.Unit.Millimeters).value })

fun JSONArray.toSnowfall(): List<Snow> =
    toDoubles().map { Snow.fromMillimeters(it * 10) }

fun Collection<Snow>.toSnowMillimetersJSONArray(): JSONArray =
    JSONArray(map { it.convertTo(Precipitation.Unit.Millimeters).value })

fun JSONArray.toUvIndices(): List<UvIndex> =
    toDoubles().map { UvIndex(value = it.toInt()) }

fun Collection<UvIndex>.toIndexJSONArray(): JSONArray =
    JSONArray(map { it.value })

fun JSONArray.toWindSpeeds(): List<WindSpeed> =
    toDoubles().map { WindSpeed.fromMetersPerSecond(it) }

fun Collection<WindSpeed>.toMetersPerSecondJSONArray(): JSONArray =
    JSONArray(map { it.convertTo(WindSpeed.Unit.MetersPerSecond).value })

fun JSONArray.toWindDirections(): List<WindDirection> =
    toDoubles().map { WindDirection(degrees = it) }

fun Collection<WindDirection>.toDegreesJSONArray(): JSONArray =
    JSONArray(map { it.degrees })

fun JSONArray.toVisibilities(): List<Visibility> =
    toDoubles().map { Visibility.fromMeters(it) }

fun Collection<Visibility>.toMetersJSONArray(): JSONArray =
    JSONArray(map { it.convertTo(Visibility.Unit.Meters).value })

fun JSONArray.toPressures(): List<Pressure> =
    toDoubles().map { Pressure.fromHectopascal(it) }

fun Collection<Pressure>.toHectopascalJSONArray(): JSONArray =
    JSONArray(map { it.convertTo(Pressure.Unit.Hectopascal).value })

fun JSONArray.toInts(): List<Int> = buildList {
    for (i in 0 until length()) {
        add(this@toInts.get(i).toString().toInt())
    }
}

fun JSONArray.toBooleans(): List<Boolean> = buildList {
    for (i in 0 until length()) {
        add(this@toBooleans.get(i).toString().toBoolean())
    }
}

private fun JSONArray.toLongs(): List<Long> = buildList {
    for (i in 0 until length()) {
        add(this@toLongs.get(i).toString().toLong())
    }
}

private fun JSONArray.toDoubles(): List<Double> = buildList {
    for (i in 0 until length()) {
        add(this@toDoubles.get(i).toString().toDouble())
    }
}