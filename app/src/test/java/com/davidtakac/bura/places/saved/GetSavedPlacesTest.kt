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

package com.davidtakac.bura.places.saved

import com.davidtakac.bura.forecast.parameters.condition.Condition
import com.davidtakac.bura.forecast.parameters.condition.ConditionMoment
import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.temperature.Temperature
import com.davidtakac.bura.forecast.parameters.temperature.TemperatureMoment
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import com.davidtakac.bura.places.Coordinates
import com.davidtakac.bura.places.Location
import com.davidtakac.bura.places.Place
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class GetSavedPlacesTest {
    @Test
    fun `gets saved place with conditions`() = runTest {
        val momentInstant = Instant.ofEpochSecond(0)
        val momentDateTime = momentInstant.atZone(ZoneOffset.UTC)
        val now = momentInstant.plus(10, ChronoUnit.MINUTES)
        val place = Place(
            name = "first", "", "", "", "", "", "",
            Location(ZoneId.of("GMT"), Coordinates(latitude = 0.0, longitude = 1.0))
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    timeZdt = momentDateTime,
                    temperature = Temperature(10.0, Temperature.Unit.DegreesCelsius)
                )
            ),
        )
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(
                    timeZdt = momentDateTime,
                    condition = Condition(0, true)
                )
            ),
        )
        val result = getSavedPlace(now, place, false, tempPeriod, condPeriod)
        Assert.assertEquals(
            SavedPlace(
                place = place,
                time = LocalTime.parse("00:10"),
                selected = false,
                conditions = SavedPlace.Conditions(
                    temp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                    minTemp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                    maxTemp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                    condition = Condition(0, true)
                )
            ),
            result
        )
    }

    @Test
    fun `gets saved place without conditions`() = runTest {
        val momentInstant = Instant.ofEpochSecond(0)
        val now = momentInstant.plus(10, ChronoUnit.MINUTES)
        val place = Place(
            name = "second", "", "", "", "", "", "",
            Location(ZoneId.of("GMT+1"), Coordinates(latitude = 0.0, longitude = 10.0))
        )
        val tempPeriod = null
        val condPeriod = null
        val result = getSavedPlace(now, place, true, tempPeriod, condPeriod)
        Assert.assertEquals(
            SavedPlace(
                place = place,
                time = LocalTime.parse("01:10"),
                selected = true,
                conditions = null,
            ),
            result
        )
    }

    @Test
    fun `gets saved place without conditions when data is mixed`() = runTest {
        val momentInstant = Instant.ofEpochSecond(0)
        val momentDateTime = momentInstant.atZone(ZoneOffset.UTC)
        val now = momentInstant.plus(10, ChronoUnit.MINUTES)
        val place = Place(
            name = "second", "", "", "", "", "", "",
            Location(ZoneId.of("GMT+1"), Coordinates(latitude = 0.0, longitude = 10.0))
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    timeZdt = momentDateTime,
                    temperature = Temperature(10.0, Temperature.Unit.DegreesCelsius)
                )
            ),
        )
        val condPeriod = null
        val result = getSavedPlace(now, place, true, tempPeriod, condPeriod)
        Assert.assertEquals(
            SavedPlace(
                place = place,
                time = LocalTime.parse("01:10"),
                selected = true,
                conditions = null,
            ),
            result
        )
    }
}