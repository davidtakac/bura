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

package com.davidtakac.bura.graphs.pop

import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.PopMoment
import com.davidtakac.bura.forecast.parameters.pop.PopPeriod
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class GetPopGraphsTest {
    @Test
    fun `constructs pop graphs`() = runTest {
        val firstMoment = unixEpochStartZdt.plus(22, ChronoUnit.HOURS)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment
        val popPeriod = PopPeriod(
            listOf(
                PopMoment(hourZdt = firstMoment, pop = Pop(0.0)),
                PopMoment(hourZdt = secondMoment, pop = Pop(0.0)),
                PopMoment(hourZdt = thirdMoment, pop = Pop(5.0))
            )
        )
        val conditionPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(hourZdt = firstMoment, condition = Condition(0, true)),
                ConditionMoment(hourZdt = secondMoment, condition = Condition(1, true)),
                ConditionMoment(hourZdt = thirdMoment, condition = Condition(2, true))
            )
        )
        val graphs = getPopGraphs(now, popPeriod, conditionPeriod)
        Assert.assertEquals(
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
                            condition = Condition(0, true)
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
                            condition = Condition(1, true)
                        ),
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("00:00"),
                                meta = GraphTime.Meta.Future
                            ),
                            pop = GraphPop(
                                Pop(5.0),
                                meta = GraphPop.Meta.Maximum
                            ),
                            condition = Condition(2, true)
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
                                Pop(5.0),
                                meta = GraphPop.Meta.Maximum
                            ),
                            condition = Condition(2, true)
                        )
                    )
                )
            ),
            graphs
        )
    }
}