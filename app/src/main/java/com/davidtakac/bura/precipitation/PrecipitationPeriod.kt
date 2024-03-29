/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.precipitation

import com.davidtakac.bura.forecast.HourPeriod
import java.time.Instant
import java.time.ZoneId

class PrecipitationPeriod(moments: List<PrecipitationMoment>) : HourPeriod<PrecipitationMoment>(moments) {
    val total: MixedPrecipitation get() = map { it.precipitation }.reduce { acc, precipitation -> acc + precipitation }

    override fun momentsUntil(hourExclusive: Instant, takeMoments: Int?) =
        super.momentsUntil(hourExclusive, takeMoments)?.let { PrecipitationPeriod(it) }

    override fun momentsFrom(hourInclusive: Instant, takeMoments: Int?) =
        super.momentsFrom(hourInclusive, takeMoments)?.let { PrecipitationPeriod(it) }

    override fun daysFrom(dayInclusive: Instant, atZone: ZoneId, takeDays: Int?) =
        super.daysFrom(dayInclusive, atZone, takeDays)?.map { PrecipitationPeriod(it) }
}