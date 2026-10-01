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

package com.davidtakac.bura.forecast.parameters.pop

import org.junit.Assert.*
import org.junit.Test

class PopTest {
    @Test
    fun equals() {
        assertEquals(Pop(1.0), Pop(1.0))
    }

    @Test
    fun compare() {
        assertTrue(Pop(5.5) > Pop(0.0))
    }

    @Test
    fun `rounds 2,49 to 0`() {
        val pop = Pop(2.49)
        assertEquals(0, pop.value)
    }

    @Test
    fun `rounds 2,5 to 5`() {
        val pop = Pop(2.5)
        assertEquals(5, pop.value)
    }

    @Test
    fun `rounds 2,7 to 5`() {
        val pop = Pop(2.7)
        assertEquals(5, pop.value)
    }

    @Test
    fun `rounds 5,0 to 5`() {
        val pop = Pop(5.0)
        assertEquals(5, pop.value)
    }

    @Test
    fun `rounds 44,49 to 45`() {
        val pop = Pop(44.49)
        assertEquals(45, pop.value)
    }

    @Test
    fun `rounds 44,5 to 45`() {
        val pop = Pop(44.5)
        assertEquals(45, pop.value)
    }

    @Test
    fun `rounds 44,7 to 45`() {
        val pop = Pop(44.7)
        assertEquals(45, pop.value)
    }

    @Test
    fun `rounds 45,0 to 45`() {
        val pop = Pop(45.0)
        assertEquals(45, pop.value)
    }

    @Test
    fun `rounds 0,0 to 0`() {
        val pop = Pop(0.0)
        assertEquals(0, pop.value)
    }

    @Test
    fun `rounds 100,0 to 100`() {
        val pop = Pop(100.0)
        assertEquals(100, pop.value)
    }
}