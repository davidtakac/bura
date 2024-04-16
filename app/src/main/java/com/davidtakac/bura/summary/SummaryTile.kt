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

package com.davidtakac.bura.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SummaryTile(
    label: @Composable () -> Unit,
    value: @Composable () -> Unit,
    bottom: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    supportingValue: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(modifier) {
        val content = @Composable {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Column {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.titleSmall,
                        LocalContentColor provides MaterialTheme.colorScheme.secondary,
                        content = label
                    )
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.headlineMedium,
                        content = value
                    )
                    supportingValue?.let {
                        CompositionLocalProvider(
                            LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                            content = it
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    content = bottom
                )
            }
        }
        if (onClick != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minWidth),
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                onClick = onClick,
                content = content
            )
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minWidth),
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                content = content
            )
        }
    }
}