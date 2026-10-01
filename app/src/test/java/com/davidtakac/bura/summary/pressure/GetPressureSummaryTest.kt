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

package com.davidtakac.bura.summary.pressure

import com.davidtakac.bura.forecast.parameters.pressure.Pressure
import com.davidtakac.bura.forecast.parameters.pressure.PressureMoment
import com.davidtakac.bura.forecast.parameters.pressure.PressurePeriod
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetPressureSummaryTest {
    @Test
    fun `when at least one moment before now, returns now and trend`() = runTest {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    timeZdt = firstMoment,
                    pressure = Pressure(0.0, Pressure.Unit.Hectopascal)
                ),
                PressureMoment(
                    timeZdt = secondMoment,
                    pressure = Pressure(1.0, Pressure.Unit.Hectopascal)
                )
            )
        )
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val summary = getPressureSummary(now, period)
        Assert.assertEquals(
            PressureSummary(
                now = Pressure(1.0, Pressure.Unit.Hectopascal),
                average = Pressure(0.5, Pressure.Unit.Hectopascal),
                trend = PressureTrend.Rising
            ),
            summary
        )
    }

    @Test
    fun `trend falling`() = runTest {
        val now = Pressure(1000.0, Pressure.Unit.Hectopascal)
        val past = Pressure(1002.0, Pressure.Unit.Hectopascal)
        Assert.assertEquals(PressureTrend.Falling, getPressureTrend(past, now))
    }

    @Test
    fun `trend rising`() = runTest {
        val now = Pressure(1002.0, Pressure.Unit.Hectopascal)
        val past = Pressure(1000.0, Pressure.Unit.Hectopascal)
        Assert.assertEquals(PressureTrend.Rising, getPressureTrend(past, now))
    }

    @Test
    fun `trend stable`() = runTest {
        val now = Pressure(1000.5, Pressure.Unit.Hectopascal)
        val past = Pressure(1000.0, Pressure.Unit.Hectopascal)
        Assert.assertEquals(PressureTrend.Stable, getPressureTrend(past, now))
    }

    @Test
    fun `trend rising on border`() = runTest {
        val now = Pressure(1001.0, Pressure.Unit.Hectopascal)
        val past = Pressure(1000.0, Pressure.Unit.Hectopascal)
        Assert.assertEquals(PressureTrend.Rising, getPressureTrend(past, now))
    }

    @Test
    fun `trend falling on border`() = runTest {
        val now = Pressure(1000.0, Pressure.Unit.Hectopascal)
        val past = Pressure(1001.0, Pressure.Unit.Hectopascal)
        Assert.assertEquals(PressureTrend.Falling, getPressureTrend(past, now))
    }

    @Test
    fun `trend stable when same`() = runTest {
        val now = Pressure(1000.0, Pressure.Unit.Hectopascal)
        val past = Pressure(1000.0, Pressure.Unit.Hectopascal)
        Assert.assertEquals(PressureTrend.Stable, getPressureTrend(past, now))
    }

    @Test
    fun `when no moments at now, summary is outdated`() = runTest {
        val firstMoment = unixEpochStartZdt
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    timeZdt = firstMoment,
                    pressure = Pressure(1.0, Pressure.Unit.Hectopascal)
                )
            )
        )
        val now = firstMoment.plus(1, ChronoUnit.HOURS)
        Assert.assertNull(getPressureSummary(now, period))
    }

    @Test
    fun `when no moments before now, summary is outdated`() = runTest {
        val firstMoment = unixEpochStartZdt
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    timeZdt = firstMoment,
                    pressure = Pressure(1.0, Pressure.Unit.Hectopascal)
                )
            )
        )
        val now = firstMoment
        val summary = getPressureSummary(now, period)
        Assert.assertNull(summary)
    }
}