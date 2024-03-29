/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.wind

import com.davidtakac.bura.forecast.HourPeriod

class WindPeriod(moments: List<WindMoment>) : HourPeriod<WindMoment>(moments) {
    val minimumSpeed get() = minOf { it.wind.speed }

    val maximumSpeed get() = maxOf { it.wind.speed }
}