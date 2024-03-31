/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura

import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.summary.feelslike.FeelsLikeSummary
import com.davidtakac.bura.summary.feelslike.GetFeelsLikeSummary
import com.davidtakac.bura.summary.feelslike.FeelsVsActual
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureMoment
import com.davidtakac.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetFeelsLikeSummaryTest {
    

    @Test
    fun `gets now and describes what it feels like`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(-1.0)
                )
            )
        )
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(0.0)
                )
            )
        )
        val useCase = GetFeelsLikeSummary(
            tempRepo = FakeTemperatureRepository(temperaturePeriod),
            feelsRepo = FakeTemperatureRepository(feelsLikePeriod),
        )
        val summary = useCase(coords, units, now)
        assertEquals(
            ForecastResult.Success(
                FeelsLikeSummary(
                    feelsLikeNow = Temperature.fromDegreesCelsius(-1.0),
                    actualNow = Temperature.fromDegreesCelsius(0.0),
                    vsActual = FeelsVsActual.Colder
                )
            ),
            summary
        )
    }

    @Test
    fun `when feels like and actual within 1 degree of each other feel is similar`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(-0.5)
                )
            )
        )
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(0.0)
                )
            )
        )
        val useCase = GetFeelsLikeSummary(
            tempRepo = FakeTemperatureRepository(temperaturePeriod),
            feelsRepo = FakeTemperatureRepository(feelsLikePeriod),
        )
        val summary = useCase(coords, units, now)
        assertEquals(
            ForecastResult.Success(
                FeelsLikeSummary(
                    feelsLikeNow = Temperature.fromDegreesCelsius(-0.5),
                    actualNow = Temperature.fromDegreesCelsius(0.0),
                    vsActual = FeelsVsActual.Similar
                )
            ),
            summary
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(0.0)
                )
            )
        )
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(0.0)
                )
            )
        )
        val useCase = GetFeelsLikeSummary(
            tempRepo = FakeTemperatureRepository(temperaturePeriod),
            feelsRepo = FakeTemperatureRepository(feelsLikePeriod),
        )
        val summary = useCase(coords, units, now)
        assertEquals(ForecastResult.Outdated, summary)
    }
}