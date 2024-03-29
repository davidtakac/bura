/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura

import com.davidtakac.bura.sun.SunEvent
import com.davidtakac.bura.sun.SunMoment
import com.davidtakac.bura.sun.SunPeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class SunMomentListTest {
    @Test
    fun `splits into future moments`() {
        val startOfTime = Instant.ofEpochSecond(0)
        val firstSunset = startOfTime.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeFirstSunset = firstSunset.minus(15, ChronoUnit.MINUTES)
        val firstSunrise = startOfTime.plus(3, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeFirstSunrise = firstSunrise.minus(15, ChronoUnit.MINUTES)
        val secondSunset = startOfTime.plus(5, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeSecondSunset = secondSunset.minus(15, ChronoUnit.MINUTES)
        val afterSecondSunset = secondSunset.plus(15, ChronoUnit.MINUTES)
        val period = SunPeriod(
            moments = listOf(
                SunMoment(firstSunset, SunEvent.Sunset),
                SunMoment(firstSunrise, SunEvent.Sunrise),
                SunMoment(secondSunset, SunEvent.Sunset)
            )
        )
        val threeMoments = period.momentsFrom(beforeFirstSunset)
        assertEquals(3, threeMoments!!.size)
        assertTrue(threeMoments[0].event == SunEvent.Sunset)
        assertTrue(threeMoments[0].time == firstSunset)
        assertTrue(threeMoments[1].event == SunEvent.Sunrise)
        assertTrue(threeMoments[1].time == firstSunrise)

        val twoMoments = period.momentsFrom(beforeFirstSunrise)
        assertEquals(2, twoMoments!!.size)
        assertTrue(twoMoments[0].event == SunEvent.Sunrise)
        assertTrue(twoMoments[0].time == firstSunrise)
        assertTrue(twoMoments[1].event == SunEvent.Sunset)
        assertTrue(twoMoments[1].time == secondSunset)

        val oneMoment = period.momentsFrom(beforeSecondSunset)
        assertEquals(1, oneMoment!!.size)
        assertTrue(oneMoment[0].event == SunEvent.Sunset)
        assertTrue(oneMoment[0].time == secondSunset)

        assertNull(period.momentsFrom(afterSecondSunset))
    }
}