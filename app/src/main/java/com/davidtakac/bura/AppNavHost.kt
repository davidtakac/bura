/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.davidtakac.bura.common.Theme
import com.davidtakac.bura.graphs.EssentialGraphsDestination
import com.davidtakac.bura.settings.SettingsScreen
import com.davidtakac.bura.settings.SelectedUnitsViewModel
import com.davidtakac.bura.summary.SummaryDestination
import java.time.LocalDate

@Composable
fun AppNavHost(theme: Theme, onThemeClick: (Theme) -> Unit) {
    val controller = rememberNavController()
    NavHost(navController = controller, startDestination = "summary") {
        composable("summary") {
            SummaryDestination(
                onHourlySectionClick = {
                    controller.navigate("essential-graphs")
                },
                onDayClick = {
                    controller.navigate("essential-graphs?initialDay=$it")
                },
                onSettingsButtonClick = {
                    controller.navigate("settings")
                }
            )
        }
        composable(
            route = "essential-graphs?initialDay={initialDay}",
            arguments = listOf(
                navArgument("initialDay") {
                    nullable = true
                    defaultValue = null
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            EssentialGraphsDestination(
                initialDay = backStackEntry.arguments?.getString("initialDay")?.let(LocalDate::parse),
                onSelectPlaceClick = controller::popBackStack,
                onBackClick = controller::popBackStack
            )
        }
        composable("settings") {
            val unitsVM =
                viewModel<SelectedUnitsViewModel>(factory = SelectedUnitsViewModel.Factory)
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
                onBackClick = controller::popBackStack
            )
        }
    }
}