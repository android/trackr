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

package com.example.android.trackr.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class TwoPaneViewModel @Inject constructor() : ViewModel() {

    val isTwoPane = MutableStateFlow(false)

    private val detailPaneUpEventChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    val detailPaneUpEvents = detailPaneUpEventChannel.receiveAsFlow()

    private val editTaskEventChannel = Channel<Long>(capacity = Channel.CONFLATED)
    val editTaskEvents = editTaskEventChannel.receiveAsFlow()

    fun onDetailPaneNavigateUp() {
        detailPaneUpEventChannel.trySend(Unit)
    }

    fun onEditTask(taskId: Long) {
        editTaskEventChannel.trySend(taskId)
    }
}
