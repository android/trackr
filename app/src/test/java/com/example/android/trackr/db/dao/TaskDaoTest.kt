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

package com.example.android.trackr.db.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.data.SeedData
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.example.android.trackr.data.Task
import com.example.android.trackr.data.TaskDetail
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.data.TaskTag
import com.example.android.trackr.data.User
import com.example.android.trackr.data.UserTask
import com.example.android.trackr.db.AppDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Instant
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var appDatabase: AppDatabase

    private lateinit var taskDao: TaskDao

    @Before
    @Throws(Exception::class)
    fun initDb() {
        appDatabase = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
            )
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .allowMainThreadQueries()
            .build()

        taskDao = appDatabase.taskDao()
    }

    @After
    @Throws(java.lang.Exception::class)
    fun closeDb() {
        appDatabase.close()
    }

    @Test
    @Throws(InterruptedException::class)
    fun getTasks_WhenNoTaskInserted() {
        val tasks: List<Task> = taskDao.getTasks().valueBlocking
        assertThat(tasks).isEmpty()
    }

    @Test
    @Throws(InterruptedException::class)
    fun getTasks_WhenTasksInserted() = runBlocking {
        taskDao.insertUsers(SeedData.Users)
        taskDao.insertTags(SeedData.Tags)
        taskDao.insertTasks(SeedData.Tasks)
        val tasks = taskDao.getTasks().valueBlocking
        assertThat(tasks).hasSize(SeedData.Tasks.size)
    }

    @Test
    fun getTaskDetailById() = runBlocking {
        taskDao.insertUsers(SeedData.Users)
        taskDao.insertTags(SeedData.Tags)
        taskDao.insertTasks(SeedData.Tasks)
        taskDao.insertTaskTags(SeedData.TaskTags)
        taskDao.insertUserTasks(SeedData.UserTasks)
        taskDao.getTaskDetailById(1L).valueBlocking!!.let { detail ->
            assertThat(detail.id).isEqualTo(1L)
            assertThat(detail.title).isEqualTo("Task 1")
            assertThat(detail.owner.username).isEqualTo("You")
            assertThat(detail.reporter.username).isEqualTo("John")
            assertThat(detail.tags).hasSize(2)
            assertThat(detail.tags[0].label).isEqualTo("Home")
            assertThat(detail.tags[1].label).isEqualTo("Hobby")
            assertThat(detail.starUsers).hasSize(1)
            assertThat(detail.starUsers[0].username).isEqualTo("You")
        }
    }

    @Test
    fun getTaskListItem() {
        val users = listOf(user1)
        val tasks = listOf(task1)
        val tags = listOf(tag1, tag2)
        val taskTags = listOf(taskTag1, taskTag2)
        val userTasks = listOf(userTask1)

        insertData(taskDao, users, tasks, tags, taskTags, userTasks)

        taskDao.getOngoingTaskListItems().valueBlocking.let { taskListItems ->
            val item = taskListItems[0]
            assertThat(item.id).isEqualTo(tasks[0].id)
            assertThat(item.title).isEqualTo(tasks[0].title)
            assertThat(item.owner).isEqualTo(users[0])
            assertThat(item.dueAt).isEqualTo(tasks[0].dueAt)
            assertThat(item.state).isEqualTo(tasks[0].state)
            assertThat(item.tags).hasSize(tags.size)
            assertThat(item.starUsers).hasSize(userTasks.size)
        }
    }

    @Test
    fun getOngoingTaskListItems() {
        val users = listOf(user1)
        val tasks = listOf(task1, task2)
        val tags = listOf(tag1, tag2)
        val taskTags = listOf(taskTag1, taskTag2)
        val userTasks = listOf(userTask1)

        insertData(taskDao, users, tasks, tags, taskTags, userTasks)
        assertThat(taskDao.getTasks().valueBlocking).hasSize(2)
        assertThat(taskDao.getOngoingTaskListItems().valueBlocking).hasSize(1)
    }

    @Test
    fun updateTaskState_persistsState() = runBlocking {
        insertData(taskDao, listOf(user1), listOf(task1))

        taskDao.getTasks().valueBlocking.let { tasks ->
            assertThat(tasks[0].state).isEqualTo(TaskState.NOT_STARTED)
        }

        taskDao.updateTaskState(task1.id, TaskState.ARCHIVED)

        taskDao.getTasks().valueBlocking.let { tasks ->
            assertThat(tasks[0].state).isEqualTo(TaskState.ARCHIVED)
        }
    }

    @Test
    fun loadUsers() = runBlocking {
        taskDao.insertUsers(SeedData.Users)
        val users = taskDao.loadUsers()
        assertThat(users).hasSize(2)
    }

    @Test
    fun loadTags() = runBlocking {
        taskDao.insertTags(SeedData.Tags)
        val tags = taskDao.loadTags()
        assertThat(tags).hasSize(3)
    }

    @Test
    fun saveTaskDetail_create() = runBlocking {
        insertData(
            taskDao = taskDao,
            users = listOf(user1, user2),
            tasks = emptyList(),
            tags = listOf(tag1, tag2, tag3),
            taskTags = emptyList()
        )

        val newDetail = TaskDetail(
            id = 1L,
            title = "title",
            description = "description",
            state = TaskState.IN_PROGRESS,
            createdAt = Instant.now(),
            dueAt = dueAt2,
            owner = user2,
            reporter = user1,
            tags = listOf(tag1, tag3),
            starUsers = emptyList()
        )
        taskDao.saveTaskDetail(newDetail)

        val updatedDetail = taskDao.loadTaskDetailById(1L)!!
        assertThat(updatedDetail.title).isEqualTo("title")
        assertThat(updatedDetail.description).isEqualTo("description")
        assertThat(updatedDetail.state).isEqualTo(TaskState.IN_PROGRESS)
        assertThat(updatedDetail.dueAt).isEqualTo(dueAt2)
        assertThat(updatedDetail.tags).containsExactly(tag1, tag3)
        assertThat(updatedDetail.reporter).isEqualTo(user1)
        assertThat(updatedDetail.owner).isEqualTo(user2)
    }

    @Test
    fun saveTaskDetail_update() = runBlocking {
        insertData(
            taskDao = taskDao,
            users = listOf(user1, user2),
            tasks = listOf(task1),
            tags = listOf(tag1, tag2, tag3),
            taskTags = listOf(taskTag1, taskTag2)
        )

        val initialDetail = taskDao.loadTaskDetailById(1L)!!
        assertThat(initialDetail.title).isEqualTo("Task 1")
        assertThat(initialDetail.description).isEmpty()
        assertThat(initialDetail.state).isEqualTo(TaskState.NOT_STARTED)
        assertThat(initialDetail.dueAt).isEqualTo(dueAt1)
        assertThat(initialDetail.tags).containsExactly(tag1, tag2)
        assertThat(initialDetail.owner).isEqualTo(user1)

        val newDetail = TaskDetail(
            id = 1L,
            title = "new title",
            description = "new description",
            state = TaskState.IN_PROGRESS,
            createdAt = initialDetail.createdAt, // The UI doesn't allow editing this.
            dueAt = dueAt2,
            owner = user2,
            reporter = initialDetail.reporter, // The UI doesn't allow editing this.
            tags = listOf(tag1, tag3),
            starUsers = emptyList()
        )
        taskDao.saveTaskDetail(newDetail)

        val updatedDetail = taskDao.loadTaskDetailById(1L)!!
        assertThat(updatedDetail.title).isEqualTo("new title")
        assertThat(updatedDetail.description).isEqualTo("new description")
        assertThat(updatedDetail.state).isEqualTo(TaskState.IN_PROGRESS)
        assertThat(updatedDetail.dueAt).isEqualTo(dueAt2)
        assertThat(updatedDetail.tags).containsExactly(tag1, tag3)
        assertThat(updatedDetail.owner).isEqualTo(user2)
    }

    companion object {
        fun insertData(
            taskDao: TaskDao,
            users: List<User>,
            tasks: List<Task> = emptyList(),
            tags: List<Tag> = emptyList(),
            taskTags: List<TaskTag> = emptyList(),
            userTasks: List<UserTask> = emptyList()
        ) = runBlocking {
            taskDao.insertUsers(users)
            taskDao.insertTasks(tasks)
            taskDao.insertTags(tags)
            taskDao.insertTaskTags(taskTags)
            taskDao.insertUserTasks(userTasks)
        }

        val dueAt1: Instant = Instant.parse("2020-12-01T00:00:00.00Z")
        val dueAt2: Instant = Instant.parse("2020-12-11T00:00:00.00Z")

        val user1 = User(id = 1L, username = "user1")
        val user2 = User(id = 2L, username = "user2")

        val task1 = Task(id = 1L, title = "Task 1", ownerId = 1L, reporterId = 1L, dueAt = dueAt1)
        val task2 = Task(
            id = 2L, title = "Task 2", state = TaskState.ARCHIVED, ownerId = 1L, reporterId = 1L
        )

        val tag1 = Tag(id = 1L, label = "tag1", color = TagColor.RED)
        val tag2 = Tag(id = 2L, label = "tag2", color = TagColor.PURPLE)
        val tag3 = Tag(id = 3L, label = "tag3", color = TagColor.BLUE)

        val taskTag1 = TaskTag(taskId = 1L, tagId = 1L)
        val taskTag2 = TaskTag(taskId = 1L, tagId = 2L)

        val userTask1 = UserTask(userId = 1L, taskId = 1L)
    }
}