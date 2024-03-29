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
import com.davidtakac.bura.summary.visibility.GetVisibilitySummary
import com.davidtakac.bura.units.Units
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.visibility.VisibilityMoment
import com.davidtakac.bura.visibility.VisibilityPeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetVisibilitySummaryTest {
    private val location = GMTLocation
    private val units = Units.Default

    private val repo = FakeVisibilityRepository(
        VisibilityPeriod(
            listOf(
                VisibilityMoment(Instant.ofEpochSecond(0), Visibility.fromMeters(1.0)),
                VisibilityMoment(
                    Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS),
                    Visibility.fromMeters(2.0)
                ),
                VisibilityMoment(
                    Instant.ofEpochSecond(0).plus(2, ChronoUnit.HOURS),
                    Visibility.fromMeters(3.0)
                )
            )
        )
    )

    @Test
    fun `gets distance and description of now`() = runTest {
        val now = Instant.ofEpochSecond(0).plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val useCase = GetVisibilitySummary(repo)
        assertEquals(
            Visibility.fromMeters(2.0),
            (useCase(location, units, now) as ForecastResult.Success).data.now
        )
    }

    @Test
    fun `summary is outdated when no now`() = runTest {
        val now = Instant.ofEpochSecond(0).plus(3, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val useCase = GetVisibilitySummary(repo)
        assertEquals(ForecastResult.Outdated, useCase(location, units, now))
    }
}