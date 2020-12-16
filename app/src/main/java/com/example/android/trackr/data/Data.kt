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

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.android.trackr.R
import org.threeten.bp.Duration
import org.threeten.bp.Instant


@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            childColumns = ["reporterId"],
            entity = User::class,
            parentColumns = ["id"]
        ),
        ForeignKey(
            childColumns = ["ownerId"],
            entity = User::class,
            parentColumns = ["id"]
        )
    ]
)
data class Task(
    @PrimaryKey
    val id: Long,

    /**
     * The task title. TODO: consider adding char limit which may help showcase a11y validation issues.
     */
    var title: String,

    /**
     * The task description, which can be verbose.
     */
    var description: String = "",

    /**
     * The state of the task.
     */
    val state: TaskState = TaskState.NOT_STARTED,

    /**
     * The team member who created the task (this defaults to the current user).
     */
    val reporterId: Long,

    /**
     * The team member who the task has been assigned to.
     */
    val ownerId: Long,

    /**
     * When this task was created.
     */
    val createdAt: Instant = Instant.now(),

    /**
     * When this task is due.
     */
    val dueAt: Instant = Instant.now() + Duration.ofDays(7)
)

const val ARCHIVED_KEY = 4 // This is referenced in a query.

enum class TaskState(val key: Int, val stringResId: Int) {
    NOT_STARTED(1, R.string.not_started),
    IN_PROGRESS(2, R.string.in_progress),
    COMPLETED(3, R.string.completed),
    ARCHIVED(ARCHIVED_KEY, R.string.archived);

    companion object {
        // TODO (b/163065333): find more efficient solution, since map may be high memory.
        private val map = values().associateBy(TaskState::key)
        fun fromKey(key: Int) = map[key]
    }
}

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey
    val id: Long,

    /**
     * A short label for the tag.
     */
    var label: String,

    // TODO: consider making the label optional and adding an icon/pattern for color-only tags.

    /**
     * A color associated with the tag.
     */
    var color: TagColor
)

// Denotes the tag text and background color to be displayed
enum class TagColor(val textColor: Int, val backgroundColor: Int) {
    BLUE(R.color.trackr_teal_700, R.color.trackr_teal_100),
    GREEN(R.color.trackr_green_700, R.color.trackr_green_100),
    PURPLE(R.color.trackr_purple_700, R.color.trackr_purple_100),
    RED(R.color.trackr_red_700, R.color.trackr_red_100)
}

@Entity(
    tableName = "task_tags",
    foreignKeys = [
        ForeignKey(
            childColumns = ["taskId"],
            entity = Task::class,
            parentColumns = ["id"]
        ),
        ForeignKey(
            childColumns = ["tagId"],
            entity = Tag::class,
            parentColumns = ["id"]
        )
    ],
    indices = [
        Index(value = ["taskId", "tagId"], unique = true)
    ]
)
data class TaskTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val tagId: Long
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: Long,

    /**
     * A short name for the user.
     */
    val username: String

    // TODO: add field for avatar.
)

@Entity(
    tableName = "user_tasks",
    foreignKeys = [
        ForeignKey(
            childColumns = ["userId"],
            entity = User::class,
            parentColumns = ["id"]
        ),
        ForeignKey(
            childColumns = ["taskId"],
            entity = Task::class,
            parentColumns = ["id"]
        )
    ],
    indices = [
        Index(value = ["userId", "taskId"], unique = true)
    ]
)
data class UserTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val taskId: Long
)
