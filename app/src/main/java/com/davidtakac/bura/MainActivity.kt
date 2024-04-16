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

package com.davidtakac.bura

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.Theme
import com.davidtakac.bura.common.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentSystemBars()
        setContent {
            val themeViewModel = viewModel<ThemeViewModel>(factory = ThemeViewModel.Factory)
            val theme = themeViewModel.state.collectAsState().value
            val useDarkTheme = when (theme) {
                Theme.Dark -> true
                Theme.Light -> false
                Theme.FollowSystem -> isSystemInDarkTheme()
            }

            LaunchedEffect(useDarkTheme) {
                setSystemBarIconColors(useDarkTheme)
            }
            AppTheme(useDarkTheme) {
                AppNavHost(
                    theme = theme,
                    onThemeClick = themeViewModel::setTheme
                )
            }
        }
    }

    private fun setTransparentSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setSystemBarIconColors(darkTheme: Boolean) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
}