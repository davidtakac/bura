/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.graphs

import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.ConditionRepository
import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.PrecipitationRepository
import com.davidtakac.bura.units.Units
import java.time.LocalDate
import java.time.LocalDateTime

class GetPrecipitationGraphs(
    private val precipRepo: PrecipitationRepository,
    private val condRepo: ConditionRepository
) {
    suspend operator fun invoke(
        coords: Coordinates,
        units: Units,
        now: LocalDateTime
    ): ForecastResult<PrecipitationGraphs> {
        val precip = precipRepo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val cond = condRepo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val precipDays = precip.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
        val condDays = cond.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
        return ForecastResult.Success(
            data = PrecipitationGraphs(
                max = precipDays.maxOf { it.max },
                graphs = precipDays.mapIndexed { dayIdx, day ->
                    PrecipitationGraph(
                        day = day.first().hour.toLocalDate(),
                        points = buildList {
                            addAll(
                                day.mapIndexed { momentIdx, moment ->
                                    PrecipitationGraphPoint(
                                        time = GraphTime(
                                            hour = moment.hour,
                                            now = now
                                        ),
                                        precip = moment.precipitation,
                                        cond = condDays[dayIdx][momentIdx].condition
                                    )
                                }
                            )
                            val firstPrecipMomentTomorrow = precipDays.getOrNull(dayIdx + 1)?.first()
                            if (firstPrecipMomentTomorrow != null) {
                                add(
                                    PrecipitationGraphPoint(
                                        time = GraphTime(
                                            hour = firstPrecipMomentTomorrow.hour,
                                            now = now
                                        ),
                                        precip = firstPrecipMomentTomorrow.precipitation,
                                        cond = condDays[dayIdx + 1][0].condition
                                    )
                                )
                            }
                        }
                    )
                }
            )
        )
    }
}

data class PrecipitationGraphs(
    val max: Precipitation,
    val graphs: List<PrecipitationGraph>
)

data class PrecipitationGraph(
    val day: LocalDate,
    val points: List<PrecipitationGraphPoint>
)

data class PrecipitationGraphPoint(
    val time: GraphTime,
    val precip: Precipitation,
    val cond: Condition
)