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

import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.gust.GustMoment
import com.davidtakac.bura.gust.GustPeriod
import com.davidtakac.bura.summary.wind.WindSummary
import com.davidtakac.bura.summary.wind.GetWindSummary
import com.davidtakac.bura.units.Units
import com.davidtakac.bura.wind.Wind
import com.davidtakac.bura.wind.WindDirection
import com.davidtakac.bura.wind.WindMoment
import com.davidtakac.bura.wind.WindPeriod
import com.davidtakac.bura.wind.WindSpeed
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class WindSummaryTest {
    private val location = GMTLocation.coordinates
    private val units = Units.Default

    @Test
    fun `gets current wind speed, direction and gust speed`() = runTest {
        val time = firstLocalDateTime
        val now = time.plus(10, ChronoUnit.MINUTES)
        val windPeriod = WindPeriod(
            listOf(
                WindMoment(
                    time,
                    Wind(WindSpeed.fromMetersPerSecond(0.0), WindDirection(0.0))
                )
            )
        )
        val gustPeriod = GustPeriod(listOf(GustMoment(time, WindSpeed.fromMetersPerSecond(1.0))))
        val useCase = GetWindSummary(
            windRepo = FakeWindRepository(windPeriod),
            gustRepo = FakeGustRepository(gustPeriod),
        )
        val summary = useCase(location, units, now)
        assertEquals(
            ForecastResult.Success(
                WindSummary(
                    windNow = Wind(WindSpeed.fromMetersPerSecond(0.0), WindDirection(0.0)),
                    gustNow = WindSpeed.fromMetersPerSecond(1.0)
                )
            ),
            summary
        )
    }

    @Test
    fun `outdated when no now`() = runTest {
        val time = firstLocalDateTime
        val now = time.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val windPeriod = WindPeriod(
            listOf(
                WindMoment(
                    time,
                    Wind(WindSpeed.fromMetersPerSecond(0.0), WindDirection(0.0))
                )
            )
        )
        val gustPeriod = GustPeriod(listOf(GustMoment(time, WindSpeed.fromMetersPerSecond(1.0))))
        val useCase = GetWindSummary(
            windRepo = FakeWindRepository(windPeriod),
            gustRepo = FakeGustRepository(gustPeriod),
        )
        val summary = useCase(location, units, now)
        assertEquals(ForecastResult.Outdated, summary)
    }
}