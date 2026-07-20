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

package com.davidtakac.bura.forecast.parameters

import com.davidtakac.bura.forecast.HourMoment
import com.davidtakac.bura.forecast.HourPeriod
import com.davidtakac.bura.forecast.requireMatching
import com.davidtakac.bura.unixEpochStart
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TestHourMoment(time: LocalDateTime) : HourMoment(time)
class TestHourPeriod(moments: List<TestHourMoment>) : HourPeriod<TestHourMoment>(moments)

class HourPeriodTest {
    @Test(expected = IllegalArgumentException::class)
    fun `cannot be empty`() {
        TestHourPeriod(listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `must be ascending`() {
        TestHourPeriod(
            listOf(
                TestHourMoment(unixEpochStart.plus(1, ChronoUnit.HOURS)),
                TestHourMoment(unixEpochStart),
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `must be complete`() {
        TestHourPeriod(
            listOf(
                TestHourMoment(unixEpochStart),
                TestHourMoment(unixEpochStart.plus(2, ChronoUnit.HOURS))
            )
        )
    }

    @Test
    fun `two periods match if their times match`() {
        val first = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        val second = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        Assert.assertTrue(first.matches(second))
    }

    @Test
    fun `two periods do not match if their times do not match`() {
        val first = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        val second = TestHourPeriod(listOf(TestHourMoment(unixEpochStart.plus(1, ChronoUnit.HOURS))))
        Assert.assertFalse(first.matches(second))
    }

    @Test
    fun `until returns moments ending with hour exclusive`() {
        val period = TestHourPeriod(
            listOf(
                TestHourMoment(unixEpochStart),
                TestHourMoment(unixEpochStart.plus(1, ChronoUnit.HOURS)),
                TestHourMoment(unixEpochStart.plus(2, ChronoUnit.HOURS))
            )
        )
        val until = period.momentsUntil(
            hourExclusive = unixEpochStart
                .plus(2, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES),
            takeLast = 1
        )
        Assert.assertEquals(1, until?.size)
        Assert.assertEquals(unixEpochStart.plus(1, ChronoUnit.HOURS), until?.get(0)?.hour)
    }

    @Test
    fun `until returns moments ending with hour exclusive when it is an hour after last moment`() {
        val period = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        val until = period.momentsUntil(
            unixEpochStart
                .plus(1, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES)
        )
        Assert.assertEquals(unixEpochStart, until?.get(0)?.hour)
    }

    @Test
    fun `until is null when no hour directly before hour exclusive`() {
        val period = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        val until = period.momentsUntil(
            unixEpochStart
                .plus(2, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES)
        )
        Assert.assertNull(until)
    }

    @Test
    fun `gets moment at hour`() {
        val period = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        Assert.assertEquals(
            unixEpochStart,
            period[unixEpochStart]?.hour
        )
    }

    @Test
    fun `moment at hour is null when no such moment`() {
        val period = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        Assert.assertNull(period[unixEpochStart.plus(1, ChronoUnit.HOURS)])
    }

    @Test
    fun `from returns moments starting with hour inclusive`() {
        val period = TestHourPeriod(
            listOf(
                TestHourMoment(unixEpochStart),
                TestHourMoment(unixEpochStart.plus(1, ChronoUnit.HOURS)),
                TestHourMoment(unixEpochStart.plus(2, ChronoUnit.HOURS))
            )
        )
        val from = period.momentsFrom(
            hourInclusive = unixEpochStart.plus(10, ChronoUnit.MINUTES),
            take = 2
        )
        Assert.assertEquals(2, from?.size)
        Assert.assertEquals(unixEpochStart.plus(1, ChronoUnit.HOURS), from?.get(1)?.hour)
    }

    @Test
    fun `from returns null when no moment with hour inclusive`() {
        val period =
            TestHourPeriod(listOf(TestHourMoment(unixEpochStart.plus(1, ChronoUnit.HOURS))))
        val from = period.momentsFrom(unixEpochStart.plus(10, ChronoUnit.MINUTES))
        Assert.assertNull(from)
    }

    @Test
    fun `days from returns days starting at day inclusive`() {
        val period = TestHourPeriod(
            listOf(
                TestHourMoment(unixEpochStart.plus(0, ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS)),
                TestHourMoment(unixEpochStart.plus(1, ChronoUnit.DAYS))
            )
        )
        val days = period.dayMomentsFrom(dayInclusive = unixEpochStart.toLocalDate(), take = 1)
        Assert.assertEquals(1, days?.size)
    }

    @Test
    fun `days from returns null when no moment with day inclusive`() {
        val period = TestHourPeriod(listOf(TestHourMoment(unixEpochStart.plus(1, ChronoUnit.DAYS))))
        Assert.assertNull(period.dayMomentsFrom(LocalDate.MIN))
    }

    @Test
    fun `gets day at time`() {
        val period = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        Assert.assertNotNull(period.momentsOn(unixEpochStart.toLocalDate()))
    }

    @Test
    fun `get day returns null when no day at time`() {
        val period = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        Assert.assertNull(period.momentsOn(LocalDate.MIN.plus(2, ChronoUnit.DAYS)))
    }

    @Test
    fun `require matching just runs on matching data`() {
        val first = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        val second = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        requireMatching(first, second)
    }

    @Test(expected = Throwable::class)
    fun `require matching throws on mismatched data`() {
        val first = TestHourPeriod(listOf(TestHourMoment(unixEpochStart)))
        val second = TestHourPeriod(listOf(TestHourMoment(unixEpochStart.plus(1, ChronoUnit.HOURS))))
        requireMatching(first, second)
    }
}