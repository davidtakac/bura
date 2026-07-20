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

package com.davidtakac.bura.summary.wind

import com.davidtakac.bura.forecast.parameters.gust.GustPeriod
import com.davidtakac.bura.forecast.parameters.wind.Wind
import com.davidtakac.bura.forecast.parameters.wind.WindPeriod
import com.davidtakac.bura.forecast.parameters.wind.WindSpeed
import java.time.ZonedDateTime

fun getWindSummary(
    now: ZonedDateTime,
    windPeriod: WindPeriod,
    gustPeriod: GustPeriod
): WindSummary? {
    return WindSummary(
        windNow = windPeriod[now.toInstant()]?.wind ?: return null,
        gustNow = gustPeriod[now.toInstant()]?.speed ?: return null
    )
}

data class WindSummary(
    val windNow: Wind,
    val gustNow: WindSpeed
)