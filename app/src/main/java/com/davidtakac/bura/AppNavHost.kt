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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.davidtakac.bura.common.Theme
import com.davidtakac.bura.common.rememberAppLocale
import com.davidtakac.bura.graphs.EssentialGraphsScreen
import com.davidtakac.bura.graphs.EssentialGraphsViewModel
import com.davidtakac.bura.place.picker.PlacePickerViewModel
import com.davidtakac.bura.settings.SettingsScreen
import com.davidtakac.bura.settings.SelectedUnitsViewModel
import com.davidtakac.bura.summary.SummaryScreen
import com.davidtakac.bura.summary.SummaryViewModel
import java.time.LocalDate

@Composable
fun AppNavHost(theme: Theme, onThemeClick: (Theme) -> Unit) {
    val controller = rememberNavController()
    NavHost(navController = controller, startDestination = "summary") {
        composable("summary") {
            val placePickerVM = viewModel<PlacePickerViewModel>(factory = PlacePickerViewModel.Factory)
            val summaryVM = viewModel<SummaryViewModel>(factory = SummaryViewModel.Factory)

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
                    placePickerVM.getSelectedPlace()
                    summaryVM.getSummary()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val pickerState = placePickerVM.state.collectAsState().value
            val selectedPlace = pickerState.selectedPlace
            var searchActive by remember(selectedPlace) { mutableStateOf(false) }
            var searchQuery by remember(searchActive, selectedPlace) {
                mutableStateOf(
                    if (searchActive) ""
                    else selectedPlace?.name ?: ""
                )
            }
            LaunchedEffect(searchActive) {
                if (!searchActive) {
                    searchQuery = selectedPlace?.name ?: ""
                    summaryVM.getSummary()
                } else {
                    placePickerVM.getSavedPlaces()
                }
            }

            val appLocale = rememberAppLocale()

            SummaryScreen(
                summaryState = summaryVM.state.collectAsState().value,
                onHourlySectionClick = { controller.navigate("essential-graphs") },
                onDayClick = { day -> controller.navigate("essential-graphs?initialDay=$day") },
                onSettingsButtonClick = { controller.navigate("settings") },

                pickerState = pickerState,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                searchActive = searchActive,
                onSearchActiveChange = { searchActive = it },
                onSearchQueryClearClick = { searchQuery = "" },
                onSearch = { placePickerVM.searchPlaces(query = searchQuery, languageCode = appLocale.language) },
                onPlaceClick = placePickerVM::selectPlace,
                onPlaceDeleteClick = placePickerVM::deletePlace,

                onTryAgainClick = summaryVM::getSummary,
                onSelectPlaceClick = { searchActive = true }
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
            val viewModel = viewModel<EssentialGraphsViewModel>(factory = EssentialGraphsViewModel.Factory)
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.getGraphs()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            EssentialGraphsScreen(
                initialDay = backStackEntry.arguments?.getString("initialDay")?.let(LocalDate::parse),
                state = viewModel.state.collectAsState().value,
                onTryAgainClick = viewModel::getGraphs,
                onSelectPlaceClick = controller::popBackStack,
                onBackClick = controller::popBackStack
            )
        }
        composable("settings") {
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
                onBackClick = controller::popBackStack
            )
        }
    }
}