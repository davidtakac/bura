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

package com.davidtakac.bura.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.davidtakac.bura.R
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

private val fallbackLocale = Locale.US
private val supportedLocales = listOf(
    fallbackLocale,
    Locale.forLanguageTag("es"),
    Locale.forLanguageTag("fr"),
    Locale.forLanguageTag("hr"),
    Locale.forLanguageTag("vi"),
    Locale("zh", "CN"),
    Locale.forLanguageTag("de"),
    Locale.forLanguageTag("ru"),
    Locale.forLanguageTag("pl"),
    Locale.forLanguageTag("sv"),
    Locale.forLanguageTag("nl"),
    Locale.forLanguageTag("ar"),
    Locale("zh", "TW"),
    Locale.forLanguageTag("cs"),
    Locale.forLanguageTag("el"),
)

@Composable
fun rememberAppLocale(): Locale {
    val config = LocalConfiguration.current
    return remember(config) {
        val defaultLocale = config.locales[0]
        val defaultLocaleSupported = supportedLocales.any { it == defaultLocale || it.language == defaultLocale.language }
        if (defaultLocaleSupported) defaultLocale else fallbackLocale
    }
}

@Composable
fun rememberDateTimeFormatter(@StringRes ofPattern: Int): DateTimeFormatter {
    val pattern = stringResource(ofPattern)
    val locale = rememberAppLocale()
    return remember(pattern, locale) {
        DateTimeFormatter
            .ofPattern(pattern, locale)
            .withDecimalStyle(DecimalStyle.of(locale))
    }
}

@Composable
fun rememberDateTimeHourFormatter(): DateTimeFormatter = rememberDateTimeFormatter(
    if (rememberIs24HourFormat()) R.string.date_time_pattern_hour
    else R.string.date_time_pattern_hour_ampm
)

@Composable
fun rememberDateTimeHourMinuteFormatter(): DateTimeFormatter = rememberDateTimeFormatter(
    if (rememberIs24HourFormat()) R.string.date_time_pattern_hour_minute
    else R.string.date_time_pattern_hour_minute_ampm
)

@Composable
fun rememberDateTimeDayAndTimeFormatter(): DateTimeFormatter = rememberDateTimeFormatter(
    if (rememberIs24HourFormat()) R.string.date_time_pattern_dow_hour_minute
    else R.string.date_time_pattern_dow_hour_minute_ampm
)

@Composable
fun rememberNumberFormat(): NumberFormat {
    val locale = rememberAppLocale()
    return remember(locale) { NumberFormat.getNumberInstance(locale) }
}
