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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.User
import com.example.android.trackr.ui.utils.WhileViewSubscribed
import com.example.android.trackr.usecase.FindTaskDetailUseCase
import com.example.android.trackr.usecase.ToggleTaskStarStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val findTaskDetailUseCase: FindTaskDetailUseCase,
    private val toggleTaskStarStateUseCase: ToggleTaskStarStateUseCase,
    private val currentUser: User
) : ViewModel() {

    val taskId = MutableStateFlow(0L)

    val detail = taskId.transformLatest { id ->
        emitAll(findTaskDetailUseCase(id))
    }.stateIn(viewModelScope, WhileViewSubscribed, null)

    val starred = detail.mapLatest { detail ->
        detail?.starUsers?.contains(currentUser) ?: false
    }.stateIn(viewModelScope, WhileViewSubscribed, false)

    fun toggleTaskStarState() {
        val id = taskId.value
        if (id <= 0L) return
        viewModelScope.launch {
            toggleTaskStarStateUseCase(id, currentUser)
        }
    }
}
