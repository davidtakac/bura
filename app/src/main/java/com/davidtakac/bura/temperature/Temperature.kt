/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.temperature

import java.util.Objects

class Temperature private constructor(
    private val degreesCelsius: Double,
    val value: Double,
    val unit: Unit
) : Comparable<Temperature> {
    fun convertTo(unit: Unit): Temperature = Temperature(
        degreesCelsius = degreesCelsius,
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
        fromDegreesCelsius(degreesCelsius + other.degreesCelsius).convertTo(unit)

    override fun compareTo(other: Temperature): Int = degreesCelsius.compareTo(other.degreesCelsius)

    override fun equals(other: Any?): Boolean =
        other is Temperature && other.degreesCelsius == degreesCelsius && other.value == value && other.unit == unit

    override fun hashCode(): Int = Objects.hash(degreesCelsius, value, unit)

    override fun toString(): String {
        val suffix = when (unit) {
            Unit.DegreesCelsius -> "°C"
            Unit.DegreesFahrenheit -> "°F"
        }
        return "${String.format("%.2f", value)}$suffix"
    }

    companion object {
        fun fromDegreesCelsius(value: Double): Temperature = Temperature(
            degreesCelsius = value,
            value = value,
            unit = Unit.DegreesCelsius
        )
    }
}