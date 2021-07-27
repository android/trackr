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

package com.example.android.trackr.ui

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.Task
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskTag
import com.example.android.trackr.data.User
import com.example.android.trackr.data.UserTask
import com.example.android.trackr.db.AppDatabase
import kotlinx.coroutines.runBlocking
import java.time.Instant

fun createDatabase(): AppDatabase {
    val db = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
        .allowMainThreadQueries()
        .build()
    populate(db)
    return db
}

private fun populate(db: AppDatabase) {
    runBlocking {
        with(db.taskDao()) {
            insertUsers(listOf(USER_OWNER, USER_CREATOR, USER_OTHER))
            insertTags(listOf(TAG_1, TAG_2))
            insertTasks(listOf(TASK_1, ARCHIVED_TASK_1, ARCHIVED_TASK_2))
            insertTaskTags(listOf(TaskTag(taskId = TASK_1.id, tagId = TAG_1.id)))
            insertUserTasks(listOf(UserTask(userId = USER_OWNER.id, taskId = TASK_1.id)))
        }
    }
}

val USER_OWNER = User(1L, "owner", Avatar.DEFAULT_USER)
val USER_CREATOR = User(2L, "creator", Avatar.DEFAULT_USER)
val USER_OTHER = User(3L, "another", Avatar.DEFAULT_USER)

val TAG_1 = Tag(1L, "tag1", TagColor.RED)
val TAG_2 = Tag(2L, "tag2", TagColor.BLUE)

val TASK_1 = Task(
    id = 1L,
    title = "in progress task",
    description = "description",
    status = TaskStatus.IN_PROGRESS,
    creatorId = USER_CREATOR.id,
    ownerId = USER_OWNER.id,
    createdAt = Instant.parse("2020-09-01T00:00:00.00Z"),
    dueAt = Instant.parse("2020-11-01T00:00:00.00Z"),
    orderInCategory = 1
)
val ARCHIVED_TASK_1 = Task(
    id = 11L,
    title = "archived task 1",
    description = "description 1",
    status = TaskStatus.COMPLETED,
    creatorId = USER_CREATOR.id,
    ownerId = USER_OWNER.id,
    createdAt = Instant.parse("2020-09-01T00:00:00.00Z"),
    dueAt = Instant.parse("2020-11-01T00:00:00.00Z"),
    orderInCategory = 1,
    isArchived = true
)
val ARCHIVED_TASK_2 = Task(
    id = 12L,
    title = "archived task 2",
    description = "description 2",
    status = TaskStatus.COMPLETED,
    creatorId = USER_CREATOR.id,
    ownerId = USER_OTHER.id,
    createdAt = Instant.parse("2020-09-01T00:00:00.00Z"),
    dueAt = Instant.parse("2020-11-01T00:00:00.00Z"),
    orderInCategory = 2,
    isArchived = true
)
