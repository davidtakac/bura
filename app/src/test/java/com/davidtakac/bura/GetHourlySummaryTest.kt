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
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.pop.PopMoment
import com.davidtakac.bura.pop.PopPeriod
import com.davidtakac.bura.summary.hourly.HourSummary
import com.davidtakac.bura.summary.hourly.GetHourlySummary
import com.davidtakac.bura.sun.SunEvent
import com.davidtakac.bura.sun.SunMoment
import com.davidtakac.bura.sun.SunPeriod
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureMoment
import com.davidtakac.bura.temperature.TemperaturePeriod
import com.davidtakac.bura.units.Units
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class GetHourlySummaryTest {
    private val location = GMTLocation.coordinates
    private val units = Units.Default

    @Test
    fun `combines weather and sun data and arranges it chronologically`() = runTest {
        val startOfTime = firstLocalDateTime.plus(5, ChronoUnit.DAYS)
        val firstMoment = startOfTime.plus(1, ChronoUnit.HOURS)
        val secondMoment = startOfTime.plus(2, ChronoUnit.HOURS)
        val thirdMoment = startOfTime.plus(3, ChronoUnit.HOURS)
        val sunriseMoment = firstMoment.plus(30, ChronoUnit.MINUTES)
        val sunsetMoment = secondMoment.plus(30, ChronoUnit.MINUTES)
        val pastSunsetMoment = sunsetMoment.minus(1, ChronoUnit.DAYS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature.fromDegreesCelsius(0.0)),
                TemperatureMoment(secondMoment, Temperature.fromDegreesCelsius(1.0)),
                TemperatureMoment(thirdMoment, Temperature.fromDegreesCelsius(2.0))
            )
        )
        val popPeriod = PopPeriod(
            moments = listOf(
                PopMoment(firstMoment, pop = Pop(0.0)),
                PopMoment(secondMoment, pop = Pop(10.0)),
                PopMoment(thirdMoment, pop = Pop(10.0))
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = false)
                ),
                ConditionMoment(
                    secondMoment,
                    Condition(wmoCode = 1, isDay = true)
                ),
                ConditionMoment(
                    thirdMoment,
                    Condition(wmoCode = 1, isDay = false)
                )
            )
        )
        val sunPeriod = SunPeriod(
            moments = listOf(
                SunMoment(pastSunsetMoment, SunEvent.Sunset),
                SunMoment(sunriseMoment, SunEvent.Sunrise),
                SunMoment(sunsetMoment, SunEvent.Sunset)
            )
        )
        val useCase = GetHourlySummary(
            tempRepo = FakeTemperatureRepository(temperaturePeriod),
            popRepo = FakePopRepository(popPeriod),
            descRepo = FakeConditionRepository(conditionPeriod),
            sunRepo = FakeSunRepository(sunPeriod),
        )
        val summary = useCase(location, units, now)
        assertEquals(
            ForecastResult.Success(
                listOf(
                    HourSummary.Weather(
                        time = firstMoment,
                        isNow = true,
                        temp = Temperature.fromDegreesCelsius(0.0),
                        desc = Condition(wmoCode = 1, isDay = false),
                        pop = null
                    ),
                    HourSummary.Sun(
                        time = sunriseMoment,
                        event = SunEvent.Sunrise
                    ),
                    HourSummary.Weather(
                        time = secondMoment,
                        isNow = false,
                        temp = Temperature.fromDegreesCelsius(1.0),
                        desc = Condition(wmoCode = 1, isDay = true),
                        pop = Pop(10.0)
                    ),
                    HourSummary.Sun(
                        time = sunsetMoment,
                        event = SunEvent.Sunset
                    ),
                    HourSummary.Weather(
                        time = thirdMoment,
                        isNow = false,
                        temp = Temperature.fromDegreesCelsius(2.0),
                        desc = Condition(wmoCode = 1, isDay = false),
                        pop = Pop(10.0)
                    ),
                )
            ),
            summary
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = firstLocalDateTime
        val now = firstMoment.plus(1, ChronoUnit.HOURS)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(1.0)
                )
            )
        )
        val popPeriod = PopPeriod(
            moments = listOf(
                PopMoment(
                    firstMoment,
                    pop = Pop(10.0)
                )
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = true)
                )
            )
        )
        val useCase = GetHourlySummary(
            tempRepo = FakeTemperatureRepository(temperaturePeriod),
            popRepo = FakePopRepository(popPeriod),
            descRepo = FakeConditionRepository(conditionPeriod),
            sunRepo = FakeSunRepository(null),
        )
        val summary = useCase(location, units, now)
        assertEquals(ForecastResult.Outdated, summary)
    }

    @Test
    fun `no sun data when no sun moments from now`() = runTest {
        val startOfTime = firstLocalDateTime
        val firstMoment = startOfTime.plus(10, ChronoUnit.HOURS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val pastSunrise = firstMoment.minus(3, ChronoUnit.HOURS)
        val pastSunset = firstMoment.minus(2, ChronoUnit.HOURS)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(0.0)
                ),
            )
        )
        val popPeriod = PopPeriod(
            moments = listOf(
                PopMoment(
                    firstMoment,
                    pop = Pop(0.0)
                ),
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = false)
                ),
            )
        )
        val sunPeriod = SunPeriod(
            moments = listOf(
                SunMoment(time = pastSunrise, event = SunEvent.Sunrise),
                SunMoment(time = pastSunset, event = SunEvent.Sunset)
            )
        )
        val useCase = GetHourlySummary(
            tempRepo = FakeTemperatureRepository(temperaturePeriod),
            popRepo = FakePopRepository(popPeriod),
            descRepo = FakeConditionRepository(conditionPeriod),
            sunRepo = FakeSunRepository(sunPeriod),
        )
        val summary = useCase(location, units, now)
        assertEquals(
            ForecastResult.Success(
                listOf(
                    HourSummary.Weather(
                        time = firstMoment.atZone(ZoneId.of("GMT")).toLocalDateTime(),
                        isNow = true,
                        temp = Temperature.fromDegreesCelsius(0.0),
                        desc = Condition(wmoCode = 1, isDay = false),
                        pop = null
                    ),
                ),
            ),
            summary
        )
    }
}