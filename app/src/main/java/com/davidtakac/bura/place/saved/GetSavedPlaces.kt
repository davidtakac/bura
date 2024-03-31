/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.place.saved

import com.davidtakac.bura.condition.ConditionPeriod
import com.davidtakac.bura.condition.ConditionRepository
import com.davidtakac.bura.place.Place
import com.davidtakac.bura.temperature.TemperaturePeriod
import com.davidtakac.bura.temperature.TemperatureRepository
import com.davidtakac.bura.units.Units
import java.time.Instant
import java.time.LocalDateTime

class GetSavedPlaces(
    private val savedPlacesRepo: SavedPlacesRepository,
    private val tempRepo: TemperatureRepository,
    private val conditionRepo: ConditionRepository
) {
    suspend operator fun invoke(selectedPlace: Place?, selectedUnits: Units, now: Instant): List<SavedPlace> {
        return savedPlacesRepo.getSavedPlaces().map { savedPlace ->
            val location = savedPlace.location
            val dateTimeAtPlace = now.atZone(savedPlace.location.timeZone).toLocalDateTime()
            val dateAtPlace = dateTimeAtPlace.toLocalDate()
            val tempDayAtPlace = tempRepo.period(location, selectedUnits)?.getDay(dateAtPlace)
            val condDayAtPlace = conditionRepo.period(location, selectedUnits)?.getDay(dateAtPlace)
            val conditions = if (tempDayAtPlace != null && condDayAtPlace != null) getConditions(
                dateTimeAtPlace,
                tempDayAtPlace,
                condDayAtPlace
            ) else null
            SavedPlace(
                place = savedPlace,
                time = now.atZone(location.timeZone).toLocalTime(),
                selected = savedPlace == selectedPlace,
                conditions = conditions
            )
        }
    }

    private fun getConditions(
        now: LocalDateTime,
        tempDay: TemperaturePeriod,
        conditionDay: ConditionPeriod
    ): SavedPlace.Conditions = SavedPlace.Conditions(
        temp = tempDay[now]?.temperature,
        minTemp = tempDay.minimum,
        maxTemp = tempDay.maximum,
        condition = conditionDay[now]?.condition
            ?: conditionDay.day ?: conditionDay.night!!
    )
}