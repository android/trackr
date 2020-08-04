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

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Duration
import org.threeten.bp.Instant


@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

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

    /*
    TODO: for these fields, set up FK relationships after defining User and Tag entities.
    /**
     * The team member who created the task (this defaults to the current user).
     */
    val reporter: User,

    /**
     * The team member who the task has been assigned to.
     */
    val owner: User,

    /**
     * An arbitrary list of tags associated with an task.
     */
    val tags: List<Tag> = emptyList()
    */

    /**
     * When this task was created.
     */
    val createdAt: Instant = Instant.now(),

    /**
     * When this task is due.
     */
    val dueAt: Instant = Instant.now() + Duration.ofDays(7)
)

enum class TaskState(val key: Int) {
    NOT_STARTED(1),
    IN_PROGRESS(2),
    COMPLETED(3),
    ARCHIVED(4);

    companion object {
        // TODO (b/163065333): find more efficient solution, since map may be high memory.
        private val map = values().associateBy(TaskState::key)
        fun fromKey(key: Int) = map[key]
    }
}

// TODO(b/163065333): convert to Entity and set up FK relationships.
data class Tag(
    val id: Long,

    /**
     * A short label for the tag.
     */
    var label: String,

    // TODO: consider making the label optional and adding an icon/pattern for color-only tags.

    /**
     * A color associated with the tag.
     */
    var color: Int = Color.rgb(255, 255, 255)
)

// TODO(b/163065333): convert to Entity and set up FK relationships.
data class User(
    val id: Long,

    /**
     * A short name for the user.
     */
    val username: String,

    // TODO: add field for avatar.

    /**
     * The list of tasks that the user has starred.
     */
    val starredTasks: List<Task> = emptyList()
)