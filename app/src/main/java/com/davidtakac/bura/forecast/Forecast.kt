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

import com.davidtakac.bura.gust.GustPeriod
import com.davidtakac.bura.humidity.HumidityPeriod
import com.davidtakac.bura.pop.PopPeriod
import com.davidtakac.bura.precipitation.PrecipitationPeriod
import com.davidtakac.bura.pressure.PressurePeriod
import com.davidtakac.bura.sun.SunPeriod
import com.davidtakac.bura.temperature.TemperaturePeriod
import com.davidtakac.bura.uvindex.UvIndexPeriod
import com.davidtakac.bura.visibility.VisibilityPeriod
import com.davidtakac.bura.condition.ConditionPeriod
import com.davidtakac.bura.wind.WindPeriod

data class Forecast(
    val temperature: TemperaturePeriod,
    val feelsLike: TemperaturePeriod,
    val dewPoint: TemperaturePeriod,
    val sun: SunPeriod?,
    val pop: PopPeriod,
    val precipitation: PrecipitationPeriod,
    val uvIndex: UvIndexPeriod,
    val wind: WindPeriod,
    val gust: GustPeriod,
    val pressure: PressurePeriod,
    val visibility: VisibilityPeriod,
    val humidity: HumidityPeriod,
    val weatherDescription: ConditionPeriod
) {
    init {
        requireMatching(
            temperature,
            feelsLike,
            dewPoint,
            pop,
            precipitation,
            uvIndex,
            wind,
            gust,
            pressure,
            visibility,
            humidity,
            weatherDescription
        )
    }
}