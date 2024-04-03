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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

// region Collapsed search bar horizontal padding workaround

// As of androidx.compose.material3:material3:1.2.1, we can't specify the horizontal padding
// of the collapsed search bar. If we do it with a modifier or spacers, the expanding animation
// will be horizontally inset for the value of that padding.
// As a workaround, all of these vals were copied verbatim from the library to make the horizontal
// padding animation the same as the expanding animation of the SearchBar. When they add the ability
// to specify padding of the collapsed full-screen search bar, this code should be removed.
private object MotionTokens {
    const val DurationLong4 = 600.0
    const val DurationMedium3 = 350.0
    const val DurationShort2 = 100.0
    val EasingEmphasizedDecelerateCubicBezier = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
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

// endregion

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
    val horizontalPadding by animateDpAsState(
        targetValue = if (active) 0.dp else 16.dp,
        animationSpec = if (active) AnimationEnterFloatSpec else AnimationExitFloatSpec,
        label = "Search bar horizontal padding"
    )

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
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .focusRequester(focusRequester)
    ) {
        PlacePickerResults(
            state = state,
            onPlaceClick = onPlaceClick,
            onPlaceDeleteClick = onPlaceDeleteClick
        )
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