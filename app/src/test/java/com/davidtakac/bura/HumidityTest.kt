/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura

import com.davidtakac.bura.humidity.Humidity
import org.junit.Assert.*
import org.junit.Test

class HumidityTest {
    @Test
    fun equals() {
        assertEquals(Humidity(1.0), Humidity(1.0))
    }

    @Test
    fun compare() {
        assertTrue(Humidity(1.0) > Humidity(0.0))
    }

    @Test
    fun plus() {
        assertEquals(Humidity(1.0) + Humidity(1.0), Humidity(2.0))
    }

    @Test
    fun divide() {
        assertEquals(Humidity(50.0) / 2, Humidity(25.0))
    }
}