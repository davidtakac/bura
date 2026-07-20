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

package com.davidtakac.bura.graphs.temperature

import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class GetTemperatureGraphsTest {
    @Test
    fun `combines data into graph points and extracts min max temps`() = runTest {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, Condition(1, true)),
                ConditionMoment(secondMoment, Condition(2, false)),
                ConditionMoment(thirdMoment, Condition(3, false))
            )
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(firstMoment, Temperature(0.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(thirdMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius))
            )
        )
        val result = getTemperatureGraphs(
            now,
            tempPeriod,
            condPeriod
        )?.graphs?.first()
        Assert.assertEquals(
            TemperatureGraph(
                day = LocalDate.parse("1970-01-01"),
                points = listOf(
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("00:00"),
                            meta = GraphTime.Meta.Past
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Minimum
                        ),
                        condition = Condition(1, true),

                        ),
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("01:00"),
                            meta = GraphTime.Meta.Present
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(1.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Regular
                        ),
                        condition = Condition(2, false),

                        ),
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("02:00"),
                            meta = GraphTime.Meta.Future
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(2.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Maximum
                        ),
                        condition = Condition(3, false),

                        )
                )
            ),
            result
        )
    }

    @Test
    fun `when all temps the same, min max equals the first temperature`() = runTest {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, Condition(1, true)),
                ConditionMoment(secondMoment, Condition(2, false)),
                ConditionMoment(thirdMoment, Condition(3, false))
            )
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(firstMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(thirdMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius))
            )
        )
        val result = getTemperatureGraphs(
            now,
            tempPeriod,
            condPeriod
        )?.graphs?.first()
        assert(result?.points?.all { it.temperature.meta == GraphTemperature.Meta.Regular } == true)
    }

    @Test
    fun `minimum takes the last min moment`() = runTest {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, Condition(1, true)),
                ConditionMoment(secondMoment, Condition(2, false)),
                ConditionMoment(thirdMoment, Condition(3, false))
            )
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(firstMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(thirdMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius))
            )
        )
        val result = getTemperatureGraphs(
            now,
            tempPeriod,
            condPeriod
        )?.graphs?.first()
        Assert.assertEquals(
            LocalTime.parse("01:00"),
            result?.points?.first { it.temperature.meta == GraphTemperature.Meta.Minimum }?.time?.value
        )
    }

    @Test
    fun `first data point of next day is included in the graph`() = runTest {
        val firstMoment = unixEpochStartZdt.plus(23, ChronoUnit.HOURS)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, Condition(1, true)),
                ConditionMoment(secondMoment, Condition(2, false)),
            )
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(firstMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
            )
        )
        val result = getTemperatureGraphs(
            now,
            tempPeriod,
            condPeriod
        )?.graphs?.first()
        Assert.assertEquals(
            TemperatureGraph(
                day = LocalDate.parse("1970-01-01"),
                points = listOf(
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("23:00"),
                            meta = GraphTime.Meta.Present
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(2.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Regular
                        ),

                        condition = Condition(1, true)
                    ),
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("00:00"),
                            meta = GraphTime.Meta.Future
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(1.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Regular
                        ),
                        condition = Condition(2, false)
                    )
                )
            ),
            result
        )
    }
}