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
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.User
import com.example.android.trackr.databinding.ListTaskBinding
import org.threeten.bp.Clock

internal class ArchiveAdapter(
    private val currentUser: User,
    private val clock: Clock
) : ListAdapter<TaskListItem, ArchiveHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveHolder {
        return ArchiveHolder.from(parent, currentUser, clock)
    }

    override fun onBindViewHolder(holder: ArchiveHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private val DiffCallback = object : DiffUtil.ItemCallback<TaskListItem>() {

    override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
        return oldItem == newItem
    }
}

internal class ArchiveHolder private constructor(
    private val binding: ListTaskBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(task: TaskListItem) {
        binding.taskListItem = task
        binding.card.setOnClickListener { /* TODO */ }
        binding.star.setOnClickListener { /* TODO */ }
    }

    companion object {
        fun from(parent: ViewGroup, currentUser: User, clock: Clock): ArchiveHolder {
            return ArchiveHolder(
                ListTaskBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .also { binding ->
                        binding.currentUser = currentUser
                        binding.clock = clock
                    }
            )
        }
    }
}
