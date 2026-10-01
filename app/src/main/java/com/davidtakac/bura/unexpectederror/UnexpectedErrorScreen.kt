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

package com.davidtakac.bura.unexpectederror

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.davidtakac.bura.R
import com.davidtakac.bura.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnexpectedErrorScreen(
    cause: String,
    onGoHomeClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.unexpected_error_screen_title))
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.unexpected_error_screen_description))
            Logs(
                text = cause, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Buttons(
                cause = cause,
                onGoHomeClick = onGoHomeClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Logs(text: String, modifier: Modifier = Modifier) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    SelectionContainer(modifier = modifier) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .clip(RoundedCornerShape(size = 8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState)
        )
    }
}

@Composable
private fun Buttons(
    cause: String,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        val lifecycleScope = LocalLifecycleOwner.current.lifecycleScope
        val clipboard = LocalClipboard.current
        val label = stringResource(R.string.unexpected_error_screen_label_clip_entry)
        FilledTonalButton(
            onClick = {
                lifecycleScope.launch {
                    val clipData = ClipData.newPlainText(label, cause)
                    clipboard.setClipEntry(ClipEntry(clipData))
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.unexpected_error_screen_btn_copy_logs))
        }
        Spacer(Modifier.width(16.dp))
        Button(onClick = onGoHomeClick, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.unexpected_error_screen_btn_go_home))
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        UnexpectedErrorScreen(dummyCause, {})
    }
}

private val dummyCause =
    """
    FATAL EXCEPTION: DefaultDispatcher-worker-3
Process: com.example.notesapp, PID: 18472
java.lang.IllegalStateException: Failed to synchronize note metadata from remote server
    at com.example.notesapp.sync.NoteSyncManager.syncAllNotes(NoteSyncManager.kt:214)
    at com.example.notesapp.sync.NoteSyncWorker.doWork(NoteSyncWorker.kt:87)
    at androidx.work.CoroutineWorker${'$'}startWork$1.invokeSuspend(CoroutineWorker.kt:68)
    at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
    at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
    at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571)
    at kotlinx.coroutines.scheduling.CoroutineScheduler${'$'}Worker.executeTask(CoroutineScheduler.kt:750)
    at kotlinx.coroutines.scheduling.CoroutineScheduler${'$'}Worker.runWorker(CoroutineScheduler.kt:678)
    at kotlinx.coroutines.scheduling.CoroutineScheduler${'$'}Worker.run(CoroutineScheduler.kt:665)
Caused by: retrofit2.HttpException: HTTP 500 Internal Server Error
    at retrofit2.KotlinExtensions${'$'}await$2$2.onResponse(KotlinExtensions.kt:53)
    at retrofit2.OkHttpCall$1.onResponse(OkHttpCall.java:161)
    at okhttp3.internal.connection.RealCall${'$'}AsyncCall.run(RealCall.kt:519)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
    at java.util.concurrent.ThreadPoolExecutor${'$'}Worker.run(ThreadPoolExecutor.java:641)
    at java.lang.Thread.run(Thread.java:923)                                                        ```
    """.trimIndent()