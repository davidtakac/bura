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

import com.davidtakac.bura.unixEpochStartZdt
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.ChronoUnit

class TemperaturePeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius)),
            )
        )
        assertEquals(Temperature(1.0, Temperature.Unit.DegreesCelsius), period.minimum)
        assertEquals(Temperature(2.0, Temperature.Unit.DegreesCelsius), period.maximum)
    }

    @Test
    fun convert() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius)),
            )
        )
        val converted = period.convertTo(Temperature.Unit.DegreesFahrenheit)
        assert(converted.all { it.temperature.unit == Temperature.Unit.DegreesFahrenheit })
    }
}