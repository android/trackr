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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.trackr.data.SeedData
import com.example.android.trackr.data.Tag

@Composable
fun TagChip(
    tag: Tag,
    modifier: Modifier = Modifier
) {
    Text(
        text = tag.label,
        color = TrackrTheme.colors.tagText(tag.color),
        modifier = modifier
            .background(
                color = TrackrTheme.colors.tagBackground(tag.color),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 6.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewTagChip() {
    TrackrTheme {
        Column {
            for (tag in SeedData.Tags) {
                TagChip(tag = tag)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
