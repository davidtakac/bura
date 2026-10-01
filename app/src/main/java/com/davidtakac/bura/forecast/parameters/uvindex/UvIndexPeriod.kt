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

package com.davidtakac.bura.forecast.parameters.uvindex

import com.davidtakac.bura.forecast.HourPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

class UvIndexPeriod(moments: List<UvIndexMoment>) : HourPeriod<UvIndexMoment>(moments) {
    val minimum get() = minOf { it.uvIndex }

    val maximum get() = maxOf { it.uvIndex }

    val protectionWindows get() = protectionWindows(dangerousUvIndex = UvIndex(3.0))

    fun periodFrom(hourInclusive: Instant, take: Int? = null) =
        momentsFrom(hourInclusive, take)?.let { UvIndexPeriod(it) }

    fun dayPeriodOn(day: LocalDate) =
        dayMomentsOn(day)?.let { UvIndexPeriod(it) }

    private fun protectionWindows(dangerousUvIndex: UvIndex): List<SunProtectionWindow> =
        buildList {
            val iterator = this@UvIndexPeriod.iterator()
            while (true) {
                val window = nextProtectionWindow(iterator, dangerousUvIndex) ?: break
                add(window)
                if (window.endExclusive == null) break
            }
        }

    private fun nextProtectionWindow(
        moments: Iterator<UvIndexMoment>,
        dangerousUvIndex: UvIndex
    ): SunProtectionWindow? {
        var windowStart: ZonedDateTime? = null
        while (moments.hasNext()) {
            val curr = moments.next()
            if (curr.uvIndex >= dangerousUvIndex) {
                windowStart = curr.timeZdt
                break
            }
        }
        if (windowStart == null) return null

        var windowEnd: ZonedDateTime? = null
        while (moments.hasNext()) {
            val curr = moments.next()
            if (curr.uvIndex < dangerousUvIndex) {
                windowEnd = curr.timeZdt
                break
            }
        }

        return SunProtectionWindow(
            startInclusive = windowStart,
            endExclusive = windowEnd
        )
    }
}

data class SunProtectionWindow(val startInclusive: ZonedDateTime, val endExclusive: ZonedDateTime?) {
    override fun toString(): String = "$startInclusive until $endExclusive"
}