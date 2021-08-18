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

package com.example.android.trackr.ui.archives

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.databinding.TaskSummaryBinding
import java.time.Clock

internal class ArchiveAdapter(
    private val clock: Clock,
    private val onItemClick: (task: TaskSummary) -> Unit,
    private val onItemLongClick: (task: TaskSummary) -> Unit,
    private val onItemStarClicked: (task: TaskSummary) -> Unit
) : ListAdapter<ArchivedTask, ArchiveHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).taskSummary.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveHolder {
        return ArchiveHolder.from(parent, clock)
    }

    override fun onBindViewHolder(holder: ArchiveHolder, position: Int) {
        val task = getItem(position)
        holder.bind(
            task = task,
            cardOnClickListener = { onItemClick(task.taskSummary) },
            cardOnLongClickListener = { onItemLongClick(task.taskSummary); true },
            starOnClickListener = { onItemStarClicked(task.taskSummary) }
        )
    }
}

private val DiffCallback = object : DiffUtil.ItemCallback<ArchivedTask>() {

    override fun areItemsTheSame(oldItem: ArchivedTask, newItem: ArchivedTask): Boolean {
        return oldItem.taskSummary.id == newItem.taskSummary.id
    }

    override fun areContentsTheSame(oldItem: ArchivedTask, newItem: ArchivedTask): Boolean {
        return oldItem == newItem
    }
}

internal class ArchiveHolder private constructor(
    private val binding: TaskSummaryBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        task: ArchivedTask,
        cardOnClickListener: View.OnClickListener,
        cardOnLongClickListener: View.OnLongClickListener,
        starOnClickListener: View.OnClickListener
    ) {
        binding.taskSummary = task.taskSummary
        binding.card.isSelected = task.selected
        binding.card.setOnClickListener(cardOnClickListener)
        binding.card.setOnLongClickListener(cardOnLongClickListener)
        binding.star.setOnClickListener(starOnClickListener)
    }

    companion object {
        fun from(parent: ViewGroup, clock: Clock): ArchiveHolder {
            return ArchiveHolder(
                TaskSummaryBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .also { binding ->
                        binding.clock = clock
                    }
            )
        }
    }
}
