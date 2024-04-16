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

package com.davidtakac.bura.wind

import java.util.Objects

class WindDirection(val degrees: Double) {
    val compass: Compass

    init {
        val index = (degrees / 22.5 + 0.5).toInt() % 16
        compass = Compass.values()[index]
    }

    enum class Compass {
        N, NNE, NE, ENE,
        E, ESE, SE, SSE,
        S, SSW, SW, WSW,
        W, WNW, NW, NNW
    }

    override fun equals(other: Any?): Boolean =
        other is WindDirection && other.degrees == degrees

    override fun hashCode(): Int = Objects.hash(degrees)

    override fun toString(): String = "${String.format("%.2f", degrees)}° ($compass)"
}