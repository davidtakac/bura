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

import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.place.Place
import com.davidtakac.bura.temperature.Temperature
import java.time.LocalTime

data class SavedPlace(
    val place: Place,
    val time: LocalTime,
    val selected: Boolean,
    val conditions: Conditions?
) {
    data class Conditions(
        val temp: Temperature?,
        val minTemp: Temperature,
        val maxTemp: Temperature,
        val condition: Condition
    )
}