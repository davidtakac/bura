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

import org.junit.Assert.*
import org.junit.Test

class VisibilityTest {
    @Test
    fun `convert to kilometers and miles`() {
        val visibility = Visibility(1000.0, Visibility.Unit.Meters)
        assertEquals(1000.0, visibility.value, 0.0)
        assertEquals(Visibility.Unit.Meters, visibility.unit)

        val km = visibility.convertTo(Visibility.Unit.Kilometers)
        assertEquals(1.0, km.value, 0.0)
        assertEquals(Visibility.Unit.Kilometers, km.unit)

        val mi = visibility.convertTo(Visibility.Unit.Miles)
        assertEquals(0.62, mi.value, 0.01)
        assertEquals(Visibility.Unit.Miles, mi.unit)
    }

    @Test
    fun equals() {
        val one = Visibility(1.0, Visibility.Unit.Meters)
        val two = Visibility(1.0, Visibility.Unit.Meters)
        two.convertTo(Visibility.Unit.Kilometers)
        assertEquals(one, two)
    }

    @Test
    fun `greater than`() {
        val less = Visibility(1.0, Visibility.Unit.Meters)
        val greater = Visibility(2.0, Visibility.Unit.Meters)
        greater.convertTo(Visibility.Unit.Kilometers)
        assertTrue(greater > less)
    }

    @Test
    fun `smart kilometers`() {
        val visibility = Visibility(90.0, Visibility.Unit.Meters)
        visibility.convertTo(Visibility.Unit.Kilometers)
        assertEquals(90.0, visibility.value, 0.0)
        assertEquals(Visibility.Unit.Meters, visibility.unit)
    }

    @Test
    fun `smart miles`() {
        val visibility = Visibility(150.0, Visibility.Unit.Meters)
        val miles = visibility.convertTo(Visibility.Unit.Miles)
        assertEquals(492.12, miles.value, 0.01)
        assertEquals(Visibility.Unit.Feet, miles.unit)
    }
}