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

import com.davidtakac.bura.unixEpochStartZdt
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

private val dangerous = UvIndex(3.0)
private val safe = UvIndex(2.0)

class UvIndexPeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = unixEpochStartZdt
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstMoment, UvIndex(0.0)),
                UvIndexMoment(secondMoment, UvIndex(1.0)),
            ),
        )
        assertEquals(UvIndex(0.0), period.minimum)
        assertEquals(UvIndex(1.0), period.maximum)
    }

    @Test
    fun `protection window with one dangerous hour`() {
        val firstDanger = unixEpochStartZdt
        val firstSafe = firstDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(firstSafe, safe)
            ),
        )
        assertEquals(
            listOf(SunProtectionWindow(firstDanger, firstSafe)),
            period.protectionWindows
        )
    }

    @Test
    fun `protection window with multiple dangerous hours`() {
        val firstDanger = unixEpochStartZdt
        val secondDanger = firstDanger.plus(1, ChronoUnit.HOURS)
        val firstSafe = secondDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(secondDanger, dangerous),
                UvIndexMoment(firstSafe, safe)
            ),
        )
        assertEquals(
            listOf(SunProtectionWindow(firstDanger, firstSafe)),
            period.protectionWindows
        )
    }

    @Test
    fun `protection window when dangerous period has no end`() {
        val firstDanger = unixEpochStartZdt
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstDanger,dangerous)))
        assertEquals(
            listOf(SunProtectionWindow(firstDanger, null)),
            period.protectionWindows
        )
    }

    @Test
    fun `no protection windows are empty when no dangerous hours`() {
        val firstSafe = unixEpochStartZdt
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstSafe, safe)))
        assertEquals(
            emptyList<SunProtectionWindow>(),
            period.protectionWindows
        )
    }

    @Test
    fun `multiple protection windows`() {
        val firstDanger = unixEpochStartZdt
        val firstSafe = firstDanger.plus(1, ChronoUnit.HOURS)
        val secondDanger = firstSafe.plus(1, ChronoUnit.HOURS)
        val secondSafe = secondDanger.plus(1, ChronoUnit.HOURS)
        val thirdDanger = secondSafe.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(firstSafe, safe),
                UvIndexMoment(secondDanger, dangerous),
                UvIndexMoment(secondSafe, safe),
                UvIndexMoment(thirdDanger, dangerous)
            ),
        )
        assertEquals(
            listOf(
                SunProtectionWindow(firstDanger, firstSafe),
                SunProtectionWindow(secondDanger, secondSafe),
                SunProtectionWindow(thirdDanger, null)
            ),
            period.protectionWindows
        )
    }
}