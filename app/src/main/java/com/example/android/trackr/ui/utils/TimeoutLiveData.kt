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

package com.example.android.trackr.ui.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Creates a copy of this [LiveData], but its value is automatically set to the [defaultValue]
 * in [timeoutMillis] milliseconds after every value change.
 */
fun <T> LiveData<T>.timeout(
    scope: CoroutineScope,
    timeoutMillis: Long,
    defaultValue: T
): LiveData<T> {
    return TimeoutLiveData(this, scope, timeoutMillis, defaultValue)
}

private class TimeoutLiveData<T>(
    source: LiveData<T>,
    scope: CoroutineScope,
    timeoutMillis: Long,
    defaultValue: T
) : MediatorLiveData<T>() {

    init {
        var previousJob: Job? = null
        addSource(source) { value ->
            previousJob = scope.launch {
                // Cancel it if there's already a timeout set up.
                previousJob?.cancel()
                previousJob = null
                // Set the source value, wait, and set the default value.
                this@TimeoutLiveData.value = value
                delay(timeoutMillis)
                this@TimeoutLiveData.value = defaultValue
            }
        }
    }
}
