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

package com.davidtakac.bura

import com.davidtakac.bura.precipitation.MixedPrecipitation
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import org.junit.Assert.assertEquals
import org.junit.Test

class PrecipitationTest {
    @Test
    fun `sums rain showers and snow`() {
        val rain = Rain.fromMillimeters(10.0)
        val showers = Showers.fromMillimeters(0.0)
        val snow = Snow.fromMillimeters(70.0)
        val sum = MixedPrecipitation.fromMillimeters(rain, showers, snow).reduce()
        sum as MixedPrecipitation
        assertEquals(20.0, sum.value, 0.01)
        assertEquals(rain, sum.rain)
        assertEquals(showers, sum.showers)
        assertEquals(snow, sum.snow)
    }

    @Test
    fun `converts to inches`() {
        val rain = Rain.fromMillimeters(10.0)
        val showers = Showers.fromMillimeters(0.0)
        val snow = Snow.fromMillimeters(70.0)
        val sum = MixedPrecipitation.fromMillimeters(rain, showers, snow).convertTo(Precipitation.Unit.Inches)
        assertEquals(
            0.787,
            sum.value,
            0.001
        )
    }

    @Test
    fun equals() {
        val rain = Rain.fromMillimeters(10.0)
        val showers = Showers.fromMillimeters(0.0)
        val snow = Snow.fromMillimeters(70.0)
        val one = MixedPrecipitation.fromMillimeters(rain, showers, snow)
        val two = MixedPrecipitation.fromMillimeters(rain, showers, snow)
        assertEquals(one, two)
    }

    @Test
    fun plus() {
        val rainOne = Rain.fromMillimeters(10.0)
        val showersOne = Showers.Zero
        val snowOne = Snow.fromMillimeters(70.0)
        val rainTwo = Rain.fromMillimeters(5.0)
        val showersTwo = Showers.fromMillimeters(10.0)
        val snowTwo = Snow.Zero
        val one = MixedPrecipitation.fromMillimeters(rainOne, showersOne, snowOne).convertTo(Precipitation.Unit.Inches)
        val two = MixedPrecipitation.fromMillimeters(rainTwo, showersTwo, snowTwo)
        val sum = (one + two).reduce()
        sum as MixedPrecipitation
        assertEquals(1.37, sum.value, 0.01)
    }
}