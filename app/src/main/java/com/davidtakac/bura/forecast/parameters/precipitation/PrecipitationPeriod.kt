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

package com.davidtakac.bura.forecast.parameters.precipitation

import com.davidtakac.bura.forecast.HourPeriod
import java.time.LocalDate
import java.time.LocalDateTime

class PrecipitationPeriod(moments: List<PrecipitationMoment>) : HourPeriod<PrecipitationMoment>(moments) {
    val total: MixedPrecipitation get() = map { it.precipitation }.reduce { acc, precipitation -> acc + precipitation }
    val max: MixedPrecipitation get() = maxOf { it.precipitation }

    fun periodUntil(hourExclusive: LocalDateTime, takeLast: Int? = null) =
        momentsUntil(hourExclusive, takeLast)?.let { PrecipitationPeriod(it) }

    fun periodFrom(hourInclusive: LocalDateTime, take: Int? = null) =
        momentsFrom(hourInclusive, take)?.let { PrecipitationPeriod(it) }

    fun dayPeriodsFrom(dayInclusive: LocalDate, take: Int? = null) =
        dayMomentsFrom(dayInclusive, take)?.map { PrecipitationPeriod(it) }

    fun convertTo(unit: Precipitation.Unit): PrecipitationPeriod {
        if (first().precipitation.unit == unit) return this
        val convertedMoments = map { PrecipitationMoment(it.zdt, it.precipitation.convertTo(unit)) }
        return PrecipitationPeriod(convertedMoments)
    }
}