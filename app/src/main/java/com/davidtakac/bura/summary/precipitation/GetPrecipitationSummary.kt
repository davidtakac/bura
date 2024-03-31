/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.precipitation

import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.PrecipitationPeriod
import com.davidtakac.bura.precipitation.PrecipitationRepository
import com.davidtakac.bura.units.Units
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val PAST_HOURS = 24
private const val FUTURE_HOURS = 24

class GetPrecipitationSummary(private val repo: PrecipitationRepository) {
    suspend operator fun invoke(
        coords: Coordinates,
        units: Units,
        now: LocalDateTime,
    ): ForecastResult<PrecipitationSummary> {
        val precipPeriod = repo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val past = calculatePast(now, precipPeriod) ?: return ForecastResult.Outdated
        val future = calculateFuture(now, precipPeriod) ?: return ForecastResult.Outdated
        return ForecastResult.Success(PrecipitationSummary(past, future))
    }

    private fun calculatePast(
        now: LocalDateTime,
        period: PrecipitationPeriod
    ): PastPrecipitation? {
        val past = period.momentsUntil(now, takeMoments = PAST_HOURS) ?: return null
        val hours = past.size
        return PastPrecipitation(
            inHours = hours,
            total = past.total.reduce()
        )
    }

    private fun calculateFuture(
        now: LocalDateTime,
        precipitation: PrecipitationPeriod
    ): FuturePrecipitation? {
        val soon = calculateFutureSoon(now, precipitation) ?: return null
        val later = calculateFutureLater(now, precipitation) ?: return soon
        return if (soon.total.value > 0) soon else later
    }

    private fun calculateFutureSoon(
        now: LocalDateTime,
        period: PrecipitationPeriod
    ): FuturePrecipitation.InHours? {
        val future = period.momentsFrom(now, takeMoments = FUTURE_HOURS) ?: return null
        return FuturePrecipitation.InHours(
            inHours = future.size,
            total = future.total.reduce()
        )
    }

    private fun calculateFutureLater(
        now: LocalDateTime,
        period: PrecipitationPeriod
    ): FuturePrecipitation? {
        val nowAfterFutureHours = now.plus(FUTURE_HOURS + 1L, ChronoUnit.HOURS)
        val afterFuture = period.momentsFrom(nowAfterFutureHours)?.daysFrom(nowAfterFutureHours.toLocalDate()) ?: return null
        val firstPrecipitation = afterFuture.firstOrNull { it.total.value > 0 }
        return if (firstPrecipitation == null) {
            FuturePrecipitation.None(inDays = afterFuture.size)
        } else {
            FuturePrecipitation.OnDay(
                onDay = firstPrecipitation.first().hour.toLocalDate(),
                total = firstPrecipitation.total.reduce()
            )
        }
    }
}

data class PrecipitationSummary(
    val past: PastPrecipitation,
    val future: FuturePrecipitation,
)

data class PastPrecipitation(
    val inHours: Int,
    val total: Precipitation
)

sealed interface FuturePrecipitation {
    data class InHours(
        val inHours: Int,
        val total: Precipitation
    ) : FuturePrecipitation

    data class OnDay(
        val onDay: LocalDate,
        val total: Precipitation
    ) : FuturePrecipitation

    data class None(
        val inDays: Int
    ) : FuturePrecipitation
}