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

package com.davidtakac.bura.summary.pressure

import com.davidtakac.bura.forecast.parameters.pressure.Pressure
import com.davidtakac.bura.forecast.parameters.pressure.PressurePeriod
import java.time.ZonedDateTime

fun getPressureSummary(
    now: ZonedDateTime,
    pressurePeriod: PressurePeriod
): PressureSummary? {
    val nowInstant = now.toInstant()
    val pressureToday = pressurePeriod.dayOn(now.toLocalDate()) ?: return null
    val pressureNow = pressurePeriod[nowInstant]?.pressure ?: return null

    val pastPressureForTrend = pressurePeriod.periodUntil(nowInstant, takeLast = 2)
        ?.firstOrNull()
        ?.pressure ?: return null
    val trend = getPressureTrend(pastPressureForTrend, pressureNow)

    return PressureSummary(
        now = pressureNow,
        average = pressureToday.average,
        trend = trend
    )
}

fun getPressureTrend(
    past: Pressure,
    now: Pressure
): PressureTrend {
    val diffHpa = now.convertTo(Pressure.Unit.Hectopascal).value - past.convertTo(Pressure.Unit.Hectopascal).value
    return when {
        diffHpa <= -1 -> PressureTrend.Falling
        diffHpa >= 1 -> PressureTrend.Rising
        else -> PressureTrend.Stable
    }
}

data class PressureSummary(
    val now: Pressure,
    val average: Pressure,
    val trend: PressureTrend
)

enum class PressureTrend {
    Rising, Falling, Stable
}