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

package com.example.android.trackr.ui.archives

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.db.dao.TaskDao

class ArchiveViewModel @ViewModelInject constructor(
    taskDao: TaskDao
) : ViewModel() {

    private val archivedTaskListItems = taskDao.getArchivedTaskListItems()
    private val selectedTaskIds = MutableLiveData(emptySet<Long>())

    val archivedTasks = MediatorLiveData<List<ArchivedTask>>().apply {
        fun update() {
            val selected = selectedTaskIds.value ?: return
            val tasks = archivedTaskListItems.value ?: return
            value = tasks.map { task ->
                ArchivedTask(task, task.id in selected)
            }
        }
        addSource(archivedTaskListItems) { update() }
        addSource(selectedTaskIds) { update() }
    }

    fun toggleTaskSelection(taskId: Long) {
        val selected = selectedTaskIds.value ?: emptySet()
        if (taskId in selected) {
            selectedTaskIds.value = selected - taskId
        } else {
            selectedTaskIds.value = selected + taskId
        }
    }
}

data class ArchivedTask(
    val taskListItem: TaskListItem,
    val selected: Boolean
)
