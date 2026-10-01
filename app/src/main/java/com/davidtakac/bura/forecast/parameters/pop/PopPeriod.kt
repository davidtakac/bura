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

package com.davidtakac.bura.forecast.parameters.pop

import com.davidtakac.bura.forecast.HourPeriod
import java.time.Instant
import java.time.LocalDate

class PopPeriod(moments: List<PopMoment>) : HourPeriod<PopMoment>(moments) {
    val maximum get() = maxOf { it.pop }

    fun periodFrom(hourInclusive: Instant, take: Int? = null) =
        momentsFrom(hourInclusive, take)?.let { PopPeriod(it) }

    fun dayPeriodsFrom(dayInclusive: LocalDate, take: Int? = null) =
        dayMomentsFrom(dayInclusive, take)?.map { PopPeriod(it) }
}