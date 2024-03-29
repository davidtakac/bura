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

import com.davidtakac.bura.forecast.HourMoment
import com.davidtakac.bura.forecast.HourPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HourPeriodTest {
    @Test(expected = IllegalArgumentException::class)
    fun `cannot be empty`() {
        HourPeriod(listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `must be ascending`() {
        HourPeriod(
            listOf(
                HourMoment(Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS)),
                HourMoment(Instant.ofEpochSecond(0)),
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `must be complete`() {
        HourPeriod(
            listOf(
                HourMoment(Instant.ofEpochSecond(0)),
                HourMoment(Instant.ofEpochSecond(0).plus(2, ChronoUnit.HOURS))
            )
        )
    }

    @Test
    fun `two periods match if their times match`() {
        val first = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        val second = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        assertTrue(first.matches(second))
    }

    @Test
    fun `two periods do not match if their times do not match`() {
        val first = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        val second =
            HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS))))
        assertFalse(first.matches(second))
    }

    @Test
    fun `until returns moments ending with hour exclusive`() {
        val moments = HourPeriod(
            listOf(
                HourMoment(Instant.ofEpochSecond(0)),
                HourMoment(Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS)),
                HourMoment(Instant.ofEpochSecond(0).plus(2, ChronoUnit.HOURS))
            )
        )
        val until = moments.momentsUntil(
            hourExclusive = Instant.ofEpochSecond(0)
                .plus(2, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES),
            takeMoments = 1
        )
        assertEquals(1, until?.size)
        assertEquals(Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS), until?.get(0)?.hour)
    }

    @Test
    fun `until returns moments ending with hour exclusive when it is an hour after last moment`() {
        val moments = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        val until = moments.momentsUntil(
            Instant.ofEpochSecond(0)
                .plus(1, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES)
        )
        assertEquals(Instant.ofEpochSecond(0), until?.get(0)?.hour)
    }

    @Test
    fun `until is null when no hour directly before hour exclusive`() {
        val moments = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        val until = moments.momentsUntil(
            Instant.ofEpochSecond(0)
                .plus(2, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES)
        )
        assertNull(until)
    }

    @Test
    fun `gets moment at hour`() {
        val moments = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        assertEquals(
            Instant.ofEpochSecond(0),
            moments[Instant.ofEpochSecond(0)]?.hour
        )
    }

    @Test
    fun `moment at hour is null when no such moment`() {
        val moments = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        assertNull(moments[Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS)])
    }

    @Test
    fun `from returns moments starting with hour inclusive`() {
        val moments = HourPeriod(
            listOf(
                HourMoment(Instant.ofEpochSecond(0)),
                HourMoment(Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS)),
                HourMoment(Instant.ofEpochSecond(0).plus(2, ChronoUnit.HOURS))
            )
        )
        val from = moments.momentsFrom(
            hourInclusive = Instant.ofEpochSecond(0).plus(10, ChronoUnit.MINUTES),
            takeMoments = 2
        )
        assertEquals(2, from?.size)
        assertEquals(Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS), from?.get(1)?.hour)
    }

    @Test
    fun `from returns null when no moment with hour inclusive`() {
        val moments =
            HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS))))
        val from = moments.momentsFrom(Instant.ofEpochSecond(0).plus(10, ChronoUnit.MINUTES))
        assertNull(from)
    }

    @Test
    fun `days from returns days starting at day inclusive`() {
        val moments = HourPeriod(
            listOf(
                HourMoment(
                    Instant.ofEpochSecond(0).plus(0, ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS)
                ),
                HourMoment(Instant.ofEpochSecond(0).plus(1, ChronoUnit.DAYS))
            )
        )
        val days = moments.daysFrom(
            dayInclusive = Instant.ofEpochSecond(0),
            atZone = ZoneId.of("GMT"),
            takeDays = 1
        )
        assertEquals(1, days?.size)
    }

    @Test
    fun `days from returns null when no moment with day inclusive`() {
        val moments =
            HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0).plus(1, ChronoUnit.DAYS))))
        assertNull(
            moments.daysFrom(
                dayInclusive = Instant.ofEpochSecond(0),
                atZone = ZoneId.of("GMT")
            )
        )
    }

    @Test
    fun `gets day at time`() {
        val moments = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        assertNotNull(moments.getDay(Instant.ofEpochSecond(0), ZoneId.of("GMT")))
    }

    @Test
    fun `get day returns null when no day at time`() {
        val moments = HourPeriod(listOf(HourMoment(Instant.ofEpochSecond(0))))
        assertNull(
            moments.getDay(
                day = Instant.ofEpochSecond(0).plus(2, ChronoUnit.DAYS),
                atZone = ZoneId.of("GMT")
            )
        )
    }
}