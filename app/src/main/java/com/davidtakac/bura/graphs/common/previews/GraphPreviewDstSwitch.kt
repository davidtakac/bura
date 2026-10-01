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

package com.davidtakac.bura.graphs.common.previews

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

sealed interface GraphPreviewDstSwitch {
    data object None : GraphPreviewDstSwitch
    data class FallBack(val seconds: Int) : GraphPreviewDstSwitch
    data class SpringForward(val seconds: Int) : GraphPreviewDstSwitch

    companion object {
        fun defaultSwitches(): List<GraphPreviewDstSwitch> = listOf(
            None,
            FallBack(seconds = 1.hours.inWholeSeconds.toInt()),
            FallBack(seconds = 30.minutes.inWholeSeconds.toInt()),
            SpringForward(seconds = 1.hours.inWholeSeconds.toInt()),
            SpringForward(seconds = 30.minutes.inWholeSeconds.toInt())
        )
    }
}