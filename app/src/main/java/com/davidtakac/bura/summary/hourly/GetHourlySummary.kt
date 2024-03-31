/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.hourly

import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.ConditionRepository
import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.pop.PopRepository
import com.davidtakac.bura.sun.SunEvent
import com.davidtakac.bura.sun.SunRepository
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureRepository
import com.davidtakac.bura.units.Units
import java.time.LocalDateTime

class GetHourlySummary(
    private val tempRepo: TemperatureRepository,
    private val popRepo: PopRepository,
    private val descRepo: ConditionRepository,
    private val sunRepo: SunRepository,
) {
    suspend operator fun invoke(
        coords: Coordinates,
        units: Units,
        now: LocalDateTime
    ): ForecastResult<List<HourSummary>> {
        val tempPeriod = tempRepo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val popPeriod = popRepo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val descPeriod = descRepo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val sunPeriod = sunRepo.period(coords, units)

        val futureTemps = tempPeriod.momentsFrom(now, takeMoments = 24) ?: return ForecastResult.Outdated
        val futurePops = popPeriod.momentsFrom(now, takeMoments = 24) ?: return ForecastResult.Outdated
        val futureDesc = descPeriod.momentsFrom(now, takeMoments = 24) ?: return ForecastResult.Outdated
        val combinedWeatherData = buildList {
            for (i in futureTemps.indices) {
                add(
                    HourSummary.Weather(
                        time = futureTemps[i].hour,
                        isNow = i == 0,
                        temp = futureTemps[i].temperature,
                        pop = futurePops[i].pop.takeIf { it.value > 0 },
                        desc = futureDesc[i].condition
                    )
                )
            }
        }
        val combinedSunData = sunPeriod
            ?.momentsFrom(now, takeMomentsUpToHoursInFuture = 24)
            ?.map {
                HourSummary.Sun(
                    time = it.time,
                    event = it.event
                )
            }
            ?: listOf()

        return ForecastResult.Success((combinedWeatherData + combinedSunData).sortedBy { it.time })
    }
}

sealed interface HourSummary {
    val time: LocalDateTime

    data class Weather(
        override val time: LocalDateTime,
        val isNow: Boolean,
        val temp: Temperature,
        val pop: Pop?,
        val desc: Condition
    ) : HourSummary

    data class Sun(
        override val time: LocalDateTime,
        val event: SunEvent
    ) : HourSummary
}