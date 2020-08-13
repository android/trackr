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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.android.trackr.data.*

@Dao
interface TaskDao {

    @Insert
    suspend fun insertUsers(users: List<User>)

    @Insert
    suspend fun insertTags(tags: List<Tag>)

    @Insert
    suspend fun insertTasks(tasks: List<Task>)

    @Insert
    suspend fun insertTaskTags(taskTags: List<TaskTag>)

    @Insert
    suspend fun insertUserTasks(userTasks: List<UserTask>)

    @Query("SELECT * FROM tasks")
    fun getTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM TaskDetail WHERE id = :id")
    fun getTaskDetailById(id: Long): LiveData<TaskDetail?>

    @Transaction
    @Query("SELECT * FROM TaskListItem")
    fun  getTaskListItems(): LiveData<List<TaskListItem>>
}