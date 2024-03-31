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

import com.davidtakac.bura.forecast.ForecastConverter
import com.davidtakac.bura.forecast.ForecastData
import com.davidtakac.bura.units.Units
import com.davidtakac.bura.humidity.Humidity
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.uvindex.UvIndex
import com.davidtakac.bura.visibility.Visibility
import com.davidtakac.bura.wind.WindDirection
import com.davidtakac.bura.wind.WindSpeed
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ForecastConverterTest {
    @Test
    fun `converts forecast data to imperial`() = runTest {
        val imperial = Units(
            temperature = Temperature.Unit.DegreesFahrenheit,
            rain = Precipitation.Unit.Inches,
            showers = Precipitation.Unit.Inches,
            snow = Precipitation.Unit.Inches,
            precipitation = Precipitation.Unit.Inches,
            windSpeed = WindSpeed.Unit.MilesPerHour,
            pressure = Pressure.Unit.InchesOfMercury,
            visibility = Visibility.Unit.Miles
        )
        val time = listOf(
            firstLocalDateTime,
            firstLocalDateTime.plus(1, ChronoUnit.HOURS)
        )
        val sunrises = listOf<LocalDateTime>()
        val sunsets = listOf<LocalDateTime>()
        val temperature = listOf(
            Temperature.fromDegreesCelsius(0.0),
            Temperature.fromDegreesCelsius(0.0)
        )
        val feelsLikeTemperature = listOf(
            Temperature.fromDegreesCelsius(0.0),
            Temperature.fromDegreesCelsius(0.0)
        )
        val dewPointTemperature = listOf(
            Temperature.fromDegreesCelsius(0.0),
            Temperature.fromDegreesCelsius(0.0)
        )
        val pop = listOf(
            Pop(value = 0.0),
            Pop(value = 0.0)
        )
        val isDay = listOf(true, false)
        val wmoCodes = listOf(1, 2)
        val rain = listOf(
            Rain.fromMillimeters(1.0),
            Rain.fromMillimeters(1.0)
        )
        val showers = listOf(
            Showers.Zero,
            Showers.Zero
        )
        val snowfall = listOf(
            Snow.Zero,
            Snow.Zero
        )
        val uvIndex = listOf(
            UvIndex(1),
            UvIndex(1)
        )
        val windSpeed = listOf(
            WindSpeed.fromMetersPerSecond(1.0),
            WindSpeed.fromMetersPerSecond(1.0)
        )
        val windDirection = listOf(
            WindDirection(10.0),
            WindDirection(10.0)
        )
        val gustSpeed = listOf(
            WindSpeed.fromMetersPerSecond(10.0),
            WindSpeed.fromMetersPerSecond(10.0)
        )
        val pressure = listOf(
            Pressure.fromHectopascal(1000.0),
            Pressure.fromHectopascal(1000.0)
        )
        val visibility = listOf(
            Visibility.fromMeters(1000.0),
            Visibility.fromMeters(1000.0)
        )
        val humidity = listOf(
            Humidity(80.0),
            Humidity(80.0)
        )
        val forecastData = ForecastData(
            timestamp = Instant.MIN,
            times = time,
            temperature = temperature,
            feelsLikeTemperature = feelsLikeTemperature,
            dewPointTemperature = dewPointTemperature,
            pop = pop,
            rain = rain,
            showers = showers,
            snow = snowfall,
            uvIndex = uvIndex,
            windSpeed = windSpeed,
            windDirection = windDirection,
            gustSpeed = gustSpeed,
            pressure = pressure,
            visibility = visibility,
            humidity = humidity,
            wmoCode = wmoCodes,
            isDay = isDay,
            sunrises = sunrises,
            sunsets = sunsets
        )
        val forecast = ForecastConverter().fromData(forecastData, toUnits = imperial)
        assertTrue(forecast.temperature.all { it.temperature.unit == imperial.temperature })
        assertTrue(forecast.feelsLike.all { it.temperature.unit == imperial.temperature })
        assertTrue(forecast.dewPoint.all { it.temperature.unit == imperial.temperature })
        assertTrue(forecast.precipitation.all { it.precipitation.unit == imperial.precipitation })
        assertTrue(forecast.wind.all { it.wind.speed.unit == imperial.windSpeed })
        assertTrue(forecast.gust.all { it.speed.unit == imperial.windSpeed })
        assertTrue(forecast.pressure.all { it.pressure.unit == imperial.pressure })
        assertTrue(forecast.visibility.all { it.visibility.unit == imperial.visibility })
    }

    @Test(expected = Exception::class)
    fun `throws when data does not match`() = runTest {
        val units = Units.Default
        val time = listOf(
            firstLocalDateTime,
            firstLocalDateTime.plus(1, ChronoUnit.HOURS)
        )
        val sunrises = listOf<LocalDateTime>()
        val sunsets = listOf<LocalDateTime>()
        val temperature = listOf(
            Temperature.fromDegreesCelsius(0.0),
            Temperature.fromDegreesCelsius(0.0)
        )
        val feelsLikeTemperature = listOf(
            Temperature.fromDegreesCelsius(0.0),
            Temperature.fromDegreesCelsius(0.0)
        )
        val dewPointTemperature = listOf(
            Temperature.fromDegreesCelsius(0.0),
            Temperature.fromDegreesCelsius(0.0)
        )
        val pop = listOf(
            Pop(value = 0.0),
            Pop(value = 0.0)
        )
        val isDay = listOf(true, false)
        val wmoCodes = listOf(1, 2)
        val rain = listOf(
            Rain.fromMillimeters(1.0),
            Rain.fromMillimeters(1.0)
        )
        val showers = listOf(
            Showers.Zero,
            Showers.Zero
        )
        val snowfall = listOf(
            Snow.Zero,
            Snow.Zero
        )
        val uvIndex = listOf(
            UvIndex(1),
            UvIndex(1)
        )
        val windSpeed = listOf(
            WindSpeed.fromMetersPerSecond(1.0),
            WindSpeed.fromMetersPerSecond(1.0)
        )
        val windDirection = listOf(
            WindDirection(10.0),
            WindDirection(10.0)
        )
        val gustSpeed = listOf(
            WindSpeed.fromMetersPerSecond(10.0),
            WindSpeed.fromMetersPerSecond(10.0)
        )
        val pressure = listOf(
            Pressure.fromHectopascal(1000.0),
            Pressure.fromHectopascal(1000.0)
        )
        val visibility = listOf(
            Visibility.fromMeters(1000.0)
            // Mismatch, should throw
        )
        val humidity = listOf(
            Humidity(80.0),
            Humidity(80.0)
        )
        val forecastData = ForecastData(
            timestamp = Instant.MIN,
            times = time,
            temperature = temperature,
            feelsLikeTemperature = feelsLikeTemperature,
            dewPointTemperature = dewPointTemperature,
            pop = pop,
            rain = rain,
            showers = showers,
            snow = snowfall,
            uvIndex = uvIndex,
            windSpeed = windSpeed,
            windDirection = windDirection,
            gustSpeed = gustSpeed,
            pressure = pressure,
            visibility = visibility,
            humidity = humidity,
            wmoCode = wmoCodes,
            isDay = isDay,
            sunrises = sunrises,
            sunsets = sunsets
        )
        ForecastConverter().fromData(forecastData, units)
    }
}