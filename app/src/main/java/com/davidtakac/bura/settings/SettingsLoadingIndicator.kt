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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.common.animateShimmerColorAsState

@Composable
fun SettingsLoadingIndicator(modifier: Modifier = Modifier) {
    val shimmerColor = animateShimmerColorAsState()
    Column(modifier = modifier.padding(vertical = 24.dp, horizontal = 16.dp)) {
        Surface(
            modifier = Modifier
                .width(120.dp)
                .height(12.dp),
            shape = MaterialTheme.shapes.small,
            color = shimmerColor.value
        ) {}
        for (i in 0..7) {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .height(56.dp)
                    .fillMaxWidth(0.45f),
                color = shimmerColor.value,
                shape = MaterialTheme.shapes.medium
            ) {}
        }
    }
}