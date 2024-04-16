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

package com.davidtakac.bura.common

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

data class AppColors(
    private val temperatureColors: List<Color>,
    val popColor: Color,
    val rainColor: Color,
    val showersColor: Color,
    val snowColor: Color,
    val precipitationColor: Color
) {
    fun temperatureColors(fromCelsius: Double, toCelsius: Double): List<Color> =
        temperatureColors.slice(getIndexOfNearestColor(fromCelsius)..getIndexOfNearestColor(toCelsius))

    private fun getIndexOfNearestColor(celsius: Double): Int =
        40 + celsius.roundToInt().coerceIn(-40, 55)

    companion object {
        val ForDarkTheme get() = AppColors(
            temperatureColors = darkTemperatureColors,
            popColor = Color(0xFF64B5F6),
            rainColor = Color(0xFF64B5F6),
            showersColor = Color(0xFF4DB6AC),
            snowColor = Color(0xFFE0E0E0),
            precipitationColor = Color(0xFF9575CD)
        )

        val ForLightTheme get() = AppColors(
            temperatureColors = darkTemperatureColors,
            popColor = Color(0xFF2196F3),
            rainColor = Color(0xFF2196F3),
            showersColor = Color(0xFF009688),
            snowColor = Color(0xFF9E9E9E),
            precipitationColor = Color(0xFF3F51B5)
        )
    }
}

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        temperatureColors = listOf(),
        popColor = Color.Unspecified,
        rainColor = Color.Unspecified,
        showersColor = Color.Unspecified,
        snowColor = Color.Unspecified,
        precipitationColor = Color.Unspecified
    )
}

private val darkTemperatureColors = listOf(
    Color(109, 22, 12),
    Color(108, 21, 11),
    Color(109, 23, 10),
    Color(109, 22, 13),
    Color(109, 22, 13),
    Color(112, 22, 11),
    Color(142, 34, 22),
    Color(157, 44, 30),
    Color(181, 58, 43),
    Color(202, 65, 49),
    Color(230, 77, 59),
    Color(233, 81, 58),
    Color(236, 86, 59),
    Color(236, 94, 56),
    Color(236, 99, 55),
    Color(237, 102, 54),
    Color(238, 108, 56),
    Color(235, 121, 51),
    Color(240, 130, 55),
    Color(240, 138, 56),
    Color(237, 147, 51),
    Color(239, 156, 54),
    Color(241, 164, 58),
    Color(242, 179, 63),
    Color(245, 187, 64),
    Color(245, 194, 67),
    Color(248, 201, 69),
    Color(244, 208, 70),
    Color(243, 207, 71),
    Color(240, 206, 73),
    Color(236, 206, 72),
    Color(235, 207, 72),
    Color(230, 206, 74),
    Color(225, 206, 78),
    Color(215, 206, 85),
    Color(208, 205, 90),
    Color(202, 206, 96),
    Color(192, 206, 108),
    Color(187, 206, 114),
    Color(182, 207, 123),
    Color(172, 208, 136),
    Color(167, 208, 142),
    Color(160, 207, 152),
    Color(156, 208, 159),
    Color(148, 208, 174),
    Color(143, 209, 182),
    Color(138, 209, 193),
    Color(133, 210, 204),
    Color(131, 209, 209),
    Color(130, 208, 210),
    Color(128, 207, 212),
    Color(128, 206, 216),
    Color(128, 206, 218),
    Color(129, 207, 219),
    Color(126, 203, 221),
    Color(126, 203, 223),
    Color(127, 203, 227),
    Color(125, 202, 228),
    Color(123, 203, 230),
    Color(122, 201, 232),
    Color(121, 200, 233),
    Color(123, 201, 237),
    Color(121, 199, 238),
    Color(121, 198, 240),
    Color(121, 198, 242),
    Color(121, 198, 244),
    Color(120, 197, 243),
    Color(121, 198, 244),
    Color(120, 198, 247),
    Color(109, 186, 242),
    Color(98, 175, 243),
    Color(82, 157, 248),
    Color(72, 145, 248),
    Color(59, 129, 243),
    Color(53, 117, 243),
    Color(53, 114, 239),
    Color(52, 110, 235),
    Color(56, 106, 229),
    Color(66, 100, 223),
    Color(69, 95, 218),
    Color(73, 95, 217),
    Color(74, 93, 214),
    Color(77, 93, 214),
    Color(79, 91, 213),
    Color(83, 89, 211),
    Color(84, 89, 209),
    Color(85, 87, 208),
    Color(86, 84, 207),
    Color(81, 73, 182),
    Color(73, 62, 167),
    Color(68, 53, 156),
    Color(63, 40, 131),
    Color(61, 33, 120),
    Color(51, 22, 104),
    Color(46, 14, 90),
).reversed()