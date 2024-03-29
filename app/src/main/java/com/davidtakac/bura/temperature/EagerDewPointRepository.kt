/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.temperature

import com.davidtakac.bura.forecast.ForecastRepository
import com.davidtakac.bura.place.Location
import com.davidtakac.bura.units.Units

class EagerDewPointRepository(private val forecastRepo: ForecastRepository) : TemperatureRepository {
    override suspend fun period(location: Location, units: Units): TemperaturePeriod? =
        forecastRepo.forecast(location, units)?.dewPoint
}