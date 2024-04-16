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

package com.davidtakac.bura.units

import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.wind.WindSpeed

interface SelectedUnitsRepository {
    suspend fun getSelectedUnits(): Units
    suspend fun selectRainUnit(unit: Precipitation.Unit)
    suspend fun selectShowersUnit(unit: Precipitation.Unit)
    suspend fun selectSnowUnit(unit: Precipitation.Unit)
    suspend fun selectMixedPrecipitationUnit(unit: Precipitation.Unit)
    suspend fun selectTemperatureUnit(unit: Temperature.Unit)
    suspend fun selectPressureUnit(unit: Pressure.Unit)
    suspend fun selectVisibilityUnit(unit: Visibility.Unit)
    suspend fun selectWindSpeedUnit(unit: WindSpeed.Unit)
}