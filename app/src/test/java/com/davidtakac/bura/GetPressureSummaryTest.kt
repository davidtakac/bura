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
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.pressure.PressureMoment
import com.davidtakac.bura.pressure.PressurePeriod
import com.davidtakac.bura.summary.pressure.PressureSummary
import com.davidtakac.bura.summary.pressure.GetPressureSummary
import com.davidtakac.bura.summary.pressure.PressureTrend
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetPressureSummaryTest {
    

    @Test
    fun `when at least one moment before now, returns now and trend`() = runTest {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = firstMoment,
                    pressure = Pressure.fromHectopascal(0.0)
                ),
                PressureMoment(
                    hour = secondMoment,
                    pressure = Pressure.fromHectopascal(1.0)
                )
            )
        )
        val repository = FakePressureRepository(period)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val useCase = GetPressureSummary(repository)
        val summary = useCase(coords, units, now)
        assertEquals(
            ForecastResult.Success(
                PressureSummary(
                    now = Pressure.fromHectopascal(1.0),
                    average = Pressure.fromHectopascal(0.5),
                    trend = PressureTrend.Rising
                )
            ),
            summary
        )
    }

    @Test
    fun `when no moments at now, summary is outdated`() = runTest {
        val firstMoment = unixEpochStart
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = firstMoment,
                    pressure = Pressure.fromHectopascal(1.0)
                )
            )
        )
        val now = firstMoment.plus(1, ChronoUnit.HOURS)
        val repository = FakePressureRepository(period)
        val useCase = GetPressureSummary(repository)
        assertEquals(
            ForecastResult.Outdated,
            useCase(coords, units, now)
        )
    }

    @Test
    fun `when no moments before now, summary is outdated`() = runTest {
        val firstMoment = unixEpochStart
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = firstMoment,
                    pressure = Pressure.fromHectopascal(1.0)
                )
            )
        )
        val now = firstMoment
        val repository = FakePressureRepository(period)
        val useCase = GetPressureSummary(repository)
        val summary = useCase(coords, units, now)
        assertEquals(ForecastResult.Outdated, summary)
    }
}