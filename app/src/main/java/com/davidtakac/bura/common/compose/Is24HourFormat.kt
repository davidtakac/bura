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

package com.davidtakac.bura.common.compose

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

private fun is24HourFormatFlow(context: Context): Flow<Boolean> = callbackFlow {
    val callback = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun deliverSelfNotifications() = true
        override fun onChange(selfChange: Boolean) {
            trySendBlocking(DateFormat.is24HourFormat(context))
        }
    }
    val uri = Settings.System.getUriFor(Settings.System.TIME_12_24)
    context.contentResolver.registerContentObserver(uri, false, callback)
    awaitClose { context.contentResolver.unregisterContentObserver(callback) }
}.distinctUntilChanged()

@Composable
fun rememberIs24HourFormat(): Boolean {
    val context = LocalContext.current
    val flow = remember(context) { is24HourFormatFlow(context) }
    return flow.collectAsState(initial = DateFormat.is24HourFormat(context)).value
}