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
import com.example.android.trackr.MainCoroutineRule
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
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TaskEditViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

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
    fun createNewTask() = runTest {
        val viewModel = createViewModel()
        viewModel.taskId = 0L

        assertThat(viewModel.taskId).isEqualTo(0L)
        assertThat(viewModel.modified.value).isFalse()
        assertThat(viewModel.title.value).isEmpty()
        assertThat(viewModel.description.value).isEmpty()
        assertThat(viewModel.owner.value).isEqualTo(currentUser)
        assertThat(viewModel.creator.value).isEqualTo(currentUser)
        assertThat(viewModel.tags.value).isEmpty()

        viewModel.title.value = "a"

        assertThat(viewModel.title.value).isEqualTo("a")
        assertThat(viewModel.modified.value).isTrue()
    }

    @Test
    fun editExistingTask() = runTest {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.taskId).isEqualTo(TASK_1.id)
        assertThat(viewModel.modified.value).isFalse()
        assertThat(viewModel.title.value).isEqualTo(TASK_1.title)
        assertThat(viewModel.description.value).isEqualTo(TASK_1.description)
        assertThat(viewModel.owner.value).isEqualTo(USER_OWNER)
        assertThat(viewModel.creator.value).isEqualTo(USER_CREATOR)
        assertThat(viewModel.dueAt.value).isEqualTo(TASK_1.dueAt)
        assertThat(viewModel.createdAt.value).isEqualTo(TASK_1.createdAt)
        assertThat(viewModel.users).hasSize(3)

        viewModel.title.value = "a"

        assertThat(viewModel.title.value).isEqualTo("a")
        assertThat(viewModel.modified.value).isTrue()
    }

    @Test
    fun editOwner() = runTest {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.owner.value).isEqualTo(USER_OWNER)
        assertThat(viewModel.modified.value).isFalse()

        viewModel.updateOwner(USER_OTHER)

        assertThat(viewModel.owner.value).isEqualTo(USER_OTHER)
        assertThat(viewModel.modified.value).isTrue()
    }

    @Test
    fun editStatus() = runTest {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.status.value).isEqualTo(TaskStatus.IN_PROGRESS)
        assertThat(viewModel.modified.value).isFalse()

        viewModel.updateState(TaskStatus.COMPLETED)

        assertThat(viewModel.status.value).isEqualTo(TaskStatus.COMPLETED)
        assertThat(viewModel.modified.value).isTrue()
    }

    @Test
    fun editDueAt() = runTest {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.dueAt.value).isEqualTo(TASK_1.dueAt)
        assertThat(viewModel.modified.value).isFalse()

        viewModel.updateDueAt(Instant.parse("2020-12-01T00:00:00.00Z"))

        assertThat(viewModel.dueAt.value).isEqualTo(Instant.parse("2020-12-01T00:00:00.00Z"))
        assertThat(viewModel.modified.value).isTrue()
    }

    @Test
    fun addTag() = runTest {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.tags.value).containsExactly(TAG_1)
        assertThat(viewModel.modified.value).isFalse()

        viewModel.addTag(TAG_2)

        assertThat(viewModel.tags.value).containsExactly(TAG_1, TAG_2)
        assertThat(viewModel.modified.value).isTrue()
    }

    @Test
    fun removeTag() = runTest {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        assertThat(viewModel.tags.value).containsExactly(TAG_1)
        assertThat(viewModel.modified.value).isFalse()

        viewModel.removeTag(TAG_1)

        assertThat(viewModel.tags.value).isEmpty()
        assertThat(viewModel.modified.value).isTrue()
    }

    @Test
    fun discardChanges() = runTest {
        val viewModel = createViewModel()
        viewModel.taskId = TASK_1.id

        var discardEventCount = 0
        val collectDiscardEvents = launch(UnconfinedTestDispatcher()) {
            viewModel.discarded.collect {
                discardEventCount++
            }
        }

        assertThat(discardEventCount).isEqualTo(0)
        viewModel.discardChanges()
        assertThat(discardEventCount).isEqualTo(1)

        collectDiscardEvents.cancel()
    }

    private val currentUser = USER_OWNER
}
