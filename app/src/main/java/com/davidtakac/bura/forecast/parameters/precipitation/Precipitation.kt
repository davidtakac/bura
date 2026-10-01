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

package com.davidtakac.bura.forecast.parameters.precipitation

import com.davidtakac.bura.forecast.parameters.precipitation.Precipitation.Unit
import java.util.Locale
import java.util.Objects

sealed class Precipitation(
    val value: Double,
    val unit: Unit
) : Comparable<Precipitation> {
    protected val millimeters = toMillimeters(value, unit)

    enum class Unit {
        Millimeters, Centimeters, Inches
    }

    override fun toString(): String {
        val unitStr = when (unit) {
            Unit.Millimeters -> "mm"
            Unit.Centimeters -> "cm"
            Unit.Inches -> "in"
        }
        return "${String.format(Locale.ROOT, "%.2f", value)} $unitStr"
    }

    override fun compareTo(other: Precipitation): Int =
        millimeters.compareTo(other.millimeters)
}

class Rain(value: Double, unit: Unit) : Precipitation(value, unit) {
    fun convertTo(unit: Unit): Rain =
        Rain(
            value = millimetersTo(millimeters, unit),
            unit = unit
        )

    operator fun plus(other: Rain): Rain =
        Rain(millimeters + other.millimeters, Unit.Millimeters).convertTo(unit)

    override fun equals(other: Any?): Boolean =
        other is Rain && other.millimeters == millimeters && other.value == value && other.unit == unit

    override fun hashCode(): Int =
        Objects.hash(millimeters, value, unit)

    companion object {
        val ZeroMillimeters get() = Rain(0.0, Unit.Millimeters)
    }
}

class Showers(value: Double, unit: Unit) : Precipitation(value, unit) {
    fun convertTo(unit: Unit): Showers =
        Showers(
            value = millimetersTo(millimeters, unit),
            unit = unit
        )

    operator fun plus(other: Showers): Showers =
        Showers(millimeters + other.millimeters, Unit.Millimeters).convertTo(unit)

    override fun equals(other: Any?): Boolean =
        other is Showers && other.millimeters == millimeters && other.value == value && other.unit == unit

    override fun hashCode(): Int =
        Objects.hash(millimeters, value, unit)

    companion object {
        val ZeroMillimeters get() = Showers(0.0, Unit.Millimeters)
    }
}

class Snow(
    value: Double,
    unit: Unit
) : Precipitation(value, unit) {
    val liquidValue: Double
    private val liquidMillimeters: Double = millimeters / 7

    init {
        liquidValue = millimetersTo(liquidMillimeters, unit)
    }

    fun convertTo(unit: Unit): Snow =
        Snow(
            value = millimetersTo(millimeters, unit),
            unit = unit
        )

    operator fun plus(other: Snow): Snow =
        Snow(millimeters + other.millimeters, Unit.Millimeters).convertTo(unit)

    override fun equals(other: Any?): Boolean =
        other is Snow && other.millimeters == millimeters && other.value == value && other.unit == unit
                && other.liquidMillimeters == liquidMillimeters && other.liquidValue == liquidValue

    override fun hashCode(): Int =
        Objects.hash(millimeters, value, unit, liquidMillimeters, liquidValue)

    companion object {
        val ZeroMillimeters get() = Snow(0.0, Unit.Millimeters)
    }
}

class MixedPrecipitation(
    val rain: Rain,
    val showers: Showers,
    val snow: Snow,
    unit: Unit
) : Precipitation(rain.convertTo(unit).value + showers.convertTo(unit).value + snow.convertTo(unit).liquidValue, unit) {
    fun convertTo(unit: Unit): MixedPrecipitation =
        MixedPrecipitation(
            rain = rain,
            showers = showers,
            snow = snow,
            unit = unit
        )

    fun reduce(): Precipitation {
        val contributors = buildList {
            rain.takeIf { it.value > 0 }?.let(::add)
            showers.takeIf { it.value > 0 }?.let(::add)
            snow.takeIf { it.value > 0 }?.let(::add)
        }
        return if (contributors.size == 1) contributors.first() else this
    }

    operator fun plus(other: MixedPrecipitation): MixedPrecipitation =
        MixedPrecipitation(
            rain = rain + other.rain,
            snow = snow + other.snow,
            showers = showers + other.showers,
            unit = Unit.Millimeters
        ).convertTo(unit)

    override fun equals(other: Any?): Boolean =
        other is MixedPrecipitation
                && other.value == value
                && other.unit == unit
                && other.rain == rain
                && other.showers == showers
                && other.snow == snow

    override fun hashCode(): Int =
        Objects.hash(millimeters, value, unit)

    override fun toString(): String =
        "${super.toString()} (Rain: $rain, Showers: $showers, Snow: $snow)"

    companion object {
        val ZeroMillimeters get() = MixedPrecipitation(
            rain = Rain.ZeroMillimeters,
            showers = Showers.ZeroMillimeters,
            snow = Snow.ZeroMillimeters,
            unit = Unit.Millimeters
        )
    }
}

private fun millimetersTo(millimeters: Double, unit: Unit): Double =
    millimeters * when (unit) {
        Unit.Millimeters -> 1.0
        Unit.Centimeters -> 0.1
        Unit.Inches -> (1 / 25.4)
    }

private fun toMillimeters(value: Double, unit: Unit): Double =
    when (unit) {
        Unit.Millimeters -> value
        Unit.Centimeters -> value * 10
        Unit.Inches -> value * 25.4
    }