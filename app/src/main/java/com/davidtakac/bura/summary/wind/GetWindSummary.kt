/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.wind

import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.gust.GustRepository
import com.davidtakac.bura.place.Location
import com.davidtakac.bura.units.Units
import com.davidtakac.bura.wind.Wind
import com.davidtakac.bura.wind.WindRepository
import com.davidtakac.bura.wind.WindSpeed
import java.time.LocalDateTime

class GetWindSummary(
    private val windRepo: WindRepository,
    private val gustRepo: GustRepository,
) {
    suspend operator fun invoke(
        location: Location,
        units: Units,
        now: LocalDateTime
    ): ForecastResult<WindSummary> {
        val windPeriod = windRepo.period(location, units) ?: return ForecastResult.FailedToDownload
        val gustPeriod = gustRepo.period(location, units) ?: return ForecastResult.FailedToDownload
        return ForecastResult.Success(
            WindSummary(
                windNow = windPeriod[now]?.wind ?: return ForecastResult.Outdated,
                gustNow = gustPeriod[now]?.speed ?: return ForecastResult.Outdated
            )
        )
    }
}

data class WindSummary(
    val windNow: Wind,
    val gustNow: WindSpeed
)