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

import com.davidtakac.bura.common.util.getJSONArrayOrNull
import com.davidtakac.bura.common.util.mapJSONObjects
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.Forecast
import com.davidtakac.bura.forecast.Moment
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
import java.time.ZonedDateTime

suspend fun convertCacheJsonToForecast(json: JSONObject): Forecast =
    withContext(Dispatchers.Default) {
        Forecast(
            timestamp = Instant.ofEpochSecond(json.getLong(CacheJsonSerialNames.TIMESTAMP)),
            temperature = TemperaturePeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.TEMPERATURE_PERIOD)
                    .mapJSONObjects(::jsonToTemperatureMoment)
            ),
            feelsLike = TemperaturePeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.FEELS_LIKE_PERIOD)
                    .mapJSONObjects(::jsonToTemperatureMoment)
            ),
            dewPoint = TemperaturePeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.DEW_POINT_PERIOD)
                    .mapJSONObjects(::jsonToTemperatureMoment)
            ),
            sun = json
                .getJSONArrayOrNull(CacheJsonSerialNames.SUN_PERIOD)
                ?.mapJSONObjects(::jsonToSunMoment)
                ?.let(::SunPeriod),
            pop = PopPeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.POP_PERIOD)
                    .mapJSONObjects(::jsonToPopMoment)
            ),
            precipitation = PrecipitationPeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.PRECIPITATION_PERIOD)
                    .mapJSONObjects(::jsonToPrecipitationMoment)
            ),
            uvIndex = UvIndexPeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.UV_INDEX_PERIOD)
                    .mapJSONObjects(::jsonToUvIndexMoment)
            ),
            wind = WindPeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.WIND_PERIOD)
                    .mapJSONObjects(::jsonToWindMoment)
            ),
            gust = GustPeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.GUST_PERIOD)
                    .mapJSONObjects(::jsonToGustMoment)
            ),
            pressure = PressurePeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.PRESSURE_PERIOD)
                    .mapJSONObjects(::jsonToPressureMoment)
            ),
            visibility = VisibilityPeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.VISIBILITY_PERIOD)
                    .mapJSONObjects(::jsonToVisibilityMoment)
            ),
            humidity = HumidityPeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.HUMIDITY_PERIOD)
                    .mapJSONObjects(::jsonToHumidityMoment)
            ),
            condition = ConditionPeriod(
                moments = json
                    .getJSONArray(CacheJsonSerialNames.CONDITION_PERIOD)
                    .mapJSONObjects(::jsonToConditionMoment)
            ),
        )
    }

private fun jsonToTemperatureMoment(jsonObject: JSONObject): TemperatureMoment =
    convertJsonToMoment(jsonObject) {
        TemperatureMoment(
            timeZdt = it,
            temperature = Temperature(
                jsonObject.getDouble(CacheJsonSerialNames.TEMPERATURE_VALUE),
                Temperature.Unit.valueOf(jsonObject.getString(CacheJsonSerialNames.TEMPERATURE_UNIT))
            )
        )
    }

private fun jsonToPopMoment(jsonObject: JSONObject): PopMoment =
    convertJsonToMoment(jsonObject) {
        PopMoment(
            timeZdt = it,
            pop = Pop(jsonObject.getDouble(CacheJsonSerialNames.POP_VALUE))
        )
    }

private fun jsonToPrecipitationMoment(jsonObject: JSONObject): PrecipitationMoment =
    convertJsonToMoment(jsonObject) {
        PrecipitationMoment(
            timeZdt = it,
            precipitation = MixedPrecipitation(
                rain = Rain(
                    value = jsonObject.getDouble(CacheJsonSerialNames.RAIN_VALUE),
                    unit = Precipitation.Unit.valueOf(jsonObject.getString(CacheJsonSerialNames.RAIN_UNIT))
                ),
                showers = Showers(
                    value = jsonObject.getDouble(CacheJsonSerialNames.SHOWERS_VALUE),
                    unit = Precipitation.Unit.valueOf(jsonObject.getString(CacheJsonSerialNames.SHOWERS_UNIT))
                ),
                snow = Snow(
                    value = jsonObject.getDouble(CacheJsonSerialNames.SNOW_VALUE),
                    unit = Precipitation.Unit.valueOf(jsonObject.getString(CacheJsonSerialNames.SNOW_UNIT))
                ),
                unit = Precipitation.Unit.valueOf(jsonObject.getString(CacheJsonSerialNames.PRECIPITATION_UNIT))
            )
        )
    }

