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

package com.davidtakac.bura.forecast.parameters.sun

import java.time.Duration
import java.time.Instant

class SunPeriod(val moments: List<SunMoment>): AbstractList<SunMoment>() {
    init {
        requireNotEmpty()
        requireAscending()
    }

    override val size: Int
        get() = moments.size

    override fun get(index: Int): SunMoment =
        moments[index]

    fun momentsFrom(time: Instant, takeMomentsUpToHoursInFuture: Int? = null): List<SunMoment>? =
        moments.filter {
            val durationBetween = Duration.between(time, it.timeInstant)
            val hoursBetween = durationBetween.toHours()
            val maxHours = takeMomentsUpToHoursInFuture ?: Int.MAX_VALUE
            durationBetween >= Duration.ZERO && hoursBetween in 0..maxHours
        }.takeIf { it.isNotEmpty() }

    private fun requireNotEmpty() =
        require(moments.isNotEmpty()) { "Moments of SunPeriod must not be empty." }

    private fun requireAscending() {
        if (moments.size == 1) return
        var previousMoment = moments[0]
        for (i in 1..moments.lastIndex) {
            val nextMoment = moments[i]
            require(previousMoment.timeInstant < nextMoment.timeInstant) {
                "Moments of SunPeriod must be sorted and unique, but contained ${previousMoment.timeZdt} and ${nextMoment.timeZdt}."
            }
            previousMoment = nextMoment
        }
    }
}