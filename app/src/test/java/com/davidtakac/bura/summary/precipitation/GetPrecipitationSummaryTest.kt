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

package com.davidtakac.bura.summary.precipitation

import com.davidtakac.bura.forecast.parameters.precipitation.MixedPrecipitation
import com.davidtakac.bura.forecast.parameters.precipitation.Precipitation
import com.davidtakac.bura.forecast.parameters.precipitation.PrecipitationMoment
import com.davidtakac.bura.forecast.parameters.precipitation.PrecipitationPeriod
import com.davidtakac.bura.forecast.parameters.precipitation.Rain
import com.davidtakac.bura.forecast.parameters.precipitation.Showers
import com.davidtakac.bura.forecast.parameters.precipitation.Snow
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class GetPrecipitationSummaryTest {
    private fun dayOfPrecipitation(
        startTime: ZonedDateTime,
        millimetersPerHour: Double
    ): List<PrecipitationMoment> =
        List(24) { hour ->
            PrecipitationMoment(
                timeZdt = startTime.plus(hour.toLong(), ChronoUnit.HOURS),
                precipitation = MixedPrecipitation(
                    rain = Rain(
                        millimetersPerHour,
                        Precipitation.Unit.Millimeters
                    ),
                    snow = Snow.ZeroMillimeters,
                    showers = Showers.ZeroMillimeters,
                    unit = Precipitation.Unit.Millimeters
                )
            )
        }

    @Test
    fun `when past and future moments exist, past and future are correct`() = runTest {
        val startTime = unixEpochStartZdt
        val period = PrecipitationPeriod(dayOfPrecipitation(startTime, 1.0))
        val middle = startTime.plus(8, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now = middle, precipPeriod = period)
        Assert.assertEquals(
            PrecipitationSummary(
                past = PastPrecipitation(
                    inHours = 8,
                    total = Rain(8.0, Precipitation.Unit.Millimeters)
                ),
                future = FuturePrecipitation.InHours(
                    inHours = 16,
                    total = Rain(16.0, Precipitation.Unit.Millimeters)
                )
            ),
            summary
        )
    }

    @Test
    fun `when no past moments, summary is outdated`() = runTest {
        val startTime = unixEpochStartZdt
        val period = PrecipitationPeriod(dayOfPrecipitation(startTime, 1.0))
        val start = startTime.plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now = start, precipPeriod = period)
        Assert.assertNull(summary)
    }

    @Test
    fun `when no future moments, summary is outdated`() = runTest {
        val startTime = unixEpochStartZdt
        val period = PrecipitationPeriod(dayOfPrecipitation(startTime, 1.0))
        val end = startTime.plus(24, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now = end, precipPeriod = period)
        Assert.assertNull(summary)
    }

    @Test
    fun `when no past or future moments, summary is outdated`() = runTest {
        val startTime = unixEpochStartZdt
        val period = PrecipitationPeriod(dayOfPrecipitation(startTime, 1.0))
        val afterEnd = startTime.plus(3, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now = afterEnd, precipPeriod = period)
        Assert.assertNull(summary)
    }

    @Test
    fun `when no precipitation in next 24 hours but on some future day, future describes that day`() =
        runTest {
            val startTime = unixEpochStartZdt
            val period = PrecipitationPeriod(
                moments = buildList {
                    addAll(dayOfPrecipitation(startTime, 0.0))
                    addAll(
                        dayOfPrecipitation(
                            startTime.plus(1, ChronoUnit.DAYS),
                            0.0
                        )
                    )
                    addAll(
                        dayOfPrecipitation(
                            startTime.plus(2, ChronoUnit.DAYS),
                            1.0
                        )
                    )
                }
            )
            val now = startTime.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
            val summary = getPrecipitationSummary(now, period)
            Assert.assertEquals(
                FuturePrecipitation.OnDay(
                    onDay = Instant.ofEpochSecond(0).plus(2, ChronoUnit.DAYS)
                        .atZone(ZoneId.of("GMT")).toLocalDate(),
                    total = Rain(23.0, Precipitation.Unit.Millimeters)
                ),
                summary?.future
            )
        }

    @Test
    fun `when no precipitation in sight, future is none expected`() = runTest {
        val startTime = unixEpochStartZdt
        val period = PrecipitationPeriod(
            moments = buildList {
                addAll(dayOfPrecipitation(startTime, 0.0))
                addAll(dayOfPrecipitation(startTime.plus(1, ChronoUnit.DAYS), 0.0))
                addAll(dayOfPrecipitation(startTime.plus(2, ChronoUnit.DAYS), 0.0))
            }
        )
        val now = startTime.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now, period)
        Assert.assertEquals(
            FuturePrecipitation.None(inDays = 1),
            summary?.future
        )
    }

    @Test
    fun `when no precipitation in next 24 hours and no days after, future has 0mm total`() =
        runTest {
            val startTime = unixEpochStartZdt
            val period = PrecipitationPeriod(
                moments = buildList {
                    addAll(dayOfPrecipitation(startTime, 0.0))
                    addAll(
                        dayOfPrecipitation(
                            startTime.plus(1, ChronoUnit.DAYS),
                            0.0
                        )
                    )
                }
            )
            val now = startTime.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
            val summary = getPrecipitationSummary(now, period)
            Assert.assertEquals(
                FuturePrecipitation.InHours(
                    inHours = 24,
                    total = MixedPrecipitation(
                        rain = Rain.ZeroMillimeters,
                        snow = Snow.ZeroMillimeters,
                        showers = Showers.ZeroMillimeters,
                        unit = Precipitation.Unit.Millimeters
                    )
                ),
                summary?.future
            )
        }

    @Test
    fun `when precipitation in next 24 hours and after, future prioritizes 24 hours`() = runTest {
        val startTime = unixEpochStartZdt
        val period = PrecipitationPeriod(
            moments = buildList {
                addAll(dayOfPrecipitation(startTime, 0.0))
                addAll(dayOfPrecipitation(startTime.plus(1, ChronoUnit.DAYS), 1.0))
                addAll(dayOfPrecipitation(startTime.plus(2, ChronoUnit.DAYS), 2.0))
            }
        )
        val now = startTime.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now, period)
        Assert.assertEquals(
            FuturePrecipitation.InHours(
                inHours = 24,
                total = Rain(24.0, Precipitation.Unit.Millimeters)
            ),
            summary?.future
        )
    }
}