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

package com.davidtakac.bura.summary.wind

import com.davidtakac.bura.forecast.parameters.gust.GustMoment
import com.davidtakac.bura.forecast.parameters.gust.GustPeriod
import com.davidtakac.bura.forecast.parameters.wind.Wind
import com.davidtakac.bura.forecast.parameters.wind.WindDirection
import com.davidtakac.bura.forecast.parameters.wind.WindMoment
import com.davidtakac.bura.forecast.parameters.wind.WindPeriod
import com.davidtakac.bura.forecast.parameters.wind.WindSpeed
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetWindSummaryTest {
    @Test
    fun `gets current wind speed, direction and gust speed`() = runTest {
        val time = unixEpochStartZdt
        val now = time.plus(10, ChronoUnit.MINUTES)
        val windPeriod = WindPeriod(
            listOf(
                WindMoment(
                    time,
                    Wind(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                )
            )
        )
        val gustPeriod = GustPeriod(
            listOf(
                GustMoment(
                    time,
                    WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
                )
            )
        )
        val summary = getWindSummary(now, windPeriod, gustPeriod)
        Assert.assertEquals(
            WindSummary(
                windNow = Wind(
                    WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond),
                    WindDirection(0.0)
                ),
                gustNow = WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
            ),
            summary
        )
    }

    @Test
    fun `outdated when no now`() = runTest {
        val time = unixEpochStartZdt
        val now = time.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val windPeriod = WindPeriod(
            listOf(
                WindMoment(
                    time,
                    Wind(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                )
            )
        )
        val gustPeriod = GustPeriod(
            listOf(
                GustMoment(
                    time,
                    WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
                )
            )
        )
        val summary = getWindSummary(now, windPeriod, gustPeriod)
        Assert.assertNull(summary)
    }
}