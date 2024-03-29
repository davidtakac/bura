/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.units

import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.wind.WindSpeed

private const val TEMP_KEY = "temp_unit"
private const val RAIN_KEY = "rain_unit"
private const val SHOWERS_KEY = "showers_unit"
private const val SNOW_KEY = "snow_unit"
private const val PRECIP_KEY = "precip_unit"
private const val PRESSURE_KEY = "pressure_unit"
private const val VIS_KEY = "vis_unit"
private const val WIND_KEY = "wind_unit"
private val DefaultUnits = Units.Default

class PrefsSelectedUnitsRepository(private val prefs: SharedPreferences) : SelectedUnitsRepository {
    override suspend fun getSelectedUnits(): Units =
        Units(
            temperature = prefs.getString(TEMP_KEY, null)?.let(Temperature.Unit::valueOf)
                ?: DefaultUnits.temperature,
            rain = prefs.getString(RAIN_KEY, null)?.let(Precipitation.Unit::valueOf)
                ?: DefaultUnits.rain,
            showers = prefs.getString(SHOWERS_KEY, null)?.let(Precipitation.Unit::valueOf)
                ?: DefaultUnits.showers,
            snow = prefs.getString(SNOW_KEY, null)?.let(Precipitation.Unit::valueOf)
                ?: DefaultUnits.snow,
            precipitation = prefs.getString(PRECIP_KEY, null)?.let(Precipitation.Unit::valueOf)
                ?: DefaultUnits.precipitation,
            windSpeed = prefs.getString(WIND_KEY, null)?.let(WindSpeed.Unit::valueOf)
                ?: DefaultUnits.windSpeed,
            pressure = prefs.getString(PRESSURE_KEY, null)?.let(Pressure.Unit::valueOf)
                ?: DefaultUnits.pressure,
            visibility = prefs.getString(VIS_KEY, null)?.let(Visibility.Unit::valueOf)
                ?: DefaultUnits.visibility
        )

    override suspend fun selectRainUnit(unit: Precipitation.Unit) =
        prefs.edit { putString(RAIN_KEY, unit.name) }

    override suspend fun selectShowersUnit(unit: Precipitation.Unit) =
        prefs.edit { putString(SHOWERS_KEY, unit.name) }

    override suspend fun selectSnowUnit(unit: Precipitation.Unit) =
        prefs.edit { putString(SNOW_KEY, unit.name) }

    override suspend fun selectMixedPrecipitationUnit(unit: Precipitation.Unit) =
        prefs.edit { putString(PRECIP_KEY, unit.name) }

    override suspend fun selectTemperatureUnit(unit: Temperature.Unit) =
        prefs.edit { putString(TEMP_KEY, unit.name) }

    override suspend fun selectPressureUnit(unit: Pressure.Unit) =
        prefs.edit { putString(PRESSURE_KEY, unit.name) }

    override suspend fun selectVisibilityUnit(unit: Visibility.Unit) =
        prefs.edit { putString(VIS_KEY, unit.name) }

    override suspend fun selectWindSpeedUnit(unit: WindSpeed.Unit) =
        prefs.edit { putString(WIND_KEY, unit.name) }
}