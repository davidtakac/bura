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

package com.davidtakac.bura.common.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private val DialogEdgePadding = 24.dp
private val TitleBottomPadding = 16.dp
private val SubtitleBottomPadding = 8.dp
private val ContentHorizontalPadding = 24.dp
private val ContentBottomPadding = 8.dp
private val ButtonSpacing = 8.dp

@Composable
fun AlertDialogWithoutHorizontalPadding(
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    text: (@Composable (Dp) -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
            modifier = modifier
        ) {
            Column {
                Box(
                    Modifier.padding(
                        start = DialogEdgePadding,
                        end = DialogEdgePadding,
                        top = DialogEdgePadding,
                        bottom = TitleBottomPadding
                    )
                ) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineSmall) {
                        title()
                    }
                }

                subtitle?.let {
                    Box(
                        Modifier.padding(
                            start = DialogEdgePadding,
                            end = DialogEdgePadding,
                            bottom = SubtitleBottomPadding
                        )
                    ) {
                        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                            it()
                        }
                    }
                }

                text?.let {
                    Box(Modifier.padding(bottom = ContentBottomPadding)) {
                        it(ContentHorizontalPadding)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = DialogEdgePadding, end = DialogEdgePadding, bottom = DialogEdgePadding)
                ) {
                    dismissButton?.let { it() }
                    Spacer(modifier = Modifier.width(ButtonSpacing))
                    confirmButton()
                }
            }
        }
    }
}