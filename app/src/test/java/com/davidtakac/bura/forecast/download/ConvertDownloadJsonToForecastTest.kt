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

package com.davidtakac.bura.forecast.download

import com.davidtakac.bura.forecast.parameters.sun.SunEvent
import com.davidtakac.bura.forecast.parameters.sun.SunMoment
import org.junit.Assert
import org.junit.Test
import java.time.ZonedDateTime

class ConvertDownloadJsonToForecastTest {
    @Test
    fun `constructs sun period from regular data`() {
        val sunrises = listOf(ZonedDateTime.parse("2025-01-01T08:00:00Z"), ZonedDateTime.parse("2025-01-02T08:00:00Z"))
        val sunsets = listOf(ZonedDateTime.parse("2025-01-01T20:00:00Z"), ZonedDateTime.parse("2025-01-02T20:00:00Z"))
        val sunPeriod = createSunPeriod(sunrises, sunsets)
        Assert.assertEquals(
            listOf(
                SunMoment(ZonedDateTime.parse("2025-01-01T08:00:00Z"), SunEvent.Rise),
                SunMoment(ZonedDateTime.parse("2025-01-01T20:00:00Z"), SunEvent.Set),
                SunMoment(ZonedDateTime.parse("2025-01-02T08:00:00Z"), SunEvent.Rise),
                SunMoment(ZonedDateTime.parse("2025-01-02T20:00:00Z"), SunEvent.Set)
            ),
            sunPeriod?.moments
        )
    }

    @Test
    fun `handles all polar night`() {
        val sunrises = listOf(ZonedDateTime.parse("2025-01-01T00:00:00Z"), ZonedDateTime.parse("2025-01-02T00:00:00Z"))
        val sunsets = listOf(ZonedDateTime.parse("2025-01-01T00:00:00Z"), ZonedDateTime.parse("2025-01-02T00:00:00Z"))
        val sunPeriod = createSunPeriod(sunrises, sunsets)
        Assert.assertNull(sunPeriod)
    }

    @Test
    fun `handles all polar day`() {
        val sunrises = listOf(ZonedDateTime.parse("2025-01-01T00:00:00Z"), ZonedDateTime.parse("2025-01-02T00:00:00Z"))
        val sunsets = listOf(ZonedDateTime.parse("2025-01-02T00:00:00Z"), ZonedDateTime.parse("2025-01-03T00:00:00Z"))
        val sunPeriod = createSunPeriod(sunrises, sunsets)
        Assert.assertNull(sunPeriod)
    }

    @Test
    fun `handles polar night between regular data`() {
        val sunrises = listOf(ZonedDateTime.parse("2025-01-01T08:00:00Z"), ZonedDateTime.parse("2025-01-02T00:00:00Z"), ZonedDateTime.parse("2025-01-03T08:00:00Z"))
        val sunsets = listOf(ZonedDateTime.parse("2025-01-01T20:00:00Z"), ZonedDateTime.parse("2025-01-02T00:00:00Z"), ZonedDateTime.parse("2025-01-03T20:00:00Z"))
        val sunPeriod = createSunPeriod(sunrises, sunsets)
        Assert.assertEquals(
            listOf(
                SunMoment(ZonedDateTime.parse("2025-01-01T08:00:00Z"), SunEvent.Rise),
                SunMoment(ZonedDateTime.parse("2025-01-01T20:00:00Z"), SunEvent.Set),
                SunMoment(ZonedDateTime.parse("2025-01-03T08:00:00Z"), SunEvent.Rise),
                SunMoment(ZonedDateTime.parse("2025-01-03T20:00:00Z"), SunEvent.Set),
            ),
            sunPeriod?.moments
        )
    }

    @Test
    fun `handles polar day between regular data`() {
        val sunrises = listOf(ZonedDateTime.parse("2025-01-01T08:00:00Z"), ZonedDateTime.parse("2025-01-02T00:00:00Z"), ZonedDateTime.parse("2025-01-03T08:00:00Z"))
        // I am not so sure that this is what OpenMeteo data would look if polar day is between regular data
        val sunsets = listOf(ZonedDateTime.parse("2025-01-01T20:00:00Z"), ZonedDateTime.parse("2025-01-03T00:00:00Z"), ZonedDateTime.parse("2025-01-03T20:00:00Z"))
        val sunPeriod = createSunPeriod(sunrises, sunsets)
        Assert.assertEquals(
            listOf(
                SunMoment(ZonedDateTime.parse("2025-01-01T08:00:00Z"), SunEvent.Rise),
                SunMoment(ZonedDateTime.parse("2025-01-01T20:00:00Z"), SunEvent.Set),
                SunMoment(ZonedDateTime.parse("2025-01-03T08:00:00Z"), SunEvent.Rise),
                SunMoment(ZonedDateTime.parse("2025-01-03T20:00:00Z"), SunEvent.Set),
            ),
            sunPeriod?.moments
        )
    }
}