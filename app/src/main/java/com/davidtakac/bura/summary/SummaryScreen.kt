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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidtakac.bura.R
import com.davidtakac.bura.common.compose.FailedToDownloadErrorScreen
import com.davidtakac.bura.common.compose.NoSelectedPlaceErrorScreen
import com.davidtakac.bura.common.compose.OutdatedErrorScreen
import com.davidtakac.bura.common.compose.animateShimmerColorAsState
import com.davidtakac.bura.places.Place
import com.davidtakac.bura.places.picker.PlacePickerState
import com.davidtakac.bura.places.picker.compose.PlacePickerSearchBar
import com.davidtakac.bura.places.search.SearchedPlace
import com.davidtakac.bura.summary.daily.compose.DailySummaryColumn
import com.davidtakac.bura.summary.daily.compose.DailySummaryColumnSkeleton
import com.davidtakac.bura.summary.feelslike.compose.FeelsLikeSummary
import com.davidtakac.bura.summary.hourly.compose.HourSummaryLazyRow
import com.davidtakac.bura.summary.hourly.compose.HourSummaryLazyRowSkeleton
import com.davidtakac.bura.summary.humidity.compose.HumiditySummary
import com.davidtakac.bura.summary.now.compose.NowSummary
import com.davidtakac.bura.summary.now.compose.NowSummarySkeleton
import com.davidtakac.bura.summary.precipitation.compose.PrecipitationSummary
import com.davidtakac.bura.summary.pressure.compose.PressureSummary
import com.davidtakac.bura.summary.sun.compose.SunSummary
import com.davidtakac.bura.summary.uvindex.compose.UvIndexSummary
import com.davidtakac.bura.summary.visibility.compose.VisibilitySummary
import com.davidtakac.bura.summary.wind.compose.WindSummary
import java.time.LocalDate

@Composable
fun SummaryScreen(
    summaryState: SummaryState,
    onHourlySectionClick: () -> Unit,
    onDayClick: (date: LocalDate) -> Unit,
    onSettingsButtonClick: () -> Unit,
    onPrecipitationClick: () -> Unit,

    pickerState: PlacePickerState,
    searchQuery: String,
    onSearchQueryChange: (query: String) -> Unit,
    onSearchQueryClearClick: () -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearch: (query: String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onSearchedPlaceClick: (SearchedPlace) -> Unit,
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
                onSearchedPlaceClick = onSearchedPlaceClick,
                onPlaceDeleteClick = onPlaceDeleteClick,
                active = searchActive,
                onActiveChange = onSearchActiveChange,
                onSettingsClick = onSettingsButtonClick,
            )
        }
    ) { contentPadding ->
        Crossfade(
            targetState = summaryState,
            modifier = Modifier
                .padding(
                    start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = contentPadding.calculateTopPadding(),
                )
                .padding(top = 8.dp)
                .fillMaxSize(),
            label = "Grid crossfade"
        ) {
            val bottomContentPadding = contentPadding.calculateBottomPadding()
            val gridContentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = bottomContentPadding + 16.dp,
            )
            when (it) {
                is SummaryState.Success -> SummaryGrid(
                    state = it,
                    onHourlyClick = onHourlySectionClick,
                    onDayClick = onDayClick,
                    onPrecipitationClick = onPrecipitationClick,
                    contentPadding = gridContentPadding,
                    modifier = Modifier.fillMaxSize()
                )

                SummaryState.Loading, SummaryState.Initial -> SummaryScreenSkeleton(
                    contentPadding = gridContentPadding,
                    modifier = Modifier.fillMaxSize()
                )

                SummaryState.FailedToDownload -> FailedToDownloadErrorScreen(
                    modifier = Modifier.padding(bottom = bottomContentPadding).fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                SummaryState.Outdated -> OutdatedErrorScreen(
                    modifier = Modifier.padding(bottom = bottomContentPadding).fillMaxSize(),
                    onTryAgainClick = onTryAgainClick
                )

                SummaryState.NoSelectedPlace -> NoSelectedPlaceErrorScreen(
                    modifier = Modifier.padding(bottom = bottomContentPadding).fillMaxSize(),
                    onSelectPlaceClick = onSelectPlaceClick
                )
            }
        }
    }
}

@Composable
private fun SummaryGrid(
    state: SummaryState.Success,
    onHourlyClick: () -> Unit,
    onDayClick: (date: LocalDate) -> Unit,
    onPrecipitationClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = contentPadding,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            NowSummary(
                state = state.now,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            HourSummaryLazyRow(
                state = state.hourly,
                onClick = onHourlyClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            DailySummaryColumn(
                state = state.daily,
                onDayClick = onDayClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            PrecipitationSummary(
                state = state.precip,
                onClick = onPrecipitationClick,
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
        item(span = StaggeredGridItemSpan.FullLine) {
            Text(
                text = stringResource(id = R.string.credit_weather),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryScreenSkeleton(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val shimmerColor = animateShimmerColorAsState()
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        item {
            NowSummarySkeleton(
                color = shimmerColor,
                withDate = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            HourSummaryLazyRowSkeleton(
                color = shimmerColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            DailySummaryColumnSkeleton(
                color = shimmerColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}