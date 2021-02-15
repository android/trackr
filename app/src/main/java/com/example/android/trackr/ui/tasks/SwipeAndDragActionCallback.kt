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
import com.example.android.trackr.ui.tasks.SwipeAndDragCallback.ItemTouchListener

/**
 * Handles swipe actions. A [RecyclerView.ViewHolder] interested in processing swipe gestures should
 * implement the [ItemTouchListener] and define appropriate action to be performed on swipe.
 */
class SwipeAndDragCallback :
    ItemTouchHelper.SimpleCallback(
        NO_DRAG,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {

    // TODO (b/165431117): consider replacing with lambda that can be passed to a constructor.
    interface ItemTouchListener {
        fun onItemSwiped()
        fun onItemMoved(fromPosition: Int, toPosition: Int)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        var dragFlags = NO_DRAG
        var swipeFlags = NO_SWIPE
        if (viewHolder is ItemTouchListener) {
            dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        }
        return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Disable swiping unless the viewHolder explicitly supports it.
        if (viewHolder is ItemTouchListener) {
            return super.getSwipeDirs(recyclerView, viewHolder)
        }
        return NO_SWIPE
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true // We track if the item has been moved to a target adapter position
    }

    override fun onMoved(
        // Note: this is called when onMove returns true
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        if (viewHolder is ItemTouchListener) {
            (viewHolder as ItemTouchListener).onItemMoved(fromPos, toPos)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is ItemTouchListener) {
            (viewHolder as ItemTouchListener).onItemSwiped()
        }
    }

    companion object {
        const val NO_DRAG = 0
        const val NO_SWIPE = 0
    }
}