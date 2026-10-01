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

package com.davidtakac.bura.settings.common.compose

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.davidtakac.bura.R
import com.davidtakac.bura.common.compose.AlertDialogWithoutHorizontalPadding

@Composable
fun MultipleChoiceDialog(
    title: String,
    choices: List<String>,
    selectedIdx: Int,
    onSelect: (idx: Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var newlySelectedIdx by remember(selectedIdx) { mutableIntStateOf(selectedIdx) }
    AlertDialogWithoutHorizontalPadding(
        title = { Text(title) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.general_btn_dialog_cancel))
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onSelect(newlySelectedIdx) }) {
                Text(stringResource(R.string.general_btn_dialog_confirm))
            }
        },
        text = { horizontalPadding ->
            LazyColumn {
                itemsIndexed(choices) { idx, item ->
                    Choice(
                        label = item,
                        selected = idx == newlySelectedIdx,
                        onClick = { newlySelectedIdx = idx },
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = horizontalPadding)
                            .fillMaxWidth()
                    )
                }
            }
        }
    )
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
                indication = LocalIndication.current,
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