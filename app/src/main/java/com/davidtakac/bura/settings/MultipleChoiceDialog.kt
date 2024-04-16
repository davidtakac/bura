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

package com.davidtakac.bura.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.davidtakac.bura.R


private val DialogPadding = 24.dp
private val TitlePadding = PaddingValues(bottom = 16.dp)
private val ContentPadding = PaddingValues(bottom = 24.dp)
private val ButtonSpacing = 8.dp

@Composable
fun MultipleChoiceDialog(
    title: String,
    choices: List<String>,
    selectedIdx: Int,
    onSelect: (idx: Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newlySelectedIdx by remember(selectedIdx) { mutableIntStateOf(selectedIdx) }
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
            modifier = modifier
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(
                        start = DialogPadding,
                        end = DialogPadding,
                        top = DialogPadding,
                        bottom = TitlePadding.calculateBottomPadding()
                    )
                )
                LazyColumn(modifier = Modifier.padding(ContentPadding)) {
                    itemsIndexed(choices) { idx, item ->
                        Choice(
                            label = item,
                            selected = idx == newlySelectedIdx,
                            onClick = { newlySelectedIdx = idx },
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = DialogPadding)
                                .fillMaxWidth()
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = DialogPadding, end = DialogPadding, bottom = DialogPadding)
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.general_btn_dialog_cancel))
                    }
                    Spacer(modifier = Modifier.width(ButtonSpacing))
                    TextButton(onClick = { onSelect(newlySelectedIdx) }) {
                        Text(stringResource(R.string.general_btn_dialog_confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun Choice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onClick
            )
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}