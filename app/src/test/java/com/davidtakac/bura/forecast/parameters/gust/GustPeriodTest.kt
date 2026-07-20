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

package com.davidtakac.bura.forecast.parameters.gust

import com.davidtakac.bura.forecast.parameters.wind.WindSpeed
import com.davidtakac.bura.unixEpochStartZdt
import org.junit.Assert
import org.junit.Test
import java.time.temporal.ChronoUnit

class GustPeriodTest {
    @Test
    fun maximum() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = GustPeriod(
            moments = listOf(
                GustMoment(firstMoment, WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond)),
                GustMoment(secondMoment, WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond))
            )
        )
        Assert.assertEquals(WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond), period.maximum)
    }

    @Test
    fun convert() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = GustPeriod(
            moments = listOf(
                GustMoment(firstMoment, WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond)),
                GustMoment(secondMoment, WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond))
            )
        )
        val converted = period.convertTo(WindSpeed.Unit.KilometersPerHour)
        assert(converted.all { it.speed.unit == WindSpeed.Unit.KilometersPerHour })
    }
}