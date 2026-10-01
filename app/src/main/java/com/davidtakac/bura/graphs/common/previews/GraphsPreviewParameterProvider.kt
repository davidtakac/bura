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

package com.davidtakac.bura.graphs.common.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

private const val momentCount = 30
abstract class GraphsPreviewParameterProvider<T>(
    private val graphPreviewValues: List<GraphPreviewValues>,
    private val nowPositions: List<GraphPreviewNowPosition> = GraphPreviewNowPosition.entries,
    private val dstSwitches: List<GraphPreviewDstSwitch> = GraphPreviewDstSwitch.defaultSwitches(),
) : PreviewParameterProvider<Pair<String, T>> {
    override val values: Sequence<Pair<String, T>>
        get() {
            val list = mutableListOf<Pair<String, T>>()
            for (nowPosition in nowPositions) {
                for (dstSwitch in dstSwitches) {
                    for (graphPreviewValues in graphPreviewValues) {
                        val label = "$nowPosition, $dstSwitch, $graphPreviewValues"
                        val times = generateTimes(dstSwitch)
                        val graphs = generateGraphs(
                            times = times,
                            nowPosition = nowPosition,
                            dstSwitch = dstSwitch,
                            values = graphPreviewValues
                        )
                        list.add(label to graphs)
                    }
                }
            }
            return sequenceOf(*list.toTypedArray())
        }

    abstract fun generateGraphs(
        times: List<ZonedDateTime>,
        nowPosition: GraphPreviewNowPosition,
        dstSwitch: GraphPreviewDstSwitch,
        values: GraphPreviewValues
    ): T

    protected fun getNow(
        times: List<ZonedDateTime>,
        nowPosition: GraphPreviewNowPosition
    ): ZonedDateTime {
        return times[
            when (nowPosition) {
                GraphPreviewNowPosition.Start -> 0
                GraphPreviewNowPosition.Middle -> times.lastIndex / 2
                GraphPreviewNowPosition.End -> times.indexOfLast { it.toLocalDate() == times.first().toLocalDate() }
            }
        ]
    }

    private fun generateTimes(dstSwitch: GraphPreviewDstSwitch): List<ZonedDateTime> {
        val startTime = Instant.ofEpochSecond(1781654400) // 00:00 in UTC
        val tzBeforeSwitch = ZoneId.ofOffset("GMT", ZoneOffset.ofTotalSeconds(0))
        val tzAfterSwitch = ZoneId.ofOffset(
            "GMT",
            ZoneOffset.ofTotalSeconds(
                when (dstSwitch) {
                    GraphPreviewDstSwitch.None -> 0
                    is GraphPreviewDstSwitch.FallBack -> -dstSwitch.seconds
                    is GraphPreviewDstSwitch.SpringForward -> dstSwitch.seconds
                }
            )
        )
        return buildList {
            repeat(momentCount) {
                add(
                    startTime
                        .plus(it.toLong(), ChronoUnit.HOURS)
                        .atZone(if (it <= 3) tzBeforeSwitch else tzAfterSwitch)
                )
            }
        }
    }
}