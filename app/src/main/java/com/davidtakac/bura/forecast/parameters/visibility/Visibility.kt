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

package com.davidtakac.bura.forecast.parameters.visibility

import com.davidtakac.bura.forecast.parameters.visibility.Visibility.Unit
import java.util.Locale
import java.util.Objects

class Visibility(
    val value: Double,
    val unit: Unit
) : Comparable<Visibility> {
    private val meters: Double = when (unit) {
        Unit.Meters -> value
        Unit.Feet -> value * 3.28084
        Unit.Kilometers -> value * 1000
        Unit.Miles -> value / 0.00062137
    }

    // Source: https://weather.metoffice.gov.uk/guides/what-does-this-forecast-mean
    val description: Description = when {
        meters <= 1_000 -> Description.VeryPoor
        meters <= 4_000 -> Description.Poor
        meters <= 10_000 -> Description.Moderate
        meters <= 20_000 -> Description.Good
        meters <= 40_000 -> Description.VeryGood
        else -> Description.Excellent
    }

    fun convertTo(unit: Unit): Visibility {
        var newUnit = unit
        var newValue = metersTo(meters, newUnit)

        if (newValue < 0.1 && unit == Unit.Kilometers) {
            newUnit = Unit.Meters
            newValue = metersTo(meters, newUnit)
        }

        if (newValue < 0.1 && unit == Unit.Miles) {
            newUnit = Unit.Feet
            newValue = metersTo(meters, newUnit)
        }

        return Visibility(
            value = newValue,
            unit = newUnit
        )
    }

    enum class Unit {
        Meters,
        Feet,
        Kilometers,
        Miles,
    }

    enum class Description {
        VeryPoor,
        Poor,
        Moderate,
        Good,
        VeryGood,
        Excellent
    }

    override fun compareTo(other: Visibility): Int = meters.compareTo(other.meters)

    override fun equals(other: Any?): Boolean =
        other is Visibility && other.meters == meters && other.value == value && other.unit == unit

    override fun hashCode(): Int = Objects.hash(meters, value, unit)

    override fun toString(): String {
        val suffix = when (unit) {
            Unit.Meters -> "m"
            Unit.Feet -> "ft"
            Unit.Kilometers -> "km"
            Unit.Miles -> "mi"
        }
        return "${String.format(Locale.ROOT, "%.2f", value)} $suffix ($description)"
    }
}

private fun metersTo(meters: Double, unit: Unit): Double = meters * when (unit) {
    Unit.Meters -> 1.0
    Unit.Feet -> 3.28084
    Unit.Kilometers -> 0.001
    Unit.Miles -> 0.000621371
}