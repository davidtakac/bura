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

import com.davidtakac.bura.forecast.parameters.precipitation.Precipitation
import com.davidtakac.bura.forecast.parameters.precipitation.PrecipitationPeriod
import java.time.LocalDate
import java.time.ZonedDateTime

private const val PAST_HOURS = 24
private const val FUTURE_HOURS = 24

fun getPrecipitationTotals(
    now: ZonedDateTime,
    precipPeriod: PrecipitationPeriod
): List<PrecipitationTotal>? {
    val today = getToday(precipPeriod, now) ?: return null
    val days = precipPeriod.dayPeriodsFrom(now.toLocalDate()) ?: return null
    val daysAfterToday = days.subList(1, days.size)
    return buildList {
        add(today)
        addAll(
            daysAfterToday.map { day ->
                PrecipitationTotal.OtherDay(
                    day = day.first().timeZdt.toLocalDate(),
                    total = day.total.reduce()
                )
            }
        )
    }
}

private fun getToday(period: PrecipitationPeriod, now: ZonedDateTime): PrecipitationTotal.Today? {
    val nowInstant = now.toInstant()
    val past = period.periodUntil(nowInstant, takeLast = PAST_HOURS) ?: return null
    val future = period.periodFrom(nowInstant, take = FUTURE_HOURS) ?: return null
    return PrecipitationTotal.Today(
        day = now.toLocalDate(),
        past = TotalPrecipitationInHours(
            hours = past.size,
            total = past.total.reduce()
        ),
        future = TotalPrecipitationInHours(
            hours = future.size,
            total = future.total.reduce()
        )
    )
}

sealed interface PrecipitationTotal {
    val day: LocalDate

    data class Today(
        override val day: LocalDate,
        val past: TotalPrecipitationInHours,
        val future: TotalPrecipitationInHours
    ) : PrecipitationTotal

    data class OtherDay(
        override val day: LocalDate,
        val total: Precipitation
    ) : PrecipitationTotal
}

data class TotalPrecipitationInHours(
    val hours: Int,
    val total: Precipitation
)