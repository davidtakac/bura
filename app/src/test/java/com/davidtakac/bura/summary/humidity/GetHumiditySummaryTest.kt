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

package com.davidtakac.bura.summary.humidity

import com.davidtakac.bura.forecast.parameters.humidity.Humidity
import com.davidtakac.bura.forecast.parameters.humidity.HumidityMoment
import com.davidtakac.bura.forecast.parameters.humidity.HumidityPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.unixEpochStartZdt
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetHumiditySummaryTest {
    @Test
    fun `gets humidity and dew point of now`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val humidityPeriod = HumidityPeriod(listOf(HumidityMoment(firstMoment, Humidity(0.0))))
        val dewPointPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        Assert.assertEquals(
            HumiditySummary(
                humidityNow = Humidity(0.0),
                dewPointNow = Temperature(0.0, Temperature.Unit.DegreesCelsius)
            ),
            getHumiditySummary(now, humidityPeriod, dewPointPeriod)
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStartZdt
        val now = firstMoment.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val humidityPeriod = HumidityPeriod(listOf(HumidityMoment(firstMoment, Humidity(0.0))))
        val dewPointPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        Assert.assertNull(getHumiditySummary(now, humidityPeriod, dewPointPeriod))
    }
}