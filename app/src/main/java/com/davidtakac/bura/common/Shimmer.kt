/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.common

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun animateShimmerColorAsState(): State<Color> {
    val transition = rememberInfiniteTransition(label = "Shimmer loop")
    return transition.animateColor(
        initialValue = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        targetValue = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1000),
            RepeatMode.Reverse
        ),
        label = "Shimmer color"
    )
}