/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.pressure

import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.pressure.PressureRepository
import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.units.Units
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class GetPressureSummary(private val repo: PressureRepository) {
    suspend operator fun invoke(
        coords: Coordinates,
        units: Units,
        now: LocalDateTime
    ): ForecastResult<PressureSummary> {
        val pressurePeriod = repo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val pressureToday = pressurePeriod.getDay(now.toLocalDate()) ?: return ForecastResult.Outdated
        val pressureNow = pressurePeriod[now]?.pressure ?: return ForecastResult.Outdated

        val nowHpa = pressureNow.convertTo(Pressure.Unit.Hectopascal).value
        val pastHpa = pressurePeriod.momentsUntil(now, takeMoments = 2)?.firstOrNull()
            ?.pressure?.convertTo(Pressure.Unit.Hectopascal)?.value
            ?: return ForecastResult.Outdated
        val diffHpa = (nowHpa - pastHpa).absoluteValue
        val trend = when {
            diffHpa < 1 -> PressureTrend.Stable
            diffHpa > 0 -> PressureTrend.Rising
            else -> PressureTrend.Falling
        }

        return ForecastResult.Success(PressureSummary(
            now = pressureNow,
            average = pressureToday.average,
            trend = trend
        ))
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