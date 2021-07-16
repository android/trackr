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

package com.example.android.trackr.compose.ui.tasks

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.android.trackr.compose.ui.TaskSummaryCard
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues

@Composable
fun Tasks(
    viewModel: TasksViewModel,
    onTaskClick: (taskId: Long) -> Unit
) {
    val taskSummaries by viewModel.taskSummaries.collectAsState(emptyList())
    LazyColumn(
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.systemBars),
    ) {
        var first = true
        items(
            items = taskSummaries,
            key = { summary -> summary.id },
        ) { summary ->
            if (first) {
                first = false
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
            TaskSummaryCard(
                summary = summary,
                onClick = { onTaskClick(summary.id) }
            )
        }
    }
}
