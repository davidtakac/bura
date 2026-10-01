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

package com.davidtakac.bura.theme

import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import org.junit.Test

class AppColorsTest {
    @Test
    fun `returns temperature colors for normal range`() {
        val min = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        val max = Temperature(20.0, Temperature.Unit.DegreesCelsius)
        val colors = AppColors.ForLightTheme
        assert(colors.temperatureColors(min, max).isNotEmpty())
    }

    @Test
    fun `maxes out at last temperature color`() {
        val min = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        val max = Temperature(100.0, Temperature.Unit.DegreesCelsius)
        val colors = AppColors.ForLightTheme
        assert(colors.temperatureColors(min, max).isNotEmpty())
    }

    @Test
    fun `mins out at first temperature color`() {
        val min = Temperature(-100.0, Temperature.Unit.DegreesCelsius)
        val max = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        val colors = AppColors.ForLightTheme
        assert(colors.temperatureColors(min, max).isNotEmpty())
    }

    @Test
    fun `maxes and mins out at first and last temperature color`() {
        val min = Temperature(-100.0, Temperature.Unit.DegreesCelsius)
        val max = Temperature(100.0, Temperature.Unit.DegreesCelsius)
        val colors = AppColors.ForLightTheme
        assert(colors.temperatureColors(min, max).isNotEmpty())
    }
}