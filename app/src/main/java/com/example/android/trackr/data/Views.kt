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

import androidx.room.*
import org.threeten.bp.Duration
import org.threeten.bp.Instant

@DatabaseView(
    """
        SELECT
            t.id, t.title, t.state, t.dueAt,
            o.id AS owner_id, o.username AS owner_username
        FROM tasks AS t
        INNER JOIN users AS o ON o.id = t.ownerId
    """
)
data class TaskListItem(
    val id: Long,

    val title: String,

    val state: TaskState,

    val dueAt: Instant,

    @Embedded(prefix = "owner_")
    val owner: User,

    @Relation(
        parentColumn = "id",
        entity = Tag::class,
        entityColumn = "id",
        associateBy = Junction(
            value = TaskTag::class,
            parentColumn = "taskId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>,

    @Relation(
        parentColumn = "id",
        entity = User::class,
        entityColumn = "id",
        associateBy = Junction(
            value = UserTask::class,
            parentColumn = "taskId",
            entityColumn = "userId"
        )
    )
    val starUsers: List<User>
)

@DatabaseView(
    """
        SELECT
            t.id, t.title, t.description, t.state, t.createdAt, t.dueAt,
            o.id AS owner_id, o.username AS owner_username,
            r.id AS reporter_id, r.username AS reporter_username
        FROM tasks AS t
        INNER JOIN users AS o ON o.id = t.ownerId
        INNER JOIN users AS r ON r.id = t.reporterId
    """
)
data class TaskDetail(

    val id: Long,

    val title: String,

    val description: String?,

    val state: TaskState,

    val createdAt: Instant,

    val dueAt: Instant,

    @Embedded(prefix = "owner_")
    val owner: User,

    @Embedded(prefix = "reporter_")
    val reporter: User,

    @Relation(
        parentColumn = "id",
        entity = Tag::class,
        entityColumn = "id",
        associateBy = Junction(
            value = TaskTag::class,
            parentColumn = "taskId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>,

    @Relation(
        parentColumn = "id",
        entity = User::class,
        entityColumn = "id",
        associateBy = Junction(
            value = UserTask::class,
            parentColumn = "taskId",
            entityColumn = "userId"
        )
    )
    val starUsers: List<User>
)