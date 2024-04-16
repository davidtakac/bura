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

package com.davidtakac.bura.units

import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.wind.WindSpeed

data class Units(
    val temperature: Temperature.Unit,
    val rain: Precipitation.Unit,
    val showers: Precipitation.Unit,
    val snow: Precipitation.Unit,
    val precipitation: Precipitation.Unit,
    val windSpeed: WindSpeed.Unit,
    val pressure: Pressure.Unit,
    val visibility: Visibility.Unit
) {
    companion object {
        val Default get() = Units(
            temperature = Temperature.Unit.DegreesCelsius,
            rain = Precipitation.Unit.Millimeters,
            showers = Precipitation.Unit.Millimeters,
            snow = Precipitation.Unit.Centimeters,
            precipitation = Precipitation.Unit.Millimeters,
            windSpeed = WindSpeed.Unit.MetersPerSecond,
            pressure = Pressure.Unit.Hectopascal,
            visibility = Visibility.Unit.Kilometers
        )
    }
}