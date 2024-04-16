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

package com.davidtakac.bura

import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.graphs.pop.GraphPop
import com.davidtakac.bura.graphs.pop.PopGraph
import com.davidtakac.bura.graphs.pop.PopGraphPoint
import com.davidtakac.bura.graphs.pop.GetPopGraphs
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.pop.PopMoment
import com.davidtakac.bura.pop.PopPeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class GetPopGraphsTest {
    

    @Test
    fun `constructs pop graphs`() = runTest {
        val firstMoment = unixEpochStart.plus(22, ChronoUnit.HOURS)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment
        val popRepo = FakePopRepository(
            PopPeriod(
                listOf(
                    PopMoment(hour = firstMoment, pop = Pop(0.0)),
                    PopMoment(hour = secondMoment, pop = Pop(0.0)),
                    PopMoment(hour = thirdMoment, pop = Pop(1.0))
                )
            )
        )
        val useCase = GetPopGraphs(popRepo)
        val graphs = (useCase(coords, units, now) as ForecastResult.Success).data
        assertEquals(
            listOf(
                PopGraph(
                    day = LocalDate.parse("1970-01-01"),
                    points = listOf(
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("22:00"),
                                meta = GraphTime.Meta.Past
                            ),
                            pop = GraphPop(
                                Pop(0.0),
                                meta = GraphPop.Meta.Regular
                            ),
                        ),
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("23:00"),
                                meta = GraphTime.Meta.Present
                            ),
                            pop = GraphPop(
                                Pop(0.0),
                                meta = GraphPop.Meta.Regular
                            ),
                        ),
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("00:00"),
                                meta = GraphTime.Meta.Future
                            ),
                            pop = GraphPop(
                                Pop(1.0),
                                meta = GraphPop.Meta.Maximum
                            ),
                        )
                    )
                ),
                PopGraph(
                    day = LocalDate.parse("1970-01-02"),
                    points = listOf(
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("00:00"),
                                meta = GraphTime.Meta.Future
                            ),
                            pop = GraphPop(
                                Pop(1.0),
                                meta = GraphPop.Meta.Maximum
                            ),
                        )
                    )
                )
            ),
            graphs
        )
    }
}