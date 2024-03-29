/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.humidity

import java.util.Objects

class Humidity(val value: Double) : Comparable<Humidity> {
    operator fun plus(other: Humidity) = Humidity(value + other.value)

    operator fun div(other: Int) = Humidity(value / other)

    override fun compareTo(other: Humidity): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        other is Humidity && other.value == value

    override fun hashCode(): Int = Objects.hash(value)

    override fun toString(): String = "${String.format("%.2f", value)}%"
}