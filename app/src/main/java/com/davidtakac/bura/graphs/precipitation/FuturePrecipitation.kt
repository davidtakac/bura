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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.precipitation.MixedPrecipitation
import com.davidtakac.bura.precipitation.Precipitation
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import com.davidtakac.bura.common.AppTheme
import java.time.LocalDate

@Composable
fun FuturePrecipitation(state: PrecipitationTotal.OtherDay, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColoredPrecipitation(
            state = state.total,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun FuturePrecipitationPreview() {
    AppTheme {
        Surface {
            FuturePrecipitation(
                state = PrecipitationTotal.OtherDay(
                    day = LocalDate.parse("1970-01-01"),
                    total = MixedPrecipitation.fromMillimeters(
                        Rain.fromMillimeters(10.0),
                        Showers.Zero,
                        Snow.fromMillimeters(70.0).convertTo(Precipitation.Unit.Centimeters)
                    )
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}