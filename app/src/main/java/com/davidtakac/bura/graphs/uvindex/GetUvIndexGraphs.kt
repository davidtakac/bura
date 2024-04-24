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

package com.davidtakac.bura.graphs.uvindex

import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.units.Units
import com.davidtakac.bura.uvindex.UvIndex
import com.davidtakac.bura.uvindex.UvIndexRepository
import java.time.LocalDateTime

class GetUvIndexGraphs(private val repo: UvIndexRepository) {
    suspend operator fun invoke(
        coords: Coordinates,
        units: Units,
        now: LocalDateTime
    ): ForecastResult<UvIndexGraphs> {
        val period = repo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val days = period.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
        val graphs = buildList {
            for (i in days.indices) {
                val day = days[i]
                val maxMoment = day.reversed().maxBy { it.uvIndex }
                val points = buildList {
                    addAll(
                        day.map {
                            UvIndexGraphPoint(
                                time = GraphTime(hour = it.hour, now = now),
                                uvIndex = GraphUvIndex(
                                    value = it.uvIndex,
                                    meta = if (it == maxMoment) GraphUvIndex.Meta.Maximum else GraphUvIndex.Meta.Regular
                                )
                            )
                        }
                    )
                    days.getOrNull(i + 1)?.first()?.let {
                        add(
                            UvIndexGraphPoint(
                                time = GraphTime(hour = it.hour, now = now),
                                uvIndex = GraphUvIndex(
                                    value = it.uvIndex,
                                    meta = GraphUvIndex.Meta.Regular
                                )
                            )
                        )
                    }
                }
                add(UvIndexGraph(points))
            }
        }
        return ForecastResult.Success(
            UvIndexGraphs(
                max = period.maximum,
                graphs = graphs
            )
        )
    }
}

data class UvIndexGraphs(val max: UvIndex, val graphs: List<UvIndexGraph>)
data class UvIndexGraph(val points: List<UvIndexGraphPoint>)

data class UvIndexGraphPoint(
    val time: GraphTime,
    val uvIndex: GraphUvIndex
)

data class GraphUvIndex(
    val value: UvIndex,
    val meta: Meta
) {
    enum class Meta {
        Regular, Maximum
    }
}