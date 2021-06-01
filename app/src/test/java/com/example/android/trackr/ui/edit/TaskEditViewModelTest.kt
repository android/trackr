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

package com.example.android.trackr.ui.edit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.Task
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskTag
import com.example.android.trackr.data.User
import com.example.android.trackr.data.UserTask
import com.example.android.trackr.db.AppDatabase
import com.example.android.trackr.ui.createDatabase
import com.example.android.trackr.usecase.LoadTagsUseCase
import com.example.android.trackr.usecase.LoadTaskDetailUseCase
import com.example.android.trackr.usecase.LoadUsersUseCase
import com.example.android.trackr.usecase.SaveTaskDetailUseCase
import com.example.android.trackr.valueBlocking
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Instant

@RunWith(AndroidJUnit4::class)
class TaskEditViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun createViewModel(): TaskEditViewModel {
        val db = createDatabase()
        populate(db)
        val taskDao = db.taskDao()
        return TaskEditViewModel(
            LoadTaskDetailUseCase(taskDao),
            LoadUsersUseCase(taskDao),
            LoadTagsUseCase(taskDao),
            SaveTaskDetailUseCase(taskDao),
            user1
        )
    }

    @Test
    fun createNewTask() {
        val viewModel = createViewModel()
        viewModel.taskId = 0L

        assertThat(viewModel.taskId).isEqualTo(0L)
        assertThat(viewModel.modified.valueBlocking).isFalse()
        assertThat(viewModel.title.valueBlocking).isEmpty()
        assertThat(viewModel.description.valueBlocking).isEmpty()
        assertThat(viewModel.owner.valueBlocking).isEqualTo(user1)
        assertThat(viewModel.creator.valueBlocking).isEqualTo(user1)
        assertThat(viewModel.tags.valueBlocking).isEmpty()

        viewModel.title.value = "a"

        assertThat(viewModel.title.valueBlocking).isEqualTo("a")
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun editExistingTask() {
        val viewModel = createViewModel()
        viewModel.taskId = 1L

        assertThat(viewModel.taskId).isEqualTo(1L)
        assertThat(viewModel.modified.valueBlocking).isFalse()
        assertThat(viewModel.title.valueBlocking).isEqualTo("title")
        assertThat(viewModel.description.valueBlocking).isEqualTo("description")
        assertThat(viewModel.owner.valueBlocking).isEqualTo(User(1L, "owner", Avatar.DEFAULT_USER))
        assertThat(viewModel.creator.valueBlocking).isEqualTo(
            User(
                2L,
                "creator",
                Avatar.DEFAULT_USER
            )
        )
        assertThat(viewModel.dueAt.valueBlocking)
            .isEqualTo(Instant.parse("2020-11-01T00:00:00.00Z"))
        assertThat(viewModel.createdAt.valueBlocking)
            .isEqualTo(Instant.parse("2020-09-01T00:00:00.00Z"))
        assertThat(viewModel.users).hasSize(3)

        viewModel.title.value = "a"

        assertThat(viewModel.title.valueBlocking).isEqualTo("a")
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun editOwner() {
        val viewModel = createViewModel()
        viewModel.taskId = 1L

        assertThat(viewModel.owner.valueBlocking).isEqualTo(User(1L, "owner", Avatar.DEFAULT_USER))
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.updateOwner(User(3L, "another", Avatar.DEFAULT_USER))

        assertThat(viewModel.owner.valueBlocking).isEqualTo(
            User(
                3L,
                "another",
                Avatar.DEFAULT_USER
            )
        )
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun editStatus() {
        val viewModel = createViewModel()
        viewModel.taskId = 1L

        assertThat(viewModel.status.valueBlocking).isEqualTo(TaskStatus.IN_PROGRESS)
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.updateState(TaskStatus.COMPLETED)

        assertThat(viewModel.status.valueBlocking).isEqualTo(TaskStatus.COMPLETED)
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun editDueAt() {
        val viewModel = createViewModel()
        viewModel.taskId = 1L

        assertThat(viewModel.dueAt.valueBlocking)
            .isEqualTo(Instant.parse("2020-11-01T00:00:00.00Z"))
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.updateDueAt(Instant.parse("2020-12-01T00:00:00.00Z"))

        assertThat(viewModel.dueAt.valueBlocking)
            .isEqualTo(Instant.parse("2020-12-01T00:00:00.00Z"))
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun addTag() {
        val viewModel = createViewModel()
        viewModel.taskId = 1L

        assertThat(viewModel.tags.valueBlocking).containsExactly(Tag(1L, "tag1", TagColor.RED))
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.addTag(Tag(2L, "tag2", TagColor.BLUE))

        assertThat(viewModel.tags.valueBlocking).containsExactly(
            Tag(1L, "tag1", TagColor.RED),
            Tag(2L, "tag2", TagColor.BLUE)
        )
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun removeTag() {
        val viewModel = createViewModel()
        viewModel.taskId = 1L

        assertThat(viewModel.tags.valueBlocking).containsExactly(Tag(1L, "tag1", TagColor.RED))
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.removeTag(Tag(1L, "tag1", TagColor.RED))

        assertThat(viewModel.tags.valueBlocking).isEmpty()
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun discardChanges() {
        val viewModel = createViewModel()
        viewModel.taskId = 1L

        assertThat(viewModel.discarded.valueBlocking).isFalse()
        viewModel.discardChanges()
        assertThat(viewModel.discarded.valueBlocking).isTrue()
    }

    private val user1 = User(1L, "owner", Avatar.DEFAULT_USER)

    private fun populate(db: AppDatabase) {
        runBlocking {
            with(db.taskDao()) {
                insertTags(
                    listOf(
                        Tag(1L, "tag1", TagColor.RED),
                        Tag(2L, "tag2", TagColor.BLUE)
                    )
                )
                insertUsers(
                    listOf(
                        user1,
                        User(2L, "creator", Avatar.DEFAULT_USER),
                        User(3L, "another", Avatar.DEFAULT_USER)
                    )
                )
                insertTasks(
                    listOf(
                        Task(
                            id = 1L,
                            title = "title",
                            description = "description",
                            status = TaskStatus.IN_PROGRESS,
                            creatorId = 2L,
                            ownerId = 1L,
                            createdAt = Instant.parse("2020-09-01T00:00:00.00Z"),
                            dueAt = Instant.parse("2020-11-01T00:00:00.00Z"),
                            orderInCategory = 1
                        )
                    )
                )
                insertTaskTags(listOf(TaskTag(taskId = 1L, tagId = 1L)))
                insertUserTasks(listOf(UserTask(userId = 1L, taskId = 1L)))
            }
        }
    }
}
