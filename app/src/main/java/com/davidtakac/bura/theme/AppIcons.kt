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

package com.davidtakac.bura.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.staticCompositionLocalOf
import com.davidtakac.bura.R

data class AppIcons(
    @param:DrawableRes val clearDay: Int,
    @param:DrawableRes val clearNight: Int,
    @param:DrawableRes val partlyCloudyDay: Int,
    @param:DrawableRes val partlyCloudyNight: Int,
    @param:DrawableRes val overcast: Int,
    @param:DrawableRes val fog: Int,
    @param:DrawableRes val drizzle: Int,
    @param:DrawableRes val rain: Int,
    @param:DrawableRes val heavyRain: Int,
    @param:DrawableRes val rainShowersDay: Int,
    @param:DrawableRes val rainShowersNight: Int,
    @param:DrawableRes val heavyRainShowersDay: Int,
    @param:DrawableRes val heavyRainShowersNight: Int,
    @param:DrawableRes val snow: Int,
    @param:DrawableRes val thunderstormWithRain: Int,
    @param:DrawableRes val thunderstormWithHail: Int,
    @param:DrawableRes val sunrise: Int,
    @param:DrawableRes val sunset: Int,
) {
    companion object {
        val ForLightTheme get() = AppIcons(
            clearDay = R.drawable.sun_for_light,
            clearNight = R.drawable.moon_for_light,
            partlyCloudyDay = R.drawable.partly_cloudy_day_for_light,
            partlyCloudyNight = R.drawable.partly_cloudy_night_for_light,
            overcast = R.drawable.overcast_for_light,
            fog = R.drawable.fog_for_light,
            drizzle = R.drawable.drizzle_for_light,
            rain = R.drawable.rain_for_light,
            heavyRain = R.drawable.heavy_rain_for_light,
            rainShowersDay = R.drawable.rain_showers_day_for_light,
            rainShowersNight = R.drawable.rain_showers_night_for_light,
            heavyRainShowersDay = R.drawable.heavy_rain_showers_day_for_light,
            heavyRainShowersNight = R.drawable.heavy_rain_showers_night_for_light,
            snow = R.drawable.snowflake_for_light,
            thunderstormWithRain = R.drawable.thunderstorm_with_rain_for_light,
            thunderstormWithHail = R.drawable.thunderstorm_with_hail_for_light,
            sunrise = R.drawable.sunrise_for_light,
            sunset = R.drawable.sunset_for_light
        )

        val ForDarkTheme get() = AppIcons(
            clearDay = R.drawable.sun_for_dark,
            clearNight = R.drawable.moon_for_dark,
            partlyCloudyDay = R.drawable.partly_cloudy_day_for_dark,
            partlyCloudyNight = R.drawable.partly_cloudy_night_for_dark,
            overcast = R.drawable.overcast_for_dark,
            fog = R.drawable.fog_for_dark,
            drizzle = R.drawable.drizzle_for_dark,
            rain = R.drawable.rain_for_dark,
            heavyRain = R.drawable.heavy_rain_for_dark,
            rainShowersDay = R.drawable.rain_showers_day_for_dark,
            rainShowersNight = R.drawable.rain_showers_night_for_dark,
            heavyRainShowersDay = R.drawable.heavy_rain_showers_day_for_dark,
            heavyRainShowersNight = R.drawable.heavy_rain_showers_night_for_dark,
            snow = R.drawable.snowflake_for_dark,
            thunderstormWithRain = R.drawable.thunderstorm_with_rain_for_dark,
            thunderstormWithHail = R.drawable.thunderstorm_with_hail_for_dark,
            sunrise = R.drawable.sunrise_for_dark,
            sunset = R.drawable.sunset_for_dark
        )
    }
}

val LocalAppIcons = staticCompositionLocalOf {
    AppIcons(
        clearDay = 0,
        clearNight = 0,
        partlyCloudyDay = 0,
        partlyCloudyNight = 0,
        overcast = 0,
        fog = 0,
        drizzle = 0,
        rain = 0,
        heavyRain = 0,
        rainShowersDay = 0,
        rainShowersNight = 0,
        heavyRainShowersDay = 0,
        heavyRainShowersNight = 0,
        snow = 0,
        thunderstormWithHail = 0,
        thunderstormWithRain = 0,
        sunrise = 0,
        sunset = 0
    )
}