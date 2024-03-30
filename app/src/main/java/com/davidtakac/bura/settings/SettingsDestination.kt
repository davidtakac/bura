/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidtakac.bura.common.Theme

@Composable
fun SettingsDestination(theme: Theme, onThemeClick: (Theme) -> Unit, onBackClick: () -> Unit) {
    val unitsVM = viewModel<SelectedUnitsViewModel>(factory = SelectedUnitsViewModel.Factory)
    LaunchedEffect(Unit) { unitsVM.getSettings() }
    SettingsScreen(
        units = unitsVM.state.collectAsState().value,
        theme = theme,
        onTemperatureUnitClick = unitsVM::selectTemperatureUnit,
        onWindUnitClick = unitsVM::selectWindUnit,
        onPrecipitationUnitClick = unitsVM::selectPrecipitationUnit,
        onRainUnitClick = unitsVM::selectRainUnit,
        onShowersUnitClick = unitsVM::selectShowersUnit,
        onSnowUnitClick = unitsVM::selectSnowUnit,
        onPressureUnitClick = unitsVM::selectPressureUnit,
        onVisibilityUnitClick = unitsVM::selectVisibilityUnit,
        onThemeClick = onThemeClick,
        onBackClick = onBackClick
    )
}