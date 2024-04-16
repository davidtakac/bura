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

import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.humidity.Humidity
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.uvindex.UvIndex
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.wind.WindDirection
import com.davidtakac.bura.wind.WindSpeed
import java.time.Instant
import java.time.LocalDateTime

class ForecastData(
    val timestamp: Instant,
    val times: List<LocalDateTime>,
    val temperature: List<Temperature>,
    val feelsLikeTemperature: List<Temperature>,
    val dewPointTemperature: List<Temperature>,
    val sunrises: List<LocalDateTime>,
    val sunsets: List<LocalDateTime>,
    val pop: List<Pop>,
    val rain: List<Rain>,
    val showers: List<Showers>,
    val snow: List<Snow>,
    val uvIndex: List<UvIndex>,
    val windSpeed: List<WindSpeed>,
    val windDirection: List<WindDirection>,
    val gustSpeed: List<WindSpeed>,
    val pressure: List<Pressure>,
    val visibility: List<Visibility>,
    val humidity: List<Humidity>,
    val wmoCode: List<Int>,
    val isDay: List<Boolean>,
)