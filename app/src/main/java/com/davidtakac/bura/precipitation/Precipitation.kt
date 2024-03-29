/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.precipitation

import java.util.Objects

sealed class Precipitation(
    protected val millimeters: Double,
    val value: Double,
    val unit: Unit
) {
    enum class Unit {
        Millimeters, Centimeters, Inches
    }

    override fun toString(): String {
        val unitStr = when (unit) {
            Unit.Millimeters -> "mm"
            Unit.Centimeters -> "cm"
            Unit.Inches -> "in"
        }
        return "${String.format("%.2f", value)} $unitStr"
    }
}

class Rain(millimeters: Double, value: Double, unit: Unit) :
    Precipitation(millimeters, value, unit) {
    fun convertTo(unit: Unit): Rain =
        Rain(
            millimeters = millimeters,
            value = millimetersTo(millimeters, unit),
            unit = unit
        )

    operator fun plus(other: Rain): Rain =
        fromMillimeters(millimeters + other.millimeters).convertTo(unit)

    override fun equals(other: Any?): Boolean =
        other is Rain && other.millimeters == millimeters && other.value == value && other.unit == unit

    override fun hashCode(): Int =
        Objects.hash(millimeters, value, unit)

    companion object {
        val Zero get() = fromMillimeters(0.0)

        fun fromMillimeters(value: Double): Rain =
            Rain(value, value, Unit.Millimeters)
    }
}

class Showers(millimeters: Double, value: Double, unit: Unit) :
    Precipitation(millimeters, value, unit) {
    fun convertTo(unit: Unit): Showers =
        Showers(
            millimeters = millimeters,
            value = millimetersTo(millimeters, unit),
            unit = unit
        )

    operator fun plus(other: Showers): Showers =
        fromMillimeters(millimeters + other.millimeters).convertTo(unit)

    override fun equals(other: Any?): Boolean =
        other is Showers && other.millimeters == millimeters && other.value == value && other.unit == unit

    override fun hashCode(): Int =
        Objects.hash(millimeters, value, unit)

    companion object {
        val Zero get() = fromMillimeters(0.0)

        fun fromMillimeters(value: Double): Showers =
            Showers(value, value, Unit.Millimeters)
    }
}

class Snow(
    private val liquidMillimeters: Double,
    val liquidValue: Double,
    millimeters: Double, value: Double, unit: Unit
) : Precipitation(millimeters, value, unit) {
    fun convertTo(unit: Unit): Snow =
        Snow(
            liquidMillimeters = liquidMillimeters,
            liquidValue = millimetersTo(liquidMillimeters, unit),
            millimeters = millimeters,
            value = millimetersTo(millimeters, unit),
            unit = unit
        )

    operator fun plus(other: Snow): Snow =
        fromMillimeters(millimeters + other.millimeters).convertTo(unit)

    override fun equals(other: Any?): Boolean =
        other is Snow && other.millimeters == millimeters && other.value == value && other.unit == unit
                && other.liquidMillimeters == liquidMillimeters && other.liquidValue == liquidValue

    override fun hashCode(): Int =
        Objects.hash(millimeters, value, unit, liquidMillimeters, liquidValue)

    companion object {
        val Zero get() = fromMillimeters(0.0)

        fun fromMillimeters(value: Double): Snow {
            val liquidMm = value / 7
            return Snow(
                liquidMillimeters = liquidMm,
                liquidValue = liquidMm,
                millimeters = value,
                value = value,
                unit = Unit.Millimeters
            )
        }
    }
}

class MixedPrecipitation(
    val rain: Rain,
    val showers: Showers,
    val snow: Snow,
    millimeters: Double, value: Double, unit: Unit
) : Precipitation(millimeters, value, unit) {
    fun convertTo(unit: Unit): MixedPrecipitation =
        MixedPrecipitation(
            rain = rain,
            showers = showers,
            snow = snow,
            millimeters = millimeters,
            value = millimetersTo(millimeters, unit),
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
        fromMillimeters(
            rain = rain + other.rain,
            showers = showers + other.showers,
            snow = snow + other.snow
        ).convertTo(unit)

    override fun equals(other: Any?): Boolean =
        other is MixedPrecipitation && other.millimeters == millimeters && other.value == value && other.unit == unit
                && other.rain == rain && other.showers == showers && other.snow == snow

    override fun hashCode(): Int =
        Objects.hash(millimeters, value, unit)

    override fun toString(): String =
        "${super.toString()} (Rain: $rain, Showers: $showers, Snow: $snow)"

    companion object {
        val Zero get() = fromMillimeters(Rain.Zero, Showers.Zero, Snow.Zero)

        fun fromMillimeters(rain: Rain, showers: Showers, snow: Snow): MixedPrecipitation {
            val rainMm = rain.convertTo(Unit.Millimeters).value
            val showersMm = showers.convertTo(Unit.Millimeters).value
            val snowLiquidMm = snow.convertTo(Unit.Millimeters).liquidValue
            val sumMm = rainMm + showersMm + snowLiquidMm
            return MixedPrecipitation(
                rain = rain,
                showers = showers,
                snow = snow,
                millimeters = sumMm,
                value = sumMm,
                unit = Unit.Millimeters
            )
        }
    }
}

private fun millimetersTo(millimeters: Double, unit: Precipitation.Unit): Double =
    millimeters * when (unit) {
        Precipitation.Unit.Millimeters -> 1.0
        Precipitation.Unit.Centimeters -> 0.1
        Precipitation.Unit.Inches -> (1 / 25.4)
    }