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

import com.davidtakac.bura.forecast.requireMatching
import com.davidtakac.bura.unixEpochStartInstant
import com.davidtakac.bura.unixEpochStartZdt
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

// #121: Non-whole start time allows us to also test places with non-whole UTC offsets like Delhi
private val startZdt = unixEpochStartZdt.plus(30, ChronoUnit.MINUTES)

class HourPeriodTest {
    @Test(expected = IllegalArgumentException::class)
    fun `cannot be empty`() {
        TestHourPeriod(listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `must be ascending`() {
        TestHourPeriod(
            listOf(
                TestMoment(startZdt.plus(1, ChronoUnit.HOURS)),
                TestMoment(startZdt),
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `must be complete`() {
        TestHourPeriod(
            listOf(
                TestMoment(startZdt),
                TestMoment(startZdt.plus(2, ChronoUnit.HOURS))
            )
        )
    }

    @Test
    fun `two periods match if their times match`() {
        val first = TestHourPeriod(listOf(TestMoment(startZdt)))
        val second = TestHourPeriod(listOf(TestMoment(startZdt)))
        Assert.assertTrue(first.matches(second))
    }

    @Test
    fun `two periods do not match if their times do not match`() {
        val first = TestHourPeriod(listOf(TestMoment(startZdt)))
        val second = TestHourPeriod(listOf(TestMoment(startZdt.plus(1, ChronoUnit.HOURS))))
        Assert.assertFalse(first.matches(second))
    }

    @Test
    fun `until returns moments ending with hour exclusive`() {
        val period = TestHourPeriod(
            listOf(
                TestMoment(startZdt),
                TestMoment(startZdt.plus(1, ChronoUnit.HOURS)),
                TestMoment(startZdt.plus(2, ChronoUnit.HOURS))
            )
        )
        val until = period.momentsUntil(
            hourExclusive = unixEpochStartInstant
                .plus(2, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES),
            takeLast = 1
        )
        Assert.assertEquals(1, until?.size)
        Assert.assertEquals(
            startZdt.plus(1, ChronoUnit.HOURS),
            until?.get(0)?.timeZdt
        )
    }

    @Test
    fun `until returns moments ending with hour exclusive when it is an hour after last moment`() {
        val period = TestHourPeriod(listOf(TestMoment(startZdt)))
        val until = period.momentsUntil(
            unixEpochStartInstant
                .plus(1, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES)
        )
        Assert.assertEquals(startZdt, until?.get(0)?.timeZdt)
    }

    @Test
    fun `until is null when no hour directly before hour exclusive`() {
        val period = TestHourPeriod(listOf(TestMoment(startZdt)))
        val until = period.momentsUntil(
            unixEpochStartInstant
                .plus(2, ChronoUnit.HOURS)
                .plus(10, ChronoUnit.MINUTES)
        )
        Assert.assertNull(until)
    }

    @Test
    fun `gets moment at hour`() {
        val period = TestHourPeriod(listOf(TestMoment(startZdt)))
        Assert.assertEquals(
            startZdt,
            period[startZdt.toInstant()]?.timeZdt
        )
    }

    @Test
    fun `moment at hour is null when no such moment`() {
        val period = TestHourPeriod(listOf(TestMoment(startZdt)))
        Assert.assertNull(period[unixEpochStartInstant.plus(1, ChronoUnit.HOURS)])
    }

    @Test
    fun `from returns moments starting with hour inclusive`() {
        val period = TestHourPeriod(
            listOf(
                TestMoment(startZdt),
                TestMoment(startZdt.plus(1, ChronoUnit.HOURS)),
                TestMoment(startZdt.plus(2, ChronoUnit.HOURS))
            )
        )
        val from = period.momentsFrom(
            hourInclusive = unixEpochStartInstant.plus(10, ChronoUnit.MINUTES),
            take = 2
        )
        Assert.assertEquals(2, from?.size)
        Assert.assertEquals(startZdt.plus(1, ChronoUnit.HOURS), from?.get(1)?.timeZdt)
    }

    @Test
    fun `from returns null when no moment with hour inclusive`() {
        val period =
            TestHourPeriod(listOf(TestMoment(startZdt.plus(1, ChronoUnit.HOURS))))
        val from = period.momentsFrom(unixEpochStartInstant.plus(10, ChronoUnit.MINUTES))
        Assert.assertNull(from)
    }

    @Test
    fun `days from returns days starting at day inclusive`() {
        val period = TestHourPeriod(
            listOf(
                TestMoment(startZdt.plus(0, ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS)),
                TestMoment(startZdt.plus(1, ChronoUnit.DAYS))
            )
        )
        val days = period.dayMomentsFrom(dayInclusive = startZdt.toLocalDate(), take = 1)
        Assert.assertEquals(1, days?.size)
    }

    @Test
    fun `days from returns null when no moment with day inclusive`() {
        val period = TestHourPeriod(listOf(TestMoment(startZdt.plus(1, ChronoUnit.DAYS))))
        Assert.assertNull(period.dayMomentsFrom(LocalDate.MIN))
    }

    @Test
    fun `gets day at time`() {
        val period = TestHourPeriod(listOf(TestMoment(startZdt)))
        Assert.assertNotNull(period.dayMomentsOn(startZdt.toLocalDate()))
    }

    @Test
    fun `get day returns null when no day at time`() {
        val period = TestHourPeriod(listOf(TestMoment(startZdt)))
        Assert.assertNull(period.dayMomentsOn(LocalDate.MIN.plus(2, ChronoUnit.DAYS)))
    }

    @Test
    fun `require matching just runs on matching data`() {
        val first = TestHourPeriod(listOf(TestMoment(startZdt)))
        val second = TestHourPeriod(listOf(TestMoment(startZdt)))
        requireMatching(first, second)
    }

    @Test(expected = Throwable::class)
    fun `require matching throws on mismatched data`() {
        val first = TestHourPeriod(listOf(TestMoment(startZdt)))
        val second = TestHourPeriod(listOf(TestMoment(startZdt.plus(1, ChronoUnit.HOURS))))
        requireMatching(first, second)
    }

    @Test
    fun `DST fall back day consists of 25 moments when DST offset is 1h`() {
        val summerTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(2))
        val standardTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(1))
        val startingTime = Instant.ofEpochSecond(1792965600) // 2026-10-25T22:00Z so that applying the offset starts us at 2026-10-26T00:00
        val times = buildList {
            repeat(30) {
                add(
                    startingTime
                        .plus(it.toLong(), ChronoUnit.HOURS)
                        .atZone(if (it <= 3) summerTimeZone else standardTimeZone)
                )
            }
        }

        val period = TestHourPeriod(times.map { TestMoment(it) })
        Assert.assertEquals(25, period.dayMomentsOn(LocalDate.parse("2026-10-26"))?.size)
    }

    @Test
    fun `DST spring forward day consists of 23 moments when DST offset is 1h`() {
        val summerTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(2))
        val standardTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(1))
        val startingTime = Instant.ofEpochSecond(1774825200) // 2026-03-29T23:00Z so that applying the offset starts us at 2026-03-30T00:00
        val times = buildList {
            repeat(30) {
                add(
                    startingTime
                        .plus(it.toLong(), ChronoUnit.HOURS)
                        .atZone(if (it <= 2) standardTimeZone else summerTimeZone)
                )
            }
        }

        val period = TestHourPeriod(times.map { TestMoment(it) })
        Assert.assertEquals(23, period.dayMomentsOn(LocalDate.parse("2026-03-30"))?.size)
    }

    @Test
    fun `DST spring forward day consists of 24 moments when DST offset is 30min`() {
        val summerTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHoursMinutes(10, 30))
        val standardTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(11))
        val startingTime = Instant.ofEpochSecond(1791034200) // 2026-10-03T13:30Z so that applying the offset starts us at 2026-10-04T00:00
        val times = buildList {
            repeat(30) {
                add(
                    startingTime
                        .plus(it.toLong(), ChronoUnit.HOURS)
                        .atZone(if (it <= 2) summerTimeZone else standardTimeZone)
                )
            }
        }

        val period = TestHourPeriod(times.map { TestMoment(it) })
        Assert.assertEquals(24, period.dayMomentsOn(LocalDate.parse("2026-10-04"))?.size)
    }

    @Test
    fun `DST fall back day consists of 25 moments when DST offset is 30min`() {
        val summerTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHoursMinutes(10, 30))
        val standardTimeZone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(11))
        val startingTime = Instant.ofEpochSecond(1775307600) // 2026-04-04T14:00Z so that applying the offset starts us at 2026-04-05T00:00
        val times = buildList {
            repeat(30) {
                add(
                    startingTime
                        .plus(it.toLong(), ChronoUnit.HOURS)
                        .atZone(if (it <= 2) standardTimeZone else summerTimeZone)
                )
            }
        }

        val period = TestHourPeriod(times.map { TestMoment(it) })
        Assert.assertEquals(25, period.dayMomentsOn(LocalDate.parse("2026-04-05"))?.size)
    }
}