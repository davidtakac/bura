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

import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureMoment
import com.davidtakac.bura.temperature.TemperaturePeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class TemperaturePeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = Instant.ofEpochSecond(0)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature.fromDegreesCelsius(1.0)),
                TemperatureMoment(secondMoment, Temperature.fromDegreesCelsius(2.0)),
            )
        )
        assertEquals(Temperature.fromDegreesCelsius(1.0), period.minimum)
        assertEquals(Temperature.fromDegreesCelsius(2.0), period.maximum)
    }
}