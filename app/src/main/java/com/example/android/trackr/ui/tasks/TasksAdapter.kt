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

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.R
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.data.User
import com.example.android.trackr.databinding.ListHeaderBinding
import com.example.android.trackr.databinding.ListTaskBinding
import org.threeten.bp.Clock
import java.util.Collections


class TasksAdapter(
    private val itemListener: ItemListener,
    private val currentUser: User,
    private val clock: Clock
) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(
        DataItemDiffCallback()
    ) {

    interface ItemListener {
        fun onHeaderClicked(headerData: HeaderData)
        fun onStarClicked(taskListItem: TaskListItem)
        fun onTaskClicked(taskListItem: TaskListItem)
        fun onTaskArchived(taskListItem: TaskListItem)
        fun onTaskDragged(fromPosition: Int, toPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent, itemListener)
            ITEM_VIEW_TYPE_TASK -> TaskViewHolder.from(parent, itemListener, currentUser, clock)
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is DataItem.TaskItem -> ITEM_VIEW_TYPE_TASK
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskViewHolder -> {
                holder.bind((getItem(position) as DataItem.TaskItem).taskListItem)
            }

            is HeaderViewHolder -> {
                holder.bind((getItem(position) as DataItem.HeaderItem).headerData)
            }
        }
    }

    /**
     * Find a header item at the specified [position] or before.
     */
    fun findHeaderItem(position: Int): DataItem.HeaderItem {
        var p = position
        while (p > 0) {
            val item = getItem(p)
            if (item is DataItem.HeaderItem) return item
            p--
        }
        return getItem(0) as DataItem.HeaderItem
    }

    fun changeTaskPosition(fromPosition: Int, toPosition: Int) {
        // TODO: persist new order in the db instead of calling submitList()
        val newList = currentList.toMutableList()
        Collections.swap(newList, fromPosition, toPosition)
        submitList(newList)
    }

    class HeaderViewHolder private constructor(
        val binding: ListHeaderBinding,
        private val itemListener: ItemListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(headerData: HeaderData) {
            binding.headerData = headerData
            binding.listener = itemListener
            ViewCompat.setStateDescription(
                binding.root,
                headerData.stateDescription(binding.root.context)
            )
            ViewCompat.setAccessibilityHeading(binding.root, true)
        }

        companion object {
            fun from(parent: ViewGroup, itemListener: ItemListener): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return HeaderViewHolder(
                    ListHeaderBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ), itemListener
                )
            }
        }
    }

    class TaskViewHolder private constructor(
        val binding: ListTaskBinding,
        private val itemListener: ItemListener,
        private val currentUser: User,
        private val clock: Clock
    ) :
        RecyclerView.ViewHolder(binding.root), SwipeAndDragCallback.ItemTouchListener {

        val accessibilityActionIds = arrayListOf<Int>()

        fun bind(taskListItem: TaskListItem) {
            val resources = binding.root.resources
            binding.taskListItem = taskListItem
            binding.listener = itemListener
            binding.clock = clock
            binding.currentUser = currentUser
            ViewCompat.setStateDescription(
                binding.root,
                resources.getString(
                    if (taskListItem.starUsers.isEmpty()) R.string.unstarred else R.string.starred
                )
            )

            // Replaces the label for the click action associated with the root view. The custom
            // label is then passed on to the user of an accessibility service (for instance, this
            // replaces Talkback's generic "double tap to activate" announcement with the more
            // descriptive "double tap to explore details" action label).
            // TODO(b/178437838): write UIAutomation test to confirm the custom action label.
            ViewCompat.replaceAccessibilityAction(
                binding.root,
                AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK,
                resources.getString(R.string.explore_details),
                null
            )

            // Clear previously added actions. If this is skipped, the actions may be duplicated
            // when a view is rebound.
            removeAccessibilityActions(binding.root)

            addArchiveAccessibilityAction(taskListItem)
            addStarAccessibilityAction(taskListItem)

            binding.executePendingBindings()
        }

        override fun onItemSwiped() {
            binding.taskListItem?.let {
                binding.listener?.onTaskArchived(it)
            }
        }

        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            binding.listener?.onTaskDragged(fromPosition, toPosition)
        }

        /**
         * Creates a custom accessibility action for archiving tasks.
         *
         * This provides the swipe-to-archive functionality to users of accessibility services who
         * may not be able to perform the swipe gesture.
         */
        private fun addArchiveAccessibilityAction(taskListItem: TaskListItem) {
            accessibilityActionIds.add(
                ViewCompat.addAccessibilityAction(
                    binding.root,
                    // The label surfaced to end user by an accessibility service.
                    binding.root.context.getString(R.string.archive)
                ) { _, _ ->
                    // The functionality associated with the label.
                    binding.listener?.onTaskArchived(taskListItem)
                    true
                })
        }

        private fun removeAccessibilityActions(view: View) {
            accessibilityActionIds.forEach { ViewCompat.removeAccessibilityAction(view, it) }
            accessibilityActionIds.clear()
        }

        /**
         * Creates a custom accessibility action for starring / unstarring tasks.
         */
        private fun addStarAccessibilityAction(taskListItem: TaskListItem) {
            accessibilityActionIds.add(
                ViewCompat.addAccessibilityAction(
                    binding.root,
                    // The label surfaced to end user by an accessibility service.
                    if (taskListItem.starUsers.contains(currentUser)) {
                        binding.root.context.getString(R.string.unstar)
                    } else {
                        binding.root.context.getString(R.string.star)
                    }
                ) { _, _ ->
                    // The functionality associated with the label.
                    binding.listener?.onStarClicked(taskListItem)
                    true
                })
        }

        companion object {
            fun from(
                parent: ViewGroup,
                itemListener: ItemListener,
                currentUser: User,
                clock: Clock
            ):
                    TaskViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return TaskViewHolder(
                    ListTaskBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ), itemListener, currentUser, clock
                )
            }
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_HEADER = 0
        const val ITEM_VIEW_TYPE_TASK = 1
    }
}

class DataItemDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem {
    abstract val id: Long

    data class TaskItem(val taskListItem: TaskListItem) : DataItem() {
        override val id = taskListItem.id
    }

    data class HeaderItem(val headerData: HeaderData) : DataItem() {
        override val id = Long.MIN_VALUE
    }
}

data class HeaderData(
    val count: Int,
    val taskState: TaskState,
    var expanded: Boolean
) {
    fun stateDescription(context: Context): String {
        return context.getString(if (expanded) R.string.expanded else R.string.collapsed)
    }

    fun label(context: Context): String {
        return context.getString(
            R.string.header_label_with_count,
            context.getString(taskState.stringResId),
            count
        )
    }
}
