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

package com.example.android.trackr.repository

import androidx.room.withTransaction
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.User
import com.example.android.trackr.data.UserTask
import com.example.android.trackr.db.AppDatabase

class TrackrRepository(
    private val db: AppDatabase,
    private val currentUser: User
) {

    private val taskDao = db.taskDao()

    fun getTaskDetailById(id: Long) = taskDao.getTaskDetailById(id)

    fun getArchivedTaskSummaries() = taskDao.getArchivedTaskSummaries()

    /**
     * Toggles the star state for the task.
     */
    suspend fun toggleTaskStarState(taskId: Long) {
        db.withTransaction {
            val userTask = taskDao.getUserTask(taskId, currentUser.id)
            if (userTask != null) {
                taskDao.deleteUserTasks(listOf(userTask))
            } else {
                taskDao.insertUserTasks(listOf(UserTask(userId = currentUser.id, taskId = taskId)))
            }
        }
    }

    /**
     * Archive tasks.
     */
    suspend fun archive(taskIds: List<Long>) {
        taskDao.updateTaskStatus(taskIds, TaskStatus.ARCHIVED)
    }

    /**
     * Unarchive tasks. This sets the state to [TaskStatus.NOT_STARTED] for all the tasks.
     */
    suspend fun unarchive(taskIds: List<Long>) {
        taskDao.updateTaskStatus(taskIds, TaskStatus.NOT_STARTED)
    }
}
