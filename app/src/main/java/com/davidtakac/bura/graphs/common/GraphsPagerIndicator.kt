/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.graphs.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.rememberDateTimeFormatter
import java.time.LocalDate

@Composable
fun GraphsPagerIndicator(
    state: List<LocalDate>,
    selected: Int,
    onClick: (date: LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow)
    ScrollableTabRow(selectedTabIndex = selected, modifier = modifier) {
        state.forEachIndexed { idx, date ->
            Tab(
                selected = idx == selected,
                onClick = { onClick(date) },
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = { Text(formatter.format(date)) },
                icon = { Text(text = "${date.dayOfMonth}") }
            )
        }
    }
}

@Composable
fun GraphsPagerIndicatorSkeleton(
    color: State<Color>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(start = 52.dp)
            .wrapContentWidth(unbounded = true, align = Alignment.Start)
    ) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .width(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Tab(
                    selected = false,
                    onClick = {},
                    enabled = false,
                    text = { Text("") },
                    icon = { Text("") },
                    modifier = Modifier.alpha(0f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(shape = MaterialTheme.shapes.small, color = color.value)
                )
            }
        }
    }
}

@Preview
@Composable
private fun GraphsPagerIndicatorPreview() {
    AppTheme {
        GraphsPagerIndicator(
            state = listOf(
                LocalDate.parse("1970-01-01"),
                LocalDate.parse("1970-01-02"),
                LocalDate.parse("1970-01-03"),
                LocalDate.parse("1970-01-04"),
                LocalDate.parse("1970-01-05"),
                LocalDate.parse("1970-01-06")
            ),
            selected = 2,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}