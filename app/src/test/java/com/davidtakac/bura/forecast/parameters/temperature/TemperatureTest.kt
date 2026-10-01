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

package com.davidtakac.bura.forecast.parameters.temperature

import org.junit.Assert.*
import org.junit.Test

class TemperatureTest {
    @Test
    fun `convert to fahrenheit`() {
        val temperature = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        assertEquals(0.0, temperature.value, 0.0)
        assertEquals(Temperature.Unit.DegreesCelsius, temperature.unit)

        val fahrenheit = temperature.convertTo(Temperature.Unit.DegreesFahrenheit)
        assertEquals(32.0, fahrenheit.value, 0.01)
        assertEquals(Temperature.Unit.DegreesFahrenheit, fahrenheit.unit)
    }

    @Test
    fun equals() {
        val one = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        val two = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        two.convertTo(Temperature.Unit.DegreesFahrenheit)
        assertEquals(one, two)
    }

    @Test
    fun `greater than`() {
        val less = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        val greater = Temperature(1.0, Temperature.Unit.DegreesCelsius)
        greater.convertTo(Temperature.Unit.DegreesFahrenheit)
        assertTrue(greater > less)
    }

    @Test
    fun plus() {
        val one = Temperature(1.0, Temperature.Unit.DegreesCelsius)
        val two = Temperature(
            2.0,
            Temperature.Unit.DegreesCelsius
        ).convertTo(Temperature.Unit.DegreesFahrenheit)
        val sum = one + two
        assertEquals(
            Temperature(3.0, Temperature.Unit.DegreesCelsius).value,
            sum.value,
            0.01
        )
    }
}