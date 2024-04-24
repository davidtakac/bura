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

package com.davidtakac.bura.graphs

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.davidtakac.bura.R
import com.davidtakac.bura.common.FailedToDownloadErrorScreen
import com.davidtakac.bura.common.NoSelectedPlaceErrorScreen
import com.davidtakac.bura.common.OutdatedErrorScreen
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.GraphsPagerIndicator
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UvIndexGraphScreen(
    state: UvIndexGraphState,
    onBackClick: () -> Unit,
    onTryAgainClick: () -> Unit,
    onSelectPlaceClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.uv_index_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Crossfade(
            targetState = state,
            modifier = Modifier.padding(contentPadding),
            label = "State crossfade"
        ) {
            when (it) {
                is UvIndexGraphState.Success -> Pager(
                    state = it,
                    modifier = Modifier.fillMaxSize()
                )

                UvIndexGraphState.Loading -> Text(text = "LOADING TODO")

                UvIndexGraphState.FailedToDownload -> FailedToDownloadErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                UvIndexGraphState.Outdated -> OutdatedErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                UvIndexGraphState.NoSelectedPlace -> NoSelectedPlaceErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSelectPlaceClick = onSelectPlaceClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Pager(
    state: UvIndexGraphState.Success,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val graphs = state.graphs
        val dates = remember(graphs.graphs) { graphs.graphs.indices.toList().map { LocalDate.parse("1970-01-0${it + 1}") } }
        val pagerState = rememberPagerState { dates.size }
        val scope = rememberCoroutineScope()
        GraphsPagerIndicator(
            state = dates,
            selected = pagerState.currentPage,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(dates.indexOf(it))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalPager(state = pagerState) { page ->
            UvIndexGraphPage(
                state = state.graphs.graphs[page],
                max = state.graphs.max,
                args = GraphArgs.rememberUvIndexArgs(),
            )
        }
    }
}
