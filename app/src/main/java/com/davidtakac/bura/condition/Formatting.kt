/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.condition

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.AppIcons
import java.lang.IllegalStateException

@DrawableRes
fun Condition.image(context: Context, appIcons: AppIcons): Int =
    when (wmoCode) {
        0, 1, // Clear and mostly clear
        -> if (isDay) appIcons.clearDay else appIcons.clearNight

        2, // Partly cloudy
        -> if (isDay) appIcons.partlyCloudyDay else appIcons.partlyCloudyNight

        3, // Overcast
        -> appIcons.overcast

        45, 48, // Fog and depositing rime fog
        -> appIcons.fog

        51, 53, 55, // Light, moderate and heavy drizzle
        56, 57, // Light and heavy freezing drizzle
        -> appIcons.drizzle

        61, 63, // Light and moderate rain
        66, // Light freezing rain
        -> appIcons.rain

        65, // Heavy rain
        67, // Heavy freezing rain
        -> appIcons.heavyRain

        80, 81, // Light and moderate rain showers
        -> if (isDay) appIcons.rainShowersDay else appIcons.rainShowersNight

        82, // Heavy rain showers
        -> if (isDay) appIcons.heavyRainShowersDay else appIcons.heavyRainShowersNight

        71, 73, 75, // Light, moderate and heavy snow
        77, // Snow grains
        85, 86 // Light and heavy snow showers
        -> appIcons.snow

        95, // Light or moderate thunderstorm
        -> appIcons.thunderstormWithRain

        96, 99 // Thunderstorm with light and heavy hail
        -> appIcons.thunderstormWithHail

        else -> throw IllegalStateException("Unknown WMO Code: $wmoCode.")
    }

private fun Condition.string(context: Context): String = context.getString(
    when (wmoCode) {
        0 -> R.string.wmo_0
        1 -> R.string.wmo_1
        2 -> R.string.wmo_2
        3 -> R.string.wmo_3
        45 -> R.string.wmo_45
        48 -> R.string.wmo_48
        51 -> R.string.wmo_51
        53 -> R.string.wmo_53
        55 -> R.string.wmo_55
        56 -> R.string.wmo_56
        57 -> R.string.wmo_57
        61 -> R.string.wmo_61
        63 -> R.string.wmo_63
        65 -> R.string.wmo_65
        66 -> R.string.wmo_66
        67 -> R.string.wmo_67
        71 -> R.string.wmo_71
        73 -> R.string.wmo_73
        75 -> R.string.wmo_75
        77 -> R.string.wmo_77
        80 -> R.string.wmo_80
        81 -> R.string.wmo_81
        82 -> R.string.wmo_82
        85 -> R.string.wmo_85
        86 -> R.string.wmo_86
        95 -> R.string.wmo_95
        96 -> R.string.wmo_96
        99 -> R.string.wmo_99
        else -> throw IllegalStateException("Unknown WMO Code: $wmoCode.")
    }
)

@Composable
fun Condition.image() = painterResource(id = image(LocalContext.current, AppTheme.icons))

@Composable
fun Condition.string() = string(LocalContext.current)