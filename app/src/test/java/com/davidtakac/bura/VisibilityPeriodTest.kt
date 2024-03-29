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

import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.visibility.VisibilityMoment
import com.davidtakac.bura.visibility.VisibilityPeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class VisibilityPeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = Instant.ofEpochSecond(0)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = VisibilityPeriod(
            moments = listOf(
                VisibilityMoment(firstMoment, Visibility.fromMeters(1.0)),
                VisibilityMoment(secondMoment, Visibility.fromMeters(2.0))
            )
        )
        assertEquals(Visibility.fromMeters(1.0), period.minimum)
        assertEquals(Visibility.fromMeters(2.0), period.maximum)
    }
}