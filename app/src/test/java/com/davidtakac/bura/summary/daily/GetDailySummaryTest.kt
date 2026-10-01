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

package com.davidtakac.bura.summary.daily

import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.PopMoment
import com.davidtakac.bura.forecast.parameters.pop.PopPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class GetDailySummaryTest {
    @Test
    fun `groups moments into days and summarizes them`() = runTest {
        val firstDayFirstMoment = unixEpochStartZdt.plus(21, ChronoUnit.HOURS)
        val firstDaySecondMoment = firstDayFirstMoment.plus(1, ChronoUnit.HOURS)
        val firstDayThirdMoment = firstDaySecondMoment.plus(1, ChronoUnit.HOURS)
        val secondDayFirstMoment = firstDayThirdMoment.plus(1, ChronoUnit.HOURS)
        val secondDaySecondMoment = secondDayFirstMoment.plus(1, ChronoUnit.HOURS)
        val now = secondDayFirstMoment.plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(
                    firstDayFirstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                ),
                TemperatureMoment(
                    firstDaySecondMoment,
                    Temperature(1.0, Temperature.Unit.DegreesCelsius)
                ),
                TemperatureMoment(
                    firstDayThirdMoment,
                    Temperature(2.0, Temperature.Unit.DegreesCelsius)
                ),
                TemperatureMoment(
                    secondDayFirstMoment,
                    Temperature(3.0, Temperature.Unit.DegreesCelsius)
                ),
                TemperatureMoment(
                    secondDaySecondMoment,
                    Temperature(4.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstDayFirstMoment,
                    Condition(wmoCode = 10, isDay = true)
                ),
                ConditionMoment(
                    firstDaySecondMoment,
                    Condition(wmoCode = 2, isDay = true)
                ),
                ConditionMoment(
                    firstDayThirdMoment,
                    Condition(wmoCode = 3, isDay = false)
                ),
                ConditionMoment(
                    secondDayFirstMoment,
                    Condition(wmoCode = 4, isDay = false)
                ),
                ConditionMoment(
                    secondDaySecondMoment,
                    Condition(wmoCode = 5, isDay = false)
                ),
            )
        )
        val popPeriod = PopPeriod(
            moments = listOf(
                PopMoment(firstDayFirstMoment, Pop(5.0)),
                PopMoment(firstDaySecondMoment, Pop(5.0)),
                PopMoment(firstDayThirdMoment, Pop(5.0)),
                PopMoment(secondDayFirstMoment, Pop(5.0)),
                PopMoment(secondDaySecondMoment, Pop(5.0)),
            )
        )
        val summary = getDailySummary(now, temperaturePeriod, conditionPeriod, popPeriod)
        Assert.assertEquals(
            DailySummary(
                minTemp = Temperature(3.0, Temperature.Unit.DegreesCelsius),
                maxTemp = Temperature(4.0, Temperature.Unit.DegreesCelsius),
                days = listOf(
                    DaySummary(
                        isToday = true,
                        time = secondDayFirstMoment.toLocalDate(),
                        tempNow = Temperature(3.0, Temperature.Unit.DegreesCelsius),
                        min = Temperature(3.0, Temperature.Unit.DegreesCelsius),
                        max = Temperature(4.0, Temperature.Unit.DegreesCelsius),
                        desc = Condition(wmoCode = 5, isDay = false),
                        pop = Pop(5.0)
                    )
                )
            ),
            summary
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val popPeriod = PopPeriod(
            listOf(
                PopMoment(
                    firstMoment,
                    Pop(1.0)
                )
            )
        )
        val conditionPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = true)
                )
            )
        )
        val summary = getDailySummary(now, temperaturePeriod, conditionPeriod, popPeriod)
        Assert.assertNull(summary)
    }
}