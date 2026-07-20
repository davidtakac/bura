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

package com.davidtakac.bura.summary.visibility.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.summary.common.compose.SummaryTile
import com.davidtakac.bura.summary.common.compose.ValueAndUnit
import com.davidtakac.bura.forecast.parameters.visibility.Visibility
import com.davidtakac.bura.forecast.parameters.visibility.unitString
import com.davidtakac.bura.forecast.parameters.visibility.valueString
import com.davidtakac.bura.summary.visibility.VisibilitySummary

@Composable
fun VisibilitySummary(state: VisibilitySummary, modifier: Modifier = Modifier) {
    SummaryTile(
        label = { Text(stringResource(R.string.vis)) },
        value = {
            ValueAndUnit(
                value = state.now.valueString(),
                unit = state.now.unitString()
            )
        },
        bottom = {
            Text(
                stringResource(
                    when (state.now.description) {
                        Visibility.Description.VeryPoor -> R.string.vis_description_very_poor
                        Visibility.Description.Poor -> R.string.vis_description_poor
                        Visibility.Description.Moderate -> R.string.vis_description_moderate
                        Visibility.Description.Good -> R.string.vis_description_good
                        Visibility.Description.VeryGood -> R.string.vis_description_very_good
                        Visibility.Description.Excellent -> R.string.vis_description_excellent
                    }
                )
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun VisibilitySummaryPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .size(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VisibilitySummary(
                state = VisibilitySummary(
                    Visibility(1020.0, Visibility.Unit.Meters).convertTo(Visibility.Unit.Kilometers)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            VisibilitySummary(
                state = VisibilitySummary(
                    Visibility(90.0, Visibility.Unit.Meters).convertTo(Visibility.Unit.Kilometers)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}