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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.android.trackr.shared.R
import java.time.Duration
import java.time.Instant

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            childColumns = ["creatorId"],
            entity = User::class,
            parentColumns = ["id"]
        ),
        ForeignKey(
            childColumns = ["ownerId"],
            entity = User::class,
            parentColumns = ["id"]
        )
    ],
    indices = [
        Index("creatorId"),
        Index("ownerId")
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    /**
     * The task title. TODO: consider adding char limit which may help showcase a11y validation issues.
     */
    val title: String,

    /**
     * The task description, which can be verbose.
     */
    val description: String = "",

    /**
     * The state of the task.
     */
    val status: TaskStatus = TaskStatus.NOT_STARTED,

    /**
     * The team member who created the task (this defaults to the current user).
     */
    val creatorId: Long,

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
    val dueAt: Instant = Instant.now() + Duration.ofDays(7),

    /**
     * Tracks the order in which tasks are presented within a category.
     */
    val orderInCategory: Int,

    /**
     * Whether this task is archived.
     */
    @ColumnInfo(
        // SQLite has no boolean type, so integers 0 and 1 are used instead. Room will do the
        // conversion automatically.
        defaultValue = "0"
    )
    val isArchived: Boolean = false
)

enum class TaskStatus(val key: Int, val stringResId: Int) {
    NOT_STARTED(1, R.string.not_started),
    IN_PROGRESS(2, R.string.in_progress),
    COMPLETED(3, R.string.completed);

    companion object {
        // TODO (b/163065333): find more efficient solution, since map may be high memory.
        private val map = values().associateBy(TaskStatus::key)
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
    val label: String,

    // TODO: consider making the label optional and adding an icon/pattern for color-only tags.

    /**
     * A color associated with the tag.
     */
    val color: TagColor
)

// Denotes the tag text and background color to be displayed
enum class TagColor(val textColor: Int, val backgroundColor: Int) {
    BLUE(R.attr.blueTagTextColor, R.attr.blueTagBackgroundColor),
    GREEN(R.attr.greenTagTextColor, R.attr.greenTagBackgroundColor),
    PURPLE(R.attr.purpleTagTextColor, R.attr.purpleTagBackgroundColor),
    RED(R.attr.redTagTextColor, R.attr.redTagBackgroundColor),
    TEAL(R.attr.tealTagTextColor, R.attr.tealTagBackgroundColor),
    YELLOW(R.attr.yellowTagTextColor, R.attr.yellowTagBackgroundColor),
}

enum class Avatar(val drawableResId: Int) {
    DARING_DOVE(R.drawable.ic_daring_dove),
    LIKEABLE_LARK(R.drawable.ic_likeable_lark),
    PEACEFUL_PUFFIN(R.drawable.ic_peaceful_puffin),
    DEFAULT_USER(R.drawable.ic_user)
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
        Index(value = ["taskId", "tagId"], unique = true),
        Index("taskId"),
        Index("tagId")
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
    val username: String,

    /**
     * The [Avatar] associated with the user.
     */
    val avatar: Avatar
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
        Index(value = ["userId", "taskId"], unique = true),
        Index("userId"),
        Index("taskId")
    ]
)
data class UserTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val taskId: Long
)
