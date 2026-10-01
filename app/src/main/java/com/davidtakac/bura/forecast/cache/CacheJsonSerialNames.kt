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

object CacheJsonSerialNames {
    // Metadata names
    const val APP_VERSION_NAME = "app_version_name"
    const val TIMESTAMP = "timestamp"

    // Period names
    const val TEMPERATURE_PERIOD = "temperature_period"
    const val FEELS_LIKE_PERIOD = "feels_like_period"
    const val DEW_POINT_PERIOD = "dew_point_period"
    const val SUN_PERIOD = "sun_period"
    const val POP_PERIOD = "pop_period"
    const val PRECIPITATION_PERIOD = "precipitation_period"
    const val UV_INDEX_PERIOD = "uv_index_period"
    const val WIND_PERIOD = "wind_period"
    const val GUST_PERIOD = "gust_period"
    const val PRESSURE_PERIOD = "pressure_period"
    const val VISIBILITY_PERIOD = "visibility_period"
    const val HUMIDITY_PERIOD = "humidity_period"
    const val CONDITION_PERIOD = "condition_period"

    // Moment field names
    const val MOMENT_TIME = "hour"
    const val TEMPERATURE_UNIT = "temperature_unit"
    const val TEMPERATURE_VALUE = "temperature_value"
    const val POP_VALUE = "pop_percent"
    const val RAIN_UNIT = "rain_unit"
    const val RAIN_VALUE = "rain_value"
    const val SHOWERS_UNIT = "showers_unit"
    const val SHOWERS_VALUE = "showers_value"
    const val SNOW_UNIT = "snow_unit"
    const val SNOW_VALUE = "snow_value"
    const val PRECIPITATION_UNIT = "precipitation_unit"
    const val UV_INDEX_VALUE = "uv_index"
    const val WIND_SPEED_VALUE = "wind_speed_value"
    const val WIND_SPEED_UNIT = "wind_speed_unit"
    const val WIND_DIRECTION_FROM_VALUE = "direction_from_degrees"
    const val PRESSURE_VALUE = "pressure_value"
    const val PRESSURE_UNIT = "pressure_unit"
    const val VISIBILITY_VALUE = "visibility_value"
    const val VISIBILITY_UNIT = "visibility_unit"
    const val HUMIDITY_VALUE = "humidity_percent"
    const val CONDITION_WMO_CODE_VALUE = "condition_wmo_code"
    const val CONDITION_IS_DAY_VALUE = "condition_is_day"

    // Sun moment field names
    const val SUN_MOMENT_TIME = "time"
    const val SUN_EVENT = "sun_event"
}