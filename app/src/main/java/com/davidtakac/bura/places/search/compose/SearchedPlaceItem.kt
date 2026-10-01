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
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.davidtakac.bura.places.search.SearchedPlace

@Composable
fun SearchedPlaceItem(state: SearchedPlace, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
            .then(modifier)
    ) {
        val title = remember(state.name, state.countryName, state.countryCode) {
            listOfNotNull(state.name, state.countryName ?: state.countryCode).joinToString(", ")
        }
        Text(title)
        val adminList = remember(state.admin1, state.admin2, state.admin3, state.admin4) {
            listOfNotNull(state.admin1, state.admin2, state.admin3, state.admin4)
        }
        if (adminList.isNotEmpty()) {
            Text(
                text = adminList.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}