/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.example.android.trackr.data

import org.threeten.bp.Duration
import org.threeten.bp.Instant

object SeedData {

    val Users = listOf(
        User(id = 1L, username = "You", avatar = Avatar.DARING_DOVE),
        User(id = 2L, username = "John", avatar = Avatar.LIKEABLE_LARK)
    )

    val Tags = listOf(
        Tag(id = 1L, label = "Home", color = TagColor.BLUE),
        Tag(id = 2L, label = "Work", color = TagColor.GREEN),
        Tag(id = 3L, label = "Hobby", color = TagColor.PURPLE)
    )

    // TODO(b/163065333): add descriptive task titles.
    val Tasks = arrayListOf(
        Task(id = 1L, title = "Task 1", ownerId = 1L, creatorId = 2L),
        Task(id = 2L, title = "Task 2", ownerId = 1L, creatorId = 1L),
        Task(
            id = 3L,
            title = "Task 3",
            state = TaskState.IN_PROGRESS,
            ownerId = 2L,
            creatorId = 1L,
            createdAt = Instant.now() - Duration.ofDays(1),
            dueAt = Instant.now() + Duration.ofDays(2)
        ),
        Task(id = 4L, title = "Task 4", state = TaskState.COMPLETED, ownerId = 2L, creatorId = 2L),
        Task(id = 5L, title = "Task 5", ownerId = 1L, creatorId = 2L),
        Task(id = 6L, title = "Task 6", ownerId = 1L, creatorId = 2L),
        Task(id = 7L, title = "Task 7", state = TaskState.ARCHIVED, ownerId = 2L, creatorId = 2L)
    )

    val TaskTags = listOf(
        TaskTag(taskId = 1L, tagId = 1L),
        TaskTag(taskId = 1L, tagId = 3L),
        TaskTag(taskId = 2L, tagId = 1L),
        TaskTag(taskId = 3L, tagId = 1L),
        TaskTag(taskId = 5L, tagId = 2L),
        TaskTag(taskId = 6L, tagId = 2L),
        TaskTag(taskId = 7L, tagId = 2L)
    )

    val UserTasks = listOf(
        UserTask(userId = 1L, taskId = 1L)
    )
}