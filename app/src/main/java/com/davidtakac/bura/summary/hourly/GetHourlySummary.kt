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

package com.davidtakac.bura.summary.hourly

import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.pop.Pop
import com.davidtakac.bura.forecast.parameters.pop.PopPeriod
import com.davidtakac.bura.forecast.parameters.sun.SunEvent
import com.davidtakac.bura.forecast.parameters.sun.SunPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import java.time.LocalDateTime
import java.time.ZonedDateTime

fun getHourlySummary(
    now: ZonedDateTime,
    tempPeriod: TemperaturePeriod,
    popPeriod: PopPeriod,
    condPeriod: ConditionPeriod,
    sunPeriod: SunPeriod?
): List<HourSummary>? {
    val nowInstant = now.toInstant()
    val futureTemps = tempPeriod.periodFrom(nowInstant, take = 24) ?: return null
    val futurePops = popPeriod.periodFrom(nowInstant, take = 24) ?: return null
    val futureDesc = condPeriod.periodFrom(nowInstant, take = 24) ?: return null
    val combinedWeatherData = buildList {
        for (i in futureTemps.indices) {
            add(
                HourSummary.Weather(
                    time = futureTemps[i].timeZdt.toLocalDateTime(),
                    isNow = i == 0,
                    temp = futureTemps[i].temperature,
                    pop = futurePops[i].pop.takeIf { it.value > 0 },
                    desc = futureDesc[i].condition
                )
            )
        }
    }
    val combinedSunData = sunPeriod
        ?.momentsFrom(nowInstant, takeMomentsUpToHoursInFuture = 24)
        ?.map {
            HourSummary.Sun(
                time = it.timeZdt.toLocalDateTime(),
                event = it.event
            )
        }
        ?: listOf()

    return (combinedWeatherData + combinedSunData).sortedBy { it.time }
}

sealed interface HourSummary {
    val time: LocalDateTime

    data class Weather(
        override val time: LocalDateTime,
        val isNow: Boolean,
        val temp: Temperature,
        val pop: Pop?,
        val desc: Condition
    ) : HourSummary

    data class Sun(
        override val time: LocalDateTime,
        val event: SunEvent
    ) : HourSummary
}