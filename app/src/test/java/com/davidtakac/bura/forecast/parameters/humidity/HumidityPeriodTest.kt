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

package com.davidtakac.bura.forecast.parameters.humidity

import com.davidtakac.bura.unixEpochStartZdt
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class HumidityPeriodTest {
    @Test
    fun average() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = HumidityPeriod(
            moments = listOf(
                HumidityMoment(firstMoment, Humidity(50.0)),
                HumidityMoment(secondMoment, Humidity(90.0))
            )
        )
        assertEquals(Humidity(70.0), period.average)
    }
}