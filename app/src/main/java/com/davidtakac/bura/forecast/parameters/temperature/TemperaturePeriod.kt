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

package com.davidtakac.bura.forecast.parameters.temperature

import com.davidtakac.bura.forecast.HourPeriod
import java.time.Instant
import java.time.LocalDate

class TemperaturePeriod(moments: List<TemperatureMoment>) : HourPeriod<TemperatureMoment>(moments) {
    val minimum get() = minOf { it.temperature }

    val maximum get() = maxOf { it.temperature }

    fun dayPeriodOn(day: LocalDate) =
        dayMomentsOn(day)?.let { TemperaturePeriod(it) }

    fun periodFrom(hourInclusive: Instant, take: Int? = null) =
        momentsFrom(hourInclusive, take)?.let { TemperaturePeriod(it) }

    fun dayPeriodsFrom(dayInclusive: LocalDate, take: Int? = null) =
        dayMomentsFrom(dayInclusive, take)?.map { TemperaturePeriod(it) }

    fun convertTo(unit: Temperature.Unit): TemperaturePeriod {
        if (first().temperature.unit == unit) return this
        val convertedMoments = map { TemperatureMoment(it.timeZdt, it.temperature.convertTo(unit)) }
        return TemperaturePeriod(convertedMoments)
    }
}
