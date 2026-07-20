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

package com.davidtakac.bura.forecast

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

abstract class HourPeriod<T : HourMoment>(private val moments: List<T>) : AbstractList<T>() {
    init {
        requireNotEmpty()
        requireAscendingAndComplete()
    }

    override val size: Int
        get() = moments.size

    override fun get(index: Int): T =
        moments[index]

    operator fun get(time: LocalDateTime): T? =
        momentsFrom(time, take = 1)?.firstOrNull()

    fun momentsUntil(hourExclusive: LocalDateTime, takeLast: Int? = null): List<T>? {
        require(takeLast == null || takeLast > 0) { "Take moments must either be null or positive." }
        val hour = hourExclusive.truncatedTo(ChronoUnit.HOURS).minus(1, ChronoUnit.HOURS)
        val indexOfHour = moments.indexOfFirst { it.hour == hour }
        return if (indexOfHour < 0) null else moments
            .slice(0..indexOfHour)
            .let { if (takeLast != null) it.takeLast(takeLast) else it }
    }

    fun momentsFrom(hourInclusive: LocalDateTime, take: Int? = null): List<T>? {
        require(take == null || take > 0) { "Take moments must either be null or positive." }
        val hour = hourInclusive.truncatedTo(ChronoUnit.HOURS)
        val indexOfHour = moments.indexOfFirst { it.hour == hour }
        return if (indexOfHour < 0) null else moments
            .subList(indexOfHour, moments.size)
            .let { if (take != null) it.take(take) else it }
    }

    fun momentsOn(day: LocalDate): List<T>? =
        dayMomentsFrom(day, take = 1)?.firstOrNull()

    fun dayMomentsFrom(
        dayInclusive: LocalDate,
        take: Int? = null
    ): List<List<T>>? {
        require(take == null || take > 0) { "Take days must either be null or positive." }
        val momentsGroupedIntoDays = moments
            .groupBy { it.hour.toLocalDate() }
            .map { it.key to it.value }
        val indexOfDay = momentsGroupedIntoDays.indexOfFirst { it.first == dayInclusive }
        return if (indexOfDay < 0) null else
            momentsGroupedIntoDays
                .subList(indexOfDay, momentsGroupedIntoDays.size)
                .map { it.second }
                .let { if (take != null) it.take(take) else it }
    }

    fun matches(other: HourPeriod<*>): Boolean =
        moments.map { it.hour } == other.moments.map { it.hour }

    private fun requireAscendingAndComplete() {
        if (moments.size == 1) return
        var previousMoment = moments[0]
        for (i in 1..moments.lastIndex) {
            val nextMoment = moments[i]
            require(ChronoUnit.HOURS.between(previousMoment.hour, nextMoment.hour) == 1L) {
                "Moments of HourPeriod be sorted and spaced by one hour, but contained ${previousMoment.hour} before ${nextMoment.hour}."
            }
            previousMoment = nextMoment
        }
    }

    private fun requireNotEmpty() =
        require(moments.isNotEmpty()) { "Moments of HourPeriod must not be empty." }
}

inline fun requireMatching(vararg periods: HourPeriod<*>, lazyMessage: () -> Any = { "Periods must have matching times." }) {
    var previous: HourPeriod<*>? = null
    for (period in periods) {
        if (previous == null) previous = period
        require(previous.matches(period), lazyMessage = lazyMessage)
        previous = period
    }
}