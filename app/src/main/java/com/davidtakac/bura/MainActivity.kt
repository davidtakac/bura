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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ColorProtection
import androidx.core.view.insets.ProtectionLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidtakac.bura.theme.AppTheme
import com.davidtakac.bura.theme.NavBarProtectionColor
import com.davidtakac.bura.theme.Theme
import com.davidtakac.bura.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContentView(R.layout.activity_main)

        findViewById<ComposeView>(R.id.cv_main).setContent {
            val themeViewModel = viewModel<ThemeViewModel>(factory = ThemeViewModel.Factory)
            val themeState = themeViewModel.state.collectAsState().value

            val isDarkTheme = themeState.theme.isDarkTheme
            LaunchedEffect(isDarkTheme) {
                setSystemBarIconColors(isDarkTheme)
            }

            AppTheme(isDarkTheme) {
                val navBarProtectionColor = themeState.navBarProtectionColor.value
                LaunchedEffect(navBarProtectionColor) {
                    setNavBarProtectionColor(navBarProtectionColor)
                }

                AppNavHost(
                    theme = themeState.theme,
                    onThemeClick = themeViewModel::setTheme,
                    onBackgroundColorChange = themeViewModel::setNavBarProtectionColor,
                )
            }
        }
    }

    private fun setSystemBarIconColors(isDarkTheme: Boolean) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }

    private fun setNavBarProtectionColor(backgroundColor: Color) {
        findViewById<ProtectionLayout>(R.id.pl_main).setProtections(
            listOf(
                ColorProtection(
                    WindowInsetsCompat.Side.BOTTOM,
                    backgroundColor.copy(alpha = 0.8f).toArgb()
                )
            )
        )
    }
}

private val Theme.isDarkTheme
    @Composable get() = when (this) {
        Theme.Dark -> true
        Theme.Light -> false
        Theme.FollowSystem -> isSystemInDarkTheme()
    }

private val NavBarProtectionColor.value
    @Composable get() = when (this) {
        is NavBarProtectionColor.Custom -> value
        NavBarProtectionColor.Default -> MaterialTheme.colorScheme.background
    }