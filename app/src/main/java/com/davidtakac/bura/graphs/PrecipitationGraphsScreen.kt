/*
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.FailedToDownloadErrorScreen
import com.davidtakac.bura.common.NoSelectedPlaceErrorScreen
import com.davidtakac.bura.common.OutdatedErrorScreen
import com.davidtakac.bura.graphs.common.GraphArgs
import com.davidtakac.bura.graphs.common.GraphsPagerIndicator
import com.davidtakac.bura.graphs.precipitation.FuturePrecipitation
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraph
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphOtherDaySummary
import com.davidtakac.bura.graphs.precipitation.PrecipitationGraphTodaySummary
import com.davidtakac.bura.graphs.precipitation.PrecipitationToday
import com.davidtakac.bura.graphs.precipitation.PrecipitationTotal
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecipitationGraphsScreen(
    state: PrecipitationGraphsState,
    initialDay: LocalDate?,
    onTryAgainClick: () -> Unit,
    onSelectPlaceClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.precip_screen_title)) },
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
                is PrecipitationGraphsState.Success -> Pager(
                    state = it,
                    initialDay = initialDay,
                    modifier = Modifier.fillMaxSize()
                )

                // todo: proper shimmering placeholder
                PrecipitationGraphsState.Loading -> CircularProgressIndicator()

                PrecipitationGraphsState.FailedToDownload -> FailedToDownloadErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                PrecipitationGraphsState.Outdated -> OutdatedErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                PrecipitationGraphsState.NoSelectedPlace -> NoSelectedPlaceErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSelectPlaceClick = onSelectPlaceClick
                )
            }
        }
    }
}

private val contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
private val verticalSpacing = 24.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Pager(
    state: PrecipitationGraphsState.Success,
    initialDay: LocalDate?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val days = remember(state.totals) { state.totals.map { it.day } }
        val args = GraphArgs.rememberPrecipArgs()
        val pagerState = rememberPagerState(initialPage = initialDay?.let { days.indexOf(it) } ?: 0) { days.size }
        val scope = rememberCoroutineScope()
        GraphsPagerIndicator(
            state = days,
            selected = pagerState.currentPage,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(days.indexOf(it))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalPager(state = pagerState) { page ->
            val total = state.totals[page]
            val graph = state.graphs.graphs[page]
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    when (total) {
                        is PrecipitationTotal.OtherDay -> PrecipitationGraphOtherDaySummary(state = total, modifier = Modifier.fillMaxWidth())
                        is PrecipitationTotal.Today -> PrecipitationGraphTodaySummary(state = total, modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    PrecipitationGraph(
                        state = graph,
                        args = args,
                        max = state.graphs.max,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .border(
                                width = Dp.Hairline,
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            .clip(MaterialTheme.shapes.large)
                    )
                }
                item {
                    when (total) {
                        is PrecipitationTotal.OtherDay -> FuturePrecipitation(
                            state = total,
                            modifier = Modifier.fillMaxWidth()
                        )

                        is PrecipitationTotal.Today -> PrecipitationToday(
                            state = total,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}