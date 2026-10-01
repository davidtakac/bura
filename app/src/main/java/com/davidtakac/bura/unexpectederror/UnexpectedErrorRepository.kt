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

package com.davidtakac.bura.unexpectederror

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnexpectedErrorRepository : UnexpectedErrorSetter, UnexpectedErrorConsumer {
    private val _state = MutableStateFlow<UnexpectedErrorState>(UnexpectedErrorState.Idle)
    override val state: Flow<UnexpectedErrorState> = _state.asStateFlow()

    override fun set(cause: Exception) {
        _state.value = UnexpectedErrorState.Ongoing(cause)
    }

    override fun consume() {
        _state.value = UnexpectedErrorState.Idle
    }
}

interface UnexpectedErrorSetter {
    fun set(cause: Exception)
}

interface UnexpectedErrorConsumer {
    val state: Flow<UnexpectedErrorState>
    fun consume()
}

sealed interface UnexpectedErrorState {
    data class Ongoing(val cause: Exception) : UnexpectedErrorState
    data object Idle : UnexpectedErrorState
}