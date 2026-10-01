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

import com.davidtakac.bura.unixEpochStartZdt
import org.junit.Assert
import org.junit.Test
import java.time.temporal.ChronoUnit

class SunPeriodTest {
    @Test
    fun `splits into future moments`() {
        val startOfTime = unixEpochStartZdt
        val firstSunset = startOfTime.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeFirstSunset = firstSunset.minus(15, ChronoUnit.MINUTES)
        val firstSunrise = startOfTime.plus(3, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeFirstSunrise = firstSunrise.minus(15, ChronoUnit.MINUTES)
        val secondSunset = startOfTime.plus(5, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeSecondSunset = secondSunset.minus(15, ChronoUnit.MINUTES)
        val afterSecondSunset = secondSunset.plus(15, ChronoUnit.MINUTES)
        val period = SunPeriod(
            moments = listOf(
                SunMoment(firstSunset, SunEvent.Set),
                SunMoment(firstSunrise, SunEvent.Rise),
                SunMoment(secondSunset, SunEvent.Set)
            )
        )
        val threeMoments = period.momentsFrom(beforeFirstSunset.toInstant())
        Assert.assertEquals(3, threeMoments!!.size)
        Assert.assertTrue(threeMoments[0].event == SunEvent.Set)
        Assert.assertTrue(threeMoments[0].timeZdt == firstSunset)
        Assert.assertTrue(threeMoments[1].event == SunEvent.Rise)
        Assert.assertTrue(threeMoments[1].timeZdt == firstSunrise)

        val twoMoments = period.momentsFrom(beforeFirstSunrise.toInstant())
        Assert.assertEquals(2, twoMoments!!.size)
        Assert.assertTrue(twoMoments[0].event == SunEvent.Rise)
        Assert.assertTrue(twoMoments[0].timeZdt == firstSunrise)
        Assert.assertTrue(twoMoments[1].event == SunEvent.Set)
        Assert.assertTrue(twoMoments[1].timeZdt == secondSunset)

        val oneMoment = period.momentsFrom(beforeSecondSunset.toInstant())
        Assert.assertEquals(1, oneMoment!!.size)
        Assert.assertTrue(oneMoment[0].event == SunEvent.Set)
        Assert.assertTrue(oneMoment[0].timeZdt == secondSunset)

        Assert.assertNull(period.momentsFrom(afterSecondSunset.toInstant()))
    }
}