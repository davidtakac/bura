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

package com.davidtakac.bura.graphs.precipitation

import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.precipitation.MixedPrecipitation
import com.davidtakac.bura.forecast.parameters.precipitation.PrecipitationPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

fun getPrecipitationGraphs(
    now: ZonedDateTime,
    precipPeriod: PrecipitationPeriod,
    condPeriod: ConditionPeriod
): PrecipitationGraphs? {
    val precipDays = precipPeriod.dayPeriodsFrom(now.toLocalDate()) ?: return null
    val condDays = condPeriod.dayPeriodsFrom(now.toLocalDate()) ?: return null
    return PrecipitationGraphs(
        now = now.toInstant(),
        max = precipDays.maxOf { it.max },
        graphs = precipDays.mapIndexed { dayIdx, day ->
            PrecipitationGraph(
                day = day.first().timeZdt.toLocalDate(),
                points = buildList {
                    addAll(
                        day.mapIndexed { momentIdx, moment ->
                            PrecipitationGraphPoint(
                                time = moment.timeZdt,
                                precip = moment.precipitation,
                                cond = condDays[dayIdx][momentIdx].condition
                            )
                        }
                    )
                }
            )
        }
    )
}

data class PrecipitationGraphs(
    val now: Instant,
    val max: MixedPrecipitation,
    val graphs: List<PrecipitationGraph>
)

data class PrecipitationGraph(
    val day: LocalDate,
    val points: List<PrecipitationGraphPoint>
)

data class PrecipitationGraphPoint(
    val time: ZonedDateTime,
    val precip: MixedPrecipitation,
    val cond: Condition
)