/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.uvindex

import com.davidtakac.bura.forecast.HourPeriod
import java.time.LocalDate
import java.time.LocalDateTime

class UvIndexPeriod(moments: List<UvIndexMoment>) : HourPeriod<UvIndexMoment>(moments) {
    val minimum get() = minOf { it.uvIndex }

    val maximum get() = maxOf { it.uvIndex }

    val protectionWindows get() = protectionWindows(dangerousUvIndex = UvIndex(3))

    override fun momentsFrom(hourInclusive: LocalDateTime, takeMoments: Int?) =
        super.momentsFrom(hourInclusive, takeMoments)?.let { UvIndexPeriod(it) }

    override fun getDay(day: LocalDate) =
        super.getDay(day)?.let { UvIndexPeriod(it) }

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
        var windowStart: LocalDateTime? = null
        while (moments.hasNext()) {
            val curr = moments.next()
            if (curr.uvIndex >= dangerousUvIndex) {
                windowStart = curr.hour
                break
            }
        }
        if (windowStart == null) return null

        var windowEnd: LocalDateTime? = null
        while (moments.hasNext()) {
            val curr = moments.next()
            if (curr.uvIndex < dangerousUvIndex) {
                windowEnd = curr.hour
                break
            }
        }

        return SunProtectionWindow(
            startInclusive = windowStart,
            endExclusive = windowEnd
        )
    }
}

data class SunProtectionWindow(val startInclusive: LocalDateTime, val endExclusive: LocalDateTime?) {
    override fun toString(): String = "$startInclusive until $endExclusive"
}