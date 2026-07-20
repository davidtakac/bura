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

package com.davidtakac.bura.graphs.precipitation

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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class GetPrecipitationTotalsTest {
    @Test
    fun `generates last and future for today and future for other days`() = runTest {
        val startOfFirstDay = unixEpochStartZdt
        val startOfSecondDay = startOfFirstDay.plus(1, ChronoUnit.DAYS)
        val period = PrecipitationPeriod(buildList {
            for (i in 0..23) {
                add(
                    PrecipitationMoment(
                        timeZdt = startOfFirstDay.plus(i.toLong(), ChronoUnit.HOURS),
                        precipitation = MixedPrecipitation(
                            rain = Rain(1.0, Precipitation.Unit.Millimeters),
                            snow = Snow(5.0, Precipitation.Unit.Millimeters),
                            showers = Showers.ZeroMillimeters,
                            unit = Precipitation.Unit.Millimeters
                        )
                    )
                )
            }
            for (i in 0..23) {
                add(
                    PrecipitationMoment(
                        timeZdt = startOfSecondDay.plus(i.toLong(), ChronoUnit.HOURS),
                        precipitation = MixedPrecipitation(
                            rain = Rain.ZeroMillimeters,
                            snow = Snow.ZeroMillimeters,
                            showers = Showers(2.0, Precipitation.Unit.Millimeters),
                            unit = Precipitation.Unit.Millimeters
                        )
                    )
                )
            }
        })
        val totals = getPrecipitationTotals(
            startOfFirstDay.plus(8, ChronoUnit.HOURS),
            period
        )
        Assert.assertEquals(
            listOf(
                PrecipitationTotal.Today(
                    day = LocalDate.parse("1970-01-01"),
                    past = TotalPrecipitationInHours(
                        hours = 8,
                        total = MixedPrecipitation(
                            rain = Rain(8.0, Precipitation.Unit.Millimeters),
                            snow = Snow(40.0, Precipitation.Unit.Millimeters),
                            showers = Showers.ZeroMillimeters,
                            unit = Precipitation.Unit.Millimeters
                        ),
                    ),
                    future = TotalPrecipitationInHours(
                        hours = 24,
                        total = MixedPrecipitation(
                            rain = Rain(16.0, Precipitation.Unit.Millimeters),
                            snow = Snow(80.0, Precipitation.Unit.Millimeters),
                            showers = Showers(16.0, Precipitation.Unit.Millimeters),
                            unit = Precipitation.Unit.Millimeters
                        )
                    )
                ),
                PrecipitationTotal.OtherDay(
                    day = LocalDate.parse("1970-01-02"),
                    total = Showers(48.0, Precipitation.Unit.Millimeters)
                )
            ),
            totals
        )
    }
}