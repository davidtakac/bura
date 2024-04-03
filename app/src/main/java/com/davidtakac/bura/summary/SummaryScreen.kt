/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.summary

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.FailedToDownloadErrorScreen
import com.davidtakac.bura.common.NoSelectedPlaceErrorScreen
import com.davidtakac.bura.common.OutdatedErrorScreen
import com.davidtakac.bura.common.animateShimmerColorAsState
import com.davidtakac.bura.place.Place
import com.davidtakac.bura.place.picker.PlacePickerSearchBar
import com.davidtakac.bura.place.picker.PlacePickerState
import com.davidtakac.bura.summary.daily.DaySummary
import com.davidtakac.bura.summary.feelslike.FeelsLikeSummary
import com.davidtakac.bura.summary.hourly.HourSummaryList
import com.davidtakac.bura.summary.hourly.HourSummaryListSkeleton
import com.davidtakac.bura.summary.humidity.HumiditySummary
import com.davidtakac.bura.summary.now.NowSummary
import com.davidtakac.bura.summary.now.NowSummarySkeleton
import com.davidtakac.bura.summary.precipitation.PrecipitationSummary
import com.davidtakac.bura.summary.pressure.PressureSummary
import com.davidtakac.bura.summary.sun.SunSummary
import com.davidtakac.bura.summary.uvindex.UvIndexSummary
import com.davidtakac.bura.summary.visibility.VisibilitySummary
import com.davidtakac.bura.summary.wind.WindSummary
import java.time.LocalDate

@Composable
fun SummaryScreen(
    summaryState: SummaryState,
    onHourlySectionClick: () -> Unit,
    onDayClick: (date: LocalDate) -> Unit,
    onSettingsButtonClick: () -> Unit,

    pickerState: PlacePickerState,
    searchQuery: String,
    onSearchQueryChange: (query: String) -> Unit,
    onSearchQueryClearClick: () -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearch: (query: String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onPlaceDeleteClick: (Place) -> Unit,

    onTryAgainClick: () -> Unit,
    onSelectPlaceClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            PlacePickerSearchBar(
                state = pickerState,
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onQueryClearClick = onSearchQueryClearClick,
                onSearchClick = onSearch,
                onPlaceClick = onPlaceClick,
                onPlaceDeleteClick = onPlaceDeleteClick,
                active = searchActive,
                onActiveChange = onSearchActiveChange,
                onSettingsClick = onSettingsButtonClick
            )
        }
    ) { contentPadding ->
        Crossfade(
            targetState = summaryState,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(top = 8.dp),
            label = "Grid crossfade"
        ) {
            when (it) {
                is SummaryState.Success -> SummaryGrid(
                    state = it,
                    onHourlyClick = onHourlySectionClick,
                    onDayClicked = onDayClick,
                    modifier = Modifier.fillMaxSize()
                )

                SummaryState.Loading -> SummaryLoadingIndicator(
                    modifier = Modifier.fillMaxSize()
                )

                SummaryState.FailedToDownload -> FailedToDownloadErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                SummaryState.Outdated -> OutdatedErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                SummaryState.NoSelectedPlace -> NoSelectedPlaceErrorScreen(
                    onSelectPlaceClick = onSelectPlaceClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SummaryGrid(
    state: SummaryState.Success,
    onHourlyClick: () -> Unit,
    onDayClicked: (date: LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        // Setting the horizontal padding to 16dp and adding empty FullLine items at the top
        // and bottom of the grid is a workaround for contentPadding. For some reason, setting it
        // causes the grid to crash with a 'position() should be called first' exception.
        modifier = modifier.padding(horizontal = 16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        columns = StaggeredGridCells.Fixed(2)
    ) {
        item(span = StaggeredGridItemSpan.Companion.FullLine) {}
        item(span = StaggeredGridItemSpan.Companion.FullLine) {
            NowSummary(
                state = state.now,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item(span = StaggeredGridItemSpan.Companion.FullLine) {
            HourSummaryList(
                state = state.hourly,
                onClick = onHourlyClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item(span = StaggeredGridItemSpan.Companion.FullLine) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                state.daily.days.forEachIndexed { index, day ->
                    val isLast = index == state.daily.days.lastIndex
                    DaySummary(
                        state = day,
                        absMin = state.daily.minTemp,
                        absMax = state.daily.maxTemp,
                        modifier = Modifier.fillMaxWidth(),
                        roundedTop = index == 0,
                        roundedBottom = isLast,
                        onClick = { onDayClicked(day.time) }
                    )
                }
            }
        }
        item {
            PrecipitationSummary(
                state = state.precip,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            UvIndexSummary(
                state = state.uvIndex,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            WindSummary(
                state = state.wind,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            PressureSummary(
                state = state.pressure,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            HumiditySummary(
                state = state.humidity,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            VisibilitySummary(
                state = state.vis,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SunSummary(
                state = state.sun,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            FeelsLikeSummary(
                state = state.feelsLike,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item(span = StaggeredGridItemSpan.Companion.FullLine) {
            Text(
                text = stringResource(id = R.string.credit_weather),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item(span = StaggeredGridItemSpan.Companion.FullLine) {}
    }
}

@Composable
private fun SummaryLoadingIndicator(modifier: Modifier = Modifier) {
    val shimmerColor = animateShimmerColorAsState()
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NowSummarySkeleton(
            color = shimmerColor,
            modifier = Modifier.fillMaxWidth()
        )
        HourSummaryListSkeleton(
            color = shimmerColor,
            modifier = Modifier.fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0..6) {
                val topRadius = if (i == 0) 16.dp else 4.dp
                val bottomRadius = if (i == 6) 16.dp else 4.dp
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth()
                        .background(
                            color = shimmerColor.value,
                            shape = RoundedCornerShape(
                                topStart = topRadius,
                                topEnd = topRadius,
                                bottomStart = bottomRadius,
                                bottomEnd = bottomRadius
                            )
                        )
                ) {}
            }
        }
    }
}