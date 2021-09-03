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

import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Convenience for the pattern of launching coroutines when the View lifecycle is in a certain
 * minimum state and canceling those coroutines when outside of that minimum state. Example:
 *
 * ```
 * repeatWithViewLifecycle {
 *   launch {
 *     viewModel.flowA.collect { ... }
 *   }
 *   launch {
 *     viewModel.flowB.collect { ... }
 *   }
 * }
 * ```
 *
 * @param minState The minimum lifecycle state. Defaults to [Lifecycle.State.STARTED].
 * @param block The code block to run.
 */
inline fun Fragment.repeatWithViewLifecycle(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    if (minState == Lifecycle.State.INITIALIZED || minState == Lifecycle.State.DESTROYED) {
        throw IllegalArgumentException("minState must be between INITIALIZED and DESTROYED")
    }
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(minState) {
            block()
        }
    }
}

fun Fragment.requireFragment(id: Int): Fragment {
    return childFragmentManager.findFragmentById(id) ?: throw IllegalArgumentException()
}

inline val View.isRtl: Boolean
    get() = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
