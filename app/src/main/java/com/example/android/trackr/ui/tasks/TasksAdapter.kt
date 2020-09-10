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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.R
import com.example.android.trackr.databinding.ListHeaderBinding
import com.example.android.trackr.databinding.ListTaskBinding
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_TASK = 1

class TasksAdapter(
    private val taskItemListener: TaskItemListener
) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(
    DataItemDiffCallback()
) {

    interface TaskItemListener {
        fun onItemClicked(taskListItem: TaskListItem)
        fun onItemArchived(taskListItem: TaskListItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_TASK -> TaskViewHolder.from(parent, taskItemListener)
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

    // TODO: refactor into use case, using a coroutine (and add tests).
    fun addHeadersAndSubmitList(context: Context, tasks: List<TaskListItem>) {
        val items = mutableListOf<DataItem>()

        val map = tasks.groupBy { it.state }

        val taskStates = listOf(
            TaskState.IN_PROGRESS,
            TaskState.NOT_STARTED,
            TaskState.COMPLETED
        )

        taskStates.forEach { state ->
            val sublist: List<TaskListItem>? = map[state]
            // Add header even if the category does not have any tasks.
            items.add(
                DataItem.HeaderItem(
                    HeaderData(
                        context.getString(
                            R.string.header_label_with_count,
                            // Category, derived from task state.
                            context.getString(
                                when (state) {
                                    TaskState.IN_PROGRESS -> R.string.in_progress
                                    TaskState.NOT_STARTED -> R.string.not_started
                                    TaskState.COMPLETED -> R.string.completed
                                    TaskState.ARCHIVED -> R.string.archived
                                }
                            ),
                            // Tasks count for category.
                            sublist?.size ?: 0
                        )
                    )
                )
            )
            sublist?.map { items.add(DataItem.TaskItem(it)) }
        }

        submitList(items)
    }

    class HeaderViewHolder private constructor(private val binding: ListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(headerData: HeaderData) {
            binding.headerData = headerData
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return HeaderViewHolder(ListHeaderBinding.inflate(layoutInflater, parent, false))
            }
        }
    }

    class TaskViewHolder private constructor(
        private val binding: ListTaskBinding,
        private val taskItemListener: TaskItemListener
    ) :
        RecyclerView.ViewHolder(binding.root), SwipeActionCallback.SwipeActionListener {

        private val accessibilityActionIds = arrayListOf<Int>()

        fun bind(taskListItem: TaskListItem) {
            binding.taskListItem = taskListItem
            binding.listener = taskItemListener
            binding.executePendingBindings()
            addArchiveAccessibilityAction(taskListItem)
        }

        override fun onSwipe() {
           binding.taskListItem?.let {
                binding.listener?.onItemArchived(it)
            }
        }

        /**
         * Creates a custom accessibility action for archiving tasks.
         *
         * This provides the swipe-to-archive functionality to users of accessibility services who
         * may not be able to perform the swipe gesture.
         */
        private fun addArchiveAccessibilityAction(taskListItem: TaskListItem) {
            // Clear previously added actions. If this is skipped, the actions may be duplicated
            // when a view is rebound.
            removeAccessibilityActions(binding.root)

            accessibilityActionIds.add(
                ViewCompat.addAccessibilityAction(
                    binding.root,
                    // The label surfaced to end user by an accessibility service.
                    binding.root.context.getString(R.string.archive)
                ) { _, _ ->
                    // The functionality associated with the label.
                    binding.listener?.onItemArchived(taskListItem)
                    true
                })
        }

        private fun removeAccessibilityActions(view: View) {
            accessibilityActionIds.forEach { ViewCompat.removeAccessibilityAction(view, it) }
            accessibilityActionIds.clear()
        }

        companion object {
            fun from(parent: ViewGroup, taskItemListener: TaskItemListener): TaskViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return TaskViewHolder(
                    ListTaskBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ), taskItemListener)
            }
        }
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
    val label: String
)
