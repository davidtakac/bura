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

package com.davidtakac.bura.forecast.parameters.pressure

import org.junit.Assert.*
import org.junit.Test

class PressureTest {
    @Test
    fun `converts to inHg and mmHg`() {
        val pressure = Pressure(1000.0, Pressure.Unit.Hectopascal)
        assertEquals(1000.0, pressure.value, 0.0)
        assertEquals(Pressure.Unit.Hectopascal, pressure.unit)

        val inHg = pressure.convertTo(Pressure.Unit.InchesOfMercury)
        assertEquals(29.53, inHg.value, 0.01)
        assertEquals(Pressure.Unit.InchesOfMercury, inHg.unit)

        val mmHg = pressure.convertTo(Pressure.Unit.MillimetersOfMercury)
        assertEquals(750.06, mmHg.value, 0.01)
        assertEquals(Pressure.Unit.MillimetersOfMercury, mmHg.unit)
    }

    @Test
    fun plus() {
        val one = Pressure(1000.0, Pressure.Unit.Hectopascal)
        val two = Pressure(1000.0, Pressure.Unit.Hectopascal)
        two.convertTo(Pressure.Unit.InchesOfMercury)
        val sum = one + two
        assertEquals(2000.0, sum.value, 0.0)
        assertEquals(Pressure.Unit.Hectopascal, sum.unit)
    }

    @Test
    fun divide() {
        val pressure = Pressure(1000.0, Pressure.Unit.Hectopascal)
        val dividend = 2
        val result = pressure / dividend
        assertEquals(Pressure(500.0, Pressure.Unit.Hectopascal), result)
    }

    @Test
    fun `greater than`() {
        val less = Pressure(1000.0, Pressure.Unit.Hectopascal)
        val greater = Pressure(1001.0, Pressure.Unit.Hectopascal)
        greater.convertTo(Pressure.Unit.InchesOfMercury)
        assertTrue(greater > less)
    }

    @Test
    fun `construct from hPa`() {
        val pressure = Pressure(1000.0, Pressure.Unit.Hectopascal)
        assertEquals(1000.0, pressure.value, 0.0)
        assertEquals(Pressure.Unit.Hectopascal, pressure.unit)
    }
}