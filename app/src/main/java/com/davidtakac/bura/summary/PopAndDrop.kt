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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.davidtakac.bura.R
import com.davidtakac.bura.pop.Pop
import com.davidtakac.bura.pop.string
import com.davidtakac.bura.common.AppTheme

@Composable
fun PopAndDrop(pop: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodySmall
    val color = MaterialTheme.colorScheme.primary
    val inlineContentMap = mapOf(
        "drop" to InlineTextContent(
            placeholder = Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.water_drop),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = color
            )
        }
    )
    val annotatedString = buildAnnotatedString {
        withStyle(style.toSpanStyle()) {
            appendInlineContent(id = "drop")
            append(pop)
        }
    }
    Text(
        text = annotatedString,
        inlineContent = inlineContentMap,
        color = color,
        modifier = modifier
    )
}

@Preview
@Composable
private fun PopPreview() {
    AppTheme {
        PopAndDrop(pop = Pop(15.0).string())
    }
}