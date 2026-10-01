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
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import java.time.LocalDate
import java.time.ZonedDateTime

fun getTemperatureGraphSummaries(
    now: ZonedDateTime,
    tempPeriod: TemperaturePeriod,
    feelsPeriod: TemperaturePeriod,
    condPeriod: ConditionPeriod
): List<TemperatureGraphSummary>? {
    val nowInstant = now.toInstant()
    val tempDays = tempPeriod.dayPeriodsFrom(now.toLocalDate()) ?: return null
    val conditionDays = condPeriod.periodFrom(nowInstant)?.dayPeriodsFrom(now.toLocalDate()) ?: return null
    val feelsLikeNow = feelsPeriod[nowInstant]?.temperature ?: return null

    return tempDays.mapIndexed { idx, tempDay ->
        val day = tempDay.first().timeZdt.toLocalDate()
        val minTemp = tempDay.minimum
        val maxTemp = tempDay.maximum
        val conditionDay = conditionDays[idx]
        val condition = conditionDay[nowInstant]?.condition ?: conditionDay.day ?: conditionDay.night!!
        val nowTemp = tempDay[nowInstant]?.temperature

        TemperatureGraphSummary(
            day = day,
            minTemp = minTemp,
            maxTemp = maxTemp,
            condition = condition,
            now = nowTemp?.let {
                TemperatureGraphNowSummary(
                    temp = nowTemp,
                    feelsLike = feelsLikeNow
                )
            }
        )
    }
}

data class TemperatureGraphSummary(
    val day: LocalDate,
    val minTemp: Temperature,
    val maxTemp: Temperature,
    val condition: Condition,
    val now: TemperatureGraphNowSummary?
)

data class TemperatureGraphNowSummary(
    val temp: Temperature,
    val feelsLike: Temperature
)