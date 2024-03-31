/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.graphs.common

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Objects

class GraphTime(
    val value: LocalTime,
    val meta: Meta
) {
    constructor(
        hour: LocalDateTime,
        now: LocalDateTime,
        timeZone: ZoneId
    ) : this(value = hour.atZone(timeZone).toLocalTime(), meta = getMeta(hour, now))

    enum class Meta {
        Past, Present, Future
    }

    override fun equals(other: Any?): Boolean =
        other is GraphTime && other.value == value && other.meta == meta

    override fun hashCode(): Int = Objects.hash(value, meta)
}

private fun getMeta(hour: LocalDateTime, now: LocalDateTime): GraphTime.Meta {
    val nowTrunc = now.truncatedTo(ChronoUnit.HOURS)
    return when {
        hour < nowTrunc -> GraphTime.Meta.Past
        hour == nowTrunc -> GraphTime.Meta.Present
        else -> GraphTime.Meta.Future
    }
}