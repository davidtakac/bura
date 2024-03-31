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
import com.davidtakac.bura.humidity.Humidity
import com.davidtakac.bura.humidity.HumidityMoment
import com.davidtakac.bura.humidity.HumidityPeriod
import com.davidtakac.bura.summary.humidity.HumiditySummary
import com.davidtakac.bura.summary.humidity.GetHumiditySummary
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureMoment
import com.davidtakac.bura.temperature.TemperaturePeriod
import com.davidtakac.bura.units.Units
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class HumiditySummaryTest {
    private val location = GMTLocation.coordinates
    private val units = Units.Default

    @Test
    fun `gets humidity and dew point of now`() = runTest {
        val firstMoment = firstLocalDateTime
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val humidityPeriod = HumidityPeriod(listOf(HumidityMoment(firstMoment, Humidity(0.0))))
        val dewPointPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(0.0)
                )
            )
        )
        val useCase = GetHumiditySummary(
            humidityRepo = FakeHumidityRepository(humidityPeriod),
            dewPointRepo = FakeTemperatureRepository(dewPointPeriod),
        )
        assertEquals(
            ForecastResult.Success(
                HumiditySummary(
                    humidityNow = Humidity(0.0),
                    dewPointNow = Temperature.fromDegreesCelsius(0.0)
                )
            ),
            useCase(location, units, now)
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = firstLocalDateTime
        val now = firstMoment.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val humidityPeriod = HumidityPeriod(listOf(HumidityMoment(firstMoment, Humidity(0.0))))
        val dewPointPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature.fromDegreesCelsius(0.0)
                )
            )
        )
        val useCase = GetHumiditySummary(
            humidityRepo = FakeHumidityRepository(humidityPeriod),
            dewPointRepo = FakeTemperatureRepository(dewPointPeriod),
        )
        assertEquals(ForecastResult.Outdated, useCase(location, units, now))
    }
}