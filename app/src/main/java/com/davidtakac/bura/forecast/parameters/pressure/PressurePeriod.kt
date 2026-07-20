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

package com.davidtakac.bura.forecast.parameters.pressure

import com.davidtakac.bura.forecast.HourPeriod
import java.time.Instant
import java.time.LocalDate

class PressurePeriod(moments: List<PressureMoment>) : HourPeriod<PressureMoment>(moments) {
    val minimum get() = minOf { it.pressure }

    val average get() = map { it.pressure }.reduce { acc, pressure -> acc + pressure } / size

    fun periodUntil(hourExclusive: Instant, takeLast: Int? = null): PressurePeriod? =
        momentsUntil(hourExclusive, takeLast)?.let { PressurePeriod(it) }

    fun dayOn(day: LocalDate): PressurePeriod? =
        dayMomentsOn(day)?.let { PressurePeriod(it) }

    fun convertTo(unit: Pressure.Unit): PressurePeriod {
        if (first().pressure.unit == unit) return this
        val convertedMoments = map { PressureMoment(it.timeZdt, it.pressure.convertTo(unit)) }
        return PressurePeriod(convertedMoments)
    }
}