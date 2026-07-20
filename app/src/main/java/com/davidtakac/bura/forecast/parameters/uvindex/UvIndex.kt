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

package com.davidtakac.bura.forecast.parameters.uvindex

import java.util.Locale
import java.util.Objects
import kotlin.math.roundToInt

class UvIndex(val preciseValue: Double) : Comparable<UvIndex> {
    val value: Int = preciseValue.roundToInt()

    val risk: Risk = when {
        value < 3 -> Risk.Low
        value < 6 -> Risk.Moderate
        value < 8 -> Risk.High
        value < 11 -> Risk.VeryHigh
        else -> Risk.Extreme
    }

    enum class Risk {
        Low,
        Moderate,
        High,
        VeryHigh,
        Extreme
    }

    override fun compareTo(other: UvIndex): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        other is UvIndex && other.value == value

    override fun hashCode(): Int = Objects.hash(value)

    override fun toString(): String = "${String.format(Locale.ROOT, "%.2f", preciseValue)} ($risk)"
}