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

package com.davidtakac.bura.common

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.davidtakac.bura.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val DARK_MODE_KEY = "dark_mode"

class ThemeViewModel(private val prefs: SharedPreferences) : ViewModel() {
    private val _state = MutableStateFlow(
        prefs.getString(DARK_MODE_KEY, null)?.let(Theme::valueOf) ?: Theme.FollowSystem
    )
    val state = _state.asStateFlow()

    fun setTheme(value: Theme) {
        prefs.edit { putString(DARK_MODE_KEY, value.name) }
        _state.value = value
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return ThemeViewModel(container.prefs) as T
            }
        }
    }
}

enum class Theme {
    Dark, Light, FollowSystem
}