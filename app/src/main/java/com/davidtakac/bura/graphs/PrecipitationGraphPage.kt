/*
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.precipitation.FuturePrecipitation
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraph
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphOtherDaySummary
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphTodaySummary
import com.davidtakac.bura.graphs.precipitation.PrecipitationToday
import com.davidtakac.bura.graphs.precipitation.PrecipitationTotal
import com.davidtakac.bura.precipitation.MixedPrecipitation

private val contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
private val verticalSpacing = 24.dp

@Composable
fun PrecipitationGraphPage(
    total: PrecipitationTotal,
    graph: PrecipitationGraph,
    max: MixedPrecipitation,
    args: GraphArgs
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            when (total) {
                is PrecipitationTotal.OtherDay -> PrecipitationGraphOtherDaySummary(state = total, modifier = Modifier.fillMaxWidth())
                is PrecipitationTotal.Today -> PrecipitationGraphTodaySummary(state = total, modifier = Modifier.fillMaxWidth())
            }
        }
        item {
            PrecipitationGraph(
                state = graph,
                args = args,
                max = max,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(
                        width = Dp.Hairline,
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    .clip(MaterialTheme.shapes.large)
            )
        }
        item {
            when (total) {
                is PrecipitationTotal.OtherDay -> FuturePrecipitation(
                    state = total,
                    modifier = Modifier.fillMaxWidth()
                )

                is PrecipitationTotal.Today -> PrecipitationToday(
                    state = total,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Text(
                text = stringResource(id = R.string.credit_weather),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}