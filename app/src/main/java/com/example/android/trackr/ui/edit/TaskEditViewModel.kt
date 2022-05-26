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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TaskDetail
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.User
import com.example.android.trackr.usecase.LoadTagsUseCase
import com.example.android.trackr.usecase.LoadTaskDetailUseCase
import com.example.android.trackr.usecase.LoadUsersUseCase
import com.example.android.trackr.usecase.SaveTaskDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    private val loadTaskDetailUseCase: LoadTaskDetailUseCase,
    private val loadUsersUseCase: LoadUsersUseCase,
    private val loadTagsUseCase: LoadTagsUseCase,
    private val saveTaskDetailUseCase: SaveTaskDetailUseCase,
    private val currentUser: User
) : ViewModel() {

    var taskId: Long = 0L
        set(value) {
            field = value
            loadInitialData(value)
        }

    val title = MutableStateFlow("")
    private var initialTitle = ""

    val description = MutableStateFlow("")
    private var initialDescription = ""

    private val _status = MutableStateFlow(TaskStatus.NOT_STARTED)
    val status: StateFlow<TaskStatus> = _status

    private val _owner = MutableStateFlow(currentUser)
    val owner: StateFlow<User> = _owner

    private val _creator = MutableStateFlow(currentUser)
    val creator: StateFlow<User> = _creator

    private val _dueAt = MutableStateFlow(Instant.now() + Duration.ofDays(7))
    val dueAt: StateFlow<Instant> = _dueAt

    private val _createdAt = MutableStateFlow(Instant.now())
    val createdAt: StateFlow<Instant> = _createdAt

    private var orderInCategory = 0

    // Whether this task should be placed on top of the status when it is saved.
    // Creating a new task puts it at the top of the status.
    private var topInCategory = true

    // This is not editable, but needed for when we save.
    private var isArchived = false

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags

    private var starUsers = mutableListOf<User>()

    lateinit var users: List<User>
        private set

    lateinit var allTags: List<Tag>
        private set

    private val _discarded = Channel<Unit>(capacity = Channel.CONFLATED)
    val discarded = _discarded.receiveAsFlow()

    /**
     * Whether any of the content is modified or not.
     */
    private val _modified = MutableStateFlow(false)
    val modified: StateFlow<Boolean> = _modified

    private var modifiedTitleDescriptionJob: Job = viewModelScope.launch {
        combine(title, description) { title, description ->
            title != initialTitle || description != initialDescription
        }.collect { modified ->
            // Other fields affect modification, so we never go from true to false.
            if (modified) {
                _modified.value = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        modifiedTitleDescriptionJob.cancel()
    }

    private fun loadInitialData(taskId: Long) {
        viewModelScope.launch {
            users = loadUsersUseCase()
            allTags = loadTagsUseCase()
            if (taskId != 0L) {
                val detail = loadTaskDetailUseCase(taskId)
                if (detail != null) {
                    initialTitle = detail.title
                    initialDescription = detail.description

                    title.value = initialTitle
                    description.value = initialDescription
                    _status.value = detail.status
                    _owner.value = detail.owner
                    _creator.value = detail.creator
                    _dueAt.value = detail.dueAt
                    _createdAt.value = detail.createdAt
                    orderInCategory = detail.orderInCategory
                    isArchived = detail.isArchived
                    topInCategory = false
                    _tags.value = detail.tags
                    starUsers.clear()
                    starUsers.addAll(detail.starUsers)
                    _modified.value = false
                }
            }
        }
    }

    fun updateState(status: TaskStatus) {
        if (_status.value != status) {
            _status.value = status
            _modified.value = true
            // The task is placed at the top of the target status.
            topInCategory = true
        }
    }

    fun updateOwner(user: User) {
        _owner.value = user
        _modified.value = true
    }

    fun updateDueAt(instant: Instant) {
        _dueAt.value = instant
        _modified.value = true
    }

    fun addTag(tag: Tag) {
        _tags.value.let { currentTags ->
            if (!currentTags.contains(tag)) {
                _tags.value = currentTags + tag
                _modified.value = true
            }
        }
    }

    fun removeTag(tag: Tag) {
        _tags.value.let { currentTags ->
            if (currentTags.contains(tag)) {
                val tags = currentTags.toMutableList()
                tags.remove(tag)
                _tags.value = tags
                _modified.value = true
            }
        }
    }

    fun save(onSaveFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                saveTaskDetailUseCase(
                    TaskDetail(
                        id = taskId,
                        title = title.value,
                        description = description.value,
                        status = _status.value,
                        createdAt = _createdAt.value,
                        dueAt = _dueAt.value,
                        orderInCategory = orderInCategory,
                        isArchived = isArchived,
                        owner = _owner.value,
                        creator = _creator.value,
                        tags = _tags.value,
                        starUsers = starUsers
                    ),
                    topInCategory
                )
                onSaveFinished(true)
            } catch (e: RuntimeException) {
                e.printStackTrace()
                onSaveFinished(false)
            }
        }
    }

    fun discardChanges() {
        _discarded.trySend(Unit)
    }
}
