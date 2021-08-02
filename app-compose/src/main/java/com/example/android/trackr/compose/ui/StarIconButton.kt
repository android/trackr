/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackr.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun StarIconButton(
    onClick: () -> Unit,
    filled: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Image(
            imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(TrackrTheme.colors.star),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStarIconButtons() {
    TrackrTheme {
        Column {
            StarIconButton(
                onClick = {},
                filled = true,
                contentDescription = null,
            )
            StarIconButton(
                onClick = {},
                filled = false,
                contentDescription = null,
            )
        }
    }
}
