/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.graphs.precipitation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.davidtakac.bura.precipitation.unitString
import com.davidtakac.bura.precipitation.valueString
import com.davidtakac.bura.summary.ValueAndUnit

@Composable
fun PrecipitationGraphSummary(
    value: @Composable () -> Unit,
    inHours: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineMedium,
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            content = value
        )
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyLarge,
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
            content = inHours
        )
    }
}

@Composable
fun PrecipitationGraphTodaySummary(
    state: PrecipitationTotal.Today,
    modifier: Modifier = Modifier
) {
    PrecipitationGraphSummary(
        value = {
            ValueAndUnit(
                value = state.past.total.valueString(),
                unit = state.past.total.unitString(),
                modifier = modifier
            )
        },
        inHours = {
            Text(text = "Total in last ${state.past.hours} hours")
        }
    )
}

@Composable
fun PrecipitationGraphOtherDaySummary(
    state: PrecipitationTotal.OtherDay,
    modifier: Modifier = Modifier
) {
    PrecipitationGraphSummary(
        value = {
            ValueAndUnit(
                value = state.total.valueString(),
                unit = state.total.unitString(),
                modifier = modifier
            )
        },
        inHours = {
            Text(text = "Total of day")
        }
    )
}