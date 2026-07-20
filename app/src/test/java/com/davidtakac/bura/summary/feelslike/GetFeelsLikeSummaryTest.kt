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

package com.davidtakac.bura.summary.feelslike

import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetFeelsLikeSummaryTest {
    @Test
    fun `gets now and describes what it feels like`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(-1.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        Assert.assertEquals(
            FeelsLikeSummary(
                feelsLikeNow = Temperature(-1.0, Temperature.Unit.DegreesCelsius),
                actualNow = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                vsActual = FeelsVsActual.Colder
            ),
            getFeelsLikeSummary(now, tempPeriod = temperaturePeriod, feelsPeriod = feelsLikePeriod)
        )
    }

    @Test
    fun `when feels like and actual within 1 degree of each other feel is similar`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(-0.5, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        Assert.assertEquals(
            FeelsLikeSummary(
                feelsLikeNow = Temperature(-0.5, Temperature.Unit.DegreesCelsius),
                actualNow = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                vsActual = FeelsVsActual.Similar
            ),
            getFeelsLikeSummary(now, tempPeriod = temperaturePeriod, feelsPeriod = feelsLikePeriod)
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        Assert.assertNull(getFeelsLikeSummary(
            now = now,
            tempPeriod = temperaturePeriod,
            feelsPeriod = feelsLikePeriod
        ))
    }
}