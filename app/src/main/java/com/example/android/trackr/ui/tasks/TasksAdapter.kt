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
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.R
import com.example.android.trackr.databinding.ListHeaderBinding
import com.example.android.trackr.databinding.ListTaskBinding
import com.example.android.trackr.data.Task
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
        fun onItemClicked(task: Task)
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
                holder.bind((getItem(position) as DataItem.TaskItem).task)
            }

            is HeaderViewHolder -> {
                holder.bind((getItem(position) as DataItem.HeaderItem).headerData)
            }
        }
    }

    // TODO: refactor into use case, using a coroutine (and add tests).
    fun addHeadersAndSubmitList(context: Context, tasks: List<Task>) {
        val items = mutableListOf<DataItem>()

        val map = tasks.groupBy { it.state }

        val taskStates = listOf(
            TaskState.IN_PROGRESS,
            TaskState.NOT_STARTED,
            TaskState.COMPLETED,
            TaskState.ARCHIVED
        )

        taskStates.forEach { state ->
            val sublist: List<Task>? = map[state]
            // Add header even if the category does not have any tasks.
            items.add(
                DataItem.HeaderItem(
                    HeaderData(
                        headerLabel(context, state),
                        sublist?.size ?: 0
                    )
                )
            )
            sublist?.map { items.add(DataItem.TaskItem(it)) }
        }

        submitList(items)
    }

    private fun headerLabel(context: Context, taskState: TaskState) : String {
        return context.getString(
            when(taskState) {
                TaskState.IN_PROGRESS -> R.string.in_progress
                TaskState.NOT_STARTED -> R.string.not_started
                TaskState.COMPLETED -> R.string.completed
                TaskState.ARCHIVED -> R.string.archived
            })
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
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.task = task
            binding.listener = taskItemListener
            binding.executePendingBindings()
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

    data class TaskItem(val task: Task) : DataItem() {
        override val id = task.id
    }

    data class HeaderItem(val headerData: HeaderData) : DataItem() {
        override val id = Long.MIN_VALUE
    }
}

data class HeaderData(
    val title: String,
    val count: Int
)
