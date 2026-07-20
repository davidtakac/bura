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

import com.davidtakac.bura.common.util.mapDoubles
import com.davidtakac.bura.common.util.mapInts
import com.davidtakac.bura.common.util.mapLongs
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.Forecast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

suspend fun convertDownloadJsonToForecast(json: JSONObject, timeZone: ZoneId): Forecast =
    withContext(Dispatchers.Default) {
        val hourly = json.getJSONObject("hourly")
        val times = hourly.getJSONArray("time").mapLongs { Instant.ofEpochSecond(it).atZone(timeZone) }

        val temperature = hourly.getJSONArray("temperature_2m").mapDoubles { Temperature(it, Temperature.Unit.DegreesCelsius) }
        val feelsLikeTemperature = hourly.getJSONArray("apparent_temperature").mapDoubles { Temperature(it, Temperature.Unit.DegreesCelsius) }
        val dewPointTemperature = hourly.getJSONArray("dew_point_2m").mapDoubles { Temperature(it, Temperature.Unit.DegreesCelsius) }
        val wmoCode = hourly.getJSONArray("weather_code").mapInts { it }
        val isDay = hourly.getJSONArray("is_day").mapInts { it == 1 }
        val pop = hourly.getJSONArray("precipitation_probability").mapDoubles(::Pop)
        val rain = hourly.getJSONArray("rain").mapDoubles { Rain(it, Precipitation.Unit.Millimeters) }
        val showers = hourly.getJSONArray("showers").mapDoubles { Showers(it, Precipitation.Unit.Millimeters) }
        val snowfall = hourly.getJSONArray("snowfall").mapDoubles { Snow(it, Precipitation.Unit.Centimeters) }
        val uvIndex = hourly.getJSONArray("uv_index").mapDoubles(::UvIndex)
        val windSpeed = hourly.getJSONArray("wind_speed_10m").mapDoubles { WindSpeed(it, WindSpeed.Unit.MetersPerSecond) }
        val windDirection = hourly.getJSONArray("wind_direction_10m").mapDoubles(::WindDirection)
        val gustSpeed = hourly.getJSONArray("wind_gusts_10m").mapDoubles { WindSpeed(it, WindSpeed.Unit.MetersPerSecond) }
        val visibility = hourly.getJSONArray("visibility").mapDoubles { Visibility(it, Visibility.Unit.Meters) }
        val humidity = hourly.getJSONArray("relative_humidity_2m").mapDoubles(::Humidity)
        val pressure = hourly.getJSONArray("pressure_msl").mapDoubles { Pressure(it, Pressure.Unit.Hectopascal) }

        val temperatureMoments = mutableListOf<TemperatureMoment>()
        val feelsLikeMoments = mutableListOf<TemperatureMoment>()
        val dewPointMoments = mutableListOf<TemperatureMoment>()
        val popMoments = mutableListOf<PopMoment>()
        val precipMoments = mutableListOf<PrecipitationMoment>()
        val uvIndexMoments = mutableListOf<UvIndexMoment>()
        val windMoments = mutableListOf<WindMoment>()
        val gustMoments = mutableListOf<GustMoment>()
        val pressureMoments = mutableListOf<PressureMoment>()
        val visibilityMoments = mutableListOf<VisibilityMoment>()
        val humidityMoments = mutableListOf<HumidityMoment>()
        val conditionMoments = mutableListOf<ConditionMoment>()

        for (i in times.indices) {
            val time = times[i]
            temperatureMoments.add(TemperatureMoment(time, temperature[i]))
            feelsLikeMoments.add(TemperatureMoment(time, feelsLikeTemperature[i]))
            dewPointMoments.add(TemperatureMoment(time, dewPointTemperature[i]))
            popMoments.add(PopMoment(time, pop[i]))
            val rain = rain[i]
            val showers = showers[i]
            val snowfall = snowfall[i]
            precipMoments.add(PrecipitationMoment(time, MixedPrecipitation(rain, showers, snowfall, Precipitation.Unit.Millimeters)))
            uvIndexMoments.add(UvIndexMoment(time, uvIndex[i]))
            windMoments.add(WindMoment(time, Wind(windSpeed[i], windDirection[i])))
            gustMoments.add(GustMoment(time, gustSpeed[i]))
            pressureMoments.add(PressureMoment(time, pressure[i]))
            visibilityMoments.add(VisibilityMoment(time, visibility[i]))
            humidityMoments.add(HumidityMoment(time, humidity[i]))
            conditionMoments.add(ConditionMoment(time, Condition(wmoCode[i], isDay[i])))
        }

        val daily = json.getJSONObject("daily")
        val sunrises = daily.getJSONArray("sunrise").mapLongs { Instant.ofEpochSecond(it).atZone(timeZone) }
        val sunsets = daily.getJSONArray("sunset").mapLongs { Instant.ofEpochSecond(it).atZone(timeZone) }

        Forecast(
            timestamp = Instant.now(),
            temperature = TemperaturePeriod(temperatureMoments),
            feelsLike = TemperaturePeriod(feelsLikeMoments),
            dewPoint = TemperaturePeriod(dewPointMoments),
            sun = createSunPeriod(sunrises, sunsets),
            pop = PopPeriod(popMoments),
            precipitation = PrecipitationPeriod(precipMoments),
            uvIndex = UvIndexPeriod(uvIndexMoments),
            wind = WindPeriod(windMoments),
            gust = GustPeriod(gustMoments),
            pressure = PressurePeriod(pressureMoments),
            visibility = VisibilityPeriod(visibilityMoments),
            humidity = HumidityPeriod(humidityMoments),
            condition = ConditionPeriod(conditionMoments)
        )
    }

fun createSunPeriod(
    sunrises: List<ZonedDateTime>,
    sunsets: List<ZonedDateTime>,
): SunPeriod? {
    val sortedSunMoments = mutableListOf<SunMoment>()
    for (i in sunrises.indices) {
        val sunrise = SunMoment(sunrises[i], SunEvent.Rise)
        val sunset = SunMoment(sunsets[i], SunEvent.Set)
        // https://github.com/davidtakac/bura/issues/97#issuecomment-3001628460
        val isPolarNight = sunrise.timeInstant == sunset.timeInstant
        val isPolarDay = ChronoUnit.HOURS.between(sunrise.timeInstant, sunset.timeInstant) == 24L
        if (isPolarNight || isPolarDay) {
            continue
        } else if (sunset.timeInstant < sunrise.timeInstant) {
            sortedSunMoments.add(sunset)
            sortedSunMoments.add(sunrise)
        } else {
            sortedSunMoments.add(sunrise)
            sortedSunMoments.add(sunset)
        }
    }
    return sortedSunMoments.takeIf { it.isNotEmpty() }?.let { SunPeriod(it) }
}
