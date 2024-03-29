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
import com.davidtakac.bura.units.Units
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetPressureSummaryTest {
    private val location = GMTLocation
    private val units = Units.Default

    @Test
    fun `when at least one moment before now, returns now and trend`() = runTest {
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = Instant.ofEpochSecond(0),
                    pressure = Pressure.fromHectopascal(0.0)
                ),
                PressureMoment(
                    hour = Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS),
                    pressure = Pressure.fromHectopascal(1.0)
                )
            )
        )
        val repository = FakePressureRepository(period)
        val now = Instant.ofEpochSecond(0)
            .plus(1, ChronoUnit.HOURS)
            .plus(10, ChronoUnit.MINUTES)
        val useCase = GetPressureSummary(repository)
        val summary = useCase(location, units, now)
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
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = Instant.ofEpochSecond(0),
                    pressure = Pressure.fromHectopascal(1.0)
                )
            )
        )
        val now = Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS)
        val repository = FakePressureRepository(period)
        val useCase = GetPressureSummary(repository)
        assertEquals(
            ForecastResult.Outdated,
            useCase(location, units, now)
        )
    }

    @Test
    fun `when no moments before now, summary is outdated`() = runTest {
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = Instant.ofEpochSecond(0),
                    pressure = Pressure.fromHectopascal(1.0)
                )
            )
        )
        val now = Instant.ofEpochSecond(0)
        val repository = FakePressureRepository(period)
        val useCase = GetPressureSummary(repository)
        val summary = useCase(location, units, now)
        assertEquals(ForecastResult.Outdated, summary)
    }
}