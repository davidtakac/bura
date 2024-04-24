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

package com.davidtakac.bura.graphs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.uvindex.UvIndexGraph
import com.davidtakac.bura.uvindex.UvIndex

private const val graphAspectRatio = 1f
private val contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
private val verticalSpacing = 24.dp

@Composable
fun UvIndexGraphPage(
    state: UvIndexGraph,
    max: UvIndex,
    args: GraphArgs,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            UvIndexGraph(
                state = state,
                max = max,
                args = args,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(graphAspectRatio)
                    .border(
                        width = Dp.Hairline,
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    .clip(MaterialTheme.shapes.large)
            )
        }
    }
}