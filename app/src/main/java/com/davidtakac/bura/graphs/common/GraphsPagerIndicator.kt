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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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