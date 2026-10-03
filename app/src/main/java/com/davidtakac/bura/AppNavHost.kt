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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.davidtakac.bura.graphs.EssentialGraphsDestination
import com.davidtakac.bura.places.picker.PlacePickerViewModel
import com.davidtakac.bura.places.search.compose.SearchedPlaceEditDestination
import com.davidtakac.bura.settings.SettingsDestination
import com.davidtakac.bura.summary.SummaryDestination
import com.davidtakac.bura.theme.Theme
import com.davidtakac.bura.unexpectederror.UnexpectedErrorScreen
import com.davidtakac.bura.unexpectederror.UnexpectedErrorUiState
import com.davidtakac.bura.unexpectederror.UnexpectedErrorViewModel
import java.time.LocalDate

@Composable
fun AppNavHost(theme: Theme, onThemeClick: (Theme) -> Unit) {
    val controller = rememberNavController()
    val unexpectedErrorVM = viewModel<UnexpectedErrorViewModel>(factory = UnexpectedErrorViewModel.Factory)
    val unexpectedErrorState = unexpectedErrorVM.state.collectAsStateWithLifecycle().value
    LaunchedEffect(unexpectedErrorState) {
        if (unexpectedErrorState is UnexpectedErrorUiState.Ongoing) {
            controller.navigate("unexpected-error") {
                popUpTo(controller.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

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
                },
                onPrecipitationClick = {
                    controller.navigate("essential-graphs")
                },
                onSearchedPlaceEditRequest = {
                    controller.navigate("searched-place-edit")
                },
            )
        }
        composable("searched-place-edit") { backStackEntry ->
            val viewModel = viewModel<PlacePickerViewModel>(
                viewModelStoreOwner = remember(backStackEntry) {
                    controller.getBackStackEntry("summary")
                },
                factory = PlacePickerViewModel.Factory,
            )
            SearchedPlaceEditDestination(
                viewModel = viewModel,
                onSearchedPlaceToEditNull = controller::navigateUp
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
                onSelectPlaceClick = controller::navigateUp,
                onBackClick = controller::navigateUp
            )
        }
        composable("settings") {
            SettingsDestination(
                theme = theme,
                onThemeClick = onThemeClick,
                onBackClick = controller::navigateUp
            )
        }
        composable("unexpected-error") {
            UnexpectedErrorScreen(
                cause = (unexpectedErrorState as UnexpectedErrorUiState.Ongoing).cause,
                onGoHomeClick = {
                    unexpectedErrorVM.consumeError()
                    controller.navigate("summary") {
                        popUpTo(controller.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}