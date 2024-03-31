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
import com.davidtakac.bura.summary.uvindex.UseProtection
import com.davidtakac.bura.summary.uvindex.GetUvIndexSummary
import com.davidtakac.bura.units.Units
import com.davidtakac.bura.uvindex.UvIndex
import com.davidtakac.bura.uvindex.UvIndexMoment
import com.davidtakac.bura.uvindex.UvIndexPeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val dangerous = UvIndex(3)
private val safe = UvIndex(2)

class UvIndexSummaryTest {
    private val location = GMTLocation.coordinates
    private val units = Units.Default

    @Test
    fun `gets now`() = runTest {
        val firstMoment = firstLocalDateTime
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstMoment, UvIndex(0))))
        val useCase = GetUvIndexSummary(FakeUvIndexRepository(period))
        assertEquals(
            UvIndex(0),
            (useCase(location, units, now) as ForecastResult.Success).data.now
        )
    }

    @Test
    fun `no next window when no dangerous periods today`() = runTest {
        val firstMoment = firstLocalDateTime
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstMoment, safe)))
        val useCase = GetUvIndexSummary(FakeUvIndexRepository(period))
        assertEquals(
            UseProtection.None,
            (useCase(location, units, now) as ForecastResult.Success).data.useProtection
        )
    }

    @Test
    fun `window when danger starts and ends later today`() = runTest {
        val startTime = firstLocalDateTime
        val now = startTime.plus(10, ChronoUnit.MINUTES)
        val firstSafe = startTime
        val firstDanger = startTime.plus(1, ChronoUnit.HOURS)
        val secondSafe = firstDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstSafe, safe),
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(secondSafe, safe),
            )
        )
        val useCase = GetUvIndexSummary(FakeUvIndexRepository(period))
        assertEquals(
            UseProtection.FromUntil(
                firstDanger.toLocalTime(),
                secondSafe.toLocalTime()
            ),
            (useCase(location, units, now) as ForecastResult.Success).data.useProtection
        )
    }

    @Test
    fun `window when danger starts now and ends later today`() = runTest {
        val firstDanger = firstLocalDateTime
        val now = firstDanger.plus(10, ChronoUnit.MINUTES)
        val firstSafe = firstDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(firstSafe, safe),
            )
        )
        val useCase = GetUvIndexSummary(FakeUvIndexRepository(period))
        assertEquals(
            UseProtection.Until(firstSafe.toLocalTime()),
            (useCase(location, units, now) as ForecastResult.Success).data.useProtection
        )
    }

    @Test
    fun `window when danger started earlier and ends later today`() = runTest {
        val startTime = firstLocalDateTime
        val firstDanger = startTime.plus(1, ChronoUnit.HOURS)
        val secondDanger = firstDanger.plus(1, ChronoUnit.HOURS)
        val now = secondDanger.plus(10, ChronoUnit.MINUTES)
        val firstSafe = secondDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(secondDanger, dangerous),
                UvIndexMoment(firstSafe, safe),
            )
        )
        val useCase = GetUvIndexSummary(FakeUvIndexRepository(period))
        assertEquals(
            UseProtection.Until(firstSafe.toLocalTime()),
            (useCase(location, units, now) as ForecastResult.Success).data.useProtection
        )
    }

    @Test
    fun `window when danger started earlier and does not end`() = runTest {
        val startTime = firstLocalDateTime
        val firstDanger = startTime.plus(1, ChronoUnit.HOURS)
        val secondDanger = firstDanger.plus(1, ChronoUnit.HOURS)
        val now = secondDanger.plus(10, ChronoUnit.MINUTES)
        val thirdDanger = secondDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(secondDanger, dangerous),
                UvIndexMoment(thirdDanger, dangerous),
            )
        )
        val useCase = GetUvIndexSummary(FakeUvIndexRepository(period))
        assertEquals(
            UseProtection.UntilEndOfDay,
            (useCase(location, units, now) as ForecastResult.Success).data.useProtection
        )
    }

    @Test
    fun `next window is resistant to multiple future windows`() = runTest {
        val startTime = firstLocalDateTime
        val firstDanger = startTime.plus(1, ChronoUnit.HOURS)
        val secondDanger = firstDanger.plus(1, ChronoUnit.HOURS)
        val now = secondDanger.plus(10, ChronoUnit.MINUTES)
        val firstSafe = secondDanger.plus(1, ChronoUnit.HOURS)
        val thirdDanger = firstSafe.plus(1, ChronoUnit.HOURS)
        val secondSafe = thirdDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(secondDanger, dangerous),
                UvIndexMoment(firstSafe, safe),
                UvIndexMoment(thirdDanger, dangerous),
                UvIndexMoment(secondSafe, safe),
            )
        )
        val useCase = GetUvIndexSummary(FakeUvIndexRepository(period))
        assertEquals(
            UseProtection.Until(firstSafe.toLocalTime()),
            (useCase(location, units, now) as ForecastResult.Success).data.useProtection
        )
    }

    @Test
    fun `outdated when no moments from now`() = runTest {
        val firstMoment = firstLocalDateTime
        val afterFirstMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val now = afterFirstMoment.plus(10, ChronoUnit.MINUTES)
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstMoment, safe)))
        val useCase = GetUvIndexSummary(FakeUvIndexRepository(period))
        assertEquals(ForecastResult.Outdated, useCase(location, units, now))
    }
}