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
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

fun getTemperatureGraphs(
    now: ZonedDateTime,
    tempPeriod: TemperaturePeriod,
    condPeriod: ConditionPeriod
): TemperatureGraphs? {
    val tempDays = tempPeriod.dayPeriodsFrom(now.toLocalDate()) ?: return null
    val conditionDays = condPeriod.dayPeriodsFrom(now.toLocalDate()) ?: return null
    return TemperatureGraphs(
        now = now.toInstant(),
        min = tempPeriod.minimum,
        max = tempPeriod.maximum,
        graphs = getGraphs(
            now = now,
            tempDays = tempDays,
            conditionDays = conditionDays
        )
    )
}

private fun getGraphs(
    now: ZonedDateTime,
    tempDays: List<TemperaturePeriod>,
    conditionDays: List<ConditionPeriod>
): List<TemperatureGraph> = buildList {
    for (i in tempDays.indices) {
        add(
            getGraph(
                now = now,
                tempDay = tempDays[i],
                conditionDay = conditionDays[i],
            )
        )
    }
}

private fun getGraph(
    now: ZonedDateTime,
    tempDay: TemperaturePeriod,
    conditionDay: ConditionPeriod
): TemperatureGraph = TemperatureGraph(
    day = tempDay.first().timeZdt.toLocalDate(),
    points = buildList {
        for (i in tempDay.indices) {
            add(
                getPoint(
                    now = now,
                    tempMoment = tempDay[i],
                    conditionMoment = conditionDay[i]
                )
            )
        }
    }
)

private fun getPoint(
    now: ZonedDateTime,
    tempMoment: TemperatureMoment,
    conditionMoment: ConditionMoment
): TemperatureGraphPoint = TemperatureGraphPoint(
    time = GraphTime(tempMoment.timeZdt, now.toInstant()),
    temperature = tempMoment.temperature,
    condition = conditionMoment.condition,
)

data class TemperatureGraphs(
    val now: Instant,
    val min: Temperature,
    val max: Temperature,
    val graphs: List<TemperatureGraph>
)

data class TemperatureGraph(
    val day: LocalDate,
    val points: List<TemperatureGraphPoint>
)

data class TemperatureGraphPoint(
    val time: GraphTime,
    val temperature: Temperature,
    val condition: Condition
)