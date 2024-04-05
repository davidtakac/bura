/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.hourly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.summary.PopAndDrop

@Composable
fun HourSummary(
    time: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    pop: (@Composable () -> Unit)?,
    temperature: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleSmall,
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
            content = time
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(32.dp)) { icon() }
            pop?.let { it() }
        }
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
            content = temperature
        )
    }
}

@Composable
fun HourSummaryMaxHeightDummy(modifier: Modifier = Modifier) {
    HourSummary(
        time = { Text("") },
        icon = {},
        pop = { PopAndDrop("") },
        temperature = { Text("") },
        modifier = modifier.alpha(0f).width(0.dp)
    )
}