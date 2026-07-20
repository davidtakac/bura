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

package com.davidtakac.bura.places.search.compose

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.compose.AlertDialogWithoutHorizontalPadding
import com.davidtakac.bura.places.search.SearchedPlace
import java.time.ZoneId

@Composable
fun SearchedPlaceEditDialog(
    searchedPlace: SearchedPlace,
    onEdit: (SearchedPlace) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialogWithoutHorizontalPadding(
        title = {
            Text(stringResource(id = R.string.searched_place_edit_tz_title))
        },
        subtitle = {
            Text(
                stringResource(
                    id = R.string.searched_place_edit_tz_description_value,
                    searchedPlace.name, searchedPlace.timeZoneId
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.general_btn_dialog_cancel))
            }
        },
        onDismissRequest = onDismiss,
        text = { horizontalPadding ->
            TimeZonePicker(
                horizontalPadding = horizontalPadding,
                onPick = { onEdit(searchedPlace.copy(timeZoneId = it)) }
            )
        }
    )
}

@Composable
private fun TimeZonePicker(
    horizontalPadding: Dp,
    onPick: (String) -> Unit
) {
    var searchValue by rememberSaveable { mutableStateOf("") }
    val trimmedSearchValue = remember (searchValue) { searchValue.trim() }
    val zoneIds = rememberSaveable { ZoneId.getAvailableZoneIds().sorted() }
    val searchedZoneIds = rememberSaveable(zoneIds, searchValue) {
        if (trimmedSearchValue.isEmpty()) {
            zoneIds
        } else {
            zoneIds.filter { it.contains(ignoreCase = true, other = trimmedSearchValue) }
        }
    }

    Column {
        TextField(
            value = searchValue,
            onValueChange = { searchValue = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null
                )
            },
            trailingIcon = if (searchValue.isNotEmpty()) {
                {
                    IconButton(onClick = { searchValue = "" }) {
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = null
                        )
                    }
                }
            } else {
                null
            },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.searched_place_edit_tz_hint),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        )

        Box(Modifier.fillMaxHeight(0.5f)) {
            if (searchedZoneIds.isEmpty()) {
                TimeZoneItem(
                    text = "No matching time zones for \"$trimmedSearchValue\"",
                    horizontalPadding = horizontalPadding,
                    onClick = null
                )
            } else {
                LazyColumn {
                    items(searchedZoneIds) { zoneId ->
                        TimeZoneItem(
                            text = zoneId,
                            horizontalPadding = horizontalPadding,
                            onClick = { onPick(zoneId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeZoneItem(
    text: String,
    horizontalPadding: Dp,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .then(
                onClick?.let {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = it
                    )
                } ?: Modifier
            )
            .padding(vertical = 12.dp, horizontal = horizontalPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
