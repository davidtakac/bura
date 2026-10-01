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

package com.davidtakac.bura.summary.uvindex

import com.davidtakac.bura.forecast.parameters.uvindex.UvIndex
import com.davidtakac.bura.forecast.parameters.uvindex.UvIndexMoment
import com.davidtakac.bura.forecast.parameters.uvindex.UvIndexPeriod
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.temporal.ChronoUnit

private val dangerous = UvIndex(3.0)
private val safe = UvIndex(2.0)

class GetUvIndexSummaryTest {
    @Test
    fun `gets now`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstMoment, UvIndex(0.0))))
        assertEquals(
            UvIndex(0.0),
            getUvIndexSummary(now, period)?.now
        )
    }

    @Test
    fun `no next window when no dangerous periods today`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstMoment, safe)))
        assertEquals(
            UseProtection.None,
            getUvIndexSummary(now, period)?.useProtection
        )
    }

    @Test
    fun `window when danger starts and ends later today`() = runTest {
        val startTime = unixEpochStartZdt
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
        assertEquals(
            UseProtection.FromUntil(
                firstDanger.toLocalTime(),
                secondSafe.toLocalTime()
            ),
            getUvIndexSummary(now, period)?.useProtection
        )
    }

    @Test
    fun `window when danger starts now and ends later today`() = runTest {
        val firstDanger = unixEpochStartZdt
        val now = firstDanger.plus(10, ChronoUnit.MINUTES)
        val firstSafe = firstDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(firstSafe, safe),
            )
        )
        assertEquals(
            UseProtection.Until(firstSafe.toLocalTime()),
            getUvIndexSummary(now, period)?.useProtection
        )
    }

    @Test
    fun `window when danger started earlier and ends later today`() = runTest {
        val startTime = unixEpochStartZdt
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
        assertEquals(
            UseProtection.Until(firstSafe.toLocalTime()),
            getUvIndexSummary(now, period)?.useProtection
        )
    }

    @Test
    fun `window when danger started earlier and does not end`() = runTest {
        val startTime = unixEpochStartZdt
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
        assertEquals(
            UseProtection.UntilEndOfDay,
            getUvIndexSummary(now, period)?.useProtection
        )
    }

    @Test
    fun `next window is resistant to multiple future windows`() = runTest {
        val startTime = unixEpochStartZdt
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
        assertEquals(
            UseProtection.Until(firstSafe.toLocalTime()),
            getUvIndexSummary(now, period)?.useProtection
        )
    }

    @Test
    fun `outdated when no moments from now`() = runTest {
        val firstMoment = unixEpochStartZdt
        val afterFirstMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val now = afterFirstMoment.plus(10, ChronoUnit.MINUTES)
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstMoment, safe)))
        assertNull(getUvIndexSummary(now, period))
    }
}