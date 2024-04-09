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

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.davidtakac.bura.common.AppTheme
import com.davidtakac.bura.condition.Condition
import com.davidtakac.bura.condition.image
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.GraphTime
import com.davidtakac.bura.graphs.common.drawTimeAxis
import com.davidtakac.bura.precipitation.MixedPrecipitation
import com.davidtakac.bura.precipitation.Rain
import com.davidtakac.bura.precipitation.Showers
import com.davidtakac.bura.precipitation.Snow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun PrecipitationGraph(
    state: PrecipitationGraph,
    args: GraphArgs,
    max: MixedPrecipitation,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()
    val rainColor = AppTheme.colors.rainColor
    val showersColor = AppTheme.colors.showersColor
    val snowColor = AppTheme.colors.snowColor
    Canvas(modifier) {
        /*drawTempAxis(
            unit = absMinTemp.unit,
            minTempC = minCelsius,
            maxTempC = maxCelsius,
            context = context,
            measurer = measurer,
            args = args
        )*/
        drawHorizontalAxisAndPlot(
            state = state,
            max = max,
            context = context,
            measurer = measurer,
            rainColor = rainColor,
            showersColor = showersColor,
            snowColor = snowColor,
            args = args
        )
    }
}

private fun DrawScope.drawHorizontalAxisAndPlot(
    state: PrecipitationGraph,
    max: MixedPrecipitation,
    rainColor: Color,
    showersColor: Color,
    snowColor: Color,
    context: Context,
    measurer: TextMeasurer,
    args: GraphArgs
) {
    val iconSize = 24.dp.toPx()
    val iconSizeRound = iconSize.roundToInt()
    val hasSpaceFor12Icons =
        (size.width - args.startGutter - args.endGutter) - (iconSizeRound * 12) >= (12 * 2.dp.toPx())
    val iconY = ((args.topGutter / 2) - (iconSize / 2)).roundToInt()
    val range = max.value

    drawTimeAxis(
        measurer = measurer,
        args = args
    ) { i, x ->
        // Temperature line
        val point = state.points.getOrNull(i) ?: return@drawTimeAxis
        val value = point.precip.value
        val y = ((1 - value / range) * (size.height - args.topGutter - args.bottomGutter)).toFloat() + args.topGutter
        drawLine(
            brush = SolidColor(rainColor),
            start = Offset(x, size.height - args.bottomGutter),
            end = Offset(x, y),
            strokeWidth = 8.dp.toPx()
        )

        // Condition icons
        if (i % (if (hasSpaceFor12Icons) 2 else 3) == 1) {
            val iconX = x - (iconSize / 2)
            val iconDrawable = AppCompatResources.getDrawable(
                context,
                point.cond.image(context, args.icons)
            )!!
            drawImage(
                image = iconDrawable.toBitmap(width = iconSizeRound, height = iconSizeRound)
                    .asImageBitmap(),
                dstOffset = IntOffset(iconX.roundToInt(), y = iconY),
                dstSize = IntSize(width = iconSizeRound, height = iconSizeRound),
            )
        }
    }
}

@Preview
@Composable
private fun PrecipitationGraphPreview() {
    val now = remember { LocalDateTime.parse("1970-01-01T08:00") }
    AppTheme {
        PrecipitationGraph(
            state = PrecipitationGraph(
                day = LocalDate.parse("1970-01-01"),
                points = List(25) {
                    PrecipitationGraphPoint(
                        time = GraphTime(
                            hour = LocalDateTime.parse("1970-01-01T00:00").plus(it.toLong(), ChronoUnit.HOURS),
                            now = now
                        ),
                        precip = MixedPrecipitation.fromMillimeters(
                            rain = Rain.fromMillimeters(Random.nextDouble(until = 5.0)),
                            showers = Showers.fromMillimeters(Random.nextDouble(until = 5.0)),
                            snow = Snow.fromMillimeters(Random.nextDouble(until = 5.0))
                        ),
                        cond = Condition(
                            wmoCode = Random.nextInt(0, 3),
                            isDay = Random.nextBoolean()
                        )
                    )
                }
            ),
            args = GraphArgs.rememberPopArgs(),
            max = MixedPrecipitation.fromMillimeters(
                Rain.fromMillimeters(15.0),
                Showers.fromMillimeters(0.0),
                Snow.fromMillimeters(0.0)
            ),
            modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).padding(16.dp)
        )
    }
}