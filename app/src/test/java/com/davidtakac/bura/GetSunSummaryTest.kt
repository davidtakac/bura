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

import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.ConditionMoment
import com.davidtakac.bura.condition.ConditionPeriod
import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.summary.sun.GetSunSummary
import com.davidtakac.bura.summary.sun.Sunrise
import com.davidtakac.bura.summary.sun.Sunset
import com.davidtakac.bura.sun.SunEvent
import com.davidtakac.bura.sun.SunMoment
import com.davidtakac.bura.sun.SunPeriod
import com.davidtakac.bura.units.Units
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetSunSummaryTest {
    private val location = GMTLocation
    private val units = Units.Default
    private val timeZone = location.timeZone

    @Test
    fun `sunrise and sunset soon`() = runTest {
        val now = Instant.ofEpochSecond(0)
        val firstMoment = now
        val secondMoment = now.plus(2, ChronoUnit.HOURS)
        val sunRepo = FakeSunRepository(
            SunPeriod(
                listOf(
                    SunMoment(firstMoment, event = SunEvent.Sunrise),
                    SunMoment(secondMoment, event = SunEvent.Sunset)
                )
            )
        )
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, condition = Condition(1, true))
                )
            )
        )
        val useCase = GetSunSummary(
            sunRepo = sunRepo,
            descRepo = descRepo
        )
        val summary = useCase(location, units, now)
        assertEquals(
            Sunrise.WithSunsetSoon(
                time = firstMoment.atZone(timeZone).toLocalTime(),
                sunset = secondMoment.atZone(timeZone).toLocalTime()
            ),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunset and sunrise soon`() = runTest {
        val now = Instant.ofEpochSecond(0)
        val firstMoment = now
        val secondMoment = now.plus(2, ChronoUnit.HOURS)
        val sunRepo = FakeSunRepository(
            SunPeriod(
                listOf(
                    SunMoment(firstMoment, event = SunEvent.Sunset),
                    SunMoment(secondMoment, event = SunEvent.Sunrise)
                )
            )
        )
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(
                        firstMoment,
                        condition = Condition(1, false)
                    )
                )
            )
        )
        val useCase = GetSunSummary(sunRepo, descRepo)
        val summary = useCase(location, units, now)
        assertEquals(
            Sunset.WithSunriseSoon(
                time = firstMoment.atZone(timeZone).toLocalTime(),
                sunrise = secondMoment.atZone(timeZone).toLocalTime()
            ),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunrise soon but sunset in two days`() = runTest {
        val now = Instant.ofEpochSecond(0)
        val firstMoment = now
        val secondMoment = now.plus(2, ChronoUnit.DAYS)
        val sunRepo = FakeSunRepository(
            SunPeriod(
                listOf(
                    SunMoment(firstMoment, event = SunEvent.Sunrise),
                    SunMoment(secondMoment, event = SunEvent.Sunset)
                )
            )
        )
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, condition = Condition(1, true))
                )
            )
        )
        val useCase = GetSunSummary(
            sunRepo = sunRepo,
            descRepo = descRepo
        )
        val summary = useCase(location, units, now)
        assertEquals(
            Sunrise.WithSunsetLater(
                time = firstMoment.atZone(timeZone).toLocalTime(),
                sunset = secondMoment.atZone(timeZone).toLocalDateTime()
            ),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunset soon but sunrise in two days`() = runTest {
        val now = Instant.ofEpochSecond(0)
        val firstMoment = now
        val secondMoment = now.plus(2, ChronoUnit.DAYS)
        val sunRepo = FakeSunRepository(
            SunPeriod(
                listOf(
                    SunMoment(firstMoment, event = SunEvent.Sunset),
                    SunMoment(secondMoment, event = SunEvent.Sunrise)
                )
            )
        )
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, condition = Condition(1, true))
                )
            )
        )
        val useCase = GetSunSummary(
            sunRepo = sunRepo,
            descRepo = descRepo
        )
        val summary = useCase(location, units, now)
        assertEquals(
            Sunset.WithSunriseLater(
                time = firstMoment.atZone(timeZone).toLocalTime(),
                sunrise = secondMoment.atZone(timeZone).toLocalDateTime()
            ),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunrise later`() = runTest {
        val now = Instant.ofEpochSecond(0)
        val firstMoment = now.plus(2, ChronoUnit.DAYS)
        val sunRepo = FakeSunRepository(
            SunPeriod(
                listOf(
                    SunMoment(firstMoment, event = SunEvent.Sunrise),
                )
            )
        )
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, condition = Condition(1, true))
                )
            )
        )
        val useCase = GetSunSummary(
            sunRepo = sunRepo,
            descRepo = descRepo
        )
        val summary = useCase(location, units, now)
        assertEquals(
            Sunrise.Later(firstMoment.atZone(timeZone).toLocalDateTime()),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunset later`() = runTest {
        val now = Instant.ofEpochSecond(0)
        val firstMoment = now.plus(2, ChronoUnit.DAYS)
        val sunRepo = FakeSunRepository(
            SunPeriod(
                listOf(
                    SunMoment(firstMoment, event = SunEvent.Sunset),
                )
            )
        )
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, condition = Condition(1, true))
                )
            )
        )
        val useCase = GetSunSummary(
            sunRepo = sunRepo,
            descRepo = descRepo
        )
        val summary = useCase(location, units, now)
        assertEquals(
            Sunset.Later(firstMoment.atZone(timeZone).toLocalDateTime()),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `night currently but no sunrise in sight`() = runTest {
        val now = Instant.ofEpochSecond(0)
        val sunRepo = FakeSunRepository(null)
        val descRepo = FakeConditionRepository(ConditionPeriod(List(48) {
            ConditionMoment(
                now.plus(it.toLong(), ChronoUnit.HOURS),
                condition = Condition(1, true)
            )
        }))
        val useCase = GetSunSummary(
            sunRepo = sunRepo,
            descRepo = descRepo
        )
        val summary = useCase(location, units, now)
        assertEquals(
            Sunrise.OutOfSight(Duration.ofHours(48)),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `day currently but no sunset in sight`() = runTest {
        val now = Instant.ofEpochSecond(0)
        val sunRepo = FakeSunRepository(null)
        val descRepo = FakeConditionRepository(ConditionPeriod(List(48) {
            ConditionMoment(
                now.plus(it.toLong(), ChronoUnit.HOURS),
                condition = Condition(1, false)
            )
        }))
        val useCase = GetSunSummary(
            sunRepo = sunRepo,
            descRepo = descRepo
        )
        val summary = useCase(location, units, now)
        assertEquals(
            Sunset.OutOfSight(Duration.ofHours(48)),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `when no current desc returns outdated`() = runTest {
        val start = Instant.ofEpochSecond(0)
        val sunRepo = FakeSunRepository(null)
        val descRepo = FakeConditionRepository(ConditionPeriod(List(48) {
            ConditionMoment(
                start.plus(it.toLong(), ChronoUnit.HOURS),
                condition = Condition(1, false)
            )
        }))
        val now = start.plus(48.toLong(), ChronoUnit.HOURS)
        val useCase = GetSunSummary(
            sunRepo = sunRepo,
            descRepo = descRepo
        )
        val summary = useCase(location, units, now)
        assertEquals(ForecastResult.Outdated, summary)
    }
}