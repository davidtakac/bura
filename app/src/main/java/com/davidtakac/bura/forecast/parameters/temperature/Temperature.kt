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

package com.davidtakac.bura.forecast.parameters.temperature

import java.util.Locale
import java.util.Objects

class Temperature(
    val value: Double,
    val unit: Unit
) : Comparable<Temperature> {
    private val degreesCelsius: Double = when (unit) {
        Unit.DegreesCelsius -> value
        Unit.DegreesFahrenheit -> (value - 32) * 5 / 9
    }

    fun convertTo(unit: Unit): Temperature = Temperature(
        value = when (unit) {
            Unit.DegreesCelsius -> degreesCelsius
            Unit.DegreesFahrenheit -> (degreesCelsius * 1.8) + 32
        },
        unit = unit
    )

    enum class Unit {
        DegreesCelsius,
        DegreesFahrenheit
    }

    operator fun plus(other: Temperature): Temperature =
        Temperature(degreesCelsius + other.degreesCelsius, Unit.DegreesCelsius).convertTo(unit)

    override fun compareTo(other: Temperature): Int = degreesCelsius.compareTo(other.degreesCelsius)

    override fun equals(other: Any?): Boolean =
        other is Temperature && other.value == value && other.unit == unit

    override fun hashCode(): Int = Objects.hash(degreesCelsius, value, unit)

    override fun toString(): String {
        val suffix = when (unit) {
            Unit.DegreesCelsius -> "°C"
            Unit.DegreesFahrenheit -> "°F"
        }
        return "${String.format(Locale.ROOT, "%.2f", value)}$suffix"
    }
}