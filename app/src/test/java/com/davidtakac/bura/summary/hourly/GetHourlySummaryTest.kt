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

package com.davidtakac.bura.summary.hourly

import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.PopMoment
import com.davidtakac.bura.forecast.parameters.pop.PopPeriod
import com.davidtakac.bura.forecast.parameters.sun.SunEvent
import com.davidtakac.bura.forecast.parameters.sun.SunMoment
import com.davidtakac.bura.forecast.parameters.sun.SunPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class GetHourlySummaryTest {
    @Test
    fun `combines weather and sun data and arranges it chronologically`() = runTest {
        val startOfTime = unixEpochStartZdt.plus(5, ChronoUnit.DAYS)
        val firstMoment = startOfTime.plus(1, ChronoUnit.HOURS)
        val secondMoment = startOfTime.plus(2, ChronoUnit.HOURS)
        val thirdMoment = startOfTime.plus(3, ChronoUnit.HOURS)
        val sunriseMoment = firstMoment.plus(30, ChronoUnit.MINUTES)
        val sunsetMoment = secondMoment.plus(30, ChronoUnit.MINUTES)
        val pastSunsetMoment = sunsetMoment.minus(1, ChronoUnit.DAYS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature(0.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(thirdMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius))
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
                SunMoment(pastSunsetMoment, SunEvent.Set),
                SunMoment(sunriseMoment, SunEvent.Rise),
                SunMoment(sunsetMoment, SunEvent.Set)
            )
        )
        val summary = getHourlySummary(
            now = now,
            tempPeriod = temperaturePeriod,
            popPeriod = popPeriod,
            condPeriod = conditionPeriod,
            sunPeriod = sunPeriod
        )
        Assert.assertEquals(
            listOf(
                HourSummary.Weather(
                    time = firstMoment.toLocalDateTime(),
                    isNow = true,
                    temp = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                    desc = Condition(wmoCode = 1, isDay = false),
                    pop = null
                ),
                HourSummary.Sun(
                    time = sunriseMoment.toLocalDateTime(),
                    event = SunEvent.Rise
                ),
                HourSummary.Weather(
                    time = secondMoment.toLocalDateTime(),
                    isNow = false,
                    temp = Temperature(1.0, Temperature.Unit.DegreesCelsius),
                    desc = Condition(wmoCode = 1, isDay = true),
                    pop = Pop(10.0)
                ),
                HourSummary.Sun(
                    time = sunsetMoment.toLocalDateTime(),
                    event = SunEvent.Set
                ),
                HourSummary.Weather(
                    time = thirdMoment.toLocalDateTime(),
                    isNow = false,
                    temp = Temperature(2.0, Temperature.Unit.DegreesCelsius),
                    desc = Condition(wmoCode = 1, isDay = false),
                    pop = Pop(10.0)
                ),
            ),
            summary
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(1, ChronoUnit.HOURS)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(1.0, Temperature.Unit.DegreesCelsius)
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
        val summary = getHourlySummary(
            now = now,
            tempPeriod = temperaturePeriod,
            popPeriod = popPeriod,
            condPeriod = conditionPeriod,
            sunPeriod = null
        )
        Assert.assertNull(summary)
    }

    @Test
    fun `no sun data when no sun moments from now`() = runTest {
        val startOfTime = unixEpochStartZdt
        val firstMoment = startOfTime.plus(10, ChronoUnit.HOURS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val pastSunrise = firstMoment.minus(3, ChronoUnit.HOURS)
        val pastSunset = firstMoment.minus(2, ChronoUnit.HOURS)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
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
                SunMoment(timeZdt = pastSunrise, event = SunEvent.Rise),
                SunMoment(timeZdt = pastSunset, event = SunEvent.Set)
            )
        )
        val summary = getHourlySummary(
            now = now,
            tempPeriod = temperaturePeriod,
            popPeriod = popPeriod,
            condPeriod = conditionPeriod,
            sunPeriod = sunPeriod
        )
        Assert.assertEquals(
            listOf(
                HourSummary.Weather(
                    time = firstMoment.toLocalDateTime(),
                    isNow = true,
                    temp = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                    desc = Condition(wmoCode = 1, isDay = false),
                    pop = null
                ),
            ),
            summary
        )
    }
}