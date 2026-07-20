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

import com.davidtakac.bura.common.util.mapToJSONArray
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.Forecast
import com.davidtakac.bura.forecast.Moment
import com.davidtakac.bura.forecast.parameters.gust.GustMoment
import com.davidtakac.bura.forecast.parameters.humidity.HumidityMoment
import com.davidtakac.bura.forecast.parameters.pop.PopMoment
import com.davidtakac.bura.forecast.parameters.precipitation.PrecipitationMoment
import com.davidtakac.bura.forecast.parameters.pressure.PressureMoment
import com.davidtakac.bura.forecast.parameters.sun.SunMoment
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.uvindex.UvIndexMoment
import com.davidtakac.bura.forecast.parameters.visibility.VisibilityMoment
import com.davidtakac.bura.forecast.parameters.wind.WindMoment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

suspend fun convertForecastToCacheJson(forecast: Forecast): JSONObject =
    withContext(Dispatchers.Default) {
        JSONObject().apply {
            put(CacheJsonSerialNames.TIMESTAMP, forecast.timestamp.epochSecond,)
            put(CacheJsonSerialNames.TEMPERATURE_PERIOD, forecast.temperature.mapToJSONArray(::temperatureMomentToJson))
            put(CacheJsonSerialNames.FEELS_LIKE_PERIOD, forecast.feelsLike.mapToJSONArray(::temperatureMomentToJson))
            put(CacheJsonSerialNames.DEW_POINT_PERIOD, forecast.dewPoint.mapToJSONArray(::temperatureMomentToJson))
            put(CacheJsonSerialNames.SUN_PERIOD, forecast.sun?.mapToJSONArray(::sunMomentToJson) ?: JSONObject.NULL)
            put(CacheJsonSerialNames.POP_PERIOD, forecast.pop.mapToJSONArray(::popMomentToJson))
            put(CacheJsonSerialNames.PRECIPITATION_PERIOD, forecast.precipitation.mapToJSONArray(::precipitationMomentToJson))
            put(CacheJsonSerialNames.UV_INDEX_PERIOD, forecast.uvIndex.mapToJSONArray(::uvIndexMomentToJson))
            put(CacheJsonSerialNames.WIND_PERIOD, forecast.wind.mapToJSONArray(::windMomentToJson))
            put(CacheJsonSerialNames.GUST_PERIOD, forecast.gust.mapToJSONArray(::gustMomentToJson))
            put(CacheJsonSerialNames.PRESSURE_PERIOD, forecast.pressure.mapToJSONArray(::pressureMomentToJson))
            put(CacheJsonSerialNames.VISIBILITY_PERIOD, forecast.visibility.mapToJSONArray(::visibilityMomentToJson))
            put(CacheJsonSerialNames.HUMIDITY_PERIOD, forecast.humidity.mapToJSONArray(::humidityMomentToJson))
            put(CacheJsonSerialNames.CONDITION_PERIOD, forecast.condition.mapToJSONArray(::conditionMomentToJson))
        }
    }

private fun temperatureMomentToJson(moment: TemperatureMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.TEMPERATURE_VALUE, moment.temperature.convertTo(Temperature.Unit.DegreesCelsius).value)
        put(CacheJsonSerialNames.TEMPERATURE_UNIT, moment.temperature.unit.name)
    }

private fun popMomentToJson(moment: PopMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.POP_VALUE, moment.pop.preciseValue)
    }

private fun precipitationMomentToJson(moment: PrecipitationMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.RAIN_VALUE, moment.precipitation.rain.value)
        put(CacheJsonSerialNames.RAIN_UNIT, moment.precipitation.rain.unit.name)

        put(CacheJsonSerialNames.SHOWERS_VALUE, moment.precipitation.showers.value)
        put(CacheJsonSerialNames.SHOWERS_UNIT, moment.precipitation.showers.unit.name)

        put(CacheJsonSerialNames.SNOW_VALUE, moment.precipitation.snow.value)
        put(CacheJsonSerialNames.SNOW_UNIT, moment.precipitation.snow.unit.name)

        put(CacheJsonSerialNames.PRECIPITATION_UNIT, moment.precipitation.unit.name)
    }

private fun sunMomentToJson(moment: SunMoment): JSONObject {
    val json = JSONObject()
    json.put(CacheJsonSerialNames.SUN_MOMENT_TIME, moment.timeZdt.toString())
    json.put(CacheJsonSerialNames.SUN_EVENT, moment.event.name)
    return json
}

private fun uvIndexMomentToJson(moment: UvIndexMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.UV_INDEX_VALUE, moment.uvIndex.preciseValue)
    }

private fun windMomentToJson(moment: WindMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.WIND_SPEED_VALUE, moment.wind.speed.value)
        put(CacheJsonSerialNames.WIND_SPEED_UNIT, moment.wind.speed.unit.name)
        put(CacheJsonSerialNames.WIND_DIRECTION_FROM_VALUE, moment.wind.from.degrees)
    }

private fun gustMomentToJson(moment: GustMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.WIND_SPEED_VALUE, moment.speed.value)
        put(CacheJsonSerialNames.WIND_SPEED_UNIT, moment.speed.unit.name)
    }

private fun pressureMomentToJson(moment: PressureMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.PRESSURE_VALUE, moment.pressure.value)
        put(CacheJsonSerialNames.PRESSURE_UNIT, moment.pressure.unit.name)
    }

private fun visibilityMomentToJson(moment: VisibilityMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.VISIBILITY_VALUE, moment.visibility.value)
        put(CacheJsonSerialNames.VISIBILITY_UNIT, moment.visibility.unit.name)
    }

private fun humidityMomentToJson(moment: HumidityMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.HUMIDITY_VALUE, moment.humidity.value)
    }

private fun conditionMomentToJson(moment: ConditionMoment): JSONObject =
    convertMomentToJson(moment) {
        put(CacheJsonSerialNames.CONDITION_WMO_CODE_VALUE, moment.condition.wmoCode)
        put(CacheJsonSerialNames.CONDITION_IS_DAY_VALUE, moment.condition.isDay)
    }

private fun <T : Moment> convertMomentToJson(moment: T, block: JSONObject.() -> Unit) =
    JSONObject().apply {
        put(CacheJsonSerialNames.MOMENT_TIME, moment.timeZdt.toString())
        block()
    }
