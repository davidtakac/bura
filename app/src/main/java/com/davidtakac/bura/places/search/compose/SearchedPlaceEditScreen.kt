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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.places.search.SearchedPlace
import java.time.ZoneId

private val horizontalPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchedPlaceEditScreen(
    searchedPlace: SearchedPlace,
    onEdit: (SearchedPlace) -> Unit,
    onCloseClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(stringResource(id = R.string.searched_place_edit_tz_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCloseClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(
                    start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = contentPadding.calculateTopPadding() + 8.dp,
                )
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(
                    id = R.string.searched_place_edit_tz_description_value,
                    searchedPlace.name, searchedPlace.timeZoneId
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
            TimeZonePicker(
                horizontalPadding = horizontalPadding,
                listContentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                onPick = { onEdit(searchedPlace.copy(timeZoneId = it)) }
            )
        }
    }
}

@Composable
private fun TimeZonePicker(
    horizontalPadding: Dp,
    listContentPadding: PaddingValues,
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
        OutlinedTextField(
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

        Box(Modifier.fillMaxSize()) {
            if (searchedZoneIds.isEmpty()) {
                TimeZoneItem(
                    text = stringResource(
                        R.string.searched_place_edit_tz_no_search_results_value,
                        trimmedSearchValue,
                    ),
                    horizontalPadding = horizontalPadding,
                    onClick = null
                )
            } else {
                LazyColumn(contentPadding = listContentPadding) {
                    itemsIndexed(searchedZoneIds) { idx, zoneId ->
                        if (idx == 0) {
                            Spacer(Modifier.size(8.dp))
                        }
                        TimeZoneItem(
                            text = zoneId,
                            horizontalPadding = horizontalPadding,
                            onClick = { onPick(zoneId) }
                        )
                        if (idx != searchedZoneIds.lastIndex) {
                            HorizontalDivider()
                        }
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
