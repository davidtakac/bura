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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.davidtakac.bura.App
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UnexpectedErrorViewModel (
    private val consumer: UnexpectedErrorConsumer
) : ViewModel() {
    val state = consumer.state
        .map {
            when (it) {
                is UnexpectedErrorState.Ongoing -> UnexpectedErrorUiState.Ongoing(
                    cause = it.cause.stackTraceToString()
                )
                UnexpectedErrorState.Idle -> UnexpectedErrorUiState.Idle
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UnexpectedErrorUiState.Idle
        )

    fun consumeError() {
        consumer.consume()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return UnexpectedErrorViewModel(container.unexpectedErrorConsumer) as T
            }
        }
    }
}

sealed interface UnexpectedErrorUiState {
    data class Ongoing(val cause: String) : UnexpectedErrorUiState
    data object Idle : UnexpectedErrorUiState
}