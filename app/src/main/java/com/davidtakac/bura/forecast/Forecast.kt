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

import com.davidtakac.bura.forecast.parameters.gust.GustPeriod
import com.davidtakac.bura.forecast.parameters.humidity.HumidityPeriod
import com.davidtakac.bura.forecast.parameters.pop.PopPeriod
import com.davidtakac.bura.forecast.parameters.precipitation.PrecipitationPeriod
import com.davidtakac.bura.forecast.parameters.pressure.PressurePeriod
import com.davidtakac.bura.forecast.parameters.sun.SunPeriod
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.forecast.parameters.uvindex.UvIndexPeriod
import com.davidtakac.bura.forecast.parameters.visibility.VisibilityPeriod
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.units.Units
import com.davidtakac.bura.forecast.parameters.wind.WindPeriod
import java.time.Instant

data class Forecast(
    val timestamp: Instant,
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
    val condition: ConditionPeriod
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
            condition
        )
    }

    fun convertTo(units: Units): Forecast =
        copy(
            temperature = temperature.convertTo(units.temperature),
            feelsLike = feelsLike.convertTo(units.temperature),
            dewPoint = dewPoint.convertTo(units.temperature),
            precipitation = precipitation.convertTo(units.precipitation),
            wind = wind.convertTo(units.windSpeed),
            gust = gust.convertTo(units.windSpeed),
            pressure = pressure.convertTo(units.pressure),
            visibility = visibility.convertTo(units.visibility)
        )
}