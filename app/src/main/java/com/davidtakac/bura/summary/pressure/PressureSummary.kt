/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary.pressure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.pressure.Pressure
import com.davidtakac.bura.pressure.image
import com.davidtakac.bura.pressure.string
import com.davidtakac.bura.pressure.unitString
import com.davidtakac.bura.pressure.valueString
import com.davidtakac.bura.summary.SummaryTile
import com.davidtakac.bura.summary.ValueAndUnit

@Composable
fun PressureSummary(state: PressureSummary, modifier: Modifier = Modifier) {
    SummaryTile(
        label = { Text(stringResource(R.string.pressure)) },
        value = {
            ValueAndUnit(
                value = state.now.valueString(),
                unit = state.now.unitString()
            )
        },
        supportingValue = {
            val style = LocalTextStyle.current
            val inlineContentMap = mapOf(
                "trend" to InlineTextContent(
                    placeholder = Placeholder(
                        width = style.fontSize,
                        height = style.fontSize,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    )
                ) {
                    Icon(
                        painter = state.trend.image(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
            val annotatedString = buildAnnotatedString {
                append(state.trend.string())
                append(" ")
                appendInlineContent(id = "trend")
            }
            Text(text = annotatedString, inlineContent = inlineContentMap)
        },
        bottom = { Text(stringResource(R.string.pressure_value_average, state.average.string())) },
        modifier = modifier
    )
}

@Preview
@Composable
private fun PressureSummaryPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .size(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PressureSummary(
                state = PressureSummary(
                    now = Pressure.fromHectopascal(1013.25),
                    average = Pressure.fromHectopascal(1010.0),
                    trend = PressureTrend.Rising
                )
            )
        }
    }
}