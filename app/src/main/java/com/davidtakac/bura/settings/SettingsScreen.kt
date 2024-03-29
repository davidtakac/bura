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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.Theme
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.units.Units
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.wind.WindSpeed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    units: Units?,
    theme: Theme,
    onTemperatureUnitClick: (Temperature.Unit) -> Unit,
    onWindUnitClick: (WindSpeed.Unit) -> Unit,
    onPrecipitationUnitClick: (Precipitation.Unit) -> Unit,
    onRainUnitClick: (Precipitation.Unit) -> Unit,
    onShowersUnitClick: (Precipitation.Unit) -> Unit,
    onSnowUnitClick: (Precipitation.Unit) -> Unit,
    onPressureUnitClick: (Pressure.Unit) -> Unit,
    onVisibilityUnitClick: (Visibility.Unit) -> Unit,
    onThemeClick: (Theme) -> Unit,
    onBackClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.settings_screen_title))
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { contentPadding ->
        Crossfade(
            targetState = units,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            label = "Settings crossfade"
        ) {
            if (it != null) SettingsList(
                units = it,
                theme = theme,
                onTemperatureUnitClick = onTemperatureUnitClick,
                onWindUnitClick = onWindUnitClick,
                onPrecipitationUnitClick = onPrecipitationUnitClick,
                onRainUnitClick = onRainUnitClick,
                onShowersUnitClick = onShowersUnitClick,
                onSnowUnitClick = onSnowUnitClick,
                onPressureUnitClick = onPressureUnitClick,
                onVisibilityUnitClick = onVisibilityUnitClick,
                onThemeClick = onThemeClick,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) else SettingsLoadingIndicator(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun SettingsList(
    units: Units,
    theme: Theme,
    onTemperatureUnitClick: (Temperature.Unit) -> Unit,
    onWindUnitClick: (WindSpeed.Unit) -> Unit,
    onPrecipitationUnitClick: (Precipitation.Unit) -> Unit,
    onRainUnitClick: (Precipitation.Unit) -> Unit,
    onShowersUnitClick: (Precipitation.Unit) -> Unit,
    onSnowUnitClick: (Precipitation.Unit) -> Unit,
    onPressureUnitClick: (Pressure.Unit) -> Unit,
    onVisibilityUnitClick: (Visibility.Unit) -> Unit,
    onThemeClick: (Theme) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTempDialog by remember { mutableStateOf(false) }
    var showWindDialog by remember { mutableStateOf(false) }
    var showPrecipDialog by remember { mutableStateOf(false) }
    var showRainDialog by remember { mutableStateOf(false) }
    var showShowersDialog by remember { mutableStateOf(false) }
    var showSnowDialog by remember { mutableStateOf(false) }
    var showPressureDialog by remember { mutableStateOf(false) }
    var showVisDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showTempDialog) {
        MultipleChoiceDialog(
            title = stringResource(R.string.settings_screen_title_temp_unit),
            choices = Temperature.Unit.entries.map { it.label() },
            selectedIdx = Temperature.Unit.entries.indexOf(units.temperature),
            onSelect = {
                onTemperatureUnitClick(Temperature.Unit.entries[it])
                showTempDialog = false
            },
            onDismissRequest = {
                showTempDialog = false
            }
        )
    }

    if (showWindDialog) {
        MultipleChoiceDialog(
            title = stringResource(id = R.string.settings_screen_title_wind_unit),
            choices = WindSpeed.Unit.entries.map { it.label() },
            selectedIdx = WindSpeed.Unit.entries.indexOf(units.windSpeed),
            onSelect = {
                onWindUnitClick(WindSpeed.Unit.entries[it])
                showWindDialog = false
            },
            onDismissRequest = {
                showWindDialog = false
            }
        )
    }

    if (showPrecipDialog) {
        MultipleChoiceDialog(
            title = stringResource(id = R.string.settings_screen_title_precip_unit),
            choices = Precipitation.Unit.entries.map { it.label() },
            selectedIdx = Precipitation.Unit.entries.indexOf(units.precipitation),
            onSelect = {
                onPrecipitationUnitClick(Precipitation.Unit.entries[it])
                showPrecipDialog = false
            },
            onDismissRequest = {
                showPrecipDialog = false
            }
        )
    }

    if (showRainDialog) {
        MultipleChoiceDialog(
            title = stringResource(id = R.string.settings_screen_title_rain_unit),
            choices = Precipitation.Unit.entries.map { it.label() },
            selectedIdx = Precipitation.Unit.entries.indexOf(units.rain),
            onSelect = {
                onRainUnitClick(Precipitation.Unit.entries[it])
                showRainDialog = false
            },
            onDismissRequest = {
                showRainDialog = false
            }
        )
    }

    if (showShowersDialog) {
        MultipleChoiceDialog(
            title = stringResource(id = R.string.settings_screen_title_showers_unit),
            choices = Precipitation.Unit.entries.map { it.label() },
            selectedIdx = Precipitation.Unit.entries.indexOf(units.showers),
            onSelect = {
                onShowersUnitClick(Precipitation.Unit.entries[it])
                showShowersDialog = false
            },
            onDismissRequest = {
                showShowersDialog = false
            }
        )
    }

    if (showSnowDialog) {
        MultipleChoiceDialog(
            title = stringResource(id = R.string.settings_screen_title_snow_unit),
            choices = Precipitation.Unit.entries.map { it.label() },
            selectedIdx = Precipitation.Unit.entries.indexOf(units.snow),
            onSelect = {
                onSnowUnitClick(Precipitation.Unit.entries[it])
                showSnowDialog = false
            },
            onDismissRequest = {
                showSnowDialog = false
            }
        )
    }

    if (showPressureDialog) {
        MultipleChoiceDialog(
            title = stringResource(id = R.string.settings_screen_title_pressure_unit),
            choices = Pressure.Unit.entries.map { it.label() },
            selectedIdx = Pressure.Unit.entries.indexOf(units.pressure),
            onSelect = {
                onPressureUnitClick(Pressure.Unit.entries[it])
                showPressureDialog = false
            },
            onDismissRequest = {
                showPressureDialog = false
            }
        )
    }

    if (showVisDialog) {
        MultipleChoiceDialog(
            title = stringResource(id = R.string.settings_screen_title_vis_unit),
            choices = Visibility.Unit.entries.map { it.label() },
            selectedIdx = Visibility.Unit.entries.indexOf(units.visibility),
            onSelect = {
                onVisibilityUnitClick(Visibility.Unit.entries[it])
                showVisDialog = false
            },
            onDismissRequest = {
                showVisDialog = false
            }
        )
    }

    if (showThemeDialog) {
        MultipleChoiceDialog(
            title = stringResource(id = R.string.settings_screen_title_theme),
            choices = Theme.entries.map { it.label() },
            selectedIdx = Theme.entries.indexOf(theme),
            onSelect = {
                onThemeClick(Theme.entries[it])
                showThemeDialog = false
            },
            onDismissRequest = {
                showThemeDialog = false
            }
        )
    }

    LazyColumn(modifier = modifier) {
        item {
            SectionLabel(label = stringResource(id = R.string.settings_screen_title_units))
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_temp_unit),
                value = units.temperature.label(),
                onClick = { showTempDialog = true }
            )
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_wind_unit),
                value = units.windSpeed.label(),
                onClick = { showWindDialog = true }
            )
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_precip_unit),
                value = units.precipitation.label(),
                onClick = { showPrecipDialog = true }
            )
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_rain_unit),
                value = units.rain.label(),
                onClick = { showRainDialog = true }
            )
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_showers_unit),
                value = units.showers.label(),
                onClick = { showShowersDialog = true }
            )
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_snow_unit),
                value = units.snow.label(),
                onClick = { showSnowDialog = true }
            )
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_pressure_unit),
                value = units.pressure.label(),
                onClick = { showPressureDialog = true }
            )
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_vis_unit),
                value = units.visibility.label(),
                onClick = { showVisDialog = true }
            )
        }
        item {
            SectionLabel(label = stringResource(id = R.string.settings_screen_title_appearance))
        }
        item {
            PreferenceButton(
                title = stringResource(id = R.string.settings_screen_title_theme),
                value = theme.label(),
                onClick = { showThemeDialog = true }
            )
        }
    }
}

