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
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

fun getPopGraphs(
    now: ZonedDateTime,
    popPeriod: PopPeriod,
    conditionPeriod: ConditionPeriod,
): PopGraphs? {
    val popDays = popPeriod.dayPeriodsFrom(now.toLocalDate()) ?: return null
    val conditionDays = conditionPeriod.dayPeriodsFrom(now.toLocalDate()) ?: return null
    return PopGraphs(
        now = now.toInstant(),
        graphs = popDays.mapIndexed { idx, popDay ->
            val conditionDay = conditionDays[idx]
            val popTomorrow = popDays.getOrNull(idx + 1)
            val conditionTomorrow = conditionDays.getOrNull(idx + 1)
            getPopGraph(popDay, conditionDay, popTomorrow, conditionTomorrow)
        }
    )
}

private fun getPopGraph(
    popDay: PopPeriod,
    conditionDay: ConditionPeriod,
    popTomorrow: PopPeriod?,
    conditionTomorrow: ConditionPeriod?
): PopGraph {
    return PopGraph(
        day = popDay.first().timeZdt.toLocalDate(),
        points = buildList {
            val firstPopTomorrow = popTomorrow?.first()
            val popDayAdjusted: PopPeriod
            val conditionDayAdjusted: ConditionPeriod
            if (firstPopTomorrow != null) {
                // To avoid an empty space at the end of every day, we add the first pop
                // of tomorrow for completeness
                val firstConditionTomorrow = conditionTomorrow!!.first()
                popDayAdjusted = PopPeriod(popDay + firstPopTomorrow)
                conditionDayAdjusted = ConditionPeriod(conditionDay + firstConditionTomorrow)
            } else {
                popDayAdjusted = popDay
                conditionDayAdjusted = conditionDay
            }
            for (i in popDayAdjusted.indices) {
                val popMoment = popDayAdjusted[i]
                val conditionMoment = conditionDayAdjusted[i]
                add(getPoint(popMoment, conditionMoment))
            }
        }
    )
}

private fun getPoint(
    moment: PopMoment,
    conditionMoment: ConditionMoment,
): PopGraphPoint = PopGraphPoint(
    time = moment.timeZdt,
    pop = moment.pop,
    condition = conditionMoment.condition
)

data class PopGraphs(
    val now: Instant,
    val graphs: List<PopGraph>,
)

data class PopGraph(
    val day: LocalDate,
    val points: List<PopGraphPoint>
)

data class PopGraphPoint(
    val time: ZonedDateTime,
    val pop: Pop,
    val condition: Condition
)