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

package com.example.android.trackr.ui.archive

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.trackr.TestApplication
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.Task
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.data.User
import com.example.android.trackr.db.AppDatabase
import com.example.android.trackr.db.dao.valueBlocking
import com.example.android.trackr.ui.archives.ArchiveViewModel
import com.example.android.trackr.ui.createDatabase
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
class ArchiveViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun select() {
        val db = createDatabase()
        populate(db)
        val viewModel = ArchiveViewModel(db.taskDao())
        viewModel.archivedTasks.valueBlocking.let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isFalse()
            assertThat(tasks[1].selected).isFalse()
        }
        viewModel.toggleTaskSelection(1L)
        viewModel.archivedTasks.valueBlocking.let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isTrue()
            assertThat(tasks[1].selected).isFalse()
        }
        viewModel.toggleTaskSelection(1L)
        viewModel.archivedTasks.valueBlocking.let { tasks ->
            assertThat(tasks).hasSize(2)
            assertThat(tasks[0].selected).isFalse()
            assertThat(tasks[1].selected).isFalse()
        }
    }

    private fun populate(db: AppDatabase) {
        runBlocking {
            with(db.taskDao()) {
                insertUsers(
                    listOf(
                        User(1L, "owner", Avatar.DEFAULT_USER),
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
                            state = TaskState.ARCHIVED,
                            creatorId = 2L,
                            ownerId = 1L,
                            createdAt = Instant.parse("2020-09-01T00:00:00.00Z"),
                            dueAt = Instant.parse("2020-11-01T00:00:00.00Z")
                        ),
                        Task(
                            id = 2L,
                            title = "title",
                            description = "description",
                            state = TaskState.ARCHIVED,
                            creatorId = 2L,
                            ownerId = 1L,
                            createdAt = Instant.parse("2020-09-01T00:00:00.00Z"),
                            dueAt = Instant.parse("2020-11-01T00:00:00.00Z")
                        )
                    )
                )
            }
        }
    }
}

