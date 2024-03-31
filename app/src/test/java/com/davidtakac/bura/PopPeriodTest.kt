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

import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.pop.PopMoment
import com.davidtakac.bura.pop.PopPeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.pow

class PopPeriodTest {
    @Test
    fun maximum() {
        val firstMoment = firstLocalDateTime
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PopPeriod(
            moments = listOf(
                PopMoment(firstMoment, pop = Pop(2.0)),
                PopMoment(secondMoment, pop = Pop(8.0)),
            )
        )
        assertEquals(Pop(8.0), period.maximum)
    }

    @Test
    fun once() {
        val firstMoment = firstLocalDateTime
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val period = PopPeriod(
            moments = listOf(
                PopMoment(firstMoment, pop = Pop(5.0)),
                PopMoment(secondMoment, pop = Pop(5.0)),
                PopMoment(thirdMoment, pop = Pop(5.0))
            )
        )
        assertEquals(Pop((1 - 0.95.pow(3)) * 100), period.once)
    }
}