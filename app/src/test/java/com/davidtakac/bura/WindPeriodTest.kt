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

import com.davidtakac.bura.wind.Wind
import com.davidtakac.bura.wind.WindDirection
import com.davidtakac.bura.wind.WindMoment
import com.davidtakac.bura.wind.WindPeriod
import com.davidtakac.bura.wind.WindSpeed
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class WindPeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = firstLocalDateTime
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = WindPeriod(
            moments = listOf(
                WindMoment(
                    firstMoment,
                    Wind(WindSpeed.fromMetersPerSecond(0.0), WindDirection(0.0))
                ),
                WindMoment(
                    secondMoment,
                    Wind(WindSpeed.fromMetersPerSecond(1.0), WindDirection(0.0))
                ),
            )
        )
        assertEquals(WindSpeed.fromMetersPerSecond(0.0), period.minimumSpeed)
        assertEquals(WindSpeed.fromMetersPerSecond(1.0), period.maximumSpeed)
    }
}