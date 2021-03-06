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

package com.example.android.trackr.ui.detail

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.User
import com.example.android.trackr.repository.TrackrRepository
import kotlinx.coroutines.launch

class TaskDetailViewModel @ViewModelInject constructor(
    private val repository: TrackrRepository,
    private val currentUser: User
) : ViewModel() {

    private val _taskId = MutableLiveData<Long>()
    var taskId: Long?
        get() = _taskId.value
        set(value) {
            _taskId.value = value!!
        }

    val detail = _taskId.switchMap { id -> repository.getTaskDetailById(id) }

    val starred = detail.map { detail ->
        detail?.starUsers?.contains(currentUser) ?: false
    }

    fun toggleTaskStarState() {
        _taskId.value?.let { taskId ->
            viewModelScope.launch {
                repository.toggleTaskStarState(taskId)
            }
        }
    }
}
