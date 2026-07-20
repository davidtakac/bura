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

import com.davidtakac.bura.unixEpochStartZdt
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class PressurePeriodTest {
    @Test
    fun minimum() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(timeZdt = firstMoment, Pressure(1000.0, Pressure.Unit.Hectopascal)),
                PressureMoment(timeZdt = secondMoment, Pressure(1000.0, Pressure.Unit.Hectopascal))
            )
        )
        assertEquals(Pressure(1000.0, Pressure.Unit.Hectopascal), period.minimum)
    }

    @Test
    fun average() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(firstMoment, Pressure(1000.0, Pressure.Unit.Hectopascal)),
                PressureMoment(secondMoment, Pressure(1010.0, Pressure.Unit.Hectopascal))
            )
        )
        assertEquals(Pressure(1005.0, Pressure.Unit.Hectopascal), period.average)
    }

    @Test
    fun convert() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(firstMoment, Pressure(1000.0, Pressure.Unit.Hectopascal)),
                PressureMoment(secondMoment, Pressure(1010.0, Pressure.Unit.Hectopascal))
            )
        )
        val converted = period.convertTo(Pressure.Unit.InchesOfMercury)
        assert(converted.all { it.pressure.unit == Pressure.Unit.InchesOfMercury })
    }
}