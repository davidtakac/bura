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
import com.davidtakac.bura.summary.daily.DailySummary
import com.davidtakac.bura.summary.daily.GetDailySummary
import com.davidtakac.bura.summary.daily.DaySummary
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureMoment
import com.davidtakac.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.pow

class GetDailySummaryTest {
    

    @Test
    fun `groups moments into days and summarizes them`() = runTest {
        val firstDayFirstMoment = unixEpochStart.plus(21, ChronoUnit.HOURS)
        val firstDaySecondMoment = firstDayFirstMoment.plus(1, ChronoUnit.HOURS)
        val firstDayThirdMoment = firstDaySecondMoment.plus(1, ChronoUnit.HOURS)
        val secondDayFirstMoment = firstDayThirdMoment.plus(1, ChronoUnit.HOURS)
        val secondDaySecondMoment = secondDayFirstMoment.plus(1, ChronoUnit.HOURS)
        val now = secondDayFirstMoment.plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstDayFirstMoment, Temperature.fromDegreesCelsius(0.0)),
                TemperatureMoment(firstDaySecondMoment, Temperature.fromDegreesCelsius(1.0)),
                TemperatureMoment(firstDayThirdMoment, Temperature.fromDegreesCelsius(2.0)),
                TemperatureMoment(secondDayFirstMoment, Temperature.fromDegreesCelsius(3.0)),
                TemperatureMoment(secondDaySecondMoment, Temperature.fromDegreesCelsius(4.0))
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
        val useCase = GetDailySummary(
            tempRepo = FakeTemperatureRepository(temperaturePeriod),
            descRepo = FakeConditionRepository(conditionPeriod),
            popRepo = FakePopRepository(popPeriod),
        )
        val summary = useCase(coords, units, now)
        assertEquals(
            ForecastResult.Success(
                DailySummary(
                    minTemp = Temperature.fromDegreesCelsius(3.0),
                    maxTemp = Temperature.fromDegreesCelsius(4.0),
                    days = listOf(
                        DaySummary(
                            isToday = true,
                            time = secondDayFirstMoment.atZone(ZoneId.of("GMT")).toLocalDate(),
                            tempNow = Temperature.fromDegreesCelsius(3.0),
                            min = Temperature.fromDegreesCelsius(3.0),
                            max = Temperature.fromDegreesCelsius(4.0),
                            desc = Condition(wmoCode = 5, isDay = false),
                            pop = Pop((1 - 0.95.pow(2)) * 100)
                        )
                    )
                )
            ),
            summary
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(0.0)
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
        val useCase = GetDailySummary(
            tempRepo = FakeTemperatureRepository(temperaturePeriod),
            descRepo = FakeConditionRepository(conditionPeriod),
            popRepo = FakePopRepository(popPeriod),
        )
        val summary = useCase(coords, units, now)
        assertEquals(ForecastResult.Outdated, summary)
    }
}