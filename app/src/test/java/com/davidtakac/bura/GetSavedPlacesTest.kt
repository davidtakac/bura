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

import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.ConditionMoment
import com.davidtakac.bura.condition.ConditionPeriod
import com.davidtakac.bura.place.Coordinates
import com.davidtakac.bura.place.Location
import com.davidtakac.bura.place.saved.GetSavedPlaces
import com.davidtakac.bura.place.Place
import com.davidtakac.bura.place.saved.SavedPlace
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.TemperatureMoment
import com.davidtakac.bura.temperature.TemperaturePeriod
import com.davidtakac.bura.units.Units
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class GetSavedPlacesTest {
    @Test
    fun `gets saved places with conditions attached if available`() = runTest {
        val momentInstant = Instant.ofEpochSecond(0)
        val momentDateTime = momentInstant.atZone(ZoneOffset.UTC).toLocalDateTime()
        val now = momentInstant.plus(10, ChronoUnit.MINUTES)
        val firstPlace = Place(
            name = "first", "", "", "",
            Location(ZoneId.of("GMT"), Coordinates(latitude = 0.0, longitude = 1.0))
        )
        val secondPlace = Place(
            name = "second", "", "", "",
            Location(ZoneId.of("GMT+1"), Coordinates(latitude = 0.0, longitude = 10.0))
        )
        val tempRepo = FakeMultipleCoordsTemperatureRepository(
            mapOf(
                firstPlace.location.coordinates to TemperaturePeriod(
                    listOf(
                        TemperatureMoment(
                            hour = momentDateTime,
                            temperature = Temperature.fromDegreesCelsius(10.0)
                        )
                    )
                ),
                secondPlace.location.coordinates to null
            )
        )
        val condRepo = FakeMultipleCoordsConditionRepository(
            mapOf(
                firstPlace.location.coordinates to ConditionPeriod(
                    listOf(
                        ConditionMoment(
                            hour = momentDateTime,
                            condition = Condition(0, true)
                        )
                    )
                ),
                secondPlace.location.coordinates to null
            )
        )
        val savedPlacesRepo = FakeSavedPlacesRepository(listOf(firstPlace, secondPlace))
        val useCase = GetSavedPlaces(savedPlacesRepo, tempRepo, condRepo)
        val result = useCase.invoke(secondPlace, Units.Default, now)
        assertEquals(
            listOf(
                SavedPlace(
                    place = firstPlace,
                    time = LocalTime.parse("00:10"),
                    selected = false,
                    conditions = SavedPlace.Conditions(
                        temp = Temperature.fromDegreesCelsius(10.0),
                        minTemp = Temperature.fromDegreesCelsius(10.0),
                        maxTemp = Temperature.fromDegreesCelsius(10.0),
                        condition = Condition(0, true)
                    )
                ),
                SavedPlace(
                    place = secondPlace,
                    time = LocalTime.parse("01:10"),
                    selected = true,
                    conditions = null,
                ),
            ),
            result
        )
    }
}