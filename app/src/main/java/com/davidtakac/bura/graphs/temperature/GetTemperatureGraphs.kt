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
        minTemp = tempPeriod.minimum,
        maxTemp = tempPeriod.maximum,
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
                nextTempDay = tempDays.getOrNull(i + 1),
                nextConditionDay = conditionDays.getOrNull(i + 1)
            )
        )
    }
}

private fun getGraph(
    now: ZonedDateTime,
    tempDay: TemperaturePeriod,
    conditionDay: ConditionPeriod,
    nextTempDay: TemperaturePeriod?,
    nextConditionDay: ConditionPeriod?
): TemperatureGraph {
    val minTempMoment = tempDay.reversed().minBy { it.temperature }
    val maxTempMoment = tempDay.reversed().maxBy { it.temperature }
    return TemperatureGraph(
        day = tempDay.first().zdt.toLocalDate(),
        points = buildList {
            for (i in tempDay.indices) {
                add(
                    getPoint(
                        now = now,
                        tempMoment = tempDay[i],
                        minTempMoment = minTempMoment,
                        maxTempMoment = maxTempMoment,
                        conditionMoment = conditionDay[i]
                    )
                )
            }
            val firstTempTomorrow = nextTempDay?.firstOrNull()
            if (firstTempTomorrow != null) {
                // The periods must match, so if there is a first temp tomorrow, there
                // must be a matching condition tomorrow too
                val firstConditionTomorrow = nextConditionDay!!.first()
                add(
                    getPoint(
                        now = now,
                        tempMoment = firstTempTomorrow,
                        minTempMoment = minTempMoment,
                        maxTempMoment = maxTempMoment,
                        conditionMoment = firstConditionTomorrow
                    )
                )
            }
        }
    )
}

private fun getPoint(
    now: ZonedDateTime,
    tempMoment: TemperatureMoment,
    minTempMoment: TemperatureMoment,
    maxTempMoment: TemperatureMoment,
    conditionMoment: ConditionMoment
): TemperatureGraphPoint = TemperatureGraphPoint(
    time = GraphTime(tempMoment.zdt, now.toInstant()),
    temperature = GraphTemperature(
        value = tempMoment.temperature,
        meta = getTempMeta(minTempMoment, maxTempMoment, tempMoment)
    ),
    condition = conditionMoment.condition,
)

private fun getTempMeta(
    minTempMoment: TemperatureMoment,
    maxTempMoment: TemperatureMoment,
    tempMoment: TemperatureMoment
): GraphTemperature.Meta =
    when {
        minTempMoment == maxTempMoment -> GraphTemperature.Meta.Regular
        tempMoment == minTempMoment -> GraphTemperature.Meta.Minimum
        tempMoment == maxTempMoment -> GraphTemperature.Meta.Maximum
        else -> GraphTemperature.Meta.Regular
    }

data class TemperatureGraphs(
    val minTemp: Temperature,
    val maxTemp: Temperature,
    val graphs: List<TemperatureGraph>
)

data class TemperatureGraph(
    val day: LocalDate,
    val points: List<TemperatureGraphPoint>
)

data class TemperatureGraphPoint(
    val time: GraphTime,
    val temperature: GraphTemperature,
    val condition: Condition
)

data class GraphTemperature(
    val value: Temperature,
    val meta: Meta
) {
    enum class Meta {
        Minimum, Maximum, Regular
    }
}