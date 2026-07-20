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

import com.davidtakac.bura.forecast.Forecast
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.gust.GustMoment
import com.davidtakac.bura.forecast.parameters.gust.GustPeriod
import com.davidtakac.bura.forecast.parameters.humidity.Humidity
import com.davidtakac.bura.forecast.parameters.humidity.HumidityMoment
import com.davidtakac.bura.forecast.parameters.humidity.HumidityPeriod
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.PopMoment
import com.davidtakac.bura.forecast.parameters.pop.PopPeriod
import com.davidtakac.bura.forecast.parameters.precipitation.MixedPrecipitation
import com.davidtakac.bura.forecast.parameters.precipitation.Precipitation
import com.davidtakac.bura.forecast.parameters.precipitation.PrecipitationMoment
import com.davidtakac.bura.forecast.parameters.precipitation.PrecipitationPeriod
import com.davidtakac.bura.forecast.parameters.precipitation.Rain
import com.davidtakac.bura.forecast.parameters.precipitation.Showers
import com.davidtakac.bura.forecast.parameters.precipitation.Snow
import com.davidtakac.bura.forecast.parameters.pressure.Pressure
import com.davidtakac.bura.forecast.parameters.pressure.PressureMoment
import com.davidtakac.bura.forecast.parameters.pressure.PressurePeriod
import com.davidtakac.bura.forecast.parameters.sun.SunEvent
import com.davidtakac.bura.forecast.parameters.sun.SunMoment
import com.davidtakac.bura.forecast.parameters.sun.SunPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.forecast.parameters.uvindex.UvIndex
import com.davidtakac.bura.forecast.parameters.uvindex.UvIndexMoment
import com.davidtakac.bura.forecast.parameters.uvindex.UvIndexPeriod
import com.davidtakac.bura.forecast.parameters.visibility.Visibility
import com.davidtakac.bura.forecast.parameters.visibility.VisibilityMoment
import com.davidtakac.bura.forecast.parameters.visibility.VisibilityPeriod
import com.davidtakac.bura.forecast.parameters.wind.Wind
import com.davidtakac.bura.forecast.parameters.wind.WindDirection
import com.davidtakac.bura.forecast.parameters.wind.WindMoment
import com.davidtakac.bura.forecast.parameters.wind.WindPeriod
import com.davidtakac.bura.forecast.parameters.wind.WindSpeed
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ConvertCacheJsonToForecastTest {
    @Test
    fun `convert json to forecast`() = runTest {
        val json = JSONObject(getJson())
        assertEquals(getForecast(), convertCacheJsonToForecast(json))
    }

    @Test
    fun `convert forecast to json`() = runTest {
        val forecast = getForecast()
        assertEquals(JSONObject(getJson()).toString(), convertForecastToCacheJson(forecast).toString())
    }
}

private val timestamp = Instant.ofEpochSecond(1779224400)
private val hour = ZonedDateTime.ofInstant(timestamp, ZoneId.systemDefault())

private fun getJson(): String = """
    {
        "timestamp": ${timestamp.epochSecond},
        "temperature_period": [
            {
                "hour": "$hour",
                "temperature_value": 1.0,
                "temperature_unit": "DegreesCelsius"
            }
        ],
        "feels_like_period": [
            {
                "hour": "$hour",
                "temperature_value": 1.0,
                "temperature_unit": "DegreesCelsius"
            }
        ],
        "dew_point_period": [
            {
                "hour": "$hour",
                "temperature_value": 1.0,
                "temperature_unit": "DegreesCelsius"
            }
        ],
        "sun_period": [
            {
                "time": "$hour",
                "sun_event": "Rise"
            }
        ],
        "pop_period": [
            {
                "hour": "$hour",
                "pop_percent": 1.0
            }
        ],
        "precipitation_period": [
            {
                "hour": "$hour",
                "rain_value": 1.0,
                "rain_unit": "Millimeters",
                "showers_value": 1.0,
                "showers_unit": "Millimeters",
                "snow_value": 1.0,
                "snow_unit": "Centimeters",
                "precipitation_unit": "Millimeters"
            }
        ],
        "uv_index_period": [
            {
                "hour": "$hour",
                "uv_index": 1
            }
        ],
        "wind_period": [
            {
                "hour": "$hour",
                "wind_speed_value": 1.0,
                "wind_speed_unit": "MetersPerSecond",
                "direction_from_degrees": 1.0
            }
        ],
        "gust_period": [
            {
                "hour": "$hour",
                "wind_speed_value": 1.0,
                "wind_speed_unit": "MetersPerSecond"
            }
        ],
        "pressure_period": [
            {
                "hour": "$hour",
                "pressure_value": 1.0,
                "pressure_unit": "Hectopascal"
            }
        ],
        "visibility_period": [
            {
                "hour": "$hour",
                "visibility_value": 1.0,
                "visibility_unit": "Kilometers"
            }
        ],
        "humidity_period": [
            {
                "hour": "$hour",
                "humidity_percent": 1.0
            }
        ],
        "condition_period": [
            {
                "hour": "$hour",
                "condition_is_day": false,
                "condition_wmo_code": 1
            }
        ]
    }
""".trimIndent()

private fun getForecast() = Forecast(
    timestamp = timestamp,
    temperature = TemperaturePeriod(
        moments = listOf(
            TemperatureMoment(
                hour,
                Temperature(1.0, Temperature.Unit.DegreesCelsius)
            )
        )
    ),
    feelsLike = TemperaturePeriod(
        listOf(
            TemperatureMoment(
                hour,
                Temperature(1.0, Temperature.Unit.DegreesCelsius)
            )
        )
    ),
    dewPoint = TemperaturePeriod(
        listOf(
            TemperatureMoment(
                hour,
                Temperature(1.0, Temperature.Unit.DegreesCelsius)
            )
        )
    ),
    sun = SunPeriod(listOf(SunMoment(hour, SunEvent.Rise))),
    pop = PopPeriod(listOf(PopMoment(hour, Pop(1.0)))),
    precipitation = PrecipitationPeriod(
        listOf(
            PrecipitationMoment(
                hour,
                precipitation = MixedPrecipitation(
                    rain = Rain(1.0, Precipitation.Unit.Millimeters),
                    showers = Showers(1.0, Precipitation.Unit.Millimeters),
                    snow = Snow(1.0, Precipitation.Unit.Centimeters),
                    unit = Precipitation.Unit.Millimeters
                )
            )
        )
    ),
    uvIndex = UvIndexPeriod(listOf(UvIndexMoment(hour, UvIndex(1.0)))),
    wind = WindPeriod(
        listOf(
            WindMoment(
                hour,
                Wind(
                    WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond),
                    WindDirection(1.0)
                )
            )
        )
    ),
    gust = GustPeriod(listOf(GustMoment(hour, WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)))),
    pressure = PressurePeriod(
        listOf(
            PressureMoment(
                hour,
                Pressure(1.0, Pressure.Unit.Hectopascal)
            )
        )
    ),
    visibility = VisibilityPeriod(
        listOf(
            VisibilityMoment(
                hour,
                Visibility(1.0, Visibility.Unit.Kilometers)
            )
        )
    ),
    humidity = HumidityPeriod(listOf(HumidityMoment(hour, Humidity(1.0)))),
    condition = ConditionPeriod(listOf(ConditionMoment(hour, Condition(1, false))))
)