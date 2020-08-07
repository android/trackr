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
import com.example.android.trackr.data.Task
import com.example.android.trackr.db.AppDatabase
import com.example.android.trackr.valueBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var appDatabase: AppDatabase

    private lateinit var taskDao: TaskDao

    @Before
    @Throws(Exception::class)
    fun initDb() {
        appDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
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
        assertTrue(tasks.isEmpty())
    }

    @Test
    @Throws(InterruptedException::class)
    fun getTasks_WhenTasksInserted() {
        taskDao.insertAll(SeedData.Tasks)
        val tasks = taskDao.getTasks().valueBlocking
        Assert.assertThat(tasks.size, `is`(SeedData.Tasks.size))
    }
}