private fun jsonToSunMoment(jsonObject: JSONObject): SunMoment = SunMoment(
    timeZdt = ZonedDateTime.parse(jsonObject.getString(CacheJsonSerialNames.SUN_MOMENT_TIME)),
    event = SunEvent.valueOf(jsonObject.getString(CacheJsonSerialNames.SUN_EVENT))
)

private fun jsonToUvIndexMoment(jsonObject: JSONObject): UvIndexMoment =
    convertJsonToMoment(jsonObject) {
        UvIndexMoment(
            timeZdt = it,
            uvIndex = UvIndex(jsonObject.getDouble(CacheJsonSerialNames.UV_INDEX_VALUE))
        )
    }

private fun jsonToWindMoment(jsonObject: JSONObject): WindMoment =
    convertJsonToMoment(jsonObject) {
        WindMoment(
            timeZdt = it,
            wind = Wind(
                speed = jsonToWindSpeed(jsonObject),
                from = WindDirection(degrees = jsonObject.getDouble(CacheJsonSerialNames.WIND_DIRECTION_FROM_VALUE)),
            )
        )
    }

private fun jsonToGustMoment(jsonObject: JSONObject): GustMoment =
    convertJsonToMoment(jsonObject) {
        GustMoment(
            timeZdt = it,
            speed = jsonToWindSpeed(jsonObject)
        )
    }

private fun jsonToWindSpeed(jsonObject: JSONObject): WindSpeed =
    WindSpeed(
        value = jsonObject.getDouble(CacheJsonSerialNames.WIND_SPEED_VALUE),
        unit = WindSpeed.Unit.valueOf(jsonObject.getString(CacheJsonSerialNames.WIND_SPEED_UNIT)),
    )

private fun jsonToPressureMoment(jsonObject: JSONObject): PressureMoment =
    convertJsonToMoment(jsonObject) {
        PressureMoment(
            timeZdt = it,
            pressure = Pressure(
                value = jsonObject.getDouble(CacheJsonSerialNames.PRESSURE_VALUE),
                unit = Pressure.Unit.valueOf(jsonObject.getString(CacheJsonSerialNames.PRESSURE_UNIT))
            )
        )
    }

private fun jsonToVisibilityMoment(jsonObject: JSONObject): VisibilityMoment =
    convertJsonToMoment(jsonObject) {
        VisibilityMoment(
            timeZdt = it,
            visibility = Visibility(
                value = jsonObject.getDouble(CacheJsonSerialNames.VISIBILITY_VALUE),
                unit = Visibility.Unit.valueOf(jsonObject.getString(CacheJsonSerialNames.VISIBILITY_UNIT)),
            )
        )
    }

private fun jsonToHumidityMoment(jsonObject: JSONObject): HumidityMoment =
    convertJsonToMoment(jsonObject) {
        HumidityMoment(
            timeZdt = it,
            humidity = Humidity(
                value = jsonObject.getDouble(CacheJsonSerialNames.HUMIDITY_VALUE),
            )
        )
    }

private fun jsonToConditionMoment(jsonObject: JSONObject): ConditionMoment =
    convertJsonToMoment(jsonObject) {
        ConditionMoment(
            timeZdt = it,
            condition = Condition(
                wmoCode = jsonObject.getInt(CacheJsonSerialNames.CONDITION_WMO_CODE_VALUE),
                isDay = jsonObject.getBoolean(CacheJsonSerialNames.CONDITION_IS_DAY_VALUE)
            )
        )
    }

private fun <T: Moment> convertJsonToMoment(jsonObject: JSONObject, block: (ZonedDateTime) -> T): T =
    block(ZonedDateTime.parse(jsonObject.getString(CacheJsonSerialNames.MOMENT_TIME)))