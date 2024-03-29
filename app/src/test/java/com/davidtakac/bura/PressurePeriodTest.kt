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

import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.pressure.PressureMoment
import com.davidtakac.bura.pressure.PressurePeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class PressurePeriodTest {
    @Test
    fun minimum() {
        val firstMoment = Instant.ofEpochSecond(0)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(hour = firstMoment, Pressure.fromHectopascal(1000.0)),
                PressureMoment(hour = secondMoment, Pressure.fromHectopascal(1000.0))
            )
        )
        assertEquals(Pressure.fromHectopascal(1000.0), period.minimum)
    }

    @Test
    fun average() {
        val firstMoment = Instant.ofEpochSecond(0)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(firstMoment, Pressure.fromHectopascal(1000.0)),
                PressureMoment(secondMoment, Pressure.fromHectopascal(1010.0))
            )
        )
        assertEquals(Pressure.fromHectopascal(1005.0), period.average)
    }
}