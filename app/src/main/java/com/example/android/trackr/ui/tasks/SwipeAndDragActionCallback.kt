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

package com.example.android.trackr.ui.tasks

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Handles swipe actions. A [RecyclerView.ViewHolder] interested in processing swipe gestures should
 * implement the [SwipeActionListener] and define appropriate action to be performed on swipe.
 */
class SwipeActionCallback :
    ItemTouchHelper.SimpleCallback(
        NO_DRAG,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {

    // TODO (b/165135681): make swipe to archive available via a custom accessibility action.
    // TODO (b/165431117): consider replacing with lambda that can be passed to a constructor.
    interface SwipeActionListener {
        fun onSwipe()
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Disable swiping unless the viewHolder explicitly supports it.
        if (viewHolder is SwipeActionListener) {
            return super.getSwipeDirs(recyclerView, viewHolder)
        }
        return NO_SWIPE
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // Not tracking if item has been moved to a target adapter position.
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is SwipeActionListener) {
            (viewHolder as SwipeActionListener).onSwipe()
        }
    }

    companion object {
        const val NO_DRAG = 0
        const val NO_SWIPE = 0
    }
}