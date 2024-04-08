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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.common.TextSkeleton
import com.davidtakac.bura.common.animateShimmerColorAsState

@Composable
fun SettingsLoadingIndicator(modifier: Modifier = Modifier) {
    val shimmerColor = animateShimmerColorAsState()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 24.dp),
        userScrollEnabled = false
    ) {
        item {
            TextSkeleton(
                color = shimmerColor,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .width(100.dp)
                    .padding(horizontal = 16.dp)
            )
        }
        items(7) {
            PreferenceButtonSkeleton(color = shimmerColor)
        }
    }
}