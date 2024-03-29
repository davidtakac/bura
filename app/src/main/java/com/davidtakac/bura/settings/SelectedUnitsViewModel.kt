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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.davidtakac.bura.App
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.units.SelectedUnitsRepository
import com.davidtakac.bura.units.Units
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.wind.WindSpeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelectedUnitsViewModel(private val repo: SelectedUnitsRepository) : ViewModel() {
    private val _state = MutableStateFlow<Units?>(null)
    val state: StateFlow<Units?> = _state.asStateFlow()

    fun getSettings() {
        viewModelScope.launch {
            _state.value = repo.getSelectedUnits()
        }
    }

    fun selectTemperatureUnit(value: Temperature.Unit) {
        viewModelScope.launch {
            repo.selectTemperatureUnit(value)
            _state.value = _state.value?.copy(temperature = value)
        }
    }

    fun selectWindUnit(value: WindSpeed.Unit) {
        viewModelScope.launch {
            repo.selectWindSpeedUnit(value)
            _state.value = _state.value?.copy(windSpeed = value)
        }
    }

    fun selectPrecipitationUnit(value: Precipitation.Unit) {
        viewModelScope.launch {
            repo.selectMixedPrecipitationUnit(value)
            _state.value = _state.value?.copy(precipitation = value)
        }
    }

    fun selectRainUnit(value: Precipitation.Unit) {
        viewModelScope.launch {
            repo.selectRainUnit(value)
            _state.value = _state.value?.copy(rain = value)
        }
    }

    fun selectShowersUnit(value: Precipitation.Unit) {
        viewModelScope.launch {
            repo.selectShowersUnit(value)
            _state.value = _state.value?.copy(showers = value)
        }
    }

    fun selectSnowUnit(value: Precipitation.Unit) {
        viewModelScope.launch {
            repo.selectSnowUnit(value)
            _state.value = _state.value?.copy(snow = value)
        }
    }

    fun selectPressureUnit(value: Pressure.Unit) {
        viewModelScope.launch {
            repo.selectPressureUnit(value)
            _state.value = _state.value?.copy(pressure = value)
        }
    }

    fun selectVisibilityUnit(value: Visibility.Unit) {
        viewModelScope.launch {
            repo.selectVisibilityUnit(value)
            _state.value = _state.value?.copy(visibility = value)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return SelectedUnitsViewModel(container.selectedUnitsRepo) as T
            }
        }
    }
}