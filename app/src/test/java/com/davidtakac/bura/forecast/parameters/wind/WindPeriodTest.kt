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

package com.davidtakac.bura.forecast.parameters.wind

import com.davidtakac.bura.unixEpochStartZdt
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class WindPeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = WindPeriod(
            moments = listOf(
                WindMoment(
                    firstMoment,
                    Wind(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                ),
                WindMoment(
                    secondMoment,
                    Wind(WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                ),
            )
        )
        assertEquals(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), period.minimumSpeed)
        assertEquals(WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond), period.maximumSpeed)
    }

    @Test
    fun convert() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = WindPeriod(
            moments = listOf(
                WindMoment(
                    firstMoment,
                    Wind(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                ),
                WindMoment(
                    secondMoment,
                    Wind(WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                ),
            )
        )
        val converted = period.convertTo(WindSpeed.Unit.Knots)
        assert(converted.all { it.wind.speed.unit == WindSpeed.Unit.Knots })
    }
}