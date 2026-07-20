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

import org.junit.Assert.*
import org.junit.Test

class UvIndexTest {
    @Test
    fun risk() {
        assertEquals(UvIndex.Risk.Low, UvIndex(0.0).risk)
        assertEquals(UvIndex.Risk.Moderate, UvIndex(3.0).risk)
        assertEquals(UvIndex.Risk.High, UvIndex(6.0).risk)
        assertEquals(UvIndex.Risk.VeryHigh, UvIndex(8.0).risk)
        assertEquals(UvIndex.Risk.Extreme, UvIndex(19.0).risk)
    }

    @Test
    fun equals() {
        assertEquals(UvIndex(1.0), UvIndex(1.0))
    }

    @Test
    fun comparison() {
        assertTrue(UvIndex(1.0) > UvIndex(0.0))
    }
}