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

package com.example.android.trackr.ui.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.trackr.TestApplication
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.Task
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.TaskTag
import com.example.android.trackr.data.User
import com.example.android.trackr.data.UserTask
import com.example.android.trackr.db.AppDatabase
import com.example.android.trackr.ui.createDatabase
import com.example.android.trackr.usecase.ArchiveUseCase
import com.example.android.trackr.usecase.GetOngoingTaskSummariesUseCase
import com.example.android.trackr.usecase.ReorderListUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.example.android.trackr.usecase.UpdateTaskStatusUseCase
import com.example.android.trackr.valueBlocking
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.threeten.bp.Instant

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class TasksViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun createViewModel(): TasksViewModel {
        val db = createDatabase()
        populate(db)
        val taskDao = db.taskDao()
        return TasksViewModel(
            GetOngoingTaskSummariesUseCase(taskDao),
            ArchiveUseCase(taskDao),
            ToggleTaskStarStateUseCase(db, taskDao),
            UpdateTaskStatusUseCase(taskDao),
            ReorderListUseCase(taskDao),
            user1
        )
    }

    @Test
    fun listItems() {
        val viewModel = createViewModel()
        viewModel.listItems.valueBlocking.let { listItems ->
            assertThat(listItems).hasSize(4)
            val task = listItems.find { it is ListItem.TypeTask } as ListItem.TypeTask
            assertThat(task.taskSummary.id).isEqualTo(1L)
        }
    }

    @Test
    fun toggleExpandedState() {
        val viewModel = createViewModel()
        viewModel.listItems.valueBlocking.let { listItems ->
            val header = listItems.find {
                it is ListItem.TypeHeader && it.headerData.taskStatus == TaskStatus.IN_PROGRESS
            } as ListItem.TypeHeader
            assertThat(header.headerData.count).isEqualTo(1L)
            assertThat(header.headerData.expanded).isTrue()
        }
        viewModel.toggleExpandedState(createHeaderItem(TaskStatus.IN_PROGRESS))
        viewModel.listItems.valueBlocking.let { listItems ->
            val header = listItems.find {
                it is ListItem.TypeHeader && it.headerData.taskStatus == TaskStatus.IN_PROGRESS
            } as ListItem.TypeHeader
            assertThat(header.headerData.expanded).isFalse()
        }
    }

    @Test
    fun archiveTask() {
        val viewModel = createViewModel()
        assertThat(viewModel.archivedItem.valueBlocking).isNull()
        viewModel.archiveTask(createTaskSummary(1L))
        assertThat(viewModel.archivedItem.valueBlocking).isNotNull()
        viewModel.unarchiveTask()
        assertThat(viewModel.archivedItem.valueBlocking).isNull()
    }

    private fun createHeaderItem(status: TaskStatus): HeaderData {
        return HeaderData(0, status, true)
    }

    private fun createTaskSummary(id: Long): TaskSummary {
        return TaskSummary(
            id,
            "",
            TaskStatus.NOT_STARTED,
            Instant.parse("2020-09-01T00:00:00.00Z"),
            1,
            user1,
            emptyList(),
            emptyList()
        )
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
