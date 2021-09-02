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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.trackr.compose.R
import com.example.android.trackr.compose.ui.TaskSummaryCard
import com.example.android.trackr.compose.ui.TrackrTheme
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.User
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Composable
fun Tasks(
    viewModel: TasksViewModel,
    onTaskClick: (taskId: Long) -> Unit,
    onAddTaskClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val statusGroups by viewModel.statusGroups.collectAsState(emptyMap())
    TasksContent(
        statusGroups = statusGroups,
        clock = viewModel.clock,
        onStatusClick = { viewModel.toggleStatusExpanded(it) },
        onStarClick = { viewModel.toggleTaskStarState(it) },
        onTaskClick = onTaskClick,
        onAddTaskClick = onAddTaskClick,
        onArchiveClick = onArchiveClick,
        onSettingsClick = onSettingsClick
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun TasksContent(
    statusGroups: Map<TaskStatus, TaskStatusGroup>,
    clock: Clock,
    onStatusClick: (status: TaskStatus) -> Unit,
    onStarClick: (taskId: Long) -> Unit,
    onTaskClick: (taskId: Long) -> Unit,
    onAddTaskClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val systemBars = LocalWindowInsets.current.systemBars
    var bottomBarHeight by remember { mutableStateOf(0) }
    Scaffold(
        modifier = Modifier.padding(
            rememberInsetsPaddingValues(
                insets = systemBars,
                applyStart = false,
                applyEnd = false,
                applyBottom = false
            )
        ),
        bottomBar = {
            TasksBottomBar(
                onArchiveClick = onArchiveClick,
                onSettingsClick = onSettingsClick,
                modifier = Modifier.onSizeChanged { size -> bottomBarHeight = size.height }
            )
        },
        floatingActionButton = {
            AddTaskButton(onClick = onAddTaskClick)
        },
        isFloatingActionButtonDocked = true
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = with(LocalDensity.current) { bottomBarHeight.toDp() + 4.dp }
            ),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            for ((status, group) in statusGroups) {
                stickyHeader(key = "header-${status.key}") {
                    TaskStatusHeader(
                        status = status,
                        count = group.summaries.size,
                        expanded = group.expanded,
                        onClick = { onStatusClick(status) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                items(items = group.summaries, key = { "task-${it.id}" }) { summary ->
                    AnimatedVisibility(
                        visible = group.expanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        TaskSummaryCard(
                            summary = summary,
                            clock = clock,
                            onStarClick = { onStarClick(summary.id) },
                            onClick = { onTaskClick(summary.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TasksBottomBar(
    onArchiveClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val systemBars = LocalWindowInsets.current.systemBars
    BottomAppBar(
        modifier = modifier,
        contentPadding = rememberInsetsPaddingValues(
            insets = systemBars,
            applyTop = false
        ),
        cutoutShape = CircleShape
    ) {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.menu),
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(onClick = onArchiveClick) {
                Text(text = stringResource(R.string.archive))
            }
            DropdownMenuItem(onClick = onSettingsClick) {
                Text(text = stringResource(R.string.settings))
            }
        }
    }
}

@Composable
private fun AddTaskButton(
    onClick: () -> Unit
) {
    FloatingActionButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_task)
        )
    }
}

@Preview
@Composable
private fun PreviewTasksContent() {
    TrackrTheme {
        TasksContent(
            statusGroups = mapOf(
                TaskStatus.IN_PROGRESS to TaskStatusGroup(
                    expanded = true,
                    summaries = listOf(
                        TaskSummary(
                            id = 1L,
                            title = "Create default illustrations for event types",
                            status = TaskStatus.IN_PROGRESS,
                            dueAt = Instant.now() + Duration.ofHours(73),
                            orderInCategory = 1,
                            isArchived = false,
                            owner = User(
                                id = 1L,
                                username = "Daring Dove",
                                avatar = Avatar.DARING_DOVE
                            ),
                            tags = listOf(
                                Tag(id = 1L, label = "2.3 release", color = TagColor.BLUE),
                                Tag(id = 4L, label = "UI/UX", color = TagColor.PURPLE),
                            ),
                            starred = true,
                        )
                    )
                )
            ),
            clock = Clock.systemDefaultZone(),
            onStatusClick = {},
            onStarClick = {},
            onTaskClick = {},
            onAddTaskClick = {},
            onArchiveClick = {},
            onSettingsClick = {}
        )
    }
}
