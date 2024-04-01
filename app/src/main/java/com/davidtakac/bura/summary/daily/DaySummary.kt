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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.image
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.summary.PopAndDrop
import com.davidtakac.bura.temperature.Temperature
import com.davidtakac.bura.temperature.string
import com.davidtakac.bura.common.rememberDateTimeFormatter
import java.time.LocalDate

private val roundedRadius = 12.dp
private val squareRadius = 4.dp

@Composable
fun DaySummary(
    state: DaySummary,
    absMin: Temperature,
    absMax: Temperature,
    modifier: Modifier = Modifier,
    roundedTop: Boolean = false,
    roundedBottom: Boolean = false,
    onClick: () -> Unit
) {
    val formatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow)
    val shape = remember(roundedTop, roundedBottom) {
        val topRadius = if (roundedTop) roundedRadius else squareRadius
        val bottomRadius = if (roundedBottom) roundedRadius else squareRadius
        RoundedCornerShape(
            topStart = topRadius,
            topEnd = topRadius,
            bottomStart = bottomRadius,
            bottomEnd = bottomRadius
        )
    }

    Surface(
        shape = shape,
        tonalElevation = 1.dp,
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    Text(
                        text = if (state.isToday) stringResource(R.string.date_time_today) else state.time.format(formatter),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    state.pop?.let {
                        PopAndDrop(it)
                    }
                }
                Image(
                    painter = state.desc.image(),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(2f)
            ) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
                    val maxTempWidth = rememberMaxTempWidth()
                    Text(
                        text = state.min.string(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.width(maxTempWidth)
                    )
                    AppleTemperatureScale(
                        absMinCelsius = absMin.convertTo(Temperature.Unit.DegreesCelsius).value,
                        absMaxCelsius = absMax.convertTo(Temperature.Unit.DegreesCelsius).value,
                        minCelsius = state.min.convertTo(Temperature.Unit.DegreesCelsius).value,
                        nowCelsius = state.tempNow?.convertTo(Temperature.Unit.DegreesCelsius)?.value,
                        maxCelsius = state.max.convertTo(Temperature.Unit.DegreesCelsius).value,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = state.max.string(),
                        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.width(maxTempWidth)
                    )
                }
            }
        }
    }
}

private val maxTemp = Temperature.fromDegreesCelsius(999.0)

@Composable
private fun rememberMaxTempWidth(): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxTempString = maxTemp.string()
    val textStyle = LocalTextStyle.current
    return remember(measurer, density, maxTempString, textStyle) {
        with(density) {
            measurer.measure(maxTempString, textStyle).size.width.toDp()
        }
    }
}

@Preview
@Composable
private fun DaySummaryPreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            val absoluteMin = remember { Temperature.fromDegreesCelsius(0.0) }
            val absoluteMax = remember { Temperature.fromDegreesCelsius(20.0) }
            DaySummary(
                absMin = absoluteMin,
                absMax = absoluteMax,
                state = DaySummary(
                    isToday = true,
                    time = LocalDate.parse("2023-01-01"),
                    tempNow = Temperature.fromDegreesCelsius(2.0),
                    min = Temperature.fromDegreesCelsius(2.0),
                    max = Temperature.fromDegreesCelsius(19.0),
                    pop = null,
                    desc = Condition(wmoCode = 1, isDay = true)
                ),
                roundedTop = true,
                roundedBottom = false,
                onClick = {}
            )
            DaySummary(
                absMin = absoluteMin,
                absMax = absoluteMax,
                state = DaySummary(
                    isToday = false,
                    time = LocalDate.parse("2023-01-02"),
                    tempNow = Temperature.fromDegreesCelsius(5.0),
                    min = Temperature.fromDegreesCelsius(0.0),
                    max = Temperature.fromDegreesCelsius(5.0),
                    pop = Pop(15.0),
                    desc = Condition(wmoCode = 51, isDay = true)
                ),
                roundedTop = false,
                roundedBottom = false,
                onClick = {}
            )
            DaySummary(
                absMin = absoluteMin,
                absMax = absoluteMax,
                state = DaySummary(
                    isToday = false,
                    time = LocalDate.parse("2023-01-03"),
                    tempNow = Temperature.fromDegreesCelsius(9.0),
                    min = Temperature.fromDegreesCelsius(7.0),
                    max = Temperature.fromDegreesCelsius(15.0),
                    pop = Pop(0.0),
                    desc = Condition(wmoCode = 2, isDay = true)
                ),
                roundedTop = false,
                roundedBottom = true,
                onClick = {}
            )
        }
    }
}