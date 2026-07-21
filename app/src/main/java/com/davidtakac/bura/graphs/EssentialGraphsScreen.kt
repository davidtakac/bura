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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.davidtakac.bura.R
import com.davidtakac.bura.common.compose.FailedToDownloadErrorScreen
import com.davidtakac.bura.common.compose.NoSelectedPlaceErrorScreen
import com.davidtakac.bura.common.compose.OutdatedErrorScreen
import com.davidtakac.bura.common.compose.animateShimmerColorAsState
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.compose.GraphsPagerIndicator
import com.davidtakac.bura.graphs.common.compose.GraphsPagerIndicatorSkeleton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssentialGraphsScreen(
    initialDay: LocalDate?,
    state: EssentialGraphsState,
    onTryAgainClick: () -> Unit,
    onSelectPlaceClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cond_screen_title)) },
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
                is EssentialGraphsState.Success -> Pager(
                    state = it,
                    initialDay = initialDay,
                    modifier = Modifier.fillMaxSize()
                )

                EssentialGraphsState.Loading, EssentialGraphsState.Initial -> EssentialGraphsLoadingIndicator(
                    modifier = Modifier.fillMaxSize()
                )

                EssentialGraphsState.FailedToDownload -> FailedToDownloadErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                EssentialGraphsState.Outdated -> OutdatedErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                EssentialGraphsState.NoSelectedPlace -> NoSelectedPlaceErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSelectPlaceClick = onSelectPlaceClick
                )
            }
        }
    }
}

@Composable
private fun Pager(
    state: EssentialGraphsState.Success,
    initialDay: LocalDate?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val summaries = state.tempGraphSummaries
        val tempGraphs = state.tempGraphs
        val dates = remember(summaries) { summaries.map { it.day } }
        val pagerState = rememberPagerState(initialPage = initialDay?.let { dates.indexOf(it) } ?: 0) { summaries.size }
        val pagerPage by remember { derivedStateOf { pagerState.currentPage } }
        val scope = rememberCoroutineScope()
        GraphsPagerIndicator(
            state = dates,
            selected = pagerPage,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(dates.indexOf(it))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        var itemIndex by remember { mutableIntStateOf(0) }
        var itemScrollOffset by remember { mutableIntStateOf(0) }
        HorizontalPager(state = pagerState) { page ->
            val listState = rememberLazyListState()
            LaunchedEffect(itemIndex, itemScrollOffset, page, pagerPage) {
                if (page != pagerPage) {
                    listState.scrollToItem(itemIndex, itemScrollOffset)
                }
            }
            LaunchedEffect(listState) {
                snapshotFlow { listState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collect {
                        if (page == pagerPage) {
                            itemIndex = it
                        }
                    }
            }
            LaunchedEffect(listState) {
                snapshotFlow { listState.firstVisibleItemScrollOffset }
                    .distinctUntilChanged()
                    .collect {
                        if (page == pagerPage) {
                            itemScrollOffset = it
                        }
                    }
            }
            EssentialGraphPage(
                listState = listState,
                now = tempGraphs.now,
                summary = summaries[page],
                temperatureGraph = tempGraphs.graphs[page],
                minTemp = tempGraphs.min,
                maxTemp = tempGraphs.max,
                temperatureArgs = GraphArgs.rememberTemperatureArgs(),
                popGraph = state.popGraphs.graphs[page],
                popArgs = GraphArgs.rememberPopArgs(),
                precipGraph = state.precipGraphs.graphs[page],
                precipMax = state.precipGraphs.max,
                precipArgs = GraphArgs.rememberPrecipitationArgs(),
                precipitationTotal = state.precipTotals[page]
            )
        }
    }
}

@Composable
private fun EssentialGraphsLoadingIndicator(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val shimmerColor = animateShimmerColorAsState()
        GraphsPagerIndicatorSkeleton(
            color = shimmerColor,
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        EssentialGraphPageLoadingIndicator(
            shimmerColor = shimmerColor,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}