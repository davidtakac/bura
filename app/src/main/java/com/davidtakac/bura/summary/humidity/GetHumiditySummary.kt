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

import com.davidtakac.bura.humidity.Humidity
import com.davidtakac.bura.humidity.HumidityRepository
import com.davidtakac.bura.forecast.ForecastResult
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureRepository
import com.davidtakac.bura.units.Units
import java.time.LocalDateTime

class GetHumiditySummary(
    private val humidityRepo: HumidityRepository,
    private val dewPointRepo: TemperatureRepository,
) {
    suspend operator fun invoke(coords: Coordinates, units: Units, now: LocalDateTime): ForecastResult<HumiditySummary> {
        val humidityPeriod = humidityRepo.period(coords, units) ?: return ForecastResult.FailedToDownload
        val dewPointPeriod = dewPointRepo.period(coords, units) ?: return ForecastResult.FailedToDownload
        return ForecastResult.Success(HumiditySummary(
            humidityNow = humidityPeriod[now]?.humidity ?: return ForecastResult.Outdated,
            dewPointNow = dewPointPeriod[now]?.temperature ?: return ForecastResult.Outdated
        ))
    }
}

data class HumiditySummary(
    val humidityNow: Humidity,
    val dewPointNow: Temperature
)