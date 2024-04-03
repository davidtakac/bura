/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun DailySummaryColumn(
    state: DailySummary,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        state.days.forEachIndexed { index, day ->
            DaySummaryRow(
                state = day,
                absMin = state.minTemp,
                absMax = state.maxTemp,
                modifier = Modifier.fillMaxWidth(),
                position = when (index) {
                    0 -> DaySummaryPosition.First
                    state.days.lastIndex -> DaySummaryPosition.Last
                    else -> DaySummaryPosition.Middle
                },
                onClick = { onDayClick(day.time) }
            )
        }
    }
}

@Composable
fun DailySummaryColumnSkeleton(
    color: State<Color>,
    modifier: Modifier = Modifier,
    rows: Int = 7,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        repeat(rows) {
            DaySummaryRowSkeleton(
                color = color,
                position = when (it) {
                    0 -> DaySummaryPosition.First
                    rows - 1 -> DaySummaryPosition.Last
                    else -> DaySummaryPosition.Middle
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}