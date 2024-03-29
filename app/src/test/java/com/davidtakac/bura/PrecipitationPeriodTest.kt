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

import com.davidtakac.bura.precipitation.MixedPrecipitation
import com.davidtakac.bura.precipitation.PrecipitationMoment
import com.davidtakac.bura.precipitation.PrecipitationPeriod
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class PrecipitationPeriodTest {
    @Test
    fun depth() {
        val period = PrecipitationPeriod(
            moments = listOf(
                PrecipitationMoment(
                    Instant.ofEpochSecond(0),
                    MixedPrecipitation.fromMillimeters(Rain.fromMillimeters(1.0), Showers.Zero, Snow.Zero),
                )
            )
        )
        assertEquals(MixedPrecipitation.fromMillimeters(Rain.fromMillimeters(1.0), Showers.Zero, Snow.Zero), period.total)
    }
}