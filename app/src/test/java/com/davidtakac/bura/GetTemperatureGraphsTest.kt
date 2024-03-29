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
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.graphs.temperature.GraphTemperature
import com.davidtakac.bura.graphs.temperature.TemperatureGraph
import com.davidtakac.bura.graphs.temperature.TemperatureGraphPoint
import com.davidtakac.bura.graphs.temperature.GetTemperatureGraphs
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureMoment
import com.davidtakac.bura.temperature.TemperaturePeriod
import com.davidtakac.bura.units.Units
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class GetTemperatureGraphsTest {
    private val location = GMTLocation
    private val units = Units.Default

    @Test
    fun `combines data into graph points and extracts min max temps`() = runTest {
        val firstMoment = Instant.ofEpochSecond(0)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, Condition(1, true)),
                    ConditionMoment(secondMoment, Condition(2, false)),
                    ConditionMoment(thirdMoment, Condition(3, false))
                )
            )
        )
        val tempRepo = FakeTemperatureRepository(
            TemperaturePeriod(
                listOf(
                    TemperatureMoment(firstMoment, Temperature.fromDegreesCelsius(0.0)),
                    TemperatureMoment(secondMoment, Temperature.fromDegreesCelsius(1.0)),
                    TemperatureMoment(thirdMoment, Temperature.fromDegreesCelsius(2.0))
                )
            )
        )
        val useCase = GetTemperatureGraphs(tempRepo, descRepo)
        val result =
            (useCase(location, units, now) as ForecastResult.Success).data.graphs.first()
        assertEquals(
            TemperatureGraph(
                day = LocalDate.parse("1970-01-01"),
                points = listOf(
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("00:00"),
                            meta = GraphTime.Meta.Past
                        ),
                        temperature = GraphTemperature(
                            value = Temperature.fromDegreesCelsius(0.0),
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
                            value = Temperature.fromDegreesCelsius(1.0),
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
                            value = Temperature.fromDegreesCelsius(2.0),
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
        val firstMoment = Instant.ofEpochSecond(0)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, Condition(1, true)),
                    ConditionMoment(secondMoment, Condition(2, false)),
                    ConditionMoment(thirdMoment, Condition(3, false))
                )
            )
        )
        val tempRepo = FakeTemperatureRepository(
            TemperaturePeriod(
                listOf(
                    TemperatureMoment(firstMoment, Temperature.fromDegreesCelsius(1.0)),
                    TemperatureMoment(secondMoment, Temperature.fromDegreesCelsius(1.0)),
                    TemperatureMoment(thirdMoment, Temperature.fromDegreesCelsius(1.0))
                )
            )
        )
        val useCase = GetTemperatureGraphs(tempRepo, descRepo)
        val result =
            (useCase(location, units, now) as ForecastResult.Success).data.graphs.first()
        assert(result.points.all { it.temperature.meta == GraphTemperature.Meta.Regular })
    }

    @Test
    fun `minimum takes the last min moment`() = runTest {
        val firstMoment = Instant.ofEpochSecond(0)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, Condition(1, true)),
                    ConditionMoment(secondMoment, Condition(2, false)),
                    ConditionMoment(thirdMoment, Condition(3, false))
                )
            )
        )
        val tempRepo = FakeTemperatureRepository(
            TemperaturePeriod(
                listOf(
                    TemperatureMoment(firstMoment, Temperature.fromDegreesCelsius(1.0)),
                    TemperatureMoment(secondMoment, Temperature.fromDegreesCelsius(1.0)),
                    TemperatureMoment(thirdMoment, Temperature.fromDegreesCelsius(2.0))
                )
            )
        )
        val useCase = GetTemperatureGraphs(tempRepo, descRepo)
        val result =
            (useCase(location, units, now) as ForecastResult.Success).data.graphs.first()
        assertEquals(
            LocalTime.parse("01:00"),
            result.points.first { it.temperature.meta == GraphTemperature.Meta.Minimum }.time.value
        )
    }

    @Test
    fun `first data point of next day is included in the graph`() = runTest {
        val firstMoment = Instant.ofEpochSecond(0).plus(23, ChronoUnit.HOURS)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val descRepo = FakeConditionRepository(
            ConditionPeriod(
                listOf(
                    ConditionMoment(firstMoment, Condition(1, true)),
                    ConditionMoment(secondMoment, Condition(2, false)),
                )
            )
        )
        val tempRepo = FakeTemperatureRepository(
            TemperaturePeriod(
                listOf(
                    TemperatureMoment(firstMoment, Temperature.fromDegreesCelsius(2.0)),
                    TemperatureMoment(secondMoment, Temperature.fromDegreesCelsius(1.0)),
                )
            )
        )
        val useCase = GetTemperatureGraphs(tempRepo, descRepo)
        val result =
            (useCase(location, units, now) as ForecastResult.Success).data.graphs.first()
        assertEquals(
            TemperatureGraph(
                day = LocalDate.parse("1970-01-01"),
                points = listOf(
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("23:00"),
                            meta = GraphTime.Meta.Present
                        ),
                        temperature = GraphTemperature(
                            value = Temperature.fromDegreesCelsius(2.0),
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
                            value = Temperature.fromDegreesCelsius(1.0),
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