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
import com.davidtakac.bura.graphs.precipitation.PrecipitationTotal
import com.davidtakac.bura.graphs.precipitation.GetPrecipitationTotals
import com.davidtakac.bura.graphs.precipitation.TotalPrecipitationInHours
import com.davidtakac.bura.precipitation.MixedPrecipitation
import com.davidtakac.bura.precipitation.PrecipitationMoment
import com.davidtakac.bura.precipitation.PrecipitationPeriod
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class GetPrecipitationTotalsTest {
    

    @Test
    fun `generates last and future for today and future for other days`() = runTest {
        val startOfFirstDay = unixEpochStart
        val startOfSecondDay = startOfFirstDay.plus(1, ChronoUnit.DAYS)
        val repo = FakePrecipitationRepository(PrecipitationPeriod(buildList {
            for (i in 0..23) {
                add(
                    PrecipitationMoment(
                        hour = startOfFirstDay.plus(i.toLong(), ChronoUnit.HOURS),
                        precipitation = MixedPrecipitation.fromMillimeters(
                            Rain.fromMillimeters(1.0),
                            Showers.Zero,
                            Snow.fromMillimeters(5.0)
                        )
                    )
                )
            }
            for (i in 0..23) {
                add(
                    PrecipitationMoment(
                        hour = startOfSecondDay.plus(i.toLong(), ChronoUnit.HOURS),
                        precipitation = MixedPrecipitation.fromMillimeters(
                            Rain.Zero,
                            Showers.fromMillimeters(2.0),
                            Snow.Zero
                        )
                    )
                )
            }
        }))
        val useCase = GetPrecipitationTotals(repo)
        val totals = (useCase(
            coords,
            units,
            startOfFirstDay.plus(8, ChronoUnit.HOURS)
        ) as ForecastResult.Success).data
        assertEquals(
            listOf(
                PrecipitationTotal.Today(
                    day = LocalDate.parse("1970-01-01"),
                    past = TotalPrecipitationInHours(
                        hours = 8,
                        total = MixedPrecipitation.fromMillimeters(
                            rain = Rain.fromMillimeters(8.0),
                            showers = Showers.Zero,
                            snow = Snow.fromMillimeters(40.0)
                        ),
                    ),
                    future = TotalPrecipitationInHours(
                        hours = 24,
                        total = MixedPrecipitation.fromMillimeters(
                            rain = Rain.fromMillimeters(16.0),
                            showers = Showers.fromMillimeters(16.0),
                            snow = Snow.fromMillimeters(80.0)
                        )
                    )
                ),
                PrecipitationTotal.Future(
                    day = LocalDate.parse("1970-01-02"),
                    total = Showers.fromMillimeters(48.0)
                )
            ),
            totals
        )
    }
}