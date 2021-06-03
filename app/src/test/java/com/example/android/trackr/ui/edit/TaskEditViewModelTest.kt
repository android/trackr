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
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.ui.TAG_1
import com.example.android.trackr.ui.TAG_2
import com.example.android.trackr.ui.TASK_1
import com.example.android.trackr.ui.USER_CREATOR
import com.example.android.trackr.ui.USER_OTHER
import com.example.android.trackr.ui.USER_OWNER
import com.example.android.trackr.ui.createDatabase
import com.example.android.trackr.usecase.LoadTagsUseCase
import com.example.android.trackr.usecase.LoadTaskDetailUseCase
import com.example.android.trackr.usecase.LoadUsersUseCase
import com.example.android.trackr.usecase.SaveTaskDetailUseCase
import com.example.android.trackr.valueBlocking
import com.google.common.truth.Truth.assertThat
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
        val taskDao = db.taskDao()
        return TaskEditViewModel(
            LoadTaskDetailUseCase(taskDao),
            LoadUsersUseCase(taskDao),
            LoadTagsUseCase(taskDao),
            SaveTaskDetailUseCase(taskDao),
            currentUser
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
        assertThat(viewModel.owner.valueBlocking).isEqualTo(currentUser)
        assertThat(viewModel.creator.valueBlocking).isEqualTo(currentUser)
        assertThat(viewModel.tags.valueBlocking).isEmpty()

        viewModel.title.value = "a"

        assertThat(viewModel.title.valueBlocking).isEqualTo("a")
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun editExistingTask() {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.taskId).isEqualTo(TASK_1.id)
        assertThat(viewModel.modified.valueBlocking).isFalse()
        assertThat(viewModel.title.valueBlocking).isEqualTo(TASK_1.title)
        assertThat(viewModel.description.valueBlocking).isEqualTo(TASK_1.description)
        assertThat(viewModel.owner.valueBlocking).isEqualTo(USER_OWNER)
        assertThat(viewModel.creator.valueBlocking).isEqualTo(USER_CREATOR)
        assertThat(viewModel.dueAt.valueBlocking).isEqualTo(TASK_1.dueAt)
        assertThat(viewModel.createdAt.valueBlocking).isEqualTo(TASK_1.createdAt)
        assertThat(viewModel.users).hasSize(3)

        viewModel.title.value = "a"

        assertThat(viewModel.title.valueBlocking).isEqualTo("a")
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun editOwner() {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.owner.valueBlocking).isEqualTo(USER_OWNER)
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.updateOwner(USER_OTHER)

        assertThat(viewModel.owner.valueBlocking).isEqualTo(USER_OTHER)
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun editStatus() {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.status.valueBlocking).isEqualTo(TaskStatus.IN_PROGRESS)
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.updateState(TaskStatus.COMPLETED)

        assertThat(viewModel.status.valueBlocking).isEqualTo(TaskStatus.COMPLETED)
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun editDueAt() {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.dueAt.valueBlocking).isEqualTo(TASK_1.dueAt)
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.updateDueAt(Instant.parse("2020-12-01T00:00:00.00Z"))

        assertThat(viewModel.dueAt.valueBlocking)
            .isEqualTo(Instant.parse("2020-12-01T00:00:00.00Z"))
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun addTag() {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.tags.valueBlocking).containsExactly(TAG_1)
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.addTag(TAG_2)

        assertThat(viewModel.tags.valueBlocking).containsExactly(TAG_1, TAG_2)
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun removeTag() {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.tags.valueBlocking).containsExactly(TAG_1)
        assertThat(viewModel.modified.valueBlocking).isFalse()

        viewModel.removeTag(TAG_1)

        assertThat(viewModel.tags.valueBlocking).isEmpty()
        assertThat(viewModel.modified.valueBlocking).isTrue()
    }

    @Test
    fun discardChanges() {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.discarded.valueBlocking).isFalse()
        viewModel.discardChanges()
        assertThat(viewModel.discarded.valueBlocking).isTrue()
    }

    private val currentUser = USER_OWNER
}
