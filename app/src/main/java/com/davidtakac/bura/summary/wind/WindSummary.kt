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

package com.davidtakac.bura.summary.wind

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.summary.SummaryTile
import com.davidtakac.bura.summary.ValueAndUnit
import com.davidtakac.bura.wind.Wind
import com.davidtakac.bura.wind.WindDirection
import com.davidtakac.bura.wind.WindSpeed
import com.davidtakac.bura.wind.bftString
import com.davidtakac.bura.wind.string
import com.davidtakac.bura.wind.unitString
import com.davidtakac.bura.wind.valueString

@Composable
fun WindSummary(state: WindSummary, modifier: Modifier = Modifier) {
    SummaryTile(
        label = { Text(text = stringResource(R.string.wind_label)) },
        value = {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ValueAndUnit(
                    value = state.windNow.speed.valueString(),
                    unit = state.windNow.speed.unitString(),
                )
            }
        },
        bottom = { Text(stringResource(R.string.wind_value_gusts_at, state.gustNow.string())) },
        supportingValue = {
            val style = LocalTextStyle.current
            val inlineContentMap = mapOf(
                "direction" to InlineTextContent(
                    placeholder = Placeholder(
                        width = style.fontSize,
                        height = style.fontSize,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.navigation),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(state.windNow.direction.degrees.toFloat() + 180f)
                    )
                }
            )
            val annotatedString = buildAnnotatedString {
                append(state.windNow.speed.bftString())
                append(" ")
                appendInlineContent(id = "direction")
            }
            Text(text = annotatedString, inlineContent = inlineContentMap)
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun WindSummaryPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .size(200.dp)
        ) {
            WindSummary(
                state = WindSummary(
                    windNow = Wind(
                        speed = WindSpeed.fromMetersPerSecond(9.0),
                        direction = WindDirection(76.0)
                    ),
                    gustNow = WindSpeed.fromMetersPerSecond(20.0)
                ),
            )
        }
    }
}