@Composable
private fun Temperature.Unit.label() = stringResource(
    when (this) {
        Temperature.Unit.DegreesCelsius -> R.string.settings_screen_label_temp_unit_celsius
        Temperature.Unit.DegreesFahrenheit -> R.string.settings_screen_label_temp_unit_fahrenheit
    }
)

@Composable
private fun WindSpeed.Unit.label() = stringResource(
    when (this) {
        WindSpeed.Unit.MetersPerSecond -> R.string.settings_screen_label_wind_unit_mps
        WindSpeed.Unit.KilometersPerHour -> R.string.settings_screen_label_wind_unit_kph
        WindSpeed.Unit.MilesPerHour -> R.string.settings_screen_label_wind_unit_mph
        WindSpeed.Unit.Knots -> R.string.settings_screen_label_wind_unit_kn
    }
)

@Composable
private fun Precipitation.Unit.label() = stringResource(
    when (this) {
        Precipitation.Unit.Millimeters -> R.string.settings_screen_label_precip_unit_mm
        Precipitation.Unit.Centimeters -> R.string.settings_screen_label_precip_unit_cm
        Precipitation.Unit.Inches -> R.string.settings_screen_label_precip_unit_in
    }
)

@Composable
private fun Pressure.Unit.label() = stringResource(
    when (this) {
        Pressure.Unit.Hectopascal -> R.string.settings_screen_label_pressure_unit_hpa
        Pressure.Unit.InchesOfMercury -> R.string.settings_screen_label_pressure_unit_inhg
        Pressure.Unit.MillimetersOfMercury -> R.string.settings_screen_label_pressure_unit_mmhg
    }
)

@Composable
private fun Visibility.Unit.label() = stringResource(
    when (this) {
        Visibility.Unit.Kilometers -> R.string.settings_screen_label_vis_unit_km
        Visibility.Unit.Miles -> R.string.settings_screen_label_vis_unit_mi
        Visibility.Unit.Meters -> R.string.settings_screen_label_vis_unit_m
        Visibility.Unit.Feet -> R.string.settings_screen_label_vis_unit_ft
    }
)

@Composable
private fun Theme.label() = stringResource(
    when (this) {
        Theme.Dark -> R.string.settings_screen_label_dark
        Theme.Light -> R.string.settings_screen_label_light
        Theme.FollowSystem -> R.string.settings_screen_label_follow_system
    }
)