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

package com.davidtakac.bura

import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.ConditionMoment
import com.davidtakac.bura.condition.ConditionPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.temporal.ChronoUnit

private const val SEVERE = 50
private const val NOT_SEVERE = 1
private const val BIGGER_NOT_SEVERE = 2

class ConditionPeriodTest {
    @Test
    fun `representative is one of the codes when all codes are the same`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val code = 1
        val period = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = code, isDay = true)
                ),
                ConditionMoment(
                    secondMoment,
                    Condition(wmoCode = code, isDay = true)
                )
            )
        )
        assertEquals(code, period.day?.wmoCode)
    }

    @Test
    fun `representative is most severe when all codes are different`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val lessSevereCode = 1
        val moreSevereCode = 2
        val period = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = lessSevereCode, isDay = true)
                ),
                ConditionMoment(
                    secondMoment,
                    Condition(wmoCode = moreSevereCode, isDay = true)
                )
            )
        )
        assertEquals(moreSevereCode, period.day?.wmoCode)
    }

    @Test
    fun `representative is most common when no severe conditions`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val period = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = NOT_SEVERE, isDay = true)
                ),
                ConditionMoment(
                    secondMoment,
                    Condition(wmoCode = NOT_SEVERE, isDay = true)
                ),
                ConditionMoment(
                    thirdMoment,
                    Condition(wmoCode = BIGGER_NOT_SEVERE, isDay = true)
                )
            )
        )
        assertEquals(NOT_SEVERE, period.day?.wmoCode)
    }

    @Test
    fun `representative is most severe when at least one severe condition`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val period = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = NOT_SEVERE, isDay = true)
                ),
                ConditionMoment(
                    secondMoment,
                    Condition(wmoCode = NOT_SEVERE, isDay = true)
                ),
                ConditionMoment(
                    thirdMoment,
                    Condition(wmoCode = SEVERE, isDay = true)
                ),
            )
        )
        assertEquals(SEVERE, period.day?.wmoCode)
    }

    @Test
    fun `determines day and night rep`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = true)
                ),
                ConditionMoment(
                    secondMoment,
                    Condition(wmoCode = 2, isDay = false)
                ),
            )
        )
        assertEquals(1, period.day?.wmoCode)
        assertEquals(2, period.night?.wmoCode)
    }

    @Test
    fun `null when no daytime`() {
        val firstMoment = unixEpochStart
        val period = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = false)
                ),
            )
        )
        assertNull(period.day)
    }

    @Test
    fun `null when no nighttime`() {
        val firstMoment = unixEpochStart
        val period = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = true)
                ),
            )
        )
        assertNull(period.night)
    }
}