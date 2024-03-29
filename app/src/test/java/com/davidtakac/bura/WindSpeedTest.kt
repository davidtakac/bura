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

import com.davidtakac.bura.wind.WindSpeed
import org.junit.Assert.*
import org.junit.Test

class WindSpeedTest {
    @Test
    fun `get bft and convert mps to kph, mph and kn`() {
        val mps = WindSpeed.fromMetersPerSecond(3.0)
        assertEquals(3.0, mps.value, 0.0)
        assertEquals(WindSpeed.Unit.MetersPerSecond, mps.unit)
        assertEquals(2, mps.beaufort)

        val kmh = mps.convertTo(WindSpeed.Unit.KilometersPerHour)
        assertEquals(10.8, kmh.value, 0.0)
        assertEquals(WindSpeed.Unit.KilometersPerHour, kmh.unit)
        assertEquals(2, kmh.beaufort)

        val mph = kmh.convertTo(WindSpeed.Unit.MilesPerHour)
        assertEquals(6.71, mph.value, 0.01)
        assertEquals(WindSpeed.Unit.MilesPerHour, mph.unit)
        assertEquals(2, mph.beaufort)

        val kn = mph.convertTo(WindSpeed.Unit.Knots)
        assertEquals(5.83, kn.value, 0.01)
        assertEquals(WindSpeed.Unit.Knots, kn.unit)
        assertEquals(2, kn.beaufort)
    }

    @Test
    fun `greater than`() {
        val speedLess = WindSpeed.fromMetersPerSecond(1.0)
        val speedGreater = WindSpeed.fromMetersPerSecond(2.0)
        speedGreater.convertTo(WindSpeed.Unit.KilometersPerHour)
        assertTrue(speedGreater > speedLess)
    }

    @Test
    fun equals() {
        val one = WindSpeed.fromMetersPerSecond(1.0)
        val two = WindSpeed.fromMetersPerSecond(1.0)
        one.convertTo(WindSpeed.Unit.MilesPerHour)
        assertTrue(one == two)
    }
}