/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.uvindex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.summary.SummaryTile
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.common.rememberDateTimeFormatter
import com.davidtakac.bura.uvindex.UvIndex
import com.davidtakac.bura.uvindex.riskString
import com.davidtakac.bura.uvindex.valueString
import java.time.LocalTime

@Composable
fun UvIndexSummary(state: UvIndexSummary, modifier: Modifier = Modifier) {
    val formatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_hour_minute)
    SummaryTile(
        label = { Text(text = stringResource(R.string.uv_index)) },
        value = { Text(text = state.now.valueString()) },
        supportingValue = { Text(text = state.now.riskString()) },
        bottom = {
            Column {
                AppleUvIndexScale(uvIndexNow = state.now, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (state.useProtection) {
                        is UseProtection.FromUntil -> {
                            val from = formatter.format(state.useProtection.startInclusive)
                            val until = formatter.format(state.useProtection.endExclusive)
                            stringResource(R.string.uv_index_protection_value_from_until, from, until)
                        }

                        is UseProtection.FromUntilEndOfDay -> {
                            val from = formatter.format(state.useProtection.startInclusive)
                            stringResource(R.string.uv_index_protection_value_from, from)
                        }

                        is UseProtection.Until -> {
                            val until = formatter.format(state.useProtection.endExclusive)
                            stringResource(R.string.uv_index_protection_value_until, until)
                        }

                        UseProtection.UntilEndOfDay ->
                            stringResource(R.string.uv_index_protection_value_until_end_of_day)

                        UseProtection.None ->
                            stringResource(R.string.uv_index_protection_value_none)
                    }
                )
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun UvIndexSummaryPreview() {
    AppTheme {
        Surface {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .size(200.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UvIndexSummary(
                    state = UvIndexSummary(
                        now = UvIndex(0),
                        useProtection = UseProtection.FromUntil(
                            startInclusive = LocalTime.parse("08:00"),
                            endExclusive = LocalTime.parse("20:00")
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                UvIndexSummary(
                    state = UvIndexSummary(
                        now = UvIndex(12),
                        useProtection = UseProtection.Until(
                            endExclusive = LocalTime.parse("20:00")
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                UvIndexSummary(
                    state = UvIndexSummary(
                        now = UvIndex(9),
                        useProtection = UseProtection.UntilEndOfDay
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}