/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.place.picker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.place.Place

internal object MotionTokens {
    const val DurationExtraLong1 = 700.0
    const val DurationExtraLong2 = 800.0
    const val DurationExtraLong3 = 900.0
    const val DurationExtraLong4 = 1000.0
    const val DurationLong1 = 450.0
    const val DurationLong2 = 500.0
    const val DurationLong3 = 550.0
    const val DurationLong4 = 600.0
    const val DurationMedium1 = 250.0
    const val DurationMedium2 = 300.0
    const val DurationMedium3 = 350.0
    const val DurationMedium4 = 400.0
    const val DurationShort1 = 50.0
    const val DurationShort2 = 100.0
    const val DurationShort3 = 150.0
    const val DurationShort4 = 200.0
    val EasingEmphasizedCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EasingEmphasizedAccelerateCubicBezier = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val EasingEmphasizedDecelerateCubicBezier = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EasingLegacyCubicBezier = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val EasingLegacyAccelerateCubicBezier = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val EasingLegacyDecelerateCubicBezier = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val EasingLinearCubicBezier = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
    val EasingStandardCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EasingStandardAccelerateCubicBezier = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
    val EasingStandardDecelerateCubicBezier = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
}

private const val AnimationEnterDurationMillis: Int = MotionTokens.DurationLong4.toInt()
private const val AnimationExitDurationMillis: Int = MotionTokens.DurationMedium3.toInt()
private const val AnimationDelayMillis: Int = MotionTokens.DurationShort2.toInt()
private val AnimationEnterEasing = MotionTokens.EasingEmphasizedDecelerateCubicBezier
private val AnimationExitEasing = CubicBezierEasing(0.0f, 1.0f, 0.0f, 1.0f)

private val AnimationEnterFloatSpec: FiniteAnimationSpec<Dp> = tween(
    durationMillis = AnimationEnterDurationMillis,
    delayMillis = AnimationDelayMillis,
    easing = AnimationEnterEasing,
)
private val AnimationExitFloatSpec: FiniteAnimationSpec<Dp> = tween(
    durationMillis = AnimationExitDurationMillis,
    delayMillis = AnimationDelayMillis,
    easing = AnimationExitEasing,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacePickerSearchBar(
    state: PlacePickerState,
    query: String,
    onQueryChange: (query: String) -> Unit,
    onQueryClearClick: () -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onSearchClick: (query: String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onPlaceDeleteClick: (Place) -> Unit,
    onSettingsClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(active) {
        if (active) focusRequester.requestFocus()
        else focusRequester.freeFocus()
    }
    val spacerWidth by animateDpAsState(
        targetValue = if (active) 0.dp else 16.dp,
        animationSpec = if (active) AnimationEnterFloatSpec else AnimationExitFloatSpec
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        //contentAlignment = Alignment.Center
    ) {
        Spacer(modifier = Modifier.width(spacerWidth))
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearchClick,
            active = active,
            onActiveChange = onActiveChange,
            leadingIcon = {
                PinOrBackButton(
                    active = active,
                    onBackClick = { onActiveChange(false) }
                )
            },
            trailingIcon = {
                SettingsOrClearButton(
                    active = active,
                    onSettingsClick = onSettingsClick,
                    onClearClick = onQueryClearClick
                )
            },
            placeholder = {
                Text(text = stringResource(id = R.string.place_picker_hint_search))
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
        ) {
            PlacePickerResults(
                state = state,
                onPlaceClick = onPlaceClick,
                onPlaceDeleteClick = onPlaceDeleteClick
            )
        }
        Spacer(modifier = Modifier.width(spacerWidth))
    }
}

@Composable
private fun PinOrBackButton(active: Boolean, onBackClick: () -> Unit) {
    Crossfade(targetState = active, label = "Pin to back button") {
        if (it) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        } else {
            // minimumInteractiveComponentSize is here so Crossfade can
            // animate without jumps
            Box(modifier = Modifier.minimumInteractiveComponentSize()) {
                Icon(
                    painter = painterResource(id = R.drawable.location_on),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun SettingsOrClearButton(
    active: Boolean,
    onSettingsClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Crossfade(targetState = active, label = "Settings to clear all button") {
        if (it) {
            IconButton(onClick = onClearClick) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = null
                )
            }
        } else {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = null
                )
            }
        }
    }
}