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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.ui.TASK_1
import com.example.android.trackr.ui.USER_OWNER
import com.example.android.trackr.ui.createDatabase
import com.example.android.trackr.usecase.ArchiveUseCase
import com.example.android.trackr.usecase.GetOngoingTaskSummariesUseCase
import com.example.android.trackr.usecase.ReorderListUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import com.example.android.trackr.usecase.UpdateTaskStatusUseCase
import com.example.android.trackr.valueBlocking
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Instant

@RunWith(AndroidJUnit4::class)
class TasksViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun createViewModel(): TasksViewModel {
        val db = createDatabase()
        val taskDao = db.taskDao()
        return TasksViewModel(
            GetOngoingTaskSummariesUseCase(taskDao),
            ArchiveUseCase(taskDao),
            ToggleTaskStarStateUseCase(db, taskDao),
            UpdateTaskStatusUseCase(taskDao),
            ReorderListUseCase(taskDao),
            currentUser
        )
    }

    @Test
    fun listItems() {
        val viewModel = createViewModel()
        viewModel.listItems.valueBlocking.let { listItems ->
            assertThat(listItems).hasSize(4)
            val task = listItems.find { it is ListItem.TypeTask } as ListItem.TypeTask
            assertThat(task.taskSummary.id).isEqualTo(TASK_1.id)
        }
    }

    @Test
    fun toggleExpandedState() {
        val viewModel = createViewModel()
        viewModel.listItems.valueBlocking.let { listItems ->
            val header = listItems.find {
                it is ListItem.TypeHeader && it.headerData.taskStatus == TaskStatus.IN_PROGRESS
            } as ListItem.TypeHeader
            assertThat(header.headerData.count).isEqualTo(1)
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
        viewModel.archiveTask(createTaskSummary(TASK_1.id))
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
            currentUser,
            emptyList(),
            emptyList()
        )
    }

    private val currentUser = USER_OWNER
}
