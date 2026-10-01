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
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.PopPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import java.time.LocalDate
import java.time.ZonedDateTime

fun getDailySummary(
    now: ZonedDateTime,
    tempPeriod: TemperaturePeriod,
    condPeriod: ConditionPeriod,
    popPeriod: PopPeriod
): DailySummary? {
    val nowInstant = now.toInstant()
    val nowDate = now.toLocalDate()
    val futureTempDays = tempPeriod.dayPeriodsFrom(nowDate) ?: return null
    val popDays = popPeriod.periodFrom(nowInstant)?.dayPeriodsFrom(nowDate) ?: return null
    val descDays = condPeriod.periodFrom(nowInstant)?.dayPeriodsFrom(nowDate) ?: return null
    return DailySummary(
        minTemp = futureTempDays.minOf { it.minimum },
        maxTemp = futureTempDays.maxOf { it.maximum },
        days = buildList {
            for (i in futureTempDays.indices) {
                add(
                    DaySummary(
                        isToday = i == 0,
                        time = futureTempDays[i].first().timeZdt.toLocalDate(),
                        tempNow = futureTempDays[i][nowInstant]?.temperature,
                        min = futureTempDays[i].minimum,
                        max = futureTempDays[i].maximum,
                        pop = popDays[i].maximum.takeIf { it.value > 0 },
                        desc = descDays[i].day ?: descDays[i].night!!
                    )
                )
            }
        }
    )
}

data class DailySummary(
    val minTemp: Temperature,
    val maxTemp: Temperature,
    val days: List<DaySummary>
)

data class DaySummary(
    val isToday: Boolean,
    val time: LocalDate,
    val tempNow: Temperature?,
    val min: Temperature,
    val max: Temperature,
    val pop: Pop?,
    val desc: Condition
)