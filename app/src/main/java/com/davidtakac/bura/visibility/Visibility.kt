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

package com.davidtakac.bura.visibility

import java.util.Objects

class Visibility private constructor(
    private val meters: Double,
    val value: Double,
    val unit: Unit
) : Comparable<Visibility> {
    val description: Description = when {
        meters < 500 -> Description.VeryLow
        meters < 1000 -> Description.Low
        meters <= 6000 -> Description.Fair
        meters <= 10_000 -> Description.Clear
        else -> Description.Perfect
    }

    fun convertTo(unit: Unit): Visibility {
        var newUnit = unit
        var newValue = convertValueTo(newUnit)

        if (newValue < 0.1 && unit == Unit.Kilometers) {
            newUnit = Unit.Meters
            newValue = convertValueTo(newUnit)
        }

        if (newValue < 0.1 && unit == Unit.Miles) {
            newUnit = Unit.Feet
            newValue = convertValueTo(newUnit)
        }

        return Visibility(
            meters = meters,
            value = newValue,
            unit = newUnit
        )
    }

    private fun convertValueTo(unit: Unit): Double = meters * when (unit) {
        Unit.Meters -> 1.0
        Unit.Feet -> 3.28084
        Unit.Kilometers -> 0.001
        Unit.Miles -> 0.000621371
    }

    enum class Unit {
        Meters,
        Feet,
        Kilometers,
        Miles,
    }

    enum class Description {
        VeryLow,
        Low,
        Fair,
        Clear,
        Perfect
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
        return "${String.format("%.2f", value)} $suffix ($description)"
    }

    companion object {
        fun fromMeters(value: Double): Visibility = Visibility(
            meters = value,
            value = value,
            unit = Unit.Meters
        )
    }
}