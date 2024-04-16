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

package com.davidtakac.bura.place.picker

import androidx.compose.runtime.*
import kotlinx.coroutines.*

private class ContentLoadingIndicatorState(
    private val showDelay: Long,
    private val minShowTime: Long
) {
    private val _isVisible: MutableState<Boolean> = mutableStateOf(false)
    val isVisible: Boolean by _isVisible

    private var startTime: Long = -1L
    private var showJob: Job? = null
    private var hideJob: Job? = null

    fun show(scope: CoroutineScope) {
        cancelDelayedHide()
        showJob = scope.launch {
            delayShow()
        }
    }

    fun hide(scope: CoroutineScope) {
        if (showJob?.isActive == true) {
            // Show didn't happen yet, so just cancel it
            cancelDelayedShow()
        } else {
            // Show did happen, so hide it after some time
            hideJob = scope.launch {
                delayHide()
            }
        }
    }

    private suspend fun delayShow() {
        startTime = -1
        delay(showDelay)
        startTime = System.currentTimeMillis()
        _isVisible.value = true
    }

    private suspend fun delayHide() {
        val diff = System.currentTimeMillis() - startTime
        if (startTime != -1L && diff < minShowTime) {
            // Ensure visible for at least some time
            delay(minShowTime - diff)
        }
        _isVisible.value = false
    }

    private fun cancelDelayedShow() {
        showJob?.cancel()
        showJob = null
        startTime = -1
    }

    private fun cancelDelayedHide() {
        hideJob?.cancel()
        hideJob = null
    }
}

@Composable
fun ContentLoadingIndicator(
    isLoading: Boolean,
    showDelay: Long = 500L,
    minShowTime: Long = 500L,
    content: @Composable (isVisible: Boolean) -> Unit
) {
    val state = remember {
        ContentLoadingIndicatorState(showDelay, minShowTime)
    }
    LaunchedEffect(isLoading) {
        if (isLoading) state.show(this)
        else state.hide(this)
    }
    content(state.isVisible)
}