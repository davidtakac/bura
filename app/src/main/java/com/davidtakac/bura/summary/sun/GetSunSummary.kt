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

package com.davidtakac.bura.summary.sun

import com.davidtakac.bura.forecast.parameters.condition.ConditionPeriod
import com.davidtakac.bura.forecast.parameters.sun.SunEvent
import com.davidtakac.bura.forecast.parameters.sun.SunMoment
import com.davidtakac.bura.forecast.parameters.sun.SunPeriod
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

private const val LATER_HOUR_THRESH = 25

fun getSunSummary(
    now: ZonedDateTime,
    sunPeriod: SunPeriod?,
    condPeriod: ConditionPeriod
): SunSummary? {
    val futureSun = sunPeriod?.momentsFrom(now.toInstant())
    val firstSun = futureSun?.firstOrNull()
    return when {
        firstSun == null -> outOfSight(now, condPeriod)
        firstSun.event == SunEvent.Rise -> sunrise(now, futureSun, firstSun)
        else -> sunset(now, futureSun, firstSun)
    }
}

private fun outOfSight(
    now: ZonedDateTime,
    condPeriod: ConditionPeriod
): SunSummary? {
    val nowInstant = now.toInstant()
    val futureDesc = condPeriod.periodFrom(nowInstant) ?: return null
    val isDayNow = futureDesc[nowInstant]!!.condition.isDay
    val lastMoment = futureDesc.last().timeZdt
    val duration = Duration.between(now, lastMoment).plusHours(1)
    return if (isDayNow) {
        Sunset.OutOfSight(duration)
    } else {
        Sunrise.OutOfSight(duration)
    }
}

private fun sunrise(
    now: ZonedDateTime,
    futureSun: List<SunMoment>,
    firstSun: SunMoment
): Sunrise {
    val sunrise = firstSun.timeZdt
    return if (ChronoUnit.HOURS.between(now, sunrise) >= LATER_HOUR_THRESH) {
        Sunrise.Later(sunrise.toLocalDateTime())
    } else {
        val sunset = futureSun[1].timeZdt
        if (ChronoUnit.HOURS.between(now, sunset) < LATER_HOUR_THRESH) {
            Sunrise.WithSunsetSoon(
                time = sunrise.toLocalTime(),
                sunset = futureSun[1].timeZdt.toLocalTime()
            )
        } else {
            Sunrise.WithSunsetLater(
                time = sunrise.toLocalTime(),
                sunset = futureSun[1].timeZdt.toLocalDateTime()
            )
        }
    }
}

private fun sunset(
    now: ZonedDateTime,
    futureSun: List<SunMoment>,
    firstSun: SunMoment
): Sunset {
    val sunset = firstSun.timeZdt
    return if (ChronoUnit.HOURS.between(now, sunset) >= LATER_HOUR_THRESH) {
        Sunset.Later(sunset.toLocalDateTime())
    } else {
        val sunrise = futureSun[1].timeZdt
        if (ChronoUnit.HOURS.between(now, sunrise) < LATER_HOUR_THRESH) {
            Sunset.WithSunriseSoon(
                time = firstSun.timeZdt.toLocalTime(),
                sunrise = futureSun[1].timeZdt.toLocalTime()
            )
        } else {
            Sunset.WithSunriseLater(
                time = firstSun.timeZdt.toLocalTime(),
                sunrise = futureSun[1].timeZdt.toLocalDateTime()
            )
        }
    }
}

sealed interface SunSummary

sealed interface Sunrise : SunSummary {
    data class WithSunsetSoon(
        val time: LocalTime,
        val sunset: LocalTime
    ) : Sunrise

    data class WithSunsetLater(
        val time: LocalTime,
        val sunset: LocalDateTime
    ) : Sunrise

    data class Later(val time: LocalDateTime) : Sunrise

    data class OutOfSight(val forDuration: Duration) : Sunrise
}

sealed interface Sunset : SunSummary {
    data class WithSunriseSoon(
        val time: LocalTime,
        val sunrise: LocalTime
    ) : Sunset

    data class WithSunriseLater(
        val time: LocalTime,
        val sunrise: LocalDateTime
    ) : Sunset

    data class Later(val time: LocalDateTime) : Sunset

    data class OutOfSight(val forDuration: Duration) : Sunset
}