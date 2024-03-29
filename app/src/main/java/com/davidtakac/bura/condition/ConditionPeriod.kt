/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.condition

import com.davidtakac.bura.forecast.HourPeriod
import java.time.Instant
import java.time.ZoneId

class ConditionPeriod(
    moments: List<ConditionMoment>
) : HourPeriod<ConditionMoment>(moments) {
    val day get() = representative(isDay = true)

    val night get() = representative(isDay = false)

    override fun momentsFrom(hourInclusive: Instant, takeMoments: Int?) =
        super.momentsFrom(hourInclusive, takeMoments)?.let { ConditionPeriod(it) }

    override fun daysFrom(dayInclusive: Instant, atZone: ZoneId, takeDays: Int?) =
        super.daysFrom(dayInclusive, atZone, takeDays)?.map { ConditionPeriod(it) }

    override fun getDay(day: Instant, atZone: ZoneId) =
        super.getDay(day, atZone)?.let { ConditionPeriod(it) }

    /**
     * Returns most severe weather code when any code in this period exceeds, or
     * when all codes are different. Otherwise returns the most common weather code.
     */
    private fun representative(isDay: Boolean): Condition? {
        val groupedByCode = filter { it.condition.isDay == isDay }
            .groupBy { it.condition }
            .takeIf { it.isNotEmpty() }
            ?: return null
        val hasSevereConditions = groupedByCode.any { it.key.wmoCode >= 50 }
        val allTheSame = groupedByCode.map { it.value.size }.toSet().size == 1

        return if (hasSevereConditions || allTheSame) {
            groupedByCode.maxBy { it.key.wmoCode }.key
        } else {
            groupedByCode.maxBy { it.value.size }.key
        }
    }
}