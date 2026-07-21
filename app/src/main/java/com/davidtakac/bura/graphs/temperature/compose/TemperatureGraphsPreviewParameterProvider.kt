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

package com.davidtakac.bura.graphs.temperature.compose

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.graphs.temperature.TemperatureGraphs
import com.davidtakac.bura.graphs.temperature.getTemperatureGraphs
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class TemperatureGraphsPreviewParameterProvider : PreviewParameterProvider<Pair<String, TemperatureGraphs>> {
    override val values: Sequence<Pair<String, TemperatureGraphs>>
        get() {
            val list = mutableListOf<Pair<String, TemperatureGraphs>>()
            for (nowPosition in NowPosition.entries) {
                for (dstSwitch in DstSwitch.testSwitches()) {
                    for (graphValues in GraphValues.testValues()) {
                        list.add("$nowPosition, $dstSwitch, $graphValues" to generateGraphs(nowPosition, dstSwitch, graphValues))
                    }
                }
            }
            return sequenceOf(*list.toTypedArray())
        }
}

private const val momentCount = 30
private fun generateGraphs(
    nowPosition: NowPosition = NowPosition.Middle,
    dstSwitch: DstSwitch = DstSwitch.None,
    graphValues: GraphValues = GraphValues.Random(from = -2.0, until = 5.0),
): TemperatureGraphs {
    val startTime = Instant.ofEpochSecond(1781654400) // 00:00 in UTC
    val tzBeforeSwitch = ZoneId.ofOffset("GMT", ZoneOffset.ofTotalSeconds(0))
    val tzAfterSwitch = ZoneId.ofOffset(
        "GMT",
        ZoneOffset.ofTotalSeconds(
            when (dstSwitch) {
                DstSwitch.None -> 0
                is DstSwitch.FallBack -> -dstSwitch.seconds
                is DstSwitch.SpringForward -> dstSwitch.seconds
            }
        )
    )
    val times = buildList {
        repeat(momentCount) {
            add(
                startTime
                    .plus(it.toLong(), ChronoUnit.HOURS)
                    .atZone(if (it <= 3) tzBeforeSwitch else tzAfterSwitch)
            )
        }
    }
    val temperatures = buildList(momentCount) {
        repeat(momentCount) {
            add(
                Temperature(
                    value = when (graphValues) {
                        is GraphValues.Flat -> graphValues.value
                        is GraphValues.Random -> Random.nextDouble(graphValues.from, graphValues.until)
                    },
                    unit = Temperature.Unit.DegreesCelsius
                )
            )
        }
    }
    val tempPeriod = TemperaturePeriod(
        times.mapIndexed { index, time ->
            TemperatureMoment(
                timeZdt = time,
                temperature = temperatures[index]
            )
        }
    )
    val condPeriod = ConditionPeriod(
        times.map {
            ConditionMoment(
                timeZdt = it,
                condition = Condition(
                    wmoCode = 0,
                    isDay = it.toLocalTime().hour >= 7
                )
            )
        }
    )
    val graphs = getTemperatureGraphs(
        now = times[
            when (nowPosition) {
                NowPosition.Start -> 0
                NowPosition.Middle -> times.lastIndex / 2
                NowPosition.End -> times.indexOfLast { it.toLocalDate() == times.first().toLocalDate() }
            }
        ],
        tempPeriod = tempPeriod,
        condPeriod = condPeriod
    )
    return graphs!!
}

private enum class NowPosition {
    Start, Middle, End
}

private sealed interface DstSwitch {
    data object None : DstSwitch
    data class FallBack(val seconds: Int) : DstSwitch
    data class SpringForward(val seconds: Int) : DstSwitch

    companion object {
        fun testSwitches(): List<DstSwitch> = listOf(
            None,
            FallBack(seconds = 1.hours.inWholeSeconds.toInt()),
            FallBack(seconds = 30.minutes.inWholeSeconds.toInt()),
            SpringForward(seconds = 1.hours.inWholeSeconds.toInt()),
            SpringForward(seconds = 30.minutes.inWholeSeconds.toInt())
        )
    }
}

private sealed interface GraphValues {
    data class Random(val from: Double, val until: Double) : GraphValues
    data class Flat(val value: Double) : GraphValues

    companion object {
        fun testValues(): List<GraphValues> = listOf(
            Random(from = -2.0, until = 30.0),
            Flat(value = 1.0)
        )
    }